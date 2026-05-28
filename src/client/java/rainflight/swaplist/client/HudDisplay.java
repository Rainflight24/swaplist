package rainflight.swaplist.client;

import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.hud.Hud;
import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.function.Consumer;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

public class HudDisplay {
    final private Identifier id;
    private boolean visible = true;

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

    /**
     * Adds the HUD's absolute placement to a fresh list component.
     */
    private static TodoListComponent makeHudComponent() {
        final TodoListComponent layout = new TodoListComponent(TodoListComponent.Overflow.TRUNCATE);
        layout.positioning(Positioning.absolute(CONFIG.listHorizontalPos(), CONFIG.listVerticalPos()));
        return layout;
    }

    /**
     * Pulls relevant info from config and (re)puts the current list on hud, if it is already visible.
     */
    void rebuild() {
        if (visible) {
            Hud.remove(id);
            Hud.add(id, HudDisplay::makeHudComponent);
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
