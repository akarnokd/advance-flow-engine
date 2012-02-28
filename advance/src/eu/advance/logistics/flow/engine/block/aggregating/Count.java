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
package eu.advance.logistics.flow.engine.block.aggregating;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Counts the elements in the given collection. Signature: Count(collection<T>)
 * -> integer
 *
 * @author TTS
 */
@Block(id = "Count", category = "aggregation", scheduler = "IO", description = "Counts the elements in the given collection.")
public class Count extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(Count.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        int count = 0;
        
        final XElement xcollection = get(IN);
        final Iterator<XElement> it = xcollection.children().iterator();
        while (it.hasNext()) {
            count++;
            it.next();
        }
        
        dispatch(OUT, resolver().create(count));
    }
}
