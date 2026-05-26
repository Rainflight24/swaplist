package rainflight.swaplist.client;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HudDisplay {
    final private Identifier id;
    private boolean visible = true;
    private boolean needsRebuild = true;
    static int INSET_SIZE = 10;

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

    private static int labelHeight(String text, int width, int lineSpacing) {
        final var font = Minecraft.getInstance().font;
        final int lines = font.split(Component.literal(text), width).size();
        return lines * (font.lineHeight + lineSpacing) - lineSpacing;
    }

    static ParentUIComponent makeLayout() {
        final int hPos = SwaplistClient.CONFIG.listHorizontalPos();
        final int vPos = SwaplistClient.CONFIG.listVerticalPos();
        final int width = SwaplistClient.CONFIG.listWidth();
        final int height = SwaplistClient.CONFIG.listHeight();
        final Color textColor = SwaplistClient.CONFIG.listColor();

        final TodoList curList = ConfigUtils.getCurList();
        final List<TodoList.ListItem> items = curList.items;

        final int layoutGap = 3;
        final int lineSpacing = 2; // LabelComponent default spacing
        final int hGap = 1;
        final int checkboxSize = 13;

        final String overflowText = ". . .";

        final FlowLayout layout = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                .gap(layoutGap);

        // Line wrapping requires manual width calculations.
        int labelWidth = width - 2 * INSET_SIZE;
        layout.child(UIComponents.label(Component.literal(curList.name))
                .color(textColor)
                .maxWidth(labelWidth));

        final int textComponentWidth = width - 2 * INSET_SIZE // overall layout cost
                - checkboxSize - hGap // inner layout cost
                // empty right-hand side space to ignore
                + BackgroundlessTextAreaComponent.innerPadding + BackgroundlessTextAreaComponent.inflateWidth;

        final int[] textHeights = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            textHeights[i] = BackgroundlessTextAreaComponent.computeHeight(items.get(i).text, textComponentWidth);
        }

        final int heightUsable = height - 2 * INSET_SIZE;
        final int titleHeight = labelHeight(curList.name, labelWidth, lineSpacing);

        int heightNeeded = titleHeight;
        for (int textHeight : textHeights) {
            heightNeeded += Math.max(checkboxSize, textHeight) + layoutGap;
        }

        final boolean truncated = heightNeeded > heightUsable && height != -1;
        int displayCount = items.size();
        if (truncated) {
            // Reserve the overflow label's height so the box never exceeds the limit.
            final int overflowHeight = labelHeight(overflowText, labelWidth, lineSpacing);
            int heightLeft = heightUsable - titleHeight - (overflowHeight + layoutGap);
            displayCount = 0;
            for (int textHeight : textHeights) {
                heightLeft -= Math.max(checkboxSize, textHeight) + layoutGap;
                if (heightLeft < 0) break;
                displayCount++;
            }
        }

        for (int i = 0; i < displayCount; i++) {
            layout.child(layoutRow(items.get(i), hGap, textComponentWidth, textColor, textHeights[i]));
        }
        if (truncated) {
            layout.child(UIComponents.label(Component.literal(overflowText)).color(textColor).maxWidth(labelWidth));
        }

        layout.positioning(Positioning.absolute(hPos, vPos))
                .padding(Insets.of(INSET_SIZE))
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        return layout;
    }

    /**
     * Creates one row of the layout.
     */
    private static UIComponent layoutRow(TodoList.ListItem listItem, int hGap, int textWidth, Color textColor, int textHeight) {
        // Ignore SmallCheckboxComponent's text field, which does not support line wrapping.
        var checkbox = UIComponents.smallCheckbox(null);
        checkbox.checked(listItem.toggled);

        var textArea = new BackgroundlessTextAreaComponent.Builder()
                .setTextColor(textColor)
                .setShowBackground(false)
                .build(Sizing.fixed(textWidth), Sizing.fixed(textHeight));
        textArea.text(listItem.text);

        return UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(checkbox)
                .child(textArea)
                .gap(hGap)
                .verticalAlignment(VerticalAlignment.CENTER);
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
