package rainflight.swaplist.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import rainflight.swaplist.Swaplist;

public class SwaplistClient implements ClientModInitializer {
    public static final rainflight.swaplist.client.SwaplistConfig CONFIG = rainflight.swaplist.client.SwaplistConfig.createAndLoad();
    protected static HudDisplay hudDisplay;

    @Override
    public void onInitializeClient() {

        KeyMapping.Category CATEGORY = new KeyMapping.Category(
                Identifier.fromNamespaceAndPath(Swaplist.MOD_ID, "swaplist")
        );

        KeyMapping openTestScreen = KeyBindingHelper.registerKeyBinding(
                new KeyMapping(
                        "key.swaplist.open_test_screen", // TODO: Translations
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_J,
                        CATEGORY
                ));

        CommandRegister.registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openTestScreen.consumeClick()) {
                if (client.player != null) {
                    // TODO: see https://docs.fabricmc.net/develop/rendering/gui/custom-screens#closing-the-screen on returning to the previous screen
                    if (Minecraft.getInstance().screen instanceof TodoListScreen)
                        continue;
                    Minecraft.getInstance().setScreen(
                            new TodoListScreen()
                    );
                }
            }
        });

        hudDisplay = new HudDisplay(Identifier.fromNamespaceAndPath(Swaplist.MOD_ID, "hud"));

        // Auto-save on modification is disabled, so flush any deferred edits on clean shutdown.
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> ConfigUtils.save());
    }

}
