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
package eu.advance.logistics.flow.engine.block.projecting;

import hu.akarnokd.utils.xml.XNElement;

import java.util.List;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Extracts an element from the collection given by the index. Signature:
 * GetItem(collection<t>, integer) -> integer
 *
 * @author TTS
 */
@Block(id = "GetItem", 
	category = "projection", 
	scheduler = "IO", 
	description = "Extracts an element from the collection given by the index", 
	parameters = { "T" }
)
public class GetItem extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(GetItem.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String COLLECTION = "collection";
    /**
     * In.
     */
    @Input("advance:integer")
    protected static final String INDEX = "index";
    /**
     * Out.
     */
    @Output("advance:real")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final List<XNElement> xcollection = resolver().getList(get(COLLECTION));
        final int key = getInt(INDEX);

        if ((key > 0) && (key < xcollection.size())) {
            dispatch(OUT, resolver().create(xcollection.get(key)));
        } else {
            dispatch(OUT, resolver().createObject());
        }
    }
}