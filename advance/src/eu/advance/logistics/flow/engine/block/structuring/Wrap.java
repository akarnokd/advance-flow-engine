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
package eu.advance.logistics.flow.engine.block.structuring;

import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.util.TreeMap;

/**
 * Create a collection from the inputs. Emits the collection when all inputs change.
 * Signature: Wrap(T*) -> Collection<T>
 * @author szmarcell
 */
@Block(id = "Wrap", category = "data-transformations", scheduler = "IO", parameters = {"T"}, description = "Create a collection from the inputs. Emits the collection when all inputs change.")
public class Wrap extends AdvanceBlock {

    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(Wrap.class.getName());
    /** In. */
    @Input(value = "?T", variable = true)
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";
    @Override
    protected void invoke() {
        TreeMap<String, XElement> treeMap = new TreeMap<String, XElement>();
        for (AdvancePort port : getReactivePorts()) {
            String name = port.name();
            treeMap.put(name, get(name));
        }
        dispatch(OUT, AdvanceData.create(treeMap.values()));
    }
}
