package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.parsing.UIParsing;
import io.wispforest.owo.util.EventSource;
import io.wispforest.owo.util.EventStream;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import rainflight.swaplist.Swaplist;

public class SelectableLabelComponent extends LabelComponent {
    static {
        UIParsing.registerFactory(
                Swaplist.of("selectable-label"),
                element -> new SelectableLabelComponent(Component.empty()));
    }

    protected final EventStream<OnSelect> selectEvents = OnSelect.newStream();

    public SelectableLabelComponent(Component text) {
        super(text);
        selectEvents.source().subscribe(() -> System.out.println("ASDF"));
    }

    @Override
    public boolean onMouseDown(MouseButtonEvent click, boolean doubled) {
        boolean result = super.onMouseDown(click, doubled);

        if (click.button() == GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            this.selectEvents.sink().onSelect();
            return true;
        }

        return result;
    }

    @Override
    public boolean onKeyPress(KeyEvent input) {
        boolean result = super.onKeyPress(input);

        if (input.isSelection()) {
            this.selectEvents.sink().onSelect();
            if (this.focusHandler() != null) {
                this.focusHandler().focus(this, FocusSource.KEYBOARD_CYCLE);
            }
            return true;
        }

        return result;
    }

    public EventSource<OnSelect> onChanged() {
        return this.selectEvents.source();
    }

    public interface OnSelect {
        static EventStream<SelectableLabelComponent.OnSelect> newStream() {
            return new EventStream<>(
                    subscribers ->
                            () -> {
                                for (var subscriber : subscribers) {
                                    subscriber.onSelect();
                                }
                            });
        }

        void onSelect();
    }
}
