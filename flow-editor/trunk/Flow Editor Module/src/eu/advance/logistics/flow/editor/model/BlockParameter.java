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

import com.google.common.base.Objects;
import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;

/**
 * <b>BlockParameter</b>
 *
 * @author TTS
 */
public class BlockParameter implements Comparable<BlockParameter> {

    public final AbstractBlock owner;
    public final Type type;
    public AdvanceBlockParameterDescription description;

    public BlockParameter(AbstractBlock parent, AdvanceBlockParameterDescription desc, Type type) {
        this.owner = parent;
        this.description = desc;
        this.type = type;
    }

    public void setId(String id) {
        owner.updateId(this, id);
        description.id = id;
        owner.getFlowDiagram().fire(FlowDescriptionChange.PARAMETER_CHANGED, this);
    }

    public String getId() {
        return description.id;
    }

    public String getPath() {
        return owner.id + "." + description.id;
    }

    public String getDisplayName() {
        if (description.displayName != null) {
            return description.displayName;
        } else {
            return description.id;
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BlockParameter other = (BlockParameter) obj;
        return Objects.equal(description.id, other.description.id)
                && Objects.equal(owner, other.owner);
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public int compareTo(BlockParameter o) {
        return description.id.compareTo(o.description.id);
    }

    public void destroy() {
        owner.removeParameter(this);
    }

    public enum Type {

        INPUT, OUTPUT
    }
}
