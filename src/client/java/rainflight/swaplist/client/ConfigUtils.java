package rainflight.swaplist.client;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

import java.util.*;
import java.util.function.Consumer;
import org.jspecify.annotations.NonNull;
import rainflight.swaplist.Swaplist;

/**
 * Utility class holding operations on config.
 */
final class ConfigUtils {
    // data constants for SwaplistConfigModel
    static final String finalDefaultListSuffix = "New List";
    static final String firstDefaultList = ConfigUtils.uniqueKey(finalDefaultListSuffix, Set.of());

    private ConfigUtils() {}

    /**
     * Converts an integer to its ordinal representation. Sourced from <a href="https://stackoverflow.com/a/6810409">stackoverflow.</a>
     *
     * @param i The integer to convert.
     * @return Its ordinal representation.
     */
    private static String ordinal(int i) {
        String[] suffixes =
                new String[] {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        return switch (i % 100) {
            case 11, 12, 13 -> i + "th";
            default -> i + suffixes[i % 10];
        };
    }

    static @NonNull String uniqueListKey() {
        var map = CONFIG.lists();
        return ConfigUtils.uniqueKey(CONFIG.defaultListSuffix(), map.keySet());
    }

    /**
     * Generates a string of the form "nth suffix" not already in the map. The lowest positive integer not in the map is
     * returned.
     *
     * @param suffix String appended to the generated key.
     * @param set    Set of taken names.
     * @return A unique key.
     */
    static @NonNull String uniqueKey(String suffix, Set<String> set) {
        int i = 1;
        String key = ordinal(i) + " " + suffix;
        while (set.contains(key)) {
            i++;
            key = ordinal(i) + " " + suffix;
        }
        return key;
    }

    /**
     * Checks if a list exists.
     *
     * @param key The key of the list to check existence for.
     * @return Whether the list exists.
     */
    static boolean isListExistent(String key) {
        return CONFIG.lists().containsKey(key);
    }

    /**
     * Persists the config to disk. Auto-save on modification is disabled (see
     * {@link SwaplistConfigModel}'s {@code @Config}), so mutators that represent a complete
     * user action call this themselves. Streaming edits ({@link #changeLine} and
     * {@link #setListPosition}) intentionally skip it and defer to an explicit save at their
     * commit point (e.g. closing the edit screen).
     */
    static void save() {
        CONFIG.save();
    }

    /**
     * Saves the provided todolist to config, while replacing the currently active list. Does not
     * persist on its own; the caller decides whether to {@link #save()}.
     *
     * @param list The todolist to save.
     */
    static void saveCurList(final TodoList list) {
        final var lists = new HashMap<>(CONFIG.lists());
        lists.put(list.name, list);
        CONFIG.lists(lists);
    }

    /**
     * Fetches a copy of the currently active TodoList from config.
     *
     * @return A copy of the current TodoList.
     */
    static TodoList getCurList() {
        String curKey = CONFIG.curActiveList();
        TodoList list = CONFIG.lists().get(curKey);

        if (list == null) {
            throw new IllegalStateException("Active list " + curKey + " is missing.");
        }
        return new TodoList(list);
    }

    static void ensureValidActiveList() {
        String oldList = CONFIG.curActiveList();
        if (CONFIG.lists().isEmpty()) {
            String nextList = newList();
            setActiveList(nextList);
            Swaplist.LOGGER.warn("No todolists found! Created a new one named {}", nextList);
        } else if (!CONFIG.lists().containsKey(oldList)) {
            String nextList = getFirstList();
            setActiveList(nextList);
            Swaplist.LOGGER.warn(
                    "Active list {} is missing. Set active list to {}", oldList, nextList);
        }
    }

    /**
     * Gets the lexicographically first key from CONFIG.lists(). Calls {@code this.newList} if CONFIG.lists() is somehow
     * empty.
     *
     * @return The lexicographically first key.
     */
    static @NonNull String getFirstList() {
        return Collections.min(CONFIG.lists().keySet());
    }

    /**
     * Creates a new list using CONFIG.defaultListSuffix.
     *
     * @return The key of the created list. It will end with the defaultListSuffix.
     */
    static @NonNull String newList() {
        Map<String, TodoList> lists = new HashMap<>(CONFIG.lists());
        String key = uniqueListKey();

        lists.put(key, new TodoList(key, new ArrayList<>()));
        CONFIG.lists(lists);
        save();
        return key;
    }

    /**
     * Deletes the given list from config.
     *
     * @param toDelete The key of the list to delete.
     * @return Whether the given list was successfully deleted.
     */
    static boolean deleteList(String toDelete) {
        var lists = new HashMap<>(CONFIG.lists());

        if (lists.containsKey(toDelete)) {
            lists.remove(toDelete);
            CONFIG.lists(lists);
            setActiveList(getFirstList());
            return true;
        }
        return false;
    }

    /**
     * Renames the current list's title, and updates its key.
     *
     * @param newName The current list's new name.
     * @return Whether the rename succeeded.
     */
    static boolean renameCurrent(String newName) {
        final Map<String, TodoList> lists = new HashMap<>(CONFIG.lists());
        TodoList list = getCurList();

        if (lists.containsKey(newName)) {
            Swaplist.LOGGER.warn(
                    "Ignored attempt at overwriting list {} during a rename.", newName);
            return false;
        }

        lists.remove(list.name);

        list.name = newName;
        lists.put(newName, list);

        CONFIG.lists(lists);
        setActiveList(newName);
        return true;
    }

    /**
     * Adds a line of text to the displayed list.
     *
     * @param line The text to display.
     */
    static void pushLine(String line) {
        final TodoList list = getCurList();
        list.items.add(new TodoList.ListItem(line, false));
        saveCurList(list);
        save();
    }

    /**
     * Removes the most recently added line of text.
     */
    static void popLine() {
        final TodoList list = getCurList();
        if (!list.items.isEmpty()) {
            list.items.removeLast();
            saveCurList(list);
            save();
        }
    }

    /**
     * Mutate the current todolist by index.
     *
     * @param idx    An index to affect.
     * @param op     The operation to perform on the index.
     * @param doSave Whether a config save should be triggered.
     */
    private static void mutateLine(int idx, Consumer<List<TodoList.ListItem>> op, boolean doSave) {
        final TodoList list = getCurList();
        if (!list.isValidIndex(idx)) return;
        op.accept(list.items);
        saveCurList(list);
        if (doSave) save();
    }

    /**
     * Removes the nth line.
     *
     * @param idx The zero-indexed index to remove.
     */
    static void removeLine(int idx) {
        mutateLine(idx, listItems -> listItems.remove(idx), true);
    }

    /**
     * Toggles the nth checkbox.
     *
     * @param idx The zero-indexed index to toggle.
     */
    static void toggleLine(int idx) {
        mutateLine(
                idx,
                listItems -> {
                    TodoList.ListItem item = listItems.get(idx);
                    item.toggled = !item.toggled;
                },
                true);
    }

    /**
     * Changes the nth checkbox's text. Fired per keystroke while editing, so it intentionally does
     * not {@link #save()}; the caller persists once at its commit point (e.g. on screen close).
     *
     * @param idx  The zero-indexed index to change.
     * @param text The box's new text.
     */
    static void changeLine(int idx, String text) {
        mutateLine(
                idx,
                listItems -> {
                    listItems.get(idx).text = text;
                },
                false);
    }

    /**
     * Changes the currently active list.
     *
     * @param key The key of the newly active list.
     */
    static void setActiveList(String key) {
        if (CONFIG.lists().containsKey(key)) {
            CONFIG.curActiveList(key);
            save();
        } else {
            Swaplist.LOGGER.warn("Attempted to set active list to nonexistent key {}.", key);
        }
    }

    /**
     * Updates the list's width.
     *
     * @param newWidth the list's new width
     */
    static void setWidth(int newWidth) {
        CONFIG.listWidth(newWidth);
        save();
    }

    /**
     * Updates the displayed list's on-screen position in memory. Called repeatedly while dragging,
     * so it intentionally does not {@link #save()}; the caller persists once at its commit point
     * (e.g. on screen close).
     *
     * @param x The new horizontal position.
     * @param y The new vertical position.
     */
    static void setListPosition(int x, int y) {
        CONFIG.listHorizontalPos(x);
        CONFIG.listVerticalPos(y);
    }

    /**
     * Saves the current list as a template with key templateName.
     *
     * @param templateName The key of the newly saved template.
     */
    static void saveCurAsTemplate(String templateName) {
        TodoList list = getCurList();
        var templates = new HashMap<>(CONFIG.templates());
        templates.put(templateName, list);
        CONFIG.templates(templates);
        save();
    }

    /**
     * Loads the template with the given name into a freshly created list, and makes it the active
     * list. The new list is named after the template using the unique "nth name" scheme (e.g.
     * "2nd TemplateName"), so loading the same template repeatedly never overwrites an existing
     * list.
     *
     * @param templateName The key of the template to load.
     * @return The key of the newly created list, or empty if no such template exists.
     */
    static Optional<String> loadTemplate(String templateName) {
        TodoList template = CONFIG.templates().get(templateName);
        if (template == null) {
            return Optional.empty();
        }

        final Map<String, TodoList> lists = new HashMap<>(CONFIG.lists());
        String key = uniqueKey(templateName, lists.keySet());

        lists.put(key, new TodoList(key, template.items));
        CONFIG.lists(lists);
        setActiveList(key);
        return Optional.of(key);
    }

    static void setHudVisibility(boolean visible) {
        CONFIG.listVisible(visible);
        save();
    }
}
