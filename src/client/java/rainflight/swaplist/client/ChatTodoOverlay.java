package rainflight.swaplist.client;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.layers.Layers;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;

public final class ChatTodoOverlay {

    static void addListToChatScreen() {

        Layers.add(
                (sx, sy) -> new FocusLayout(sx, sy, FlowLayout.Algorithm.VERTICAL),
                (instance) -> {
                    SwaplistClient.hudDisplay.setHideUnderScreen(true);
                    instance.adapter.rootComponent.child(TodoListScreen.makeDraggableList(false));
                },
                ChatScreen.class);

        // When ChatScreen closes, disable the HUD override.
        ScreenEvents.AFTER_INIT.register(
                (client, screeen, scaledWidth, scaledHeight) -> {
                    if (screeen instanceof ChatScreen) {
                        ScreenEvents.remove(screeen)
                                .register(
                                        (screen ->
                                                SwaplistClient.hudDisplay.setHideUnderScreen(
                                                        false)));
                    }
                });
    }

    /**
     * Special layout that allows keyboard focus shifts only when a child in it is focused.
     */
    static class FocusLayout extends FlowLayout {

        protected FocusLayout(
                Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
            super(horizontalSizing, verticalSizing, algorithm);
        }

        /**
         * Disallow controlling the chat overlay in most ways (focus changing, selections). This prevents clashing with
         * the chat screen. This is necessary as {@code BaseParentUIComponent} does not consume focus cycling inputs,
         * causing input duplication across the chat screen.
         **/
        @Override
        public boolean onKeyPress(KeyEvent input) {
            if (this.focusHandler == null) return false;

            // Accept focus cycling only when a child is focused.
            if (input.isCycleFocus()) {
                if (this.focusHandler.focused() != null) {
                    this.focusHandler.cycle(!input.hasShiftDown());
                    return true;
                }
                return false;
            } else if ((input.isUp() || input.isDown() || input.isLeft() || input.isRight()) && input.hasAltDown()) {
                if (this.focusHandler.focused() != null) {
                    this.focusHandler.moveFocus(input.key());
                    return true;
                }
                return false;
            }

            return super.onKeyPress(input);
        }
    }
}
