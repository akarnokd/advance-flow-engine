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
package eu.advance.logistics.flow.editor;

import eu.advance.logistics.flow.editor.diagram.BlockWidget;
import eu.advance.logistics.flow.editor.diagram.FlowDiagramScene;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.editor.model.BlockParameter;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.BlockBind;
import eu.advance.logistics.flow.model.AdvanceBlockBind;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.model.AdvanceBlockReference;
import eu.advance.logistics.flow.model.AdvanceCompositeBlock;
import eu.advance.logistics.flow.model.AdvanceCompositeBlockParameterDescription;
import java.awt.Point;
import java.beans.PropertyVetoException;
import java.util.Map;
import java.util.Set;
import javax.swing.JComponent;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author TTS
 */
public class FlowDiagramController {

    private FlowDescriptionDataObject dataObject;
    private CompositeBlock flowDiagram = new CompositeBlock("flow-diagram"); // Model
    private FlowDiagramScene scene = new FlowDiagramScene(this); // View
    private JComponent navigationPanel;

    public FlowDiagramController(FlowDescriptionDataObject dataObject) {
        this.dataObject = dataObject;
        FlowDiagramFactory.configure(scene);
    }

    public FlowDiagramScene getScene() {
        return scene;
    }

    public JComponent getNavigationPanel() {
        if (navigationPanel == null) {
            navigationPanel = scene.createSatelliteView();
        }
        return navigationPanel;
    }

    public BlockParameter createInput(AdvanceBlockParameterDescription desc) {
        BlockParameter param = new BlockParameter(flowDiagram, desc, BlockParameter.Type.INPUT);
        // Model
        flowDiagram.inputParameters.put(desc.id, param);
        // View
        // TODO
        return param;
    }

    public BlockParameter createOutput(AdvanceBlockParameterDescription desc) {
        BlockParameter param = new BlockParameter(flowDiagram, desc, BlockParameter.Type.OUTPUT);
        // Model
        flowDiagram.outputParameters.put(desc.id, param);
        // View
        // TODO
        return param;
    }

    public SimpleBlock createBlock(AdvanceBlockDescription desc) {
        String id = generateId(flowDiagram.blocks.keySet(), desc.id);
        SimpleBlock block = new SimpleBlock(id, desc);
        // Model
        flowDiagram.blocks.put(id, block);
        // View        
        scene.addBlock(block);
        setModified(true);
        return block;
    }

    private String generateId(Set<String> keys, String base) {
        String id;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            id = base + i;
            if (!keys.contains(id)) {
                return id;
            }
        }
        return generateId(keys, base + "#");
    }

    public String generateBindId(String id) {
        Set<String> ids = flowDiagram.binds.keySet();
        return ids.contains(id) ? generateId(ids, id) : id;
    }

    public BlockBind createBind(String id,
            BlockParameter src,
            BlockParameter dst) {

        // check if the bind already exists
        if (find(src, dst) != null) {
            return null;
        }

        if (id == null) {
            // if id was not specified then create a new id
            id = BlockBind.createId(src, dst);
        }

        // check that the id is unique
        id = generateBindId(id);

        BlockBind c = new BlockBind(src, dst);

        // add to Model
        flowDiagram.binds.put(id, c);

        // add to View
        scene.addConnection(c);

        setModified(true);
        return c;
    }

    public BlockBind find(BlockParameter src, BlockParameter dst) {
        for (BlockBind bind : flowDiagram.binds.values()) {
            if (bind.equals(src, dst)) {
                return bind;
            }
        }
        return null;
    }

    public void setLocation(SimpleBlock blockNode, Point point) {
        Widget widget = scene.findWidget(blockNode);
        if (widget != null) {
            widget.setPreferredLocation(point);
        } else {
            // something wrong!
        }
        setModified(true);
    }

    void set(AdvanceCompositeBlock compositeBlock) {
        BlockRegistry r = BlockRegistry.getInstance();
        for (Map.Entry<String, AdvanceBlockReference> e : compositeBlock.blocks.entrySet()) {
            AdvanceBlockReference ref = e.getValue();
            assert e.getKey().equals(ref.id);
            AdvanceBlockDescription desc = r.findType(ref.type);
            if (desc != null) {
                SimpleBlock block = new SimpleBlock(ref.id, desc);
                // add to Model
                flowDiagram.blocks.put(ref.id, block);
                // add to View
                scene.addBlock(block);
            }
        }

        for (Map.Entry<String, AdvanceCompositeBlockParameterDescription> e : compositeBlock.inputs.entrySet()) {
            assert e.getKey().equals(e.getValue().id);
            createInput(e.getValue());
        }

        for (Map.Entry<String, AdvanceCompositeBlockParameterDescription> e : compositeBlock.outputs.entrySet()) {
            assert e.getKey().equals(e.getValue().id);
            createOutput(e.getValue());
        }

        for (AdvanceBlockBind bind : compositeBlock.bindings) {
            BlockParameter src = find(bind.sourceBlock, bind.sourceParameter);
            BlockParameter dst = find(bind.destinationBlock, bind.destinationParameter);
            if (src != null && dst != null) {
                BlockBind c = new BlockBind(src, dst);
                // add to Model
                flowDiagram.binds.put(bind.id, c);
                // add to View
                scene.addConnection(c);
            }
        }
    }

    AdvanceCompositeBlock get() {
        AdvanceCompositeBlock aCompositeBlock = new AdvanceCompositeBlock();

        for (Map.Entry<String, SimpleBlock> e : flowDiagram.blocks.entrySet()) {
            AdvanceBlockReference aBlockRef = new AdvanceBlockReference();
            aBlockRef.id = e.getKey();
            aBlockRef.parent = aCompositeBlock;
            aBlockRef.type = e.getValue().description.id;
            aCompositeBlock.blocks.put(aBlockRef.id, aBlockRef);
        }

        for (Map.Entry<String, BlockBind> e : flowDiagram.binds.entrySet()) {
            BlockBind bind = e.getValue();
            AdvanceBlockBind aBlockBind = new AdvanceBlockBind();
            aBlockBind.id = e.getKey();
            aBlockBind.sourceBlock = bind.source.owner.id;
            aBlockBind.sourceParameter = bind.source.getId();
            aBlockBind.destinationBlock = bind.destination.owner.id;
            aBlockBind.destinationParameter = bind.destination.getId();
            aCompositeBlock.bindings.add(aBlockBind);
        }
        return aCompositeBlock;
    }

    private BlockParameter find(String blockId, String paramId) {
        SimpleBlock block = flowDiagram.blocks.get(blockId);
        return (block != null) ? block.getInputOrOutputParameter(paramId) : null;
    }

    public boolean setBlockId(SimpleBlock target, String id) {
        if (flowDiagram.blocks.containsKey(id)) {
            return false;
        }
        // update Model
        SimpleBlock block = flowDiagram.blocks.remove(target.id);
        if (block != target) {
            // something wrong!
        }
        flowDiagram.blocks.put(id, target);

        // update View
        BlockWidget widget = (BlockWidget) scene.findWidget(target);
        if (widget != null) {
            widget.setNodeName(id);
        } else {
            // something wrong!
        }
        setModified(true);
        return true;
    }

    private void setModified(boolean value) {
        dataObject.setModified(value);
    }

    public boolean isModified() {
        return dataObject.isModified();
    }

    public String getDisplayName() {
        return dataObject.getName();
    }

    public void close() throws PropertyVetoException {
        dataObject.setValid(false);
        dataObject = null;
        flowDiagram = null;
        scene = null;
        navigationPanel = null;
    }
}
