/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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
package eu.advance.logistics.flow.engine.block.streaming;

import java.util.LinkedList;
import java.util.TreeMap;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Concatenate two collections.
 * Signature: ConcatCollection(collection<t>, collection<t>) -> collection<t>
 * @author szmarcell
 */
@Block(id = "ConcatCollection", category = "streaming", scheduler = "IO", parameters = { "T" }, description = "Concatenate two collections.")
public class ConcatCollection extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(ConcatCollection .class.getName());
    /** In. */
    @Input(value = "advance:collection<?T>", variable = true)
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
        TreeMap<String, XElement> collections = new TreeMap<String, XElement>();
        
        for (AdvancePort port : getReactivePorts()) {
            String name = port.name();
            collections.put(name, get(name));
        }
        LinkedList<XElement> result = Lists.newLinkedList();
        for (XElement collection : collections.values()) {
            result.addAll(AdvanceData.getList(collection));
        }
        dispatch(OUT, AdvanceData.create(result));
    }
    
}
