package rainflight.swaplist.client;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HudDisplay {
    final private Identifier id;
    private boolean visible = true;
    private boolean needsRebuild = true;

    public HudDisplay(Identifier id) {
        this.id = id;

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (needsRebuild) {
                needsRebuild = false;
                rebuild();
            }
        });

        Consumer<Integer> intConsumer = _unused -> needsRebuild = true;
        Consumer<String> stringConsumer = _unused -> needsRebuild = true;
        Consumer<Color> colorConsumer = _unused -> needsRebuild = true;
        Consumer<Map<String, TodoList>> listsConsumer = _unused -> needsRebuild = true;

        SwaplistClient.CONFIG.subscribeToListWidth(intConsumer);
        SwaplistClient.CONFIG.subscribeToListHeight(intConsumer);
        SwaplistClient.CONFIG.subscribeToListHorizontalPos(intConsumer);
        SwaplistClient.CONFIG.subscribeToListVerticalPos(intConsumer);
        SwaplistClient.CONFIG.subscribeToCurActiveList(stringConsumer);
        SwaplistClient.CONFIG.subscribeToListColor(colorConsumer);
        SwaplistClient.CONFIG.subscribeToLists(listsConsumer);
    }

    private static UIComponent makeLayout() {
        final int hPos = SwaplistClient.CONFIG.listHorizontalPos();
        final int vPos = SwaplistClient.CONFIG.listVerticalPos();
        final int width = SwaplistClient.CONFIG.listWidth();
        final int height = SwaplistClient.CONFIG.listHeight();
        final Color textColor = SwaplistClient.CONFIG.listColor();

        final TodoList curList = ConfigUtils.getCurList();
        final List<TodoList.ListItem> items = curList.items;
        final int insetSize = 10;

        final int layoutGap = 3;

        final FlowLayout layout = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                .gap(layoutGap);

        // Line wrapping requires manual width calculations.
        layout.child(UIComponents.label(Component.literal(curList.name))
                .color(textColor)
                .maxWidth(width - 2 * insetSize));

        for (TodoList.ListItem listItem : items) {
            final int gap = 5;
            final int checkboxSize = 13; // TODO: dynamically determine checkbox size

            // Hack which ignores SmallCheckboxComponent's text field, as it does not support line wrapping.
            var checkbox = UIComponents.smallCheckbox(null);
            checkbox.checked(listItem.toggled);

            final int textWidth = width - gap - 2 * insetSize - checkboxSize
                    + BackgroundlessTextAreaComponent.inflateWidth + BackgroundlessTextAreaComponent.innerPadding;

            var textArea = new BackgroundlessTextAreaComponent.Builder()
                    .setTextColor(textColor)
                    .setShowBackground(false)
                    .build(Sizing.fixed(textWidth), Sizing.fixed(BackgroundlessTextAreaComponent.computeHeight(listItem.text, textWidth)));
            textArea.text(listItem.text);

            layout.child(UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                    .child(checkbox)
                    .child(textArea)
                    .gap(gap)
                    .verticalAlignment(VerticalAlignment.CENTER));
        }

        layout.positioning(Positioning.absolute(hPos, vPos))
                .padding(Insets.of(insetSize))
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        return layout;
    }

    /**
     * Pulls relevant info from config and (re)puts the current list on hud, if it is already visible.
     */
    void rebuild() {
        if (visible) {
            Hud.remove(id);
            Hud.add(id, HudDisplay::makeLayout);
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
        return ConfigUtils.getCurList().items.size();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rebuild();
    }

}
