package rainflight.swaplist.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import rainflight.swaplist.Swaplist;
import rainflight.swaplist.client.ui.*;

public class SwaplistClient implements ClientModInitializer {
    public static final rainflight.swaplist.client.SwaplistConfig CONFIG =
            rainflight.swaplist.client.SwaplistConfig.createAndLoad();
    public static HudDisplay hudDisplay;

    @Override
    public void onInitializeClient() {
        KeyMapping.Category category =
                new KeyMapping.Category(
                        Identifier.fromNamespaceAndPath(Swaplist.MOD_ID, "swaplist"));

        KeyMapping openTodoListScreen =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.swaplist.open_todolist_screen", // TODO: Translations
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_J,
                                category));

        KeyMapping openTestScreen =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.swaplist.open_test_screen", // TODO: Translations
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_Y,
                                category));

        KeyMapping openTestModelScreen =
                KeyBindingHelper.registerKeyBinding(
                        new KeyMapping(
                                "key.swaplist.open_model_screen", // TODO: Translations
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_MINUS,
                                category));

        CommandRegister.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(
                client -> {
                    while (openTodoListScreen.consumeClick()) {
                        if (client.player != null) {
                            if (client.screen instanceof TodoListScreen) continue;
                            client.setScreen(new TodoListScreen());
                        }
                    }

                    while (openTestScreen.consumeClick()) {
                        if (client.player != null) {
                            if (client.screen instanceof ListsScreen) continue;
                            client.setScreen(new ListsScreen(Component.empty()));
                        }
                    }

//                    while (openTestModelScreen.consumeClick()) {
//                        if (client.player != null) {
//                            if (client.screen instanceof MyModelScreen) continue;
//                            client.setScreen(new MyModelScreen());
//                        }
//                    }
                });

        hudDisplay = new HudDisplay(Swaplist.of("hud"));
        ChatTodoOverlay.addListToChatScreen();
        ConfigUtils.ensureValidActiveList();

        // Auto-save on modification is disabled, so flush any deferred edits to clean shutdown.
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigUtils.save());
    }
}
