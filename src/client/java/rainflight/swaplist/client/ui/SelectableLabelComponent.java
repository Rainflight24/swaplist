package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.network.chat.Component;
import rainflight.swaplist.Swaplist;

/**
 * Focusable LabelComponent.
 */
public class SelectableLabelComponent extends LabelComponent {
    static {
        UIParsing.registerFactory(
                Swaplist.of("selectable-label"),
                element -> new SelectableLabelComponent(Component.empty()));
    }

    public SelectableLabelComponent(Component text) {
        super(text);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }
}
