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


import hu.akarnokd.utils.pool.Pool;
import hu.akarnokd.utils.xml.XNElement;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;

/**
 * Delete a batch of entries from the given datastore by using the query and
 * parameters. Returns the number of total elements deleted. Signature:
 * JDBCDeleteAll(datasource, string, collection<map<string, object>>) -> integer
 *
 * @author TTS
 */
@Block(id = "JDBCDeleteAll", category = "db", scheduler = "IO", description = "Delete a batch of entries from the given datastore by using the query and parameters. Returns the number of total elements deleted")
public class JDBCDeleteAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCDeleteAll.class.getName());
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
     * In.
     */
    @Input("advance:collection<advance:map<advance:string,advance:object>>")
    protected static final String LIST = "collection";
    /**
     * The total number of updates.
     */
    @Output("advance:integer")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        JDBCConnection conn = null;
        try {
            final String dataSourceStr = getString(DATASOURCE);
            final Pool<JDBCConnection> ds = this.settings.context.pools.get(JDBCConnection.class, dataSourceStr);
            conn = ds.get();
            ds.put(conn);
        } catch (Exception ex) {
            log(ex);
        }

        if (conn != null) {
            final String query = getString(QUERY);
            final List<XNElement> paramList = resolver().getList(get(LIST));

            if (query != null) {
                try {

                    final PreparedStatement pstm = conn.db().prepare(query);
                    int results = 0;
                    for (XNElement el : paramList) {

                        final Map<XNElement, XNElement> paramMap = resolver().getMap(el);
                        final Set<XNElement> keySet = paramMap.keySet();

                        // basing on types fill the prepared_statement
                        int paramCount = 1;
                        for (XNElement e : keySet) {
                            paramCount = JDBCConverter.convert(resolver(), paramMap.get(e), pstm, paramCount);
                            pstm.addBatch();
                        }
                    }
                    for (int i : pstm.executeBatch()) {
                    	results += i > 0 ? i : 0;
                    }

                    dispatch(OUT, resolver().create(results));
                    return;
                } catch (Exception ex) {
                    log(ex);
                }
            }
        }

        dispatch(OUT, resolver().create(0));
    }
}
