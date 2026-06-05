package rainflight.swaplist.client;

import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.layers.Layers;
import net.minecraft.client.gui.screens.ChatScreen;

public final class ChatTodoOverlay {

    static void addListToChatScreen() {
        SwaplistClient.hudDisplay.setVisible(false);

        Layers.add(
                UIContainers::verticalFlow,
                (instance) -> instance.adapter.rootComponent.child(TodoListScreen.makeDraggableList()),
                ChatScreen.class
        );
    }
}
