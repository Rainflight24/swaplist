package rainflight.swaplist.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.component.ButtonComponent;
import io.wispforest.owo.ui.component.LabelComponent;
import io.wispforest.owo.ui.component.UIComponents;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.NotNull;

public class MyFirstScreen extends BaseOwoScreen<FlowLayout> {

    public static UIComponent f() {
        LabelComponent label1 = UIComponents.label(Component.literal("Item 1"));


        ButtonComponent button1 = UIComponents.button(
                Component.literal("A buttonAAAAAAAAAAAAAAAAAAAAAA-\n"),
                button -> {
                    System.out.println("click");
                    button.setMessage(Component.literal("clicked"));
                    label1.text(Component.literal("Item 1'"));
                });

        var textBox = UIComponents.textBox(Sizing.fixed(200), "asodpifgrdshiofdshygiudsfugifdhgpfdug8p9dfugp98dfugpiufd8gufdgudpofgudfougodfugodfg");
        textBox.setBordered(false);

        return UIContainers.verticalFlow(Sizing.fixed(400), Sizing.content())
                .child(label1)
                .child(button1)
                .child(textBox)
                .child(UIComponents.textArea(Sizing.fixed(200), Sizing.fixed(200), "asodpifgrdshiofdshygiudsfugifdhgpfdug8p9dfugp98dfugpiufd8gufdgudpofgudfougodfugodfgasodpifgrdshiofdshygiudsfugifdhgpfdug8p9dfugp98dfugpiufd8gufdgudpofgudfougodfugodfgasodpifgrdshiofdshygiudsfugifdhgpfdug8p9dfugp98dfugpiufd8gufdgudpofgudfougodfugodfg"))
                .positioning(Positioning.absolute(50, 50))
                .padding(Insets.of(10))
                .surface(Surface.PANEL)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);
    }

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {


        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);


        rootComponent.child(f());

    }
}

// todo: check out Hud.java for building HUDs w/owo
