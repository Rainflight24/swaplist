package rainflight.swaplist.client;

import static rainflight.swaplist.client.SwaplistClient.CONFIG;

import io.wispforest.owo.ui.base.BaseOwoScreen;
import io.wispforest.owo.ui.container.DraggableContainer;
import io.wispforest.owo.ui.container.FlowLayout;
import io.wispforest.owo.ui.container.UIContainers;
import io.wispforest.owo.ui.core.*;
import io.wispforest.owo.ui.event.MouseDrag;
import net.minecraft.client.input.MouseButtonEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

public class TodoListScreen extends BaseOwoScreen<FlowLayout> {

    // Captured before init() hides the HUD, so onClose() restores the prior state.
    private final boolean hudWasVisible = SwaplistClient.hudDisplay.isVisible();

    @Override
    protected @NotNull OwoUIAdapter<FlowLayout> createAdapter() {
        return OwoUIAdapter.create(this, UIContainers::verticalFlow);
    }

    @Override
    protected void build(FlowLayout rootComponent) {
        rootComponent
                .surface(Surface.vanillaPanorama(false).and(Surface.blur(5, 10)))
                .horizontalAlignment(HorizontalAlignment.CENTER)
                .verticalAlignment(VerticalAlignment.CENTER);

        rootComponent.child(makeDraggableList(true));
    }

    static @NonNull TodoListDraggable makeDraggableList(boolean checkboxFocus) {
        var listLayout = new TodoListComponent(TodoListComponent.Overflow.UNBOUNDED, checkboxFocus);
        var draggable = new TodoListDraggable(Sizing.content(), Sizing.content(), listLayout);
        draggable
                .foreheadSize(0)
                .positioning(
                        Positioning.absolute(CONFIG.listHorizontalPos(), CONFIG.listVerticalPos()))
                .mouseDrag()
                .subscribe(new DragListener<>(draggable));
        return draggable;
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

    private static class DragListener<T extends UIComponent> implements MouseDrag {
        private final DraggableContainer<T> draggableContainer;

        public DragListener(DraggableContainer<T> draggableContainer) {
            this.draggableContainer = draggableContainer;
        }

        @Override
        public boolean onMouseDrag(
                MouseButtonEvent unusedClick, double unusedDeltaX, double unusedDeltaY) {
            ConfigUtils.setListPosition(draggableContainer.x(), draggableContainer.y());
            return true;
        }
    }

    /**
     * A {@link DraggableContainer} whose drag handle is the list's title rather than a blank top
     * strip.
     */
    private static final class TodoListDraggable extends DraggableContainer<TodoListComponent> {
        private TodoListDraggable(
                Sizing horizontalSizing, Sizing verticalSizing, TodoListComponent child) {
            super(horizontalSizing, verticalSizing, child);
        }

        @Override
        public @Nullable UIComponent childAt(int x, int y) {
            if (this.isInBoundingBox(x, y) && this.child.isOverTitle(x, y)) {
                return this;
            }
            return super.childAt(x, y);
        }
    }
}
