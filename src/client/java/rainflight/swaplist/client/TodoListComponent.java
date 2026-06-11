package rainflight.swaplist.client;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;

/**
 * Todolist layout. Pulls and mutates config for related information.
 */
public class TodoListComponent extends FlowLayout {

    static final int INSET_SIZE = 10;
    private static final int LAYOUT_GAP = 3;
    private static final int LINE_SPACING = 2; // LabelComponent default spacing
    private static final int H_GAP = 1;
    private static final int CHECKBOX_SIZE =
            13; // Manually computed size for SmallCheckboxComponent.
    private static final String OVERFLOW_TEXT = ". . .";
    private final Overflow overflow;
    private final boolean checkboxFocus;

    // The title label, captured each build() so the drag wrapper can locate its bounds.
    private LabelComponent titleLabel;

    public TodoListComponent(Overflow overflow, boolean checkboxFocus) {
        super(Sizing.fixed(CONFIG.listWidth()), Sizing.content(), Algorithm.VERTICAL);
        this.overflow = overflow;
        this.checkboxFocus = checkboxFocus;

        this.gap(LAYOUT_GAP)
                .padding(Insets.of(INSET_SIZE))
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        build();
    }

    private static int labelHeight(String text, int width, int lineSpacing) {
        final var font = Minecraft.getInstance().font;
        final int lines = font.split(Component.literal(text), width).size();
        return lines * (font.lineHeight + lineSpacing) - lineSpacing;
    }

    private UIComponent layoutRow(
            TodoList.ListItem listItem, int index, int textWidth, Color textColor) {
        // Ignore SmallCheckboxComponent's text field, which does not support line wrapping.
        // var checkbox = UIComponents.smallCheckbox(null).checked(listItem.toggled);
        var checkbox =
                checkboxFocus
                        ? new SmallCheckboxComponent(null)
                        : new FocuslessSmallCheckboxComponent(null);
        checkbox.checked(listItem.toggled).onChanged().subscribe(new CheckboxListener(index));

        // The text area sizes its own height to fit the text.
        var textArea =
                new BackgroundlessTextAreaComponent.Builder()
                        .setTextColor(textColor)
                        .setShowBackground(false)
                        .build(Sizing.fixed(textWidth));
        textArea.text(listItem.text).onChanged().subscribe(new TextAreaListener(index));

        return UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                .child(checkbox)
                .child(textArea)
                .gap(H_GAP)
                .verticalAlignment(VerticalAlignment.CENTER);
    }

    /**
     * Rebuilds the rows from current config in place.
     */
    void build() {
        this.clearChildren();

        final int width = CONFIG.listWidth();
        horizontalSizing(Sizing.fixed(width));

        final Color textColor = CONFIG.listColor();
        final TodoList curList = ConfigUtils.getCurList();
        final List<TodoList.ListItem> items = curList.items;

        // Line wrapping requires manual width calculations.
        final int labelWidth = width - 2 * INSET_SIZE;
        titleLabel =
                UIComponents.label(Component.literal(curList.name))
                        .color(textColor)
                        .maxWidth(labelWidth)
                        .shadow(true);
        this.child(titleLabel);

        final int textComponentWidth =
                width
                        - 2 * INSET_SIZE // overall layout cost
                        - CHECKBOX_SIZE
                        - H_GAP // inner layout cost
                        // empty right-hand side space to ignore
                        + BackgroundlessTextAreaComponent.innerPadding
                        + BackgroundlessTextAreaComponent.inflateWidth;

        final int[] textHeights = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            textHeights[i] =
                    BackgroundlessTextAreaComponent.computeHeight(
                            items.get(i).text, textComponentWidth);
        }

        final int displayCount = displayCount(curList.name, textHeights, labelWidth);
        final boolean truncated = displayCount < items.size();

        for (int i = 0; i < displayCount; i++) {
            this.child(layoutRow(items.get(i), i, textComponentWidth, textColor));
        }
        if (truncated) {
            this.child(
                    UIComponents.label(Component.literal(OVERFLOW_TEXT))
                            .color(textColor)
                            .maxWidth(labelWidth)
                            .shadow(true));
        }
    }

    /**
     * Whether the absolute point {@code (x, y)} lies within the title's drag-handle region.
     */
    public boolean isOverTitle(int unusedX, int y) {
        return titleLabel.y() <= y && y <= titleLabel.y() + titleLabel.height();
    }

    /**
     * Number of rows to show: the item count, or fewer when truncating to fit {@code listHeight}.
     */
    private int displayCount(String title, int[] textHeights, int labelWidth) {
        final int height = CONFIG.listHeight();
        if (overflow != Overflow.TRUNCATE || height == -1) {
            return textHeights.length;
        }

        final int titleHeight = labelHeight(title, labelWidth, LINE_SPACING);
        int heightNeeded = titleHeight;
        for (int textHeight : textHeights) {
            heightNeeded += Math.max(CHECKBOX_SIZE, textHeight) + LAYOUT_GAP;
        }

        final int heightUsable = height - 2 * INSET_SIZE;
        if (heightNeeded <= heightUsable) {
            return textHeights.length;
        }

        // Reserve the overflow label's height so the box never exceeds the limit.
        final int overflowHeight = labelHeight(OVERFLOW_TEXT, labelWidth, LINE_SPACING);
        int heightLeft = heightUsable - titleHeight - (overflowHeight + LAYOUT_GAP);
        int count = 0;
        for (int textHeight : textHeights) {
            heightLeft -= Math.max(CHECKBOX_SIZE, textHeight) + LAYOUT_GAP;
            if (heightLeft < 0) break;
            count++;
        }
        return count;
    }

    // Handles content taller than the configured height.
    public enum Overflow {
        /**
         * Drop rows that don't fit and append an overflow label; honors {@code listHeight}.
         */
        TRUNCATE,
        /**
         * Show every row regardless of height.
         */
        UNBOUNDED
    }

    private static class CheckboxListener implements SmallCheckboxComponent.OnChanged {
        private final int idx;

        public CheckboxListener(int idx) {
            this.idx = idx;
        }

        @Override
        public void onChanged(boolean nowChecked) {
            ConfigUtils.toggleLine(idx);
        }
    }

    private static class TextAreaListener implements BackgroundlessTextAreaComponent.OnChanged {
        private final int idx;

        public TextAreaListener(int idx) {
            this.idx = idx;
        }

        @Override
        public void onChanged(String value) {
            ConfigUtils.changeLine(idx, value);
        }
    }

    /**
     * Variant checkbox with mouse-only controls.
     */
    static class FocuslessSmallCheckboxComponent extends SmallCheckboxComponent {
        public FocuslessSmallCheckboxComponent(Component label) {
            super(label);
        }

        @Override
        public boolean onKeyPress(KeyEvent input) {
            return false;
        }
    }
}
