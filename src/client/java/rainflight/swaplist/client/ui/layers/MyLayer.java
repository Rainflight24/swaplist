package rainflight.swaplist.client.ui.layers;

import io.wispforest.owo.ui.core.ParentUIComponent;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.layers.Layer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import net.minecraft.client.gui.screens.Screen;

/**
 * Extended to gain access to its constructor for PreLayers.
 */
public class MyLayer<S extends Screen, R extends ParentUIComponent>
        extends io.wispforest.owo.ui.layers.Layer<S, R> {
    protected MyLayer(
            BiFunction<Sizing, Sizing, R> rootComponentMaker,
            Consumer<Layer<S, R>.Instance> instanceInitializer) {
        super(rootComponentMaker, instanceInitializer);
    }
}
