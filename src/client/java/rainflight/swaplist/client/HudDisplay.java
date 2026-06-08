package rainflight.swaplist.client;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

import io.wispforest.owo.ui.core.Color;
import io.wispforest.owo.ui.core.Positioning;
import io.wispforest.owo.ui.hud.Hud;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.resources.Identifier;

public class HudDisplay {
    private final Identifier id;
    private boolean hideUnderScreen = false;

    public HudDisplay(Identifier id) {
        this.id = id;

        Consumer<Boolean> boolConsumer = unused -> rebuild();
        Consumer<Integer> intConsumer = unused -> rebuild();
        Consumer<String> stringConsumer = unused -> rebuild();
        Consumer<Color> colorConsumer = unused -> rebuild();
        Consumer<Map<String, TodoList>> listsConsumer = unused -> rebuild();

        CONFIG.subscribeToListVisible(boolConsumer);
        CONFIG.subscribeToListWidth(intConsumer);
        CONFIG.subscribeToListHeight(intConsumer);
        CONFIG.subscribeToListHorizontalPos(intConsumer);
        CONFIG.subscribeToListVerticalPos(intConsumer);
        CONFIG.subscribeToCurActiveList(stringConsumer);
        CONFIG.subscribeToListColor(colorConsumer);
        CONFIG.subscribeToLists(listsConsumer);

        rebuild();
    }

    /**
     * Adds the HUD's absolute placement to a fresh list component.
     */
    private static TodoListComponent makeHudComponent() {
        final TodoListComponent layout =
                new TodoListComponent(TodoListComponent.Overflow.TRUNCATE, true, false);
        layout.positioning(
                Positioning.absolute(CONFIG.listHorizontalPos(), CONFIG.listVerticalPos()));
        return layout;
    }

    /**
     * Pulls relevant info from config and (re)puts the current list on hud, if it is already visible.
     */
    void rebuild() {
        if (isVisible()) {
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
        return CONFIG.listVisible() && !hideUnderScreen;
    }

    /**
     * Non-persistently changes HUD visibility of the todolist. Useful when the list is on a non-HUD surface.
     */
    public void setHideUnderScreen(boolean hideUnderScreen) {
        this.hideUnderScreen = hideUnderScreen;
        rebuild();
    }
}
