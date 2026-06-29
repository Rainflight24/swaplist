package rainflight.swaplist.client.ui;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.SmallCheckboxComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.parsing.UIParsing;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import rainflight.swaplist.Swaplist;
import rainflight.swaplist.client.ConfigUtils;
import rainflight.swaplist.client.TodoList;

/**
 * Todolist layout. Pulls and mutates config for related information.
 */
public class TodoListComponent extends FlowLayout {

    static final int INSET_SIZE = 10;
    private static final int LAYOUT_GAP = 3;
    private static final int LINE_SPACING = 2; // LabelComponent default spacing
    private static final int H_GAP = 1;
    // Manually computed size for SmallCheckboxComponent.
    private static final int CHECKBOX_SIZE = 13;
    private static final String OVERFLOW_TEXT = ". . .";

    static {
        UIParsing.registerFactory(
                Swaplist.of("todolist"),
                element -> new TodoListComponent(Overflow.UNBOUNDED, false));
    }

    private final Overflow overflow;
    // Whether to show the "new row" button. Editable surfaces (edit screen, chat overlay) set this.
    private final boolean showAddRow;

    // The title label, captured each build() so the drag wrapper can locate its bounds.
    private LabelComponent titleLabel;
    // The last text input of the list, captured each build() so the New List button knows what to
    // change focus to.
    private BackgroundlessTextAreaComponent lastTextArea;
    private List<SmallCheckboxComponent>
            checkboxes; // Cache for toggling a specific checkbox via hotbar swap.

    public TodoListComponent(Overflow overflow, boolean showAddRow) {
        super(Sizing.fixed(CONFIG.listWidth()), Sizing.content(), Algorithm.VERTICAL);
        this.overflow = overflow;
        this.showAddRow = showAddRow;

        this.gap(LAYOUT_GAP)
                .padding(Insets.of(INSET_SIZE))
                .surface(Surface.BLANK)
                .horizontalAlignment(HorizontalAlignment.LEFT)
                .verticalAlignment(VerticalAlignment.TOP);

        build();
    }

    private static int labelHeight(String text, int width) {
        final var font = Minecraft.getInstance().font;
        final int lines = font.split(Component.literal(text), width).size();
        return lines * (font.lineHeight + TodoListComponent.LINE_SPACING)
                - TodoListComponent.LINE_SPACING;
    }

    /**
     * Creates one row of the layout. Must be called during build().
     */
    private UIComponent layoutRow(
            TodoList.ListItem listItem, int index, int textWidth, Color textColor) {
        // Ignore SmallCheckboxComponent's text field, which does not support line wrapping.
        var checkbox = new SmallCheckboxComponent(null);
        checkbox.checked(listItem.toggled).onChanged().subscribe(new CheckboxListener(index));
        checkboxes.add(checkbox);

        // The text area sizes its own height to fit its text.
        var textArea =
                new BackgroundlessTextAreaComponent.Builder()
                        .setTextColor(textColor)
                        .setShowBackground(false)
                        .build(Sizing.fixed(textWidth));
        textArea.text(listItem.text).onChanged().subscribe(new TextAreaListener(index));
        lastTextArea = textArea;

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
                        - H_GAP; // inner layout cost

        final int[] textHeights = new int[items.size()];
        for (int i = 0; i < items.size(); i++) {
            textHeights[i] =
                    BackgroundlessTextAreaComponent.computeHeight(
                            items.get(i).text, textComponentWidth);
        }

        final int displayCount = displayCount(curList.name, textHeights, labelWidth);
        final boolean truncated = displayCount < items.size();

        this.checkboxes = new ArrayList<>();
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
        if (showAddRow) {
            appendAddRow();
        }

        // Subscribe non-handled keypresses to this's onKeyPress().
        ArrayList<UIComponent> descendants = new ArrayList<>();
        this.collectDescendants(descendants);
        for (UIComponent descendant : descendants) {
            descendant.keyPress().subscribe(this::onKeyPress);
        }
    }

    /**
     * Appends a clickable "new row" button which adds an empty row to the current list.
     */
    private void appendAddRow() {
        this.child(
                UIComponents.button(
                        Component.literal("New Row"),
                        (buttonComponent -> {
                            ConfigUtils.pushLine("");
                            this.build();

                            // Change focus to the newly added row.
                            if (lastTextArea != null) {
                                assert this.focusHandler() != null;
                                this.focusHandler().focus(lastTextArea, FocusSource.KEYBOARD_CYCLE);
                            }
                        })));
    }

    /**
     * Whether the absolute point {@code (x, y)} lies within the title's drag-handle region.
     */
    public boolean isOverTitle(int ignoredX, int y) {
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

        final int titleHeight = labelHeight(title, labelWidth);
        int heightNeeded = titleHeight;
        for (int textHeight : textHeights) {
            heightNeeded += Math.max(CHECKBOX_SIZE, textHeight) + LAYOUT_GAP;
        }

        final int heightUsable = height - 2 * INSET_SIZE;
        if (heightNeeded <= heightUsable) {
            return textHeights.length;
        }

        // Reserve the overflow label's height so the box never exceeds the limit.
        final int overflowHeight = labelHeight(OVERFLOW_TEXT, labelWidth);
        int heightLeft = heightUsable - titleHeight - (overflowHeight + LAYOUT_GAP);
        int count = 0;
        for (int textHeight : textHeights) {
            heightLeft -= Math.max(CHECKBOX_SIZE, textHeight) + LAYOUT_GAP;
            if (heightLeft < 0) break;
            count++;
        }
        return count;
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        KeyMapping[] keyHotbarSlots = Minecraft.getInstance().options.keyHotbarSlots;

        for (int i = 0; i < Math.min(keyHotbarSlots.length, checkboxes.size()); ++i) {
            if (keyHotbarSlots[i].matches(input)) {
                checkboxes.get(i).toggle();
                return true;
            }
        }

        return super.onKeyPress(input);
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

    private record CheckboxListener(int idx) implements SmallCheckboxComponent.OnChanged {

        @Override
        public void onChanged(boolean nowChecked) {
            ConfigUtils.toggleLine(idx);
        }
    }

    private record TextAreaListener(int idx) implements BackgroundlessTextAreaComponent.OnChanged {

        @Override
        public void onChanged(String value) {
            ConfigUtils.changeLine(idx, value);
        }
    }
}
