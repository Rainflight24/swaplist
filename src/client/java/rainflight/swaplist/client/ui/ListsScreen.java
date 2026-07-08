package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class ListsScreen extends BaseOwoScreen<FlowLayout> {
    public ListsScreen(Component title) {
        super(title);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .child(
                        UIComponents.dropdown(Sizing.content())
                                .button(
                                        Component.literal("Option 1"),
                                        button -> {
                                            // Handle button click event
                                        })
                                .checkbox(Component.literal("Option 2"), false, ignored -> {})
                                .nested(
                                        Component.literal("Submenu"),
                                        Sizing.content(),
                                        submenu ->
                                                submenu.button(
                                                        Component.literal("Submenu Option"),
                                                        button -> {
                                                            // Handle submenu button click event
                                                        }))
                                .closeWhenNotHovered(false)
                                .padding(Insets.of(5))
                                .surface(Surface.TOOLTIP))
                .child(
                        UIContainers.verticalScroll(
                                        Sizing.content(),
                                        Sizing.fill(15),
                                        UIContainers.verticalFlow(
                                                        Sizing.content(), Sizing.content())
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 1")))
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 2")))
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 2.1")))
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 2.2")))
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 2.3")))
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 2.4")))
                                                .child(
                                                        UIComponents.label(
                                                                Component.literal("Item 3"))))
                                .scrollbarThiccness(4)
                                .scrollbar(ScrollContainer.Scrollbar.vanilla())
                                .padding(Insets.of(10))
                                .surface(Surface.PANEL))
                .child(
                        UIContainers.collapsible(
                                        Sizing.content(),
                                        Sizing.content(),
                                        Component.literal("Collapsible Section"),
                                        true)
                                .child(UIComponents.label(Component.literal("Content")))
                                .padding(Insets.of(10))
                                .surface(Surface.PANEL)
                                .horizontalAlignment(HorizontalAlignment.CENTER)
                                .verticalAlignment(VerticalAlignment.CENTER));
    }
}
