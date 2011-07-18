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

import eu.advance.logistics.flow.model.AdvanceBlockParameterDescription;

/**
 * <b>BlockParameter</b>
 *
 * @author TTS
 */
public class BlockParameter implements Comparable<BlockParameter> {

    public final AbstractBlock owner;
    public final Type type;
    public final AdvanceBlockParameterDescription description;

    public BlockParameter(AbstractBlock parent, AdvanceBlockParameterDescription desc, Type type) {
        this.owner = parent;
        this.description = desc;
        this.type = type;
    }

    public String getId() {
        return owner.id + "." + description.id;
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
        return getId().equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public int compareTo(BlockParameter o) {
        return description.id.compareTo(o.description.id);
    }

    public enum Type {

        INPUT, OUTPUT
    }
}
