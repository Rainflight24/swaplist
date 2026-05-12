package rainflight.swaplist.client;

import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;
import rainflight.swaplist.Swaplist;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused") // model class fields are not directly used
@Modmenu(modId = Swaplist.MOD_ID)
@Config(name = Swaplist.MOD_ID, wrapperName = "SwaplistConfig")
public class SwaplistConfigModel {

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