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

/**
 * <b>BlockBind</b>
 *
 * @author TTS
 */
public class BlockBind implements Comparable<BlockBind> {

    private CompositeBlock parent;
    public String id;
    public final BlockParameter source;
    public final BlockParameter destination;
    private String errorMessage;

    public BlockBind(CompositeBlock parent, String id, BlockParameter src, BlockParameter dst) {
        this.parent = parent;
        this.id = id;
        this.source = src;
        this.destination = dst;
    }

    public boolean equals(BlockParameter src, BlockParameter dst) {
        return src.equals(source) && dst.equals(destination);
    }

//    @Override
//    public boolean equals(Object obj) {
//        if (obj instanceof BlockBind) {
//            final BlockBind conn = ((BlockBind) obj);
//            return equals(conn.source, conn.destination);
//        }
//
//        return false;
//    }
//
//    @Override
//    public int hashCode() {
//        return Objects.hashCode(source, destination);
//    }

    @Override
    public int compareTo(BlockBind other) {
        return id.compareTo(other.id);
    }

    public void destroy() {
        parent.removeBind(this);
    }

    public CompositeBlock getParent() {
        return parent;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
        parent.getFlowDiagram().fire(FlowDescriptionChange.BIND_ERROR_MESSAGE, this);
    }
    
    public FlowDescription getFlowDescription() {
        return parent.getFlowDiagram();
    }
    
}
