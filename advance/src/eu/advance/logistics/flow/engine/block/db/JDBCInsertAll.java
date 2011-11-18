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
package eu.advance.logistics.flow.engine.block.db;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Inserts a collection of values into the datastore via the given SQL statement and fills in the parameters from the supplied map. Once the batch insert is complete, the operator returns a collection with the auto generated keys for each of the input maps.
 * Signature: JDBCInsertAll(datasource, string, collection<map<string, object>>) -> collection<map<string, object>>
 * @author szmarcell
 */
@Block(id = "___JDBCInsertAll", category = "db", scheduler = "IO", description = "Inserts a collection of values into the datastore via the given SQL statement and fills in the parameters from the supplied map. Once the batch insert is complete, the operator returns a collection with the auto generated keys for each of the input maps.")
public class JDBCInsertAll extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(JDBCInsertAll .class.getName());
    /** In. */
    @Input("advance:real")
    protected static final String IN = "in";
    /** Out. */
    @Output("advance:real")
    protected static final String OUT = "out";
    /** The running count. */
    private int count;
    /** The running sum. */
    private double value;
    // TODO implement 
    @Override
    protected void invoke() {
        double val = getDouble(IN);
        value = (value * count++ + val) / count;
        dispatch(OUT, resolver().create(value));
    }
    
}
