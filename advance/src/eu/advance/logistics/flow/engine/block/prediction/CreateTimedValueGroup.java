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

package eu.advance.logistics.flow.engine.block.prediction;

import hu.akarnokd.utils.xml.XNElement;

import java.text.ParseException;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Creates a record from a date, group and value triplets.
 * @author karnokd, 2012.02.27.
 */
@Block(id = "CreateTimedValueGroup", 
category = "prediction", scheduler = "NOW", 
description = "Creates a record from a date, group and value triplets used by the K-means ARX learner.")
public class CreateTimedValueGroup extends AdvanceBlock {
	/** The timestamp. */
	@Input("advance:timestamp")
	protected static final String TIMESTAMP = "timestamp";
	/** The group. */
	@Input("advance:string")
	protected static final String GROUP = "group";
	/** The value. */
	@Input("advance:real")
	protected static final String VALUE = "value";
	/** The output record. */
	@Output("advance:timedvaluegroup")
	protected static final String OUT = "out";
	@Override
	protected void invoke() {
		
		try {
			XNElement result = new XNElement("timedvaluegroup");
			
			result.set("timestamp", getTimestamp(TIMESTAMP));
			result.set("group", getString(GROUP));
			result.set("value", getDouble(VALUE));
			
			dispatch(OUT, result);
		} catch (ParseException ex) {
			log(ex);
		}
	}

}
