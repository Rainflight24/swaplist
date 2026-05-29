package rainflight.swaplist.client;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Suggests based on the contents of the provided supplier.
 */
public class CollectionSuggestionProvider<T> implements SuggestionProvider<FabricClientCommandSource> {
    private final Supplier<Collection<T>> data;

    public CollectionSuggestionProvider(Supplier<Collection<T>> data) {
        this.data = data;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<FabricClientCommandSource> context, SuggestionsBuilder builder) {

        for (T x : data.get()) {
            builder.suggest(x.toString());
        }

        return builder.buildFuture();
    }
}