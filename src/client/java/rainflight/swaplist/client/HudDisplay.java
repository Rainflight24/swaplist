package rainflight.swaplist.client;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class HudDisplay {
    final Identifier id;
    private boolean visible = true;

    public HudDisplay(Identifier id) {
        SwaplistClient.CONFIG.subscribeToListWidth(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListHeight(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListHorizontalPos(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListVerticalPos(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListColor(t -> rebuild());

        this.id = id;
        rebuild();
    }

    private static @NonNull ArrayList<TodoList.ListItem> getItems() {
        return new ArrayList<>(SwaplistClient.CONFIG.items());
    }

    /**
     * Pulls relevant info from config and puts the current list on hud.
     */
    private void rebuild() {
        if (visible) {
            final int hPos = SwaplistClient.CONFIG.listHorizontalPos();
            final int vPos = SwaplistClient.CONFIG.listVerticalPos();
            final int width = SwaplistClient.CONFIG.listWidth();
            final int height = SwaplistClient.CONFIG.listHeight();
            final Color color = SwaplistClient.CONFIG.listColor();

            Hud.remove(id);
            Hud.add(id, () -> {
                final FlowLayout fl = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                        .gap(3);

                final List<TodoList.ListItem> items = getItems();
                for (TodoList.ListItem listItem : items) {
                    final Component c = Component.literal(listItem.text);

                    final int gap = 5;

                    var checkbox = UIComponents.smallCheckbox(null);
                    checkbox.checked(listItem.toggled);

                    var label = UIComponents.label(c).maxWidth(width - gap * 2 - checkbox.width() - 30).color(color); // TODO: make a better way of determining sizing besides subtracting a bunch of random numbers
                    fl.child(UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                            .child(checkbox)
                            .child(label)
                            .gap(5)
                            .verticalAlignment(VerticalAlignment.CENTER));
                }

                fl.positioning(Positioning.absolute(hPos, vPos))
                        .padding(Insets.of(10))
                        .surface(Surface.BLANK)
                        .horizontalAlignment(HorizontalAlignment.LEFT)
                        .verticalAlignment(VerticalAlignment.TOP);

                return fl;
            });
        } else {
            Hud.remove(id);
        }
    }

    /**
     * Determines the number of items in the list.
     *
     * @return ^
     */
    public int itemCount() {
        return getItems().size();
    }

    /**
     * Adds a line of text to the displayed list.
     *
     * @param line The text to display.
     */
    public void pushLine(String line) {
        final List<TodoList.ListItem> items = getItems();
        items.add(new TodoList.ListItem(line, false));
        SwaplistClient.CONFIG.items(items);
        rebuild();
    }

    /**
     * Removes the most recently added line of text.
     */
    public void popLine() {
        final List<TodoList.ListItem> items = getItems();
        if (!items.isEmpty()) {
            items.removeLast();
            rebuild();
            SwaplistClient.CONFIG.items(items);
        }
    }

    /**
     * Removes the nth line.
     *
     * @param idx The one-indexed index to remove.
     */
    public void removeLine(int idx) {
        final List<TodoList.ListItem> items = getItems();
        if (idx >= 1 && idx <= items.size()) {
            items.remove(idx - 1);
            rebuild();
        }
        SwaplistClient.CONFIG.items(items);
    }

    /**
     * Toggles the nth checkbox.
     *
     * @param idx The one-indexed index to toggle.
     */
    public void toggleLine(int idx) {
        final List<TodoList.ListItem> items = getItems();
        TodoList.ListItem old = items.get(idx - 1);
        items.set(idx - 1, new TodoList.ListItem(old.text, !old.toggled));
        rebuild();
        SwaplistClient.CONFIG.items(items);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rebuild();
    }

}
