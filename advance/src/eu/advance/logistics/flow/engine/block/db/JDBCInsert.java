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

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Inserts a new value into the datastore via the given SQL statement and fills
 * in the parameters from the supplied map. If completed, the output is a map
 * for the auto-generated keys if any. Signature: JDBCInsert(datasource, string,
 * map<string, object>) -> boolean
 *
 * @author TTS
 */
@Block(id = "JDBCInsert", category = "db", scheduler = "IO", description = "Issues the given update SQL query into the datasource and fills in the query parameters from the supplied mapping from column name to column value. Once the update succeeded, a Boolean TRUE is emitted by the operator")
public class JDBCInsert extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCInsert.class.getName());
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
    @Output("advance:map<advance:string,advance:object>")
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
                final Map<XElement, XElement> paramMap = resolver().getMap(get(MAP));

                if (query != null) {
                    try {
                        final Set<XElement> keySet = paramMap.keySet();
                        final String[] columns = new String[keySet.size()];

                        //retrieve columns names
                        int count = 0;
                        for (XElement e : keySet) {
                            columns[count] = resolver().getString(e);
                            count++;
                        }

                        final PreparedStatement pstm = conn.getConnection().prepareStatement(query, columns);

                        // basing on types fill the prepared_statement
                        int paramCount = 1;
                        for (XElement e : keySet) {
                            paramCount = JDBCConverter.convert(resolver(), paramMap.get(e), pstm, paramCount);
                        }

                        pstm.executeUpdate();

                        final ResultSet rs = pstm.getGeneratedKeys();
                        if (rs != null) {
                            final ResultSetMetaData rsmd = rs.getMetaData();
                            while (rs.next()) {
                                dispatch(OUT, JDBCConverter.create(resolver(), rs, rsmd));
                            }
                            rs.close();
                        }
                    } catch (Exception ex) {
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
