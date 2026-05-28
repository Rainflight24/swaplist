package rainflight.swaplist.client;

import org.jspecify.annotations.NonNull;
import rainflight.swaplist.Swaplist;

import java.util.*;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

/**
 * Utility class holding operations on config.
 */
final class ConfigUtils {
    private ConfigUtils() {
    }

    /**
     * Converts an integer to its ordinal representation. Sourced from <a href="https://stackoverflow.com/a/6810409">stackoverflow.</a>
     *
     * @param i The integer to convert.
     * @return Its ordinal representation.
     */
    private static String ordinal(int i) {
        String[] suffixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
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
    private static @NonNull String uniqueKey(String suffix, Set<String> set) {
        int i = 1;
        String key;
        while (set.contains(key = ordinal(i) + " " + suffix)) {
            i++;
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
     * Saves the provided todolist to config, while replacing the currently active list.
     *
     * @param list The todolist to save.
     */
    static void saveCurList(final TodoList list) {
        final Map<String, TodoList> lists = new HashMap<>(CONFIG.lists());
        lists.put(list.name, list);
        CONFIG.lists(lists);
    }

    /**
     * Fetches a copy of the currently active TodoList from config, and performs sanitization.
     *
     * @return A copy of the current TodoList.
     */
    static @NonNull TodoList getCurList() {
        Map<String, TodoList> lists = CONFIG.lists();
        String curKey = CONFIG.curActiveList();
        TodoList oldList = lists.get(curKey);

        if (oldList == null) {
            String key;
            Swaplist.LOGGER.warn("Could not find list {}.", curKey);

            if (lists.isEmpty()) {
                key = newList();
                Swaplist.LOGGER.warn("Created list {}.", key);
            } else {
                key = getFirstList();
            }
            Swaplist.LOGGER.warn("Instead loading list {}.", key);
            setActiveList(key);

            return new TodoList(lists.get(key));
        }
        return new TodoList(oldList);
    }

    /**
     * Gets the lexicographically first key from CONFIG.lists(). Calls {@code this.newList} if CONFIG.lists() is somehow
     * empty.
     *
     * @return The lexicographically first key.
     */
    static @NonNull String getFirstList() {
        SortedMap<String, TodoList> lists = new TreeMap<>(CONFIG.lists());
        return lists.keySet().stream().findFirst().or(() -> Optional.of(newList())).get();
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
            Swaplist.LOGGER.warn("Ignored attempt at overwriting list {} during a rename.", newName);
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
    }

    /**
     * Removes the most recently added line of text.
     */
    static void popLine() {
        final TodoList list = getCurList();
        if (!list.items.isEmpty()) {
            list.items.removeLast();
            saveCurList(list);
        }
    }

    /**
     * Removes the nth line.
     *
     * @param idx The zero-indexed index to remove.
     */
    static void removeLine(int idx) {
        final TodoList list = getCurList();
        if (idx >= 0 && idx < list.items.size()) {
            list.items.remove(idx);
            saveCurList(list);
        }
    }

    /**
     * Toggles the nth checkbox.
     *
     * @param idx The zero-indexed index to toggle.
     */
    static void toggleLine(int idx) {
        final TodoList list = getCurList();
        if (idx >= 0 && idx < list.items.size()) {
            TodoList.ListItem old = list.items.get(idx);
            list.items.set(idx, new TodoList.ListItem(old.text, !old.toggled));
            saveCurList(list);
        }
    }

    /**
     * Changes the nth checkbox's text.
     *
     * @param idx The zero-indexed index to change.
     * @param text The box's new text.
     */
    static void changeLine(int idx, String text) {
        final TodoList list = getCurList();
        if (idx >= 0 && idx < list.items.size()) {
            TodoList.ListItem old = list.items.get(idx);
            list.items.set(idx, new TodoList.ListItem(text, old.toggled));
            saveCurList(list);
        }
    }

    /**
     * Changes the currently active list.
     *
     * @param key The key of the newly active list.
     */
    static void setActiveList(String key) {
        if (CONFIG.lists().containsKey(key)) {
            CONFIG.curActiveList(key);
        } else {
            Swaplist.LOGGER.warn("Attempted to set active list to nonexistent key {}.", key);
        }
    }

    /**
     * Updates the list's width.
     * @param newWidth the list's new width
     */
    static void setWidth(int newWidth) {
        CONFIG.listWidth(newWidth);
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
    }
}
