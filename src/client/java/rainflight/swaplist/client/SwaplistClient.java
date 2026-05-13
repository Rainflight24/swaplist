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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;
import rainflight.swaplist.Swaplist;

import java.util.ArrayList;

public class SwaplistClient implements ClientModInitializer {
    public static final rainflight.swaplist.client.SwaplistConfig CONFIG = rainflight.swaplist.client.SwaplistConfig.createAndLoad();
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

        hudDisplay = new HudDisplay(Identifier.parse("test1"));
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

            dispatcher.register(ClientCommandManager.literal("width")
                    .then(ClientCommandManager.argument("newWidth", IntegerArgumentType.integer(100))
                            .executes(this::executeWidth))
                    .executes(this::executeShowWidth));

            dispatcher.register(ClientCommandManager.literal("new")
                    .executes(this::executeNew));

            dispatcher.register(ClientCommandManager.literal("swap") // TODO: make a ArgumentType with cap = size
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer(1))
                            .executes(this::executeSwap)));

            dispatcher.register(ClientCommandManager.literal("rename")
                    .then(ClientCommandManager.argument("newName", StringArgumentType.greedyString())
                            .executes(this::executeRename)));

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
        } else {
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
        } else {
            context.getSource().sendError(Component.literal("Index %d must be between 1 and the length of the list, %d."
                    .formatted(idx, hudDisplay.itemCount())));
            return -1;
        }
    }

    private int executeShowWidth(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("Current width: %d pixels."
                .formatted(SwaplistClient.CONFIG.listWidth())));
        return 1;
    }

    private int executeWidth(CommandContext<FabricClientCommandSource> context) {
        int width = IntegerArgumentType.getInteger(context, "newWidth");
        SwaplistClient.CONFIG.listWidth(width);
        context.getSource().sendFeedback(Component.literal("Set width to: %d pixels."
                .formatted(width)));
        return 1;
    }

    private int executeNew(CommandContext<FabricClientCommandSource> context) {
        // Create a new TodoList.
        SwaplistConfigModel.sanitizeLists();
        var l = new ArrayList<>(SwaplistClient.CONFIG.lists());
        l.add(new TodoList());
        SwaplistClient.CONFIG.lists(l);

        // Also change the active list.
        SwaplistClient.CONFIG.curActiveList(l.size());
        return 1;
    }

    private int executeSwap(CommandContext<FabricClientCommandSource> context) {
        SwaplistConfigModel.sanitizeLists();
        int index = IntegerArgumentType.getInteger(context, "index");

        if (index >= 1 && index <= SwaplistClient.CONFIG.lists().size()) {
            SwaplistClient.CONFIG.curActiveList(index);
            return 1;
        }

        context.getSource().sendError(Component.literal("Index %d must be between 1 and the number of lists, %d."
                .formatted(index, SwaplistClient.CONFIG.lists().size())));
        return -1;
    }

    private int executeRename(CommandContext<FabricClientCommandSource> context) {
        SwaplistConfigModel.sanitizeLists();
        String newName = StringArgumentType.getString(context, "newName");
        TodoList list = SwaplistConfigModel.getCurList();
        list.name = newName;
        SwaplistConfigModel.saveCurList(list);
        return 1;
    }
}
