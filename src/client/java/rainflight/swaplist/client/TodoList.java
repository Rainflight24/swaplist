package rainflight.swaplist.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure for storing versions of the list.
 */
public class TodoList {

    public String name;
    public final List<ListItem> items;

    public TodoList() {
        this.name = "Todo List";
        this.items = new ArrayList<>();
    }

    public TodoList(String name, List<ListItem> items) {
        this.name = name;
        this.items = new ArrayList<>(items);
    }

    public TodoList(TodoList oldList) {
        this(oldList.name, oldList.items);
    }

    /**
     * Determines if an index is within the bounds of the todolist.
     */
    public boolean isValidIndex(int idx) {
        return idx >= 0 && idx < this.items.size();
    }

    public static final class ListItem {
        public String text;
        public boolean toggled;

        @SuppressWarnings(
                "unused") // default constructor necessary for owo-lib config serialization
        public ListItem() {}

        public ListItem(String text, boolean toggled) {
            this.text = text;
            this.toggled = toggled;
        }
    }
}
