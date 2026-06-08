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
                (sx, sy) -> new NoKbFocusLayout(sx, sy, FlowLayout.Algorithm.VERTICAL),
                (instance) -> {
                    SwaplistClient.hudDisplay.setHideUnderScreen(true);
                    instance.adapter.rootComponent.child(TodoListScreen.makeDraggableList(false));

                    // When ChatScreen closes, disable the HUD override.
                    ScreenEvents.AFTER_INIT.register(
                            (client, screeen , scaledWidth, scaledHeight) -> {
                                ScreenEvents.remove(screeen).register(
                                        (screen -> {
                                            SwaplistClient.hudDisplay.setHideUnderScreen(false);
                                        })
                                );
                            }
                    );
                },
                ChatScreen.class);
    }

    static class NoKbFocusLayout extends FlowLayout {

        protected NoKbFocusLayout(
                Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
            super(horizontalSizing, verticalSizing, algorithm);
        }

        /**
         * Disallow controlling the chat overlay in most ways (focus changing, selections). This prevents clashing with
         * the chat screen.
         **/
        @Override
        public boolean onKeyPress(KeyEvent input) {
            if (input.isCycleFocus()) {
                return false;
            } else if ((input.isUp() || input.isDown() || input.isLeft() || input.isRight())
                    && input.hasAltDown()) {
                // Blank the alt modifier to block switching focus with it.
                return super.onKeyPress(
                        new KeyEvent(input.key(), input.scancode(), input.modifiers() & 0b011));
            }
            return super.onKeyPress(input);
        }
    }
}
