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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.util.List;
import java.util.logging.Logger;

/**
 * Removes the given value from the input collection and return a new collection
 * with the number of items removed. Signature: RemoveItem(collection<t>, t) ->
 * (collection<t>, integer)
 *
 * @author TTS
 */
@Block(id = "RemoveItem", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Removes the given value from the input collection and return a new collection with the number of items removed", 
	parameters = { "T" }
)
public class RemoveItem extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(RemoveValue.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String COLLECTION = "collection";
    /**
     * In.
     */
    @Input("?T")
    protected static final String VALUE = "value";
    /**
     * Out.
     */
    @Output("advance:collection<?T>")
    protected static final String OUT_COLLECTION = "out_collection";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT_COUNT = "out_count";

    @Override
    protected void invoke() {
        final List<XElement> list = resolver().getList(get(COLLECTION));
        final XElement value = get(VALUE);

        int count = 0;
        if (list.contains(value)) {

            int index = 0;
            for (XElement el : list) {
                if (el.equals(value)) {
                    list.remove(index);
                    count++;
                }
                
                index++;
            }
        }


        dispatch(OUT_COLLECTION, resolver().create(list));
        dispatch(OUT_COLLECTION, resolver().create(count));
    }
}
