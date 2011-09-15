package eu.advance.logistics.flow.editor.model;

import com.google.common.collect.Lists;
import java.util.List;

/**
 *
 * @author TTS
 */
public class FlowDescription extends CompositeBlock {

    private List<FlowDescriptionListener> listeners = Lists.newArrayList();
    private CompositeBlock activeBlock;

    public FlowDescription(String id) {
        super(id);
    }

    public CompositeBlock getActiveBlock() {
        return activeBlock;
    }

    public void setActiveBlock(CompositeBlock activeBlock) {
        CompositeBlock old = this.activeBlock;
        this.activeBlock = activeBlock;
        fire(FlowDescriptionChange.ACTIVE_COMPOSITE_BLOCK_CHANGED, old, activeBlock);
    }

    @Override
    public FlowDescription getFlowDiagram() {
        return this;
    }

    public void fire(FlowDescriptionChange event, Object... params) {
        for (FlowDescriptionListener l : listeners) {
            l.flowDescriptionChanged(event, params);
        }
    }

    public void addListener(FlowDescriptionListener l) {
        listeners.add(l);
    }

    public void removeListener(FlowDescriptionListener l) {
        listeners.remove(l);
    }
}
