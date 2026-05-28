package rainflight.swaplist.client;

import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

public class HudDisplay {
    final private Identifier id;
    private boolean visible = true;
    static int INSET_SIZE = 10;

    public HudDisplay(Identifier id) {
        this.id = id;

        Consumer<Integer> intConsumer = _unused -> rebuild();
        Consumer<String> stringConsumer = _unused -> rebuild();
        Consumer<Color> colorConsumer = _unused -> rebuild();
        Consumer<Map<String, TodoList>> listsConsumer = _unused -> rebuild();

        CONFIG.subscribeToListWidth(intConsumer);
        CONFIG.subscribeToListHeight(intConsumer);
        CONFIG.subscribeToListHorizontalPos(intConsumer);
        CONFIG.subscribeToListVerticalPos(intConsumer);
        CONFIG.subscribeToCurActiveList(stringConsumer);
        CONFIG.subscribeToListColor(colorConsumer);
        CONFIG.subscribeToLists(listsConsumer);
    }

    private static int labelHeight(String text, int width, int lineSpacing) {
        final var font = Minecraft.getInstance().font;
        final int lines = font.split(Component.literal(text), width).size();
        return lines * (font.lineHeight + lineSpacing) - lineSpacing;
    }

    static ParentUIComponent makeLayout() {
        final int hPos = CONFIG.listHorizontalPos();
        final int vPos = CONFIG.listVerticalPos();
        final int width = CONFIG.listWidth();
        final int height = CONFIG.listHeight();
        final Color textColor = CONFIG.listColor();

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
            layout.child(layoutRow(items.get(i), i, hGap, textComponentWidth, textColor));
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
     * Listener which toggles the corresponding checkbox's config.
     */
    private static class checkboxListener implements SmallCheckboxComponent.OnChanged {
        final private int idx;
        public checkboxListener(int idx) {
            this.idx = idx;
        }

        @Override
        public void onChanged(boolean nowChecked) {
            ConfigUtils.toggleLine(idx);
        }
    }

    /**
     * Creates one row of the layout.
     */
    private static UIComponent layoutRow(TodoList.ListItem listItem, int checkboxIndex, int hGap, int textWidth, Color textColor) {
        // Ignore SmallCheckboxComponent's text field, which does not support line wrapping.
        var checkbox = UIComponents.smallCheckbox(null)
                .checked(listItem.toggled);
        checkbox.onChanged().subscribe(new checkboxListener(checkboxIndex));

        // The text area sizes its own height to fit the text.
        var textArea = new BackgroundlessTextAreaComponent.Builder()
                .setTextColor(textColor)
                .setShowBackground(false)
                .build(Sizing.fixed(textWidth));
        textArea.text(listItem.text)
                .onChanged().subscribe(new textAreaListener(checkboxIndex));

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

    private static class textAreaListener implements BackgroundlessTextAreaComponent.OnChanged {
        final private int idx;

        public textAreaListener(int idx) {
            this.idx = idx;
        }

        @Override
        public void onChanged(String value) {
            ConfigUtils.changeLine(idx, value);
        }
    }
}
