package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.GridLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.OwoUIAdapter;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;
import rainflight.swaplist.Swaplist;

public class TestScreen extends BaseOwoScreen<GridLayout> {
    protected TestScreen(Component title) {
        super(title);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        Swaplist.LOGGER.debug("keyPressed {}", input);
        return super.keyPressed(input);
    }

    @Override
    protected @NotNull OwoUIAdapter<GridLayout> createAdapter() {
        return OwoUIAdapter.create(this, (sx, sy) -> UIContainers.grid(sx, sy, 3, 3));
    }

    @Override
    protected void build(GridLayout rootComponent) {
        for (int i = 0; i < 3; ++i)
            for (int j = 0; j < 3; ++j)
                rootComponent.child(UIComponents.checkbox(Component.literal("ASDF")), i, j);
    }
}
