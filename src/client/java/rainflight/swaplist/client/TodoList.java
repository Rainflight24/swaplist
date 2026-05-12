package rainflight.swaplist.client;

import java.util.ArrayList;
import java.util.List;

/**
 * Structure for storing versions of the list.
 */
public class TodoList {

    public String name;
    public List<ListItem> items;

    public TodoList() {
        this.name = "Todo List";
        this.items = new ArrayList<>();
    }

    public TodoList(String name, List<ListItem> items) {
        this.name = name;
        this.items = items;
    }

    public TodoList(TodoList oldList) {
        this.name = oldList.name;
        this.items = oldList.items;
    }

    final public static class ListItem {
        public String text;
        public boolean toggled;

        public ListItem() {
        }

        public ListItem(String text, boolean toggled) {
            this.text = text;
            this.toggled = toggled;
        }
    }
}
