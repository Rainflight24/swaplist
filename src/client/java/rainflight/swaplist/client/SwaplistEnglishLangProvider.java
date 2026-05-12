package rainflight.swaplist.client;

import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricLanguageProvider;
import net.minecraft.core.HolderLookup;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class SwaplistEnglishLangProvider extends FabricLanguageProvider {
    protected SwaplistEnglishLangProvider(FabricDataOutput dataOutput, CompletableFuture<HolderLookup.Provider> registryLookup) {
        super(dataOutput, "en_us", registryLookup);
    }

    @Override
    public void generateTranslations(HolderLookup.@NonNull Provider holderLookup, TranslationBuilder translationBuilder) {
        translationBuilder.add("text.config.swaplist.option.listWidth", "List width");
        translationBuilder.add("text.config.swaplist.option.listWidth.tooltip", "in pixels, must be at least 100");
        translationBuilder.add("text.config.swaplist.option.listHeight", "List max height");
        translationBuilder.add("text.config.swaplist.option.listHeight.tooltip", "in pixels");
        translationBuilder.add("text.config.swaplist.option.listColor", "List color");


        translationBuilder.add("text.config.swaplist.option.listHorizontalPos", "List horizontal position");
        translationBuilder.add("text.config.swaplist.option.listHorizontalPos.tooltip", "in pixels from the left");
        translationBuilder.add("text.config.swaplist.option.listVerticalPos", "List vertical position");
        translationBuilder.add("text.config.swaplist.option.listVerticalPos.tooltip", "in pixels from the top");

    }
}
