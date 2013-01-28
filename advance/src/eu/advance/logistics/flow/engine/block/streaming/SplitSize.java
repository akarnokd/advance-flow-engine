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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Split the source collection into equally sized sub-collections. Signature:
 * SplitSize(collection<t>, integer) -> collection<collection<t>>
 *
 * @author TTS
 */
@Block(id = "SplitSize", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Split the source collection into equally sized sub-collections", 
	parameters = { "T" }
)
public class SplitSize extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(SplitEven.class.getName());
    /**
     * In.
     */
    @Input("advance:collection<?T>")
    protected static final String COLLECTION = "collection";
    /**
     * In.
     */
    @Input("advance:integer")
    protected static final String SIZE = "size";
    /**
     * Out.
     */
    @Output("advance:collection<collection<?T>>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final List<XElement> list = resolver().getList(get(COLLECTION));
        final int subSize = getInt(SIZE);

        final int size = list.size();
        if (subSize > size) {
            log(new IllegalArgumentException());
            return;
        }

        final ArrayList<XElement> result = new ArrayList<XElement>();

        int currEl = 0;
        ArrayList<XElement> currList = new ArrayList<XElement>();
        for (XElement el : list) {
            if (currEl < subSize) {
                currList.add(el);
                currEl++;
            } else {
                result.add(resolver().create(currList));
                currList = new ArrayList<XElement>();
                currEl = 0;
            }
        }

        dispatch(OUT, resolver().create(result));
    }
}
