package rainflight.swaplist.client;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.MouseDrag;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

public class TodoListScreen extends BaseOwoScreen<FlowLayout> {

    // Captured before init() hides the HUD, so onClose() restores the prior state.
    private final boolean hudWasVisible = SwaplistClient.hudDisplay.isVisible();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        int foreheadSize = TodoListComponent.INSET_SIZE;

        rootComponent
                .surface(Surface.VANILLA_TRANSLUCENT)
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        var listLayout = new TodoListComponent(TodoListComponent.Overflow.UNBOUNDED)
                // Clear the top padding to make space for the drag container.
                .padding(Insets.of(TodoListComponent.INSET_SIZE).withTop(0));

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
        // Commit edits accumulated while the screen was open.
        ConfigUtils.save();
        SwaplistClient.hudDisplay.setVisible(hudWasVisible);
        super.onClose();
    }

    private static class dragListener<T extends UIComponent> implements MouseDrag {
        private final DraggableContainer<T> draggableContainer;

        public dragListener(DraggableContainer<T> draggableContainer) {
            this.draggableContainer = draggableContainer;
        }

        @Override
        public boolean onMouseDrag(MouseButtonEvent _click, double _deltaX, double _deltaY) {
            ConfigUtils.setListPosition(draggableContainer.x(), draggableContainer.y());
            return true;
        }
    }
}
