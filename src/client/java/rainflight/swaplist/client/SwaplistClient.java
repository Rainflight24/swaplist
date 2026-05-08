package rainflight.swaplist.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.arguments.ComponentArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import rainflight.swaplist.Swaplist;

public class SwaplistClient implements ClientModInitializer {
    private HudDisplay hudDisplay;

    private static int executeAddDocs(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("~/add."));
        return 1;
    }

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

        registerCommands();

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openTestScreen.consumeClick()) {
                if (client.player != null) {
                    // TODO: see https://docs.fabricmc.net/develop/rendering/gui/custom-screens#closing-the-screen on returning to the previous screen
                    if (Minecraft.getInstance().screen instanceof MyFirstScreen)
                        continue;
                    Minecraft.getInstance().setScreen(
                            new MyFirstScreen()
                    );
                }
            }
        });

        hudDisplay = new HudDisplay(Identifier.parse("test1"), 25, 30);
    }

    private void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("desc", StringArgumentType.greedyString())
                            .executes(this::executeAdd))
                    .executes(SwaplistClient::executeAddDocs));

            dispatcher.register(ClientCommandManager.literal("pop")
                    .executes(this::executePop));

            dispatcher.register(ClientCommandManager.literal("show")
                    .executes(context -> {
                        hudDisplay.setVisible(true);
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("hide")
                    .executes(context -> {
                        hudDisplay.setVisible(false);
                        return 1;
                    }));

            dispatcher.register(ClientCommandManager.literal("toggle")
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                            .executes(this::executeToggle)));

            dispatcher.register(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                            .executes(this::executeRemove)));
        });
    }

    private int executeAdd(CommandContext<FabricClientCommandSource> context) {
        String value1 = StringArgumentType.getString(context, "desc");
        hudDisplay.pushLine(value1);
        return 1;
    }

    private int executePop(CommandContext<FabricClientCommandSource> context) {
        hudDisplay.popLine();
        return 1;
    }

    private int executeRemove(CommandContext<FabricClientCommandSource> context) {
        int idx = IntegerArgumentType.getInteger(context, "index");
        if (idx >= 1 && idx <= hudDisplay.itemCount()) {
            hudDisplay.removeLine(idx);
            return 1;
        }
        else {
            context.getSource().sendError(Component.literal("Index %d must be between 1 and the length of the list, %d."
                    .formatted(idx, hudDisplay.itemCount())));
            return -1;
        }
    }

    private int executeToggle(CommandContext<FabricClientCommandSource> context) {
        int idx = IntegerArgumentType.getInteger(context, "index");
        if (idx >= 1 && idx <= hudDisplay.itemCount()) {
            hudDisplay.toggleLine(idx);
            return 1;
        }
        else {
            context.getSource().sendError(Component.literal("Index %d must be between 1 and the length of the list, %d."
                    .formatted(idx, hudDisplay.itemCount())));
            return -1;
        }
    }
}
