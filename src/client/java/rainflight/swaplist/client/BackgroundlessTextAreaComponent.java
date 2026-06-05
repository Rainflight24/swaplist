package rainflight.swaplist.client;

import io.wispforest.owo.mixin.ui.access.MultiLineEditBoxAccessor;
import io.wispforest.owo.mixin.ui.access.MultilineTextFieldAccessor;
import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.CursorStyle;
import io.wispforest.owo.ui.core.Size;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.parsing.UIModel;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import io.wispforest.owo.util.Observable;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.MultilineTextField;
import net.minecraft.client.gui.components.Whence;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.w3c.dom.Element;
import rainflight.swaplist.Swaplist;

/**
 * Copy of {@code io.wispforest.owo.ui.component.TextAreaComponent} with no background.
 */
@SuppressWarnings({"unused", "UnusedReturnValue"}) // functionality from owo-lib left as-is
public class BackgroundlessTextAreaComponent extends MultiLineEditBox {

    public static int inflateWidth = 9; // see inflate()
    public static int innerPadding = 4; // see innerPadding()
    protected final Observable<String> textValue = Observable.of("");
    protected final EventStream<OnChanged> changedEvents = OnChanged.newStream();
    protected final MultilineTextField editBox;
    protected final Observable<Boolean> displayCharCount = Observable.of(false);
    protected final Observable<Integer> maxLines = Observable.of(-1);

    /**
     * Whether the vertical axis sizes itself to the text, i.e. the caller passed {@link Sizing#content()}.
     */
    private final boolean autoHeight;

    /**
     * Last height applied by {@link #resizeToContent()}; {@code -1} until first applied.
     */
    private int appliedContentHeight = -1;

    protected BackgroundlessTextAreaComponent(Sizing horizontalSizing, Sizing verticalSizing) {
        this(
                horizontalSizing,
                verticalSizing,
                Component.empty(),
                Color.WHITE,
                false,
                Color.WHITE,
                false);
    }

    protected BackgroundlessTextAreaComponent(
            Sizing horizontalSizing,
            Sizing verticalSizing,
            Component message,
            Color textColor,
            boolean textShadow,
            Color cursorColor,
            boolean showBackground) {
        super(
                Minecraft.getInstance().font,
                0,
                0,
                0,
                0,
                Component.empty(),
                message,
                textColor.argb(),
                textShadow,
                cursorColor.argb(),
                showBackground,
                true);
        this.editBox = ((MultiLineEditBoxAccessor) this).owo$getTextField();

        // owo cannot resolve Sizing.content() for this widget type (its VanillaWidgetComponent
        // wrapper only handles TextAreaComponent), so we honor content() ourselves: install a
        // placeholder height and let resizeToContent() drive it. Sizing.fixed(...) is passed
        // through and left untouched.
        this.autoHeight = verticalSizing.isContent();
        this.sizing(horizontalSizing, this.autoHeight ? Sizing.fixed(0) : verticalSizing);

        this.textValue.observe(this.changedEvents.sink()::onChanged);
        Observable.observeAll(
                this.widgetWrapper()::notifyParentIfMounted, this.displayCharCount, this.maxLines);

        super.setValueListener(
                s -> {
                    this.textValue.set(s);
                    this.resizeToContent();

                    if (this.maxLines.get() < 0) return;
                    this.widgetWrapper().notifyParentIfMounted();
                });
    }

    /**
     * Computes the component height needed to display {@code text} with the given component width.
     *
     * @param text           to display
     * @param componentWidth the width allocated to the component
     * @return the desired height
     */
    public static int computeHeight(String text, int componentWidth) {
        final int innerWidth = componentWidth - 2 * innerPadding - inflateWidth;
        final var font = Minecraft.getInstance().font;
        final int lineCount = font.split(Component.literal(text), innerWidth).size();
        return lineCount * font.lineHeight + 2 * innerPadding;
    }

    /**
     * When {@link #autoHeight auto-sizing}, resizes the component vertically to exactly fit the current
     * text at the current width. Changing the vertical sizing notifies the parent layout, which
     * re-inflates this component; the {@link #appliedContentHeight} guard keeps that from looping.
     * No-op for a fixed height, or until a width is known (i.e. after the first inflate).
     */
    private void resizeToContent() {
        if (!this.autoHeight || this.width() <= 0) return;

        final int desired = computeHeight(this.getValue(), this.width());
        if (desired == this.appliedContentHeight) return;

        this.appliedContentHeight = desired;
        this.verticalSizing(Sizing.fixed(desired));
    }

    @Override
    @Deprecated(forRemoval = true)
    public void setValueListener(@NonNull Consumer<String> changeListener) {
        Swaplist.LOGGER.warn("setChangeListener stub on BackgroundlessTextAreaComponent invoked");
    }

    @Override
    public void update(float delta, int mouseX, int mouseY) {
        super.update(delta, mouseX, mouseY);
        this.cursorStyle(
                this.scrollbarVisible() && mouseX >= this.getX() + this.width - 9
                        ? CursorStyle.NONE
                        : CursorStyle.TEXT);
    }

