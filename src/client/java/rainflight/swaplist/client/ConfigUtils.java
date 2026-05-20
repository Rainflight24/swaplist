package rainflight.swaplist.client;

import org.jspecify.annotations.NonNull;
import rainflight.swaplist.Swaplist;

import java.util.*;

final public class ConfigUtils {
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

    public static @NonNull String uniqueListKey() {
        var map = SwaplistClient.CONFIG.lists();
        return ConfigUtils.uniqueKey(SwaplistClient.CONFIG.defaultListSuffix(), map.keySet());
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
        String key = ordinal(i) + " " + suffix;
        while (set.contains(ordinal(i) + " " + suffix)) {
            i++;
            key = ordinal(i) + " " + suffix;
        }
        return key;
    }

    /**
     * Saves the provided todolist to config, while replacing the currently active list.
     *
     * @param list The todolist to save.
     */
    public static void saveCurList(final TodoList list) {
        final Map<String, TodoList> lists = new HashMap<>(SwaplistClient.CONFIG.lists());
        lists.put(list.name, list);
        SwaplistClient.CONFIG.lists(lists);
    }

    /**
     * Fetches a copy of the currently active TodoList from config, and performs sanitization.
     *
     * @return A copy of the current TodoList.
     */
    public static @NonNull TodoList getCurList() {
        Map<String, TodoList> lists = SwaplistClient.CONFIG.lists();
        String curKey = SwaplistClient.CONFIG.curActiveList();
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
            SwaplistClient.CONFIG.curActiveList(key);

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
    public static @NonNull String getFirstList() {
        SortedMap<String, TodoList> lists = new TreeMap<>(SwaplistClient.CONFIG.lists());
        return lists.keySet().stream().findFirst().or(() -> Optional.of(newList())).get();
    }

    /**
     * Creates a new list using CONFIG.defaultListSuffix.
     *
     * @return The key of the created list. It will end with the defaultListSuffix.
     */
    public static @NonNull String newList() {
        Map<String, TodoList> lists = SwaplistClient.CONFIG.lists();
        String key;
        key = uniqueListKey();

        lists.put(key, new TodoList(key, new ArrayList<>()));
        SwaplistClient.CONFIG.lists(new HashMap<>(lists));
        return key;
    }
}
