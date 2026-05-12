package rainflight.swaplist.client;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;
import net.minecraft.resources.Identifier;
import rainflight.swaplist.Swaplist;

import java.util.ArrayList;
import java.util.List;

@Modmenu(modId = Swaplist.MOD_ID)
@Config(name = Swaplist.MOD_ID, wrapperName = "SwaplistConfig")
public class SwaplistConfigModel {

//    @Nest
//    public ThisIsNested nestedObject = new ThisIsNested();
//
//    public static class ThisIsNested {
//        public boolean aNestedValue = false;
//        public int anotherNestedValue = 42;
//    }

    @Hook
    @PredicateConstraint("listWidthPredicate")
    public int listWidth = 200;

    public static boolean listWidthPredicate(int width) {
        return width >= 100;
    }

    @Hook
    public int listHeight = 500;

    public int listHorizontalPos = 0, listVerticalPos = 100;

    @Hook
    public Color listColor = new Color(0, 128, 255);

    @ExcludeFromScreen
    public List<TodoList> todos;

    @ExcludeFromScreen
    public int activeTodo = 1;

    public List<TodoList.ListItem> items = new ArrayList<>();
}