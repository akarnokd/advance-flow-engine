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
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceData;

/**
 * Issues the given update SQL query into the datasource and fills in the query parameters from the supplied mapping from column name to column value. Once the update succeeded, a Boolean TRUE is emitted by the operator.
 * Signature: JDBCUpdate(datasource, string, map<string, object>) -> boolean
 * @author szmarcell
 */
@Block(id = "___JDBCUpdate", category = "db", scheduler = "IO", description = "Issues the given update SQL query into the datasource and fills in the query parameters from the supplied mapping from column name to column value. Once the update succeeded, a Boolean TRUE is emitted by the operator.")
public class JDBCUpdate extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(JDBCUpdate .class.getName());
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
        dispatch(OUT, AdvanceData.create(value));
    }
    
}
