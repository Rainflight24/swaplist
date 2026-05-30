package rainflight.swaplist.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

public class ListIndexSuggestionProvider implements SuggestionProvider<FabricClientCommandSource> {
    @Override
    public CompletableFuture<Suggestions> getSuggestions(
            CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {

        int size = ConfigUtils.getCurList().items.size();
        for (int i = 0; i < size; ++i) {
            builder.suggest(i + 1);
            builder.suggest(-i - 1);
        }

        return builder.buildFuture();
    }
}
