package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import net.minecraft.resources.Identifier;

public class MyModelScreen extends BaseUIModelScreen<FlowLayout> {

    public MyModelScreen() {
        super(
                FlowLayout.class,
                DataSource.asset(Identifier.fromNamespaceAndPath("swaplist", "test_ui_model")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {}
}
