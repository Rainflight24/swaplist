package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.core.OwoUIGraphics;
import net.minecraft.network.chat.Component;

/**
 * LabelComponent highlightable on mouse focus.
 */
public class SelectableLabelComponent extends LabelComponent {
    public SelectableLabelComponent(Component text) {
        super(text);
    }

    @Override
    public boolean canFocus(FocusSource source) {
        return true;
    }

    @Override
    public void draw(OwoUIGraphics graphics, int mouseX, int mouseY, float partialTicks, float delta) {
        super.draw(graphics, mouseX, mouseY, partialTicks, delta);
        if (this.focusHandler() != null && this.focusHandler().focused() == this) {
            super.drawFocusHighlight(graphics, mouseX, mouseY, partialTicks, delta);
        }
    }

    @Override
    public void drawFocusHighlight(OwoUIGraphics context, int mouseX, int mouseY, float partialTicks, float delta) {
    }
}
