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
package eu.advance.logistics.flow.editor.undo;

import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.model.AdvanceConstantBlock;
import java.awt.Point;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 *
 * @author TTS
 */
public class UndoRedoListener implements FlowDescriptionListener {

    private static final Logger LOGGER = Logger.getLogger("UndoRedo");
    private FlowDescription flowDescription;
    private UndoRedoProxy undoRedoProxy;
    private Map<CompositeBlock, UndoRedoManager> urm = new HashMap<CompositeBlock, UndoRedoManager>();
    private boolean restoring;

    public UndoRedoListener(FlowDescription flowDescription, UndoRedoProxy undoRedoProxy) {
        this.flowDescription = flowDescription;
        this.undoRedoProxy = undoRedoProxy;

        undoRedoProxy.setActive(getUndoRedoManager(flowDescription.getActiveBlock()));
    }

    public boolean isRestoring() {
        return restoring;
    }

    public void setRestoring(boolean restoring) {
        this.restoring = restoring;
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        if (restoring) {
            return;
        }

        UndoableEdit edit = null;
        switch (event) {
            case BLOCK_RENAMED:
                edit = new BlockRenamed((AbstractBlock) params[0], (String) params[1], (String) params[2]);
                break;
            case BLOCK_MOVED:
                edit = new BlockMoved((AbstractBlock) params[0], (Point) params[1], (Point) params[2]);
                break;
            case SIMPLE_BLOCK_ADDED:
                edit = new SimpleBlockAdded((CompositeBlock) params[0], (SimpleBlock) params[1]);
                break;
            case SIMPLE_BLOCK_REMOVED:
                edit = new SimpleBlockRemoved((CompositeBlock) params[0], (SimpleBlock) params[1]);
                break;
            case COMPOSITE_BLOCK_ADDED:
                edit = new CompositeBlockAdded((CompositeBlock) params[0], (CompositeBlock) params[1]);
                break;
            case COMPOSITE_BLOCK_REMOVED:
                edit = new CompositeBlockRemoved((CompositeBlock) params[0], (CompositeBlock) params[1]);
                break;
            case CONSTANT_BLOCK_ADDED:
                edit = new ConstantBlockAdded((CompositeBlock) params[0], (ConstantBlock) params[1]);
                break;
            case CONSTANT_BLOCK_REMOVED:
                edit = new ConstantBlockRemoved((CompositeBlock) params[0], (ConstantBlock) params[1]);
                break;
            case CONSTANT_BLOCK_CHANGED:
                edit = new ConstantBlockChanged((ConstantBlock) params[0], (AdvanceConstantBlock) params[1], (AdvanceConstantBlock) params[2]);
                break;
            case BIND_CREATED:
                edit = new BindCreated((CompositeBlock) params[0], (BlockBind) params[1]);
                break;
            case BIND_REMOVED:
                edit = new BindRemoved((CompositeBlock) params[0], (BlockBind) params[1]);
                break;
            case BIND_ERROR_MESSAGE:
                break;
            case PARAMETER_CREATED:
                edit = new ParameterCreated((AbstractBlock) params[0], (BlockParameter) params[1]);
                break;
            case PARAMETER_REMOVED:
                edit = new ParameterRemoved((AbstractBlock) params[0], (BlockParameter) params[1]);
                break;
            case PARAMETER_RENAMED:
                edit = new ParameterRenamed((BlockParameter) params[0], (String) params[1], (String) params[2]);
                break;
            case ACTIVE_COMPOSITE_BLOCK_CHANGED:
                undoRedoProxy.setActive(getUndoRedoManager((CompositeBlock) params[1]));
                break;
            case CLOSED:
                break;
            case SAVING:
                break;
        }
        if (edit != null) {
            CompositeBlock key = flowDescription.getActiveBlock();
            if (key == null) {
                LOGGER.warning("No active composite block.");
            } else {
                edit.setUndoRedoListener(this);
                getUndoRedoManager(key).addEditAndNotify(edit);
            }
        }
    }

    private UndoRedoManager getUndoRedoManager(CompositeBlock key) {
        UndoRedoManager manager = urm.get(key);
        if (manager == null) {
            manager = new UndoRedoManager();
            urm.put(key, manager);
        }
        return manager;
    }
}
