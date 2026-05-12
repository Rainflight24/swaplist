package rainflight.swaplist.client;

import java.util.List;

/**
 * Structure for storing versions of the list.
 */
public class TodoList {

    public String name;
    public List<ListItem> items;

    public TodoList() {
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
