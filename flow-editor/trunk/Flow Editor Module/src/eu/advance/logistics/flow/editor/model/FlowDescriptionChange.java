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

/**
 *
 * @author TTS
 */
public enum FlowDescriptionChange {

    BLOCK_RENAMED, BLOCK_MOVED,
    SIMPLE_BLOCK_ADDED, SIMPLE_BLOCK_REMOVED,
    COMPOSITE_BLOCK_ADDED, COMPOSITE_BLOCK_REMOVED,
    CONSTANT_BLOCK_ADDED, CONSTANT_BLOCK_REMOVED, CONSTANT_BLOCK_CHANGED,
    ACTIVE_COMPOSITE_BLOCK_CHANGED,
    BIND_CREATED, BIND_REMOVED, BIND_ERROR_MESSAGE,
    PARAMETER_CREATED, PARAMETER_REMOVED, PARAMETER_CHANGED,
    SAVING, CLOSED;
}
