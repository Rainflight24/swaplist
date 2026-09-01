package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.base.BaseUIModelScreen;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.Sizing;
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
                            UIContainers.horizontalScroll(
                                            Sizing.fill(100),
                                            Sizing.content(),
                                            this.model
                                                    .expandTemplate(
                                                            LabelComponent.class,
                                                            "catalogue-entry@swaplist:test_ui_model",
                                                            Map.of())
                                                    .<LabelComponent>configure(
                                                            label ->
                                                                    label.text(
                                                                            Component.literal(
                                                                                    list.name))))
                                    .<ScrollContainer<LabelComponent>>configure(scroll -> {}));
        }
    }
}
