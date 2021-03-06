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



import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.utils.pool.Pool;
import hu.akarnokd.utils.xml.XNElement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;

/**
 * Issues an SQL query into the datasource once a trigger object arrives and
 * returns the resulting rows all at once mapping from column name to column
 * value. Signature: JDBCQueryAll(trigger, datasource, string) ->
 * collection<map<string, object>>
 *
 * @author TTS
 */
@Block(id = "JDBCQueryAll", category = "db", scheduler = "IO", description = "Issues an SQL query into the datasource once a trigger object arrives and returns the resulting rows all at once mapping from column name to column value.")
public class JDBCQueryAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCQueryAll.class.getName());
    /**
     * In.
     */
    @Input("advance:boolean")
    protected static final String TRIGGER = "trigger";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String DATASOURCE = "datasource";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String QUERY = "query";
    /**
     * Out.
     */
    @Output("advance:collection<advance:map<advance:string,advance:object>>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        // called on trigger
    }

    @Override
    public Observer<Void> run() {
        addCloseable(Reactive.observeOn(getInput(TRIGGER), scheduler()).register(new Observer<XNElement>() {

            @Override
            public void next(XNElement value) {
                if (resolver().getBoolean(value)) {
                    execute();
                }
            }

            @Override
            public void error(Throwable ex) {
            }

            @Override
            public void finish() {
            }
        }));
        return new RunObserver();
    }
    /** Execute the query. */
    private void execute() {
        JDBCConnection conn = null;
        try {
            final String dataSourceStr = getString(DATASOURCE);
            final Pool<JDBCConnection> ds = this.settings.context.pools.get(JDBCConnection.class, dataSourceStr);
            conn = ds.get();
            try {
                final String query = getString(QUERY);
                final PreparedStatement stm = conn.db().prepare(query);
                try {
	                final ResultSet rs = stm.executeQuery();
	                try {
	                    final ResultSetMetaData rsmd = rs.getMetaData();
	                    List<XNElement> result = Lists.newArrayList();
	                    while (rs.next()) {
	                    	result.add(JDBCConverter.create(resolver(), rs, rsmd));
	                    }
	                    dispatch(OUT, resolver().create(result));
	                } finally {
	                	rs.close();
	                }
                } finally {
                	stm.close();
                }
            } finally {
            	ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
        }
    }
}
