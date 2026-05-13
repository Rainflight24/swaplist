package rainflight.swaplist.client;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;

public class HudDisplay {
    final Identifier id;
    private boolean visible = true;

    public HudDisplay(Identifier id) {
        SwaplistClient.CONFIG.subscribeToListWidth(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListHeight(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListHorizontalPos(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListVerticalPos(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToListColor(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToLists(t -> rebuild());
        SwaplistClient.CONFIG.subscribeToCurActiveList(t -> rebuild());

        this.id = id;
        rebuild();
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
                final TodoList curList = SwaplistConfigModel.getCurList();
                final List<TodoList.ListItem> items = curList.items;
                final int insetSize = 10;

                final FlowLayout fl = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                        .gap(3);
                // Label line wrapping requires manual width calculations.
                fl.child(UIComponents.label(Component.literal(curList.name))
                        .color(color)
                        .maxWidth(width - 2*insetSize));

                for (TodoList.ListItem listItem : items) {
                    final Component c = Component.literal(listItem.text);

                    final int gap = 5;
                    final int checkboxSize = 13; // TODO: is this really the best way of determining the checkbox's size
                    var checkbox = UIComponents.smallCheckbox(null);
                    checkbox.checked(listItem.toggled);

                    var label = UIComponents.label(c).maxWidth(width - gap - 2*insetSize - checkboxSize).color(color); // TODO: make a better way of determining sizing besides subtracting a bunch of random numbers
                    fl.child(UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                            .child(checkbox)
                            .child(label)
                            .gap(gap)
                            .verticalAlignment(VerticalAlignment.CENTER));
                }

                fl.positioning(Positioning.absolute(hPos, vPos))
                        .padding(Insets.of(insetSize))
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
        return SwaplistConfigModel.getCurList().items.size();
    }

    /**
     * Adds a line of text to the displayed list.
     *
     * @param line The text to display.
     */
    public void pushLine(String line) {
        final TodoList list = SwaplistConfigModel.getCurList();
        list.items.add(new TodoList.ListItem(line, false));
        SwaplistConfigModel.saveCurList(list);
    }

    /**
     * Removes the most recently added line of text.
     */
    public void popLine() {
        final TodoList list = SwaplistConfigModel.getCurList();
        if (!list.items.isEmpty()) {
            list.items.removeLast();
            SwaplistConfigModel.saveCurList(list);
        }
    }

    /**
     * Removes the nth line.
     *
     * @param idx The one-indexed index to remove.
     */
    public void removeLine(int idx) {
        final TodoList list = SwaplistConfigModel.getCurList();
        if (idx >= 1 && idx <= list.items.size()) {
            list.items.remove(idx - 1);
            SwaplistConfigModel.saveCurList(list);
        }
    }

    /**
     * Toggles the nth checkbox.
     *
     * @param idx The one-indexed index to toggle.
     */
    public void toggleLine(int idx) {
        final TodoList list = SwaplistConfigModel.getCurList();
        TodoList.ListItem old = list.items.get(idx - 1);
        list.items.set(idx - 1, new TodoList.ListItem(old.text, !old.toggled));
        SwaplistConfigModel.saveCurList(list);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rebuild();
    }

}
