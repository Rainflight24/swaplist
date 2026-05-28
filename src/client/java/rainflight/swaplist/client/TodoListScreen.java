package rainflight.swaplist.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.MouseDrag;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import rainflight.swaplist.Swaplist;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

public class TodoListScreen extends BaseOwoScreen<FlowLayout> {


    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        int foreheadSize = HudDisplay.INSET_SIZE;

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        var listLayout = HudDisplay.makeLayout()
                // Clear the top padding to make space for the drag container.
                .padding(Insets.of(HudDisplay.INSET_SIZE).withTop(0));

        var draggable = UIContainers.draggable(Sizing.content(), Sizing.content(), listLayout);
        draggable.foreheadSize(foreheadSize)
                .positioning(Positioning.absolute(CONFIG.listHorizontalPos(),
                        CONFIG.listVerticalPos()))
                .mouseDrag().subscribe(new dragListener<>(draggable));

        rootComponent.child(draggable);

    }


    @Override
    protected void init() {
        SwaplistClient.hudDisplay.setVisible(false);
        super.init();
    }

    @Override
    public void onClose() {
        SwaplistClient.hudDisplay.setVisible(true);
        super.onClose();
    }

    private void save() {

    }

    private static class dragListener<T extends UIComponent> implements MouseDrag {
        private final DraggableContainer<T> draggableContainer;

        public dragListener(DraggableContainer<T> draggableContainer) {
            this.draggableContainer = draggableContainer;
        }

        @Override
        public boolean onMouseDrag(MouseButtonEvent _click, double _deltaX, double _deltaY) {
            CONFIG.listHorizontalPos(draggableContainer.x());
            CONFIG.listVerticalPos(draggableContainer.y());
            return true;
        }
    }
}
