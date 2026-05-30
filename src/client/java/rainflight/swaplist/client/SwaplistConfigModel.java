package rainflight.swaplist.client;

import static rainflight.swaplist.client.ConfigUtils.finalDefaultListSuffix;
import static rainflight.swaplist.client.ConfigUtils.firstDefaultList;

import blue.endless.jankson.Comment;
import io.wispforest.owo.config.annotation.*;
import io.wispforest.owo.ui.core.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import rainflight.swaplist.Swaplist;

@SuppressWarnings("unused") // model class fields are not directly used
@Modmenu(modId = Swaplist.MOD_ID)
@Config(name = Swaplist.MOD_ID, wrapperName = "SwaplistConfig", saveOnModification = false)
public class SwaplistConfigModel {

    @Hook
    @PredicateConstraint("listWidthPredicate")
    public int listWidth = 200;

    @Hook public int listHeight = 500;
    @Hook public int listHorizontalPos = 0;
    @Hook public int listVerticalPos = 100;
    @Hook public Color listColor = new Color(0, 64, 255);

    @Hook @ExcludeFromScreen
    public Map<String, TodoList> lists =
            new HashMap<>(
                    Map.of(firstDefaultList, new TodoList(firstDefaultList, new ArrayList<>())));

    @ExcludeFromScreen public Map<String, TodoList> templates = new HashMap<>();
    @Hook public String curActiveList = firstDefaultList;

    @Comment("The default name for new lists.")
    public String defaultListSuffix = finalDefaultListSuffix;

    public static boolean listWidthPredicate(int width) {
        return width >= 100;
    }
}
