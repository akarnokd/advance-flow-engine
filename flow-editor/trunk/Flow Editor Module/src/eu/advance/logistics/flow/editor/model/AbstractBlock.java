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
package eu.advance.logistics.flow.editor.model;

import java.awt.Point;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openide.util.NbBundle;

import com.google.common.base.Objects;
import com.google.common.collect.Maps;

import eu.advance.logistics.flow.editor.model.BlockParameter.Type;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;

/**
 * <b>AbstractBlock</b>
 *
 * @author TTS
 */
public abstract class AbstractBlock implements Comparable<AbstractBlock> {

    protected Map<String, BlockParameter> inputParameters = Maps.newHashMap();
    protected Map<String, BlockParameter> outputParameters = Maps.newHashMap();
    protected CompositeBlock parent;
    protected String id;
    private Point location;

    public FlowDescription getFlowDiagram() {
        return parent.getFlowDiagram();
    }

    public abstract void destroy();

    public List<BlockBind> getActiveBinds() {
        return parent.getActiveBinds(this);
    }

    public String getId() {
        return id;
    }

    public boolean setId(String id) {
        if (this.id.equals(id)) {
            return true;
        }
        if (id == null || id.isEmpty() || !parent.updateBlockId(this, id)) {
            return false;
        }
        String old = this.id;
        this.id = id;
        getFlowDiagram().fire(FlowDescriptionChange.BLOCK_RENAMED, this, old, id);
        return true;
    }

    public BlockParameter createInput(AdvanceBlockParameterDescription desc) {
        BlockParameter param = new BlockParameter(this, desc, BlockParameter.Type.INPUT);
        if (desc.id == null || inputParameters.containsKey(desc.id)) {
            desc.id = generateId(inputParameters.keySet(), "in");
        }
        addParameter(param);
        return param;
    }

    public Collection<BlockParameter> getInputs() {
        return inputParameters.values();
    }

    public BlockParameter createOutput(AdvanceBlockParameterDescription desc) {
        BlockParameter param = new BlockParameter(this, desc, BlockParameter.Type.OUTPUT);
        if (desc.id == null || outputParameters.containsKey(desc.id)) {
            desc.id = generateId(outputParameters.keySet(), "out");
        }
        addParameter(param);
        return param;
    }

    boolean containsId(BlockParameter param, String id) {
        if (param.type == Type.INPUT) {
            return inputParameters.containsKey(id);
        } else {
            return outputParameters.containsKey(id);
        }
    }

    void updateId(BlockParameter param, String id) {
        if (param.type == Type.INPUT) {
            updateId(inputParameters, param, param.getId(), id);
        } else {
            updateId(outputParameters, param, param.getId(), id);
        }
    }

    private static void updateId(Map<String, BlockParameter> data, BlockParameter param, String oldId, String newId) {
        if (data.containsKey(newId)) {
            throw new IllegalArgumentException(NbBundle.getBundle(AbstractBlock.class).getString("INVALID_ID") + newId);
        }
        if (data.remove(oldId) != param) {
            // something wrong
        }
        data.put(newId, param);
    }

    public Collection<BlockParameter> getOutputs() {
        return outputParameters.values();
    }

    public void addParameter(BlockParameter param) {
        if (param.type == BlockParameter.Type.INPUT) {
            inputParameters.put(param.getId(), param);
        } else {
            outputParameters.put(param.getId(), param);
        }
        getFlowDiagram().fire(FlowDescriptionChange.PARAMETER_CREATED, this, param);

    }

    public void removeParameter(BlockParameter param) {
        if (param.type == BlockParameter.Type.INPUT) {
            inputParameters.remove(param.getId());
        } else {
            outputParameters.remove(param.getId());
        }
        getFlowDiagram().fire(FlowDescriptionChange.PARAMETER_REMOVED, this, param);
    }

    public BlockParameter getInputOrOutputParameter(String paramId) {
        BlockParameter param = inputParameters.get(paramId);
        return (param != null) ? param : outputParameters.get(paramId);
    }

    public CompositeBlock getParent() {
        return parent;
    }

    public void setParent(CompositeBlock parent) {
        this.parent = parent;
    }

    @Override
    public int compareTo(AbstractBlock other) {
        return id.compareTo(other.id);
    }

    public void setLocation(Point point) {
        if (!Objects.equal(location, point)) {
            Point old = this.location;
            location = point;
            getFlowDiagram().fire(FlowDescriptionChange.BLOCK_MOVED, this, old, location);
        }
    }

    public Point getLocation() {
        return location;
    }

    protected static String generateId(Set<String> keys, String base) {
        String id;
        for (int i = 1; i < Integer.MAX_VALUE; i++) {
            id = base + i;
            if (!keys.contains(id)) {
                return id;
            }
        }
        return generateId(keys, base + "#");
    }
}
