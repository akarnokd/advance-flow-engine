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

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;

/**
 * <b>BlockParameter</b>
 *
 * @author TTS
 */
public class BlockParameter implements Comparable<BlockParameter> {

    public final AbstractBlock owner;
    public final Type type;
    private AdvanceBlockParameterDescription description;
    private String id;

    public BlockParameter(AbstractBlock parent, AdvanceBlockParameterDescription desc, Type type) {
        this.owner = parent;
        this.description = desc;
        this.type = type;
        this.id = desc.id;
    }

    public AdvanceBlockParameterDescription getDescription() {
        return description;
    }

    public void setDescription(AdvanceBlockParameterDescription description) {
        AdvanceBlockParameterDescription old = this.description;
        this.description = description;
        owner.getFlowDiagram().fire(FlowDescriptionChange.PARAMETER_CHANGED, this, old, description);
    }

    public boolean canChangeId(String id) {
        BlockParameter p = owner.getInputOrOutputParameter(id);
        return p == this || p == null;
    }

    public void setId(String id) {
        owner.updateId(this, id);
        String old = this.id;
        description.id = id;
        this.id = id;
        owner.getFlowDiagram().fire(FlowDescriptionChange.PARAMETER_RENAMED, this, old, id);
    }

    public String getId() {
        return id;
    }

    public String getPath() {
        return owner.id + "." + id;
    }

    public String getDisplayName() {
        if (description.displayName != null) {
            return description.displayName;
        } else {
            return description.id;
        }
    }

    public FlowDescription getFlowDescription() {
        return owner.getFlowDiagram();
    }

    @Override
    public int compareTo(BlockParameter o) {
        return id.compareTo(o.id);
    }

    public void destroy() {
        owner.removeParameter(this);
    }

    public enum Type {

        INPUT, OUTPUT
    }
}
