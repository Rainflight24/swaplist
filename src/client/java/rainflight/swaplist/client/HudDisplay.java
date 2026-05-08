package rainflight.swaplist.client;

import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rainflight.swaplist.Swaplist;

import java.util.ArrayList;
import java.util.List;

public class HudDisplay {
    final Identifier id;
    private final List<Component> items;
    private final List<Boolean> toggled;
    private int width, height;
    private boolean visible = true;

    public HudDisplay(Identifier id, int width, int height) {
        this.id = id;
        items = new ArrayList<>();
        toggled = new ArrayList<>();
        this.width = width;
        this.height = height;

        rebuild();
    }

    private void rebuild() {
        if (visible) {
            Hud.remove(id);
            Hud.add(id, () -> {
                FlowLayout fl = UIContainers.verticalFlow(Sizing.fixed(width), Sizing.content())
                        .gap(3);

                Swaplist.LOGGER.debug(fl.padding().toString());
                for (int i=0; i<items.size(); i++) {
                    Component c = items.get(i);

                    final int gap = 5;

                    var checkbox = UIComponents.smallCheckbox(null);
                    checkbox.checked(toggled.get(i));


                    var label = UIComponents.label(c).maxWidth(width - gap*2 - checkbox.width());
                    fl.child(UIContainers.horizontalFlow(Sizing.content(), Sizing.content())
                            .child(checkbox)
                            .child(label)
                            .gap(5)
                            .verticalAlignment(VerticalAlignment.CENTER));
                }

                fl.positioning(Positioning.absolute(50, 50))
                        .padding(Insets.of(10))
                        .surface(Surface.BLANK)
                        .horizontalAlignment(HorizontalAlignment.LEFT)
                        .verticalAlignment(VerticalAlignment.TOP);

                return fl;
            });
        } else {
            Hud.remove(id);
        }
    }

    public int itemCount() {
        return items.size();
    }

    public Component item(int index) {
        return items.get(index);
    }

    /**
     * Adds a line of text to the displayed list.
     *
     * @param line The text to display.
     */
    public void pushLine(String line) {
        items.add(Component.literal(line));
        toggled.add(false);
        rebuild();
    }

    /**
     * Removes the most recently added line of text.
     */
    public void popLine() {
        if (!items.isEmpty()) {
            items.removeLast();
            toggled.removeLast();
            rebuild();
        }
    }

    /**
     * Removes the nth line.
     *
     * @param idx The one-indexed index to remove.
     */
    public void removeLine(int idx) {
        if (idx >= 1 && idx <= items.size()) {
            items.remove(idx - 1);
            toggled.remove(idx - 1);
            rebuild();
        }
    }

    /**
     * Toggles the nth checkbox.
     *
     * @param idx The one-indexed index to toggle.
     */
    public void toggleLine(int idx) {
        toggled.set(idx - 1, !toggled.get(idx - 1));
        rebuild();
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
        rebuild();
    }
}
