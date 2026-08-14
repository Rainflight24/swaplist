package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.container.FlowLayout;
import java.util.Map;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import rainflight.swaplist.Swaplist;
import rainflight.swaplist.client.SwaplistClient;

public class MyModelScreen extends BaseUIModelScreen<FlowLayout> {

    public MyModelScreen() {
        super(
                FlowLayout.class,
                DataSource.asset(
                        Identifier.fromNamespaceAndPath(Swaplist.MOD_ID, "test_ui_model")));
    }

    @Override
    protected void build(FlowLayout rootComponent) {}

    @Override
    protected void init() {
        super.init();

        if (this.uiAdapter == null) return;

        for (var list : SwaplistClient.CONFIG.lists()) {
            this.uiAdapter
                    .rootComponent
                    .childById(FlowLayout.class, "catalogue-container")
                    .child(
                            this.model
                                    .expandTemplate(
                                            SelectableLabelComponent.class,
                                            "catalogue-entry@swaplist:test_ui_model",
                                            Map.of())
                                    .configure(
                                            component -> {
                                                var c = (SelectableLabelComponent) component;
                                                c.text(Component.literal(list.name));
                                            }));
        }
    }
}
