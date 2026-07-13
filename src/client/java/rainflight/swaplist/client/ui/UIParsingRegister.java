package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.parsing.UIParsing;
import net.minecraft.network.chat.Component;
import rainflight.swaplist.Swaplist;

public class UIParsingRegister {
    public static void init() {
        UIParsing.registerFactory(
                Swaplist.of("selectable-label"),
                element -> new SelectableLabelComponent(Component.empty()));
        UIParsing.registerFactory(Swaplist.of("scroll"), ScrollContainer::parse);

        UIParsing.registerFactory(
                Swaplist.of("todolist"),
                element -> new TodoListComponent(TodoListComponent.Overflow.UNBOUNDED, false));
    }
}
