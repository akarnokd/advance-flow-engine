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


import hu.akarnokd.utils.xml.XNElement;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.api.Pool;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;

/**
 * Delete entries from the given datastore by using the query and parameters.
 * Returns the number of elements deleted. Signature: JDBCDelete(datasource,
 * string, map<k, v>) -> integer
 *
 * @author TTS
 */
@Block(id = "JDBCDelete", 
	category = "db", 
	scheduler = "IO", 
	description = "Delete entries from the given datastore by using the query and parameters. Returns the number of elements deleted" 
)
public class JDBCDelete extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCDelete.class.getName());
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
    @Input("advance:map<advance:string,advance:object>")
    protected static final String MAP = "map";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        JDBCConnection conn = null;
        try {
            final String dataSourceStr = getString(DATASOURCE);
            final Pool<JDBCConnection> ds = getPool(JDBCConnection.class, dataSourceStr);
            conn = ds.get();
            try {
                final String query = getString(QUERY);
                final Map<XNElement, XNElement> paramMap = resolver().getMap(get(MAP));

                if (query != null) {
                    try {
                        final Set<XNElement> keySet = paramMap.keySet();
                        final String[] columns = new String[keySet.size()];

                        //retrieve columns names
                        int count = 0;
                        for (XNElement e : keySet) {
                            columns[count] = resolver().getString(e);
                            count++;
                        }

                        final PreparedStatement pstm = conn.db().prepare(query, (Object[])columns);

                        // basing on types fill the prepared_statement
                        int paramCount = 1;
                        for (XNElement e : keySet) {
                            paramCount = JDBCConverter.convert(resolver(), paramMap.get(e), pstm, paramCount);
                        }

                        final int res = pstm.executeUpdate();
                        conn.commit();
                        dispatch(OUT, resolver().create(res));
                        return;
                    } catch (Exception ex) {
                        conn.rollbackSilently();
                        log(ex);
                    }
                }

            } finally {
                ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
        }
    }
}
