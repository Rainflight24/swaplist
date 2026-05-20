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
import java.util.Map;
import java.util.Optional;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;
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
                            .suggests(new ListIndexSuggestionProvider())
                            .executes(CommandRegister::executeToggle)));

            dispatcher.register(ClientCommandManager.literal("remove")
                    .then(ClientCommandManager.argument("index", IntegerArgumentType.integer())
                            .suggests(new ListIndexSuggestionProvider())
                            .executes(CommandRegister::executeRemove)));

            dispatcher.register(ClientCommandManager.literal("width")
                    .then(ClientCommandManager.argument("new_width", IntegerArgumentType.integer(100))
                            .executes(CommandRegister::executeWidth))
                    .executes(CommandRegister::executeShowWidth));

            dispatcher.register(ClientCommandManager.literal("new")
                    .executes(CommandRegister::executeNew));

            dispatcher.register(ClientCommandManager.literal("swap") // TODO: make a ArgumentType with cap = size
                    .then(ClientCommandManager.argument("list_name", StringArgumentType.greedyString())
                            .suggests(new ListSuggestionProvider())
                            .executes(CommandRegister::executeSwap)));

            dispatcher.register(ClientCommandManager.literal("rename")
                    .then(ClientCommandManager.argument("new_name", StringArgumentType.greedyString())
                            .executes(CommandRegister::executeRename)));

            dispatcher.register(ClientCommandManager.literal("save")
                    .then(ClientCommandManager.argument("template_name", StringArgumentType.string())
                            .executes(CommandRegister::executeSave)));

            dispatcher.register(ClientCommandManager.literal("delete")
                    .executes(context -> executeDelete(context, SwaplistClient.CONFIG.curActiveList()))
                    .then(ClientCommandManager.argument("to_delete", StringArgumentType.greedyString())
                            .suggests(new ListSuggestionProvider())
                            .executes(context ->
                                    executeDelete(context, StringArgumentType.getString(context, "to_delete")))));
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

    /**
     * Takes a index from user input. Indices may be negative for wraparound indices. Error messages are sent with list
     * indices out of range, and Optional.empty() is returned in this case.
     *
     * @param context The command context in question.
     * @return The user's intended index, zero-indexed.
     */
    private static Optional<Integer> handleListIndex(CommandContext<FabricClientCommandSource> context) {
        int idx = IntegerArgumentType.getInteger(context, "index");
        int size = hudDisplay.itemCount();
        if (idx >= 1 && idx <= size) {
            return Optional.of(idx - 1);
        } else if (idx <= -1 && idx >= -size) {
            return Optional.of(size + idx);
        } else {
            context.getSource().sendError(Component.literal(
                    "Index %1$d is out of range. Must be between 1 and %2$d (the length of the list), or -%2$d and -1."
                            .formatted(idx, hudDisplay.itemCount())));
            return Optional.empty();
        }
    }

    private static int executeRemove(CommandContext<FabricClientCommandSource> context) {
        Optional<Integer> idx = handleListIndex(context);
        if (idx.isPresent()) {
            hudDisplay.removeLine(idx.get());
            return 1;
        } else {
            return -1;
        }
    }

    private static int executeToggle(CommandContext<FabricClientCommandSource> context) {
        Optional<Integer> idx = handleListIndex(context);
        if (idx.isPresent()) {
            hudDisplay.toggleLine(idx.get());
            return 1;
        } else {
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
        String key = ConfigUtils.uniqueListKey();
        var map = new HashMap<>(SwaplistClient.CONFIG.lists());
        map.put(key, new TodoList(key, new ArrayList<>()));
        SwaplistClient.CONFIG.lists(map);
        SwaplistClient.CONFIG.curActiveList(key);

        return 1;
    }

    private static int executeSwap(CommandContext<FabricClientCommandSource> context) {
        String name = StringArgumentType.getString(context, "list_name");

        if (SwaplistClient.CONFIG.lists().containsKey(name)) {
            SwaplistClient.CONFIG.curActiveList(name);
            return 1;
        } else {
            context.getSource().sendError(Component.literal("Provided list (%s) does not exist".formatted(name)));
            return -1;
        }
    }

    private static int executeRename(CommandContext<FabricClientCommandSource> context) {
        String newName = StringArgumentType.getString(context, "new_name");
        final Map<String, TodoList> lists = new HashMap<>(SwaplistClient.CONFIG.lists());
        TodoList list = ConfigUtils.getCurList();

        lists.remove(list.name);

        list.name = newName;
        lists.put(newName, list);

        SwaplistClient.CONFIG.lists(lists);
        CONFIG.curActiveList(newName);

        return 1;
    }

    private static int executeDelete(CommandContext<FabricClientCommandSource> context, String toDelete) {
        var lists = new HashMap<>(SwaplistClient.CONFIG.lists());

        if (lists.containsKey(toDelete)) {
            lists.remove(toDelete);
            SwaplistClient.CONFIG.lists(lists);
            SwaplistClient.CONFIG.curActiveList(ConfigUtils.getFirstList());
            return 1;
        } else {
            context.getSource().sendError(Component.literal("Provided list (%s) does not exist".formatted(toDelete)));
            return -1;
        }
    }


    private static int executeSave(CommandContext<FabricClientCommandSource> context) {
        String templateName = StringArgumentType.getString(context, "template_name");

        TodoList list = ConfigUtils.getCurList();
        var templates = new HashMap<>(SwaplistClient.CONFIG.templates());
        templates.put(templateName, list);
        SwaplistClient.CONFIG.templates(templates);

        return 1;
    }
}
