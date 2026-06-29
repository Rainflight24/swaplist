package rainflight.swaplist.client.ui;

import io.wispforest.owo.ui.container.ScrollContainer;
import io.wispforest.owo.ui.core.Sizing;
import io.wispforest.owo.ui.core.UIComponent;
import io.wispforest.owo.ui.parsing.UIParsing;
import org.w3c.dom.Element;
import rainflight.swaplist.Swaplist;

public class MyScrollContainer<C extends UIComponent> extends ScrollContainer<C> {
    static {
        UIParsing.registerFactory(Swaplist.of("scroll"), ScrollContainer::parse);
    }

    // ------
    // Scroll
    // ------

    public static <C extends UIComponent> MyScrollContainer<C> verticalScroll(
            Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new MyScrollContainer<>(
                ScrollContainer.ScrollDirection.VERTICAL, horizontalSizing, verticalSizing, child);
    }

    public static <C extends UIComponent> MyScrollContainer<C> horizontalScroll(
            Sizing horizontalSizing, Sizing verticalSizing, C child) {
        return new MyScrollContainer<>(
                ScrollContainer.ScrollDirection.HORIZONTAL,
                horizontalSizing,
                verticalSizing,
                child);
    }

    public static MyScrollContainer<?> parse(Element element) {
        return element.getAttribute("direction").equals("vertical")
                ? verticalScroll(Sizing.content(), Sizing.content(), null)
                : horizontalScroll(Sizing.content(), Sizing.content(), null);
    }

    protected MyScrollContainer(
            ScrollDirection direction, Sizing horizontalSizing, Sizing verticalSizing, C child) {
        super(direction, horizontalSizing, verticalSizing, child);
    }
}
