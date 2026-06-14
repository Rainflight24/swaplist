package rainflight.swaplist.client;

import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.layers.Layers;
import io.wispforest.owo.ui.util.FocusHandler;
import java.util.ArrayList;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

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

        protected FocusLayout(Sizing horizontalSizing, Sizing verticalSizing, Algorithm algorithm) {
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
            } else if ((input.isUp() || input.isDown() || input.isLeft() || input.isRight())
                    && input.hasAltDown()) {
                if (this.focusHandler.focused() != null) {
                    this.focusHandler.moveFocus(input.key());
                    return true;
                }
                return false;
            }

            return super.onKeyPress(input);
        }

        @Override
        public void mount(ParentUIComponent parent, int x, int y) {
            super.mount(parent, x, y);
            this.focusHandler = new ConsistentFocusHandler(this);
        }

        /**
         * Focus handler that makes alt-down focus shifting symmetric with other directions.
         * Note: This is undesired owo behavior, as alt-down focus shifting is handled inconsistently by
         * owo -- see PR #484 in owo-lib.
         */
        static class ConsistentFocusHandler extends FocusHandler {
            public ConsistentFocusHandler(ParentUIComponent root) {
                super(root);
            }

            @Override
            public void moveFocus(int keyCode) {
                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    var allChildren = new ArrayList<UIComponent>();
                    this.root.collectDescendants(allChildren);
                    allChildren.removeIf(
                            component ->
                                    !component.canFocus(UIComponent.FocusSource.KEYBOARD_CYCLE));
                    var closest = this.focused;

                    int closestX = Integer.MAX_VALUE, closestY = Integer.MAX_VALUE;

                    for (var child : allChildren) {
                        if (child == this.focused) continue;
                        assert this.focused
                                != null; // This is an assumption made in superclass code.
                        if (child.y() < this.focused.y() + this.focused.height()
                                || child.y() > closestY
                                || Math.abs(child.x() - this.focused.x()) > closestX) continue;

                        closest = child;
                        closestX = Math.abs(child.x() - this.focused.x());
                        closestY = child.y();
                    }

                    this.focus(closest, UIComponent.FocusSource.KEYBOARD_CYCLE);
                } else {
                    super.moveFocus(keyCode);
                }
            }
        }
    }
}
