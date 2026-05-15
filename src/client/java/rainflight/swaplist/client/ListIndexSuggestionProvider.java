package rainflight.swaplist.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.concurrent.CompletableFuture;

public class ListIndexSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {

//        for (TodoList.ListItem listItem : ConfigUtils.getCurList().items) {
//            builder.suggest(listItem.text);
//        }

        for (int i = 0; i < ConfigUtils.getCurList().items.size(); ++i) {
            builder.suggest(i + 1);
            builder.suggest(-i - 1);
        }

        return builder.buildFuture();
    }
}
