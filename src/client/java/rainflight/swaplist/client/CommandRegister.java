package rainflight.swaplist.client;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;

import static rainflight.swaplist.client.SwaplistClient.hudDisplay;

public class CommandRegister {
    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("add")
                    .then(ClientCommandManager.argument("desc", StringArgumentType.greedyString())
                            .executes(CommandRegister::executeAdd)));

            dispatcher.register(ClientCommandManager.literal("pop")
                    .executes(CommandRegister::executePop));

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
                            .executes(CommandRegister::executeToggle)));

            dispatcher.register(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                            .executes(CommandRegister::executeRemove)));

            dispatcher.register(ClientCommandManager.literal("width")
                    .then(ClientCommandManager.argument("newWidth", IntegerArgumentType.integer(100))
                            .executes(CommandRegister::executeWidth))
                    .executes(CommandRegister::executeShowWidth));

            dispatcher.register(ClientCommandManager.literal("new")
                    .executes(CommandRegister::executeNew));

            dispatcher.register(ClientCommandManager.literal("swap") // TODO: make a ArgumentType with cap = size
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer(1))
                            .executes(CommandRegister::executeSwap)));

            dispatcher.register(ClientCommandManager.literal("rename")
                    .then(ClientCommandManager.argument("newName", StringArgumentType.greedyString())
                            .executes(CommandRegister::executeRename)));

            dispatcher.register(ClientCommandManager.literal("save")
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                            .then(ClientCommandManager.argument("templateName", StringArgumentType.greedyString())
                                    .executes(CommandRegister::executeSave))));

        });
    }

    private static int executeAdd(CommandContext<FabricClientCommandSource> context) {
        String value1 = StringArgumentType.getString(context, "desc");
        hudDisplay.pushLine(value1);
        return 1;
    }

    private static int executePop(CommandContext<FabricClientCommandSource> context) {
        hudDisplay.popLine();
        return 1;
    }

    private static int executeRemove(CommandContext<FabricClientCommandSource> context) {
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

    private static int executeToggle(CommandContext<FabricClientCommandSource> context) {
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

    private static int executeShowWidth(CommandContext<FabricClientCommandSource> context) {
        context.getSource().sendFeedback(Component.literal("Current width: %d pixels."
                .formatted(SwaplistClient.CONFIG.listWidth())));
        return 1;
    }

    private static int executeWidth(CommandContext<FabricClientCommandSource> context) {
        int width = IntegerArgumentType.getInteger(context, "newWidth");
        SwaplistClient.CONFIG.listWidth(width);
        context.getSource().sendFeedback(Component.literal("Set width to: %d pixels."
                .formatted(width)));
        return 1;
    }

    private static int executeNew(CommandContext<FabricClientCommandSource> context) {
        // Create a new TodoList.
        SwaplistConfigModel.sanitizeLists();
        var l = new ArrayList<>(SwaplistClient.CONFIG.lists());
        l.add(new TodoList());
        SwaplistClient.CONFIG.lists(l);

        // Also change the active list.
        SwaplistClient.CONFIG.curActiveList(l.size());
        return 1;
    }

    private static int executeSwap(CommandContext<FabricClientCommandSource> context) {
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

    private static int executeRename(CommandContext<FabricClientCommandSource> context) {
        SwaplistConfigModel.sanitizeLists();
        String newName = StringArgumentType.getString(context, "newName");
        TodoList list = SwaplistConfigModel.getCurList();
        list.name = newName;
        SwaplistConfigModel.saveCurList(list);
        return 1;
    }

    private static int executeSave(CommandContext<FabricClientCommandSource> context) {
        SwaplistConfigModel.sanitizeLists();
        int index = IntegerArgumentType.getInteger(context, "index");
        String templateName = StringArgumentType.getString(context, "templateName");

        TodoList list = SwaplistConfigModel.getCurList();
        var templates = new HashMap<>(SwaplistClient.CONFIG.templates());
        templates.put(templateName, list);
        SwaplistClient.CONFIG.templates(templates);

        return 1;
    }
}
