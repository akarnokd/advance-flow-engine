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

import hu.akarnokd.utils.xml.XNElement;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Remove a range from the source collection and return two new collections, one
 * without the elements of the range, another with the elements of the range.
 * Signature: RemoveIndexRange(collection<t>, integer, integer) ->
 * (collection<t>, collection<t>)
 *
 * @author TTS
 */
@Block(id = "RemoveIndexRange", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Remove a range from the source collection and return two new collections, one without the elements of the range, another with the elements of the range", 
	parameters = { "T" }
)
public class RemoveIndexRange extends AdvanceBlock {

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
    @Input("advance:integer")
    protected static final String START = "start";
    /**
     * In.
     */
    @Input("advance:integer")
    protected static final String END = "end";
    /**
     * Out.
     */
    @Output("advance:collection<?T>")
    protected static final String OUT_COLLECTION = "out_collection";
    /**
     * Out.
     */
    @Output("advance:collection<?T>")
    protected static final String OUT_REMOVED = "out_removed";

    @Override
    protected void invoke() {
        final List<XNElement> list = resolver().getList(get(COLLECTION));
        final int start = getInt(START);
        final int end = getInt(END);

        if ((start < 0) || (end > list.size()) || (end < start)) {
            log(new IllegalArgumentException());
            return;
        }

        final List<XNElement> newList = new ArrayList<XNElement>();
        final List<XNElement> exctracted = new ArrayList<XNElement>();
        for (int i = 0, n = list.size(); i < n; i++) {
            final XNElement el = list.get(i);

            if ((i >= start) && (i <= end)) {
                exctracted.add(el);
            } else {
                newList.add(el);
            }
        }

        dispatch(OUT_COLLECTION, resolver().create(newList));
        dispatch(OUT_REMOVED, resolver().create(exctracted));
    }
}
