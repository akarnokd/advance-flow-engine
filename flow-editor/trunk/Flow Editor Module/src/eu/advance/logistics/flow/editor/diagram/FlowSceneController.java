/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
 *
 * This file is part of Advance.
 *
 * Advance is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * Advance is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Advance.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 */
package eu.advance.logistics.flow.editor.diagram;

import com.google.common.base.Objects;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import java.awt.EventQueue;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author TTS
 */
class FlowSceneController implements FlowDescriptionListener {

    private final FlowScene scene;
    private final WidgetBuilder widgetBuilder;

    FlowSceneController(FlowScene scene) {
        this.scene = scene;
        this.widgetBuilder = new WidgetBuilder(scene);
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        switch (event) {
            case BLOCK_RENAMED:
                rename((AbstractBlock) params[0]);
                break;
            case BLOCK_MOVED:
                updateLocation((AbstractBlock) params[0]);
                break;
            case SIMPLE_BLOCK_ADDED:
                blockAdded((CompositeBlock) params[0], (SimpleBlock) params[1]);
                break;
            case SIMPLE_BLOCK_REMOVED:
                blockRemoved((AbstractBlock) params[1]);
                break;
            case COMPOSITE_BLOCK_ADDED:
                blockAdded((CompositeBlock) params[0], (CompositeBlock) params[1]);
                break;
            case COMPOSITE_BLOCK_REMOVED:
                blockRemoved((AbstractBlock) params[1]);
                break;
            case CONSTANT_BLOCK_ADDED:
                blockAdded((CompositeBlock) params[0], (ConstantBlock) params[1]);
                break;
            case CONSTANT_BLOCK_REMOVED:
                blockRemoved((AbstractBlock) params[1]);
                break;
            case CONSTANT_BLOCK_CHANGED:
                constantBlockChanged((ConstantBlock) params[0], null);
                break;
            case ACTIVE_COMPOSITE_BLOCK_CHANGED:
                saveLocations((CompositeBlock) params[0]);
                scene.clean();
                init((CompositeBlock) params[1]);
                scene.validate();
                break;
            case BIND_CREATED:
                bindCreated((AbstractBlock) params[0], (BlockBind) params[1]);
                break;
            case BIND_REMOVED:
                bindRemoved((BlockBind) params[1]);
                break;
            case BIND_ERROR_MESSAGE:
                bindErrorMessage((BlockBind) params[0]);
                break;
            case PARAMETER_CREATED:
                scene.addPin((AbstractBlock) params[0], (BlockParameter) params[1]);
                scene.validate();
                break;
            case PARAMETER_REMOVED:
                scene.removePin((BlockParameter) params[1]);
                scene.validate();
                break;
            case PARAMETER_RENAMED:
            case PARAMETER_CHANGED:
                parameterRenamedOrChanged((BlockParameter) params[0]);
                break;
            case COMPILATION_RESULT:
                compilationResultChanged((AdvanceCompilationResult) params[0]);
            case CLOSED:
                break;
            case SAVING:
                saveLocations(((FlowDescription) params[0]).getActiveBlock());
                break;
        }
        if (event != FlowDescriptionChange.CLOSED) {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    scene.validate();
                }
            });
        }
    }

    private void updateLocation(AbstractBlock block) {
        if (scene.isAdjusting()) {
            return;
        }
        Widget widget = scene.findWidget(block);
        if (widget != null && !Objects.equal(block.getLocation(), widget.getPreferredLocation())) {
            widget.setPreferredLocation(block.getLocation());
            scene.validate();
        } else {
            // something wrong!
        }
    }

    private void rename(AbstractBlock block) {
        BlockWidget widget = (BlockWidget) scene.findWidget(block);
        if (widget != null) {
            widget.setNodeName(block.getId());
            scene.validate();
        } else {
            // something wrong!
        }
    }

    private void blockAdded(CompositeBlock parent, AbstractBlock block) {
        Widget widget = scene.addNode(block);
        if (widget instanceof BlockWidget) {
            widgetBuilder.configure((BlockWidget) widget, block);
        } else if (widget instanceof ConstantBlockWidget) {
            widgetBuilder.configure((ConstantBlockWidget) widget, (ConstantBlock) block);
            constantBlockChanged((ConstantBlock) block, (ConstantBlockWidget) widget);
        }
        scene.validate();
    }

    private void constantBlockChanged(ConstantBlock block, ConstantBlockWidget widget) {
        if (widget == null) {
            widget = (ConstantBlockWidget) scene.findWidget(block);
        }
        widget.update();
        scene.validate();
    }

    private void blockRemoved(AbstractBlock block) {
        for (BlockParameter param : block.getInputs()) {
            scene.removePin(param);
        }
        for (BlockParameter param : block.getOutputs()) {
            scene.removePin(param);
        }
        scene.removeNode(block);
        scene.validate();
    }

    private void bindCreated(AbstractBlock owner, BlockBind bind) {
        scene.addEdge(bind);
        scene.setEdgeSource(bind, bind.source);
        scene.setEdgeTarget(bind, bind.destination);
        bindErrorMessage(bind); // also update bind status
        scene.validate();
    }

    private void bindRemoved(BlockBind bind) {
        scene.setEdgeSource(bind, null);
        scene.setEdgeTarget(bind, null);
        scene.removeEdge(bind);
        scene.validate();
    }

    private void bindErrorMessage(BlockBind bind) {
        BlockConnectionWidget w = (BlockConnectionWidget) scene.findWidget(bind);
        w.setError(bind.getErrorMessage() != null);
        scene.validate();
        scene.repaint();
    }

    private void parameterRenamedOrChanged(BlockParameter param) {
        Widget w = scene.findWidget(param);
        if (w instanceof PinWidget) {
            WidgetBuilder.configure(((PinWidget) w), param);
            scene.validate();
        }
    }

    void init(CompositeBlock root) {
        scene.addNode(root);
        // inputs
        for (BlockParameter param : WidgetBuilder.sort(root.getInputs())) {
            scene.addPin(root, param);
        }
        // outputs
        for (BlockParameter param : WidgetBuilder.sort(root.getOutputs())) {
            scene.addPin(root, param);
        }
        scene.validate();
        // blocks, composites
//        boolean needsLayout = false;
        for (AbstractBlock block : root.getChildren()) {
            blockAdded(root, block);
            if (block.getLocation() != null) {
                updateLocation(block);
//            } else {
//                needsLayout = true;
            }
        }
        // binds
        for (BlockBind bind : root.getBinds()) {
            bindCreated(root, bind);
        }

//        if (needsLayout) {
//            scene.layoutScene();
//        }
    }

    private void saveLocations(CompositeBlock root) {
        for (AbstractBlock child : root.getChildren()) {
            Widget w = scene.findWidget(child);
            if (w != null) {
                child.setLocation(w.getLocation());
            }
        }
    }

    private void compilationResultChanged(AdvanceCompilationResult compilationResult) {
        if (compilationResult != null) {
            for (BlockBind bind : scene.getEdges()) {
                Widget w = scene.findWidget(bind);
                if (w instanceof BlockConnectionWidget) {
                    AdvanceType at = compilationResult.wireTypes.get(bind.id);
                    ((BlockConnectionWidget) w).setError(at == null);
                }
            }
        } else {
            for (BlockBind bind : scene.getEdges()) {
                Widget w = scene.findWidget(bind);
                if (w instanceof BlockConnectionWidget) {
                    ((BlockConnectionWidget) w).setError(false);
                }
            }
        }
        scene.validate();
    }
}