    @Override
    protected void renderDecorations(GuiGraphics context) {
        this.height -= 1;

        var matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(-9, 1);

        int previousMaxLength = this.editBox.characterLimit();
        this.editBox.setCharacterLimit(Integer.MAX_VALUE);

        super.renderDecorations(context);

        this.editBox.setCharacterLimit(previousMaxLength);

        matrices.popMatrix();
        this.height += 1;

        if (this.displayCharCount.get()) {
            var text =
                    this.editBox.hasCharacterLimit()
                            ? Component.translatable(
                                    "gui.multiLineEditBox.character_limit",
                                    this.editBox.value().length(),
                                    this.editBox.characterLimit())
                            : Component.literal(String.valueOf(this.editBox.value().length()));

            var textRenderer = Minecraft.getInstance().font;
            context.drawString(
                    textRenderer,
                    text,
                    this.getX() + this.width - textRenderer.width(text),
                    this.getY() + this.height + 3,
                    0xa0a0a0);
        }
    }

    @Override
    public boolean mouseClicked(@NonNull MouseButtonEvent click, boolean doubled) {
        this.width -= 9;
        var result = super.mouseClicked(click, doubled);
        this.width += 9;

        return result;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent input) {
        // Divergence from TextAreaComponent behavior -- let pressing tab cycle focus.
        if (input.isCycleFocus()) {
            return false;
        }
        return super.keyPressed(input);
    }

    @Override
    public void inflate(Size space) {
        super.inflate(space);

        int cursor = this.editBox.cursor();
        int selection = ((MultilineTextFieldAccessor) this.editBox).owo$getSelectCursor();

        ((MultilineTextFieldAccessor) this.editBox)
                .owo$setWidth(this.width() - this.totalInnerPadding() - 9);
        this.editBox.setValue(this.getValue(), false);

        super.inflate(space);
        this.editBox.setValue(this.getValue(), false);

        this.editBox.seekCursor(Whence.ABSOLUTE, cursor);
        ((MultilineTextFieldAccessor) this.editBox).owo$setSelectCursor(selection);

        // Show the top of the text to clip overflow at top.
        this.setScrollAmount(0);

        this.resizeToContent();
    }

    public EventSource<OnChanged> onChanged() {
        return changedEvents.source();
    }

    public BackgroundlessTextAreaComponent maxLines(int maxLines) {
        this.maxLines.set(maxLines);
        return this;
    }

    public int maxLines() {
        return this.maxLines.get();
    }

    public BackgroundlessTextAreaComponent displayCharCount(boolean displayCharCount) {
        this.displayCharCount.set(displayCharCount);
        return this;
    }

    public boolean displayCharCount() {
        return this.displayCharCount.get();
    }

    public BackgroundlessTextAreaComponent text(String text) {
        this.setValue(text);
        return this;
    }

    @Override
    public int heightOffset() {
        return this.displayCharCount.get() ? -12 : 0;
    }

    @Override
    public void parseProperties(UIModel model, Element element, Map<String, Element> children) {
        super.parseProperties(model, element, children);

        UIParsing.apply(
                children, "display-char-count", UIParsing::parseBool, this::displayCharCount);
        UIParsing.apply(
                children, "max-length", UIParsing::parseUnsignedInt, this::setCharacterLimit);
        UIParsing.apply(children, "max-lines", UIParsing::parseUnsignedInt, this::maxLines);
        UIParsing.apply(children, "text", $ -> $.getTextContent().strip(), this::text);
    }

    public interface OnChanged {
        static EventStream<OnChanged> newStream() {
            return new EventStream<>(
                    subscribers ->
                            value -> {
                                for (var subscriber : subscribers) {
                                    subscriber.onChanged(value);
                                }
                            });
        }

        void onChanged(String value);
    }

    public static class Builder {
        private Color textColor = Color.WHITE;
        private boolean showBackground = true;
        private boolean textShadow = false;
        private Color cursorColor = Color.WHITE;

        private Component message = Component.empty();

        public Builder setTextColor(Color color) {
            this.textColor = color;
            return this;
        }

        public Builder setShowBackground(boolean showBackground) {
            this.showBackground = showBackground;
            return this;
        }

        public Builder setTextShadow(boolean textShadow) {
            this.textShadow = textShadow;
            return this;
        }

        public Builder setCursorColor(Color cursorColor) {
            this.cursorColor = cursorColor;
            return this;
        }

        public Builder setMessage(Component message) {
            this.message = message;
            return this;
        }

        public BackgroundlessTextAreaComponent build(
                Sizing horizontalSizing, Sizing verticalSizing) {
            return new BackgroundlessTextAreaComponent(
                    horizontalSizing,
                    verticalSizing,
                    message,
                    textColor,
                    textShadow,
                    cursorColor,
                    showBackground);
        }

        /**
         * Builds a component that sizes its own height to fit its text; only the width is supplied.
         * Equivalent to passing {@link Sizing#content()} as the vertical sizing.
         */
        public BackgroundlessTextAreaComponent build(Sizing horizontalSizing) {
            return build(horizontalSizing, Sizing.content());
        }
    }
}
