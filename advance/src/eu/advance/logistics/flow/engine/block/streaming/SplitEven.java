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
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Split the collection into the given number of sub-collections evenly.
 * Signature: SplitEven(collection<t>, integer) -> collection<collection<t>>
 *
 * @author TTS
 */
@Block(id = "SplitEven", 
	category = "streaming", 
	scheduler = "IO", 
	description = "Split the collection into the given number of sub-collections evenly.", 
	parameters = { "T" }
)
public class SplitEven extends AdvanceBlock {

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
    protected static final String NUMBER = "number";
    /**
     * Out.
     */
    @Output("advance:collection<collection<?T>>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final List<XNElement> list = resolver().getList(get(COLLECTION));
        final int number = getInt(NUMBER);

        final int size = list.size();
        if (number > size) {
            log(new IllegalArgumentException());
            return;
        }

        final int elPerColl = size / number;
        final ArrayList<XNElement> result = new ArrayList<XNElement>();

        int currEl = 0;
        ArrayList<XNElement> currList = new ArrayList<XNElement>();
        for (XNElement el : list) {
            if (currEl < elPerColl) {
                currList.add(el);
                currEl++;
            } else {
                result.add(resolver().create(currList));
                currList = new ArrayList<XNElement>();
                currEl = 0;
            }
        }

        dispatch(OUT, resolver().create(result));
    }
}
