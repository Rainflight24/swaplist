package rainflight.swaplist.client;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;
import org.jspecify.annotations.NonNull;
import rainflight.swaplist.Swaplist;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused") // model class fields are not directly used
@Modmenu(modId = Swaplist.MOD_ID)
@Config(name = Swaplist.MOD_ID, wrapperName = "SwaplistConfig")
public class SwaplistConfigModel {

    @Hook
    @PredicateConstraint("listWidthPredicate")
    public int listWidth = 200;

    public static boolean listWidthPredicate(int width) {
        return width >= 100;
    }

    public static boolean activeListPredicate(int idx) {
        return idx >= 1 && idx <= SwaplistClient.CONFIG.lists().size();
    }

    @Hook
    public int listHeight = 500;

    @Hook
    public int listHorizontalPos = 0, listVerticalPos = 100;

    @Hook
    public Color listColor = new Color(0, 64, 255);

    @Hook
    @ExcludeFromScreen
    public List<TodoList> lists = new ArrayList<>(List.of(new TodoList()));

//    @ExcludeFromScreen TODO: uncomment once done testing
    @Hook
    public int curActiveList = 1;

    /**
     * Fetches a copy of the currently active TodoList from config, and performs sanitization.
     *
     * @return A copy of the current TodoList.
     */
    static @NonNull TodoList getCurList() {
        return new TodoList(SwaplistClient.CONFIG.lists().get(fetchCurIndex()));
    }

    /**
     * Fetches curActiveList from config, and performs sanitization on curActiveList and lists.
     *
     * @return curActiveList
     */
    protected static int fetchCurIndex() {
        sanitizeLists();
        return SwaplistClient.CONFIG.curActiveList() - 1;
    }

    protected static void sanitizeLists() {
        // Sanitization is performed here rather than with owo, so CONFIG.lists can be repaired.
        if (SwaplistClient.CONFIG.lists().isEmpty()) {
            SwaplistClient.CONFIG.lists(new ArrayList<>(List.of(new TodoList())));
            Swaplist.LOGGER.warn("There are no lists, a default list has been added");
        }

        int currentIndex = SwaplistClient.CONFIG.curActiveList() - 1;
        // Sanitization is performed here rather than with owo as the size of CONFIG.lists is dynamic.
        if (currentIndex < 0 || currentIndex >= SwaplistClient.CONFIG.lists().size()) {
            SwaplistClient.CONFIG.curActiveList(1); // sensible default
            currentIndex = 0;
            Swaplist.LOGGER.warn("curActiveList (currently list #{}) is out of bounds and has been reset to 1", currentIndex + 1);
        }
    }

    /**
     * Saves the provided todolist to config, while replacing the currently active list.
     *
     * @param list The todolist to save.
     */
    protected static void saveCurList(final TodoList list) {
        int index = fetchCurIndex();
        final List<TodoList> lists = new ArrayList<>(SwaplistClient.CONFIG.lists());
        lists.set(index, list);
        SwaplistClient.CONFIG.lists(lists);
    }
}