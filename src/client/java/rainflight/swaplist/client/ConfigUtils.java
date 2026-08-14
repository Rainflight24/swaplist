package rainflight.swaplist.client;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import rainflight.swaplist.Swaplist;

/**
 * Utility class holding operations on config.
 */
public final class ConfigUtils {
    // data constants for SwaplistConfigModel
    static final String finalDefaultListSuffix = "New List";
    static final String firstDefaultList = ConfigUtils.uniqueName(finalDefaultListSuffix, Set.of());

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

    /**
     * Generates a unique list name, based on the default list suffix.
     * @return A unique list name.
     */
    public static @NonNull String uniqueListKey() {
        return ConfigUtils.uniqueName(CONFIG.defaultListSuffix());
    }

    /**
     * Generates a list name of the form "nth suffix" not already present in Config.lists().
     *
     * @param suffix String appended to the generated key.
     * @return A unique key.
     */
    public static @NonNull String uniqueName(String suffix) {
        return uniqueName(
                suffix,
                CONFIG.lists().stream().map(todoList -> todoList.name).collect(Collectors.toSet()));
    }

    /**
     * Generates a name of the form "nth suffix" not already present in the list.
     *
     * @param suffix String appended to the generated key.
     * @param data   Collection of names not to generate.
     * @return A unique key.
     */
    public static @NonNull String uniqueName(String suffix, Collection<String> data) {
        Set<String> set = new HashSet<>(data);
        int i = 1;
        String key = ordinal(i) + " " + suffix;
        while (set.contains(key)) {
            i++;
            key = ordinal(i) + " " + suffix;
        }
        return key;
    }

    /**
     * Persists the config to disk. Auto-save on modification is disabled (see
     * {@link SwaplistConfigModel}'s {@code @Config}), so mutators that represent a complete
     * user action call this themselves. Streaming edits ({@link #changeLine} and
     * {@link #setListPosition}) intentionally skip it and defer to an explicit save at their
     * commit point (e.g. closing the edit screen).
     */
    public static void save() {
        CONFIG.save();
    }

    /**
     * Fetches the currently active TodoList from config.
     *
     * @return The current TodoList.
     */
    public static TodoList getCurList() {
        Optional<TodoList> list = fetchList(CONFIG.curActiveList());

        if (list.isEmpty()) {
            throw new IllegalStateException(
                    "Active list " + CONFIG.curActiveList() + " is missing.");
        }
        return list.get();
    }

    public static void ensureValidActiveList() {
        Optional<TodoList> oldList = fetchList(CONFIG.curActiveList());
        if (CONFIG.lists().isEmpty()) {
            TodoList nextList = newList();
            setActiveList(nextList);
            Swaplist.LOGGER.warn("No todolists found! Created a new one named {}", nextList);
        } else if (oldList.isEmpty()) {
            TodoList nextList = getFirstList();
            setActiveList(nextList);
            Swaplist.LOGGER.warn(
                    "Active list {} is missing. Set active list to {}", oldList, nextList);
        }
    }

    /**
     * Gets the lexicographically first key from CONFIG.lists(). Requires CONFIG.lists() to be non-empty.
     *
     * @return The lexicographically first key.
     */
    public static @NonNull TodoList getFirstList() {
        if (CONFIG.lists().isEmpty()) throw new IllegalStateException("CONFIG.lists() is empty!");
        return Collections.min(CONFIG.lists());
    }

    /**
     * Creates a new list using CONFIG.defaultListSuffix.
     *
     * @return The created TodoList. Its name ends with the defaultListSuffix.
     */
    public static @NonNull TodoList newList() {
        var lists = CONFIG.lists();
        String key = uniqueListKey();

        TodoList newList = new TodoList(key, new ArrayList<>());
        lists.add(newList);
        CONFIG.lists(lists);
        save();
        return newList;
    }

    /**
     * Deletes the given list from config. If the current list was deleted, the first list is loaded.
     * Creates a new, default list when the last list is deleted.
     *
     * @param toDelete The key of the list to delete.
     * @return Whether the given list was successfully deleted.
     */
    public static boolean deleteList(String toDelete) {
        var lists = CONFIG.lists();
        var firstList = getFirstList();

        for (TodoList tlist : lists) {
            if (tlist.name.equals(toDelete)) {
                lists.remove(tlist);
                CONFIG.lists(lists);

                // If the current list was deleted, load the first one.
                if (firstList == tlist) {
                    if (CONFIG.lists().isEmpty()) {
                        newList();
                    }
                    setActiveList(getFirstList());
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Renames the current list's title, and updates its key.
     *
     * @param newName The current list's new name.
     * @return Whether the rename succeeded.
     */
    public static boolean renameCurrent(String newName) {
        var lists = CONFIG.lists();
        TodoList list = getCurList();
        list.name = newName;

        CONFIG.lists(lists);
        CONFIG.curActiveList(newName);
        save();
        return true;
    }

    /**
     * Adds a line of text to the displayed list.
     *
     * @param line The text to display.
     */
    public static void pushLine(String line) {
        final TodoList list = getCurList();
        list.items.add(new TodoList.ListItem(line, false));
        save();
    }

    /**
     * Removes the most recently added line of text.
     */
    public static void popLine() {
        final TodoList list = getCurList();
        if (!list.items.isEmpty()) {
            list.items.removeLast();
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
        if (doSave) save();
    }

    /**
     * Removes the nth line.
     *
     * @param idx The zero-indexed index to remove.
     */
    public static void removeLine(int idx) {
        mutateLine(idx, listItems -> listItems.remove(idx), true);
    }

    /**
     * Toggles the nth checkbox.
     *
     * @param idx The zero-indexed index to toggle.
     */
    public static void toggleLine(int idx) {
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
    public static void changeLine(int idx, String text) {
        mutateLine(idx, listItems -> listItems.get(idx).text = text, false);
    }

    /**
     * Fetches the TodoList with a given name.
     *
     * @param name The name of the TodoList to fetch.
     * @return The desired TodoList, if it exists.
     */
    public static Optional<TodoList> fetchList(String name) {
        var lists = CONFIG.lists();

        for (TodoList list : lists) if (list.name.equals(name)) return Optional.of(list);

        return Optional.empty();
    }

    /**
     * Changes the currently active list.
     *
     * @param tlist The list to set as active.
     */
    public static void setActiveList(TodoList tlist) {
        if (CONFIG.lists().contains(tlist)) {
            CONFIG.curActiveList(tlist.name);
            save();
        } else {
            Swaplist.LOGGER.warn("Attempted to set active list to nonexistent list {}.", tlist);
        }
    }

    /**
     * Updates the list's width.
     *
     * @param newWidth the list's new width
     */
    public static void setWidth(int newWidth) {
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
    public static void setListPosition(int x, int y) {
        CONFIG.listHorizontalPos(x);
        CONFIG.listVerticalPos(y);
    }

    /**
     * Saves the current list as a template with key templateName.
     *
     * @param templateName The key of the newly saved template.
     */
    public static void saveCurAsTemplate(String templateName) {
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
    public static Optional<String> loadTemplate(String templateName) {
        TodoList template = CONFIG.templates().get(templateName);
        if (template == null) {
            return Optional.empty();
        }

        var lists = CONFIG.lists();
        String name = uniqueName(templateName);
        lists.add(new TodoList(name, template.items));
        CONFIG.lists(lists);
        CONFIG.curActiveList(name);
        save();
        return Optional.of(name);
    }

    public static void setHudVisibility(boolean visible) {
        CONFIG.listVisible(visible);
        save();
    }
}
