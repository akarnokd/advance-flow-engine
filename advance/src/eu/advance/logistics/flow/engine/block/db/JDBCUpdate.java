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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.xml.XElement;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Issues the given update SQL query into the datasource and fills in the query
 * parameters from the supplied mapping from column name to column value. Once
 * the update succeeded, a Boolean TRUE is emitted by the operator. Signature:
 * JDBCUpdate(datasource, string, map<string, object>) -> boolean
 *
 * @author TTS
 */
@Block(id = "JDBCUpdate", category = "db", scheduler = "IO", description = "Issues the given update SQL query into the datasource and fills in the query parameters from the supplied mapping from column name to column value. Once the update succeeded, a Boolean TRUE is emitted by the operator")
public class JDBCUpdate extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCUpdate.class.getName());
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
    @Input("advance:map")
    protected static final String MAP = "map";
    /**
     * Out.
     */
    @Output("advance:boolean")
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
            final Map<XElement, XElement> param_map = resolver().getMap(get(MAP));

            if (query != null) {
                try {
                    final Set<XElement> keySet = param_map.keySet();
                    final String[] columns = new String[keySet.size()];

                    //retrieve columns names
                    int count = 0;
                    for (XElement e : keySet) {
                        columns[count] = resolver().getString(e);
                        count++;
                    }

                    final PreparedStatement pstm = conn.getConnection().prepareStatement(query, columns);

                    // basing on types fill the prepared_statement
                    int param_count = 1;
                    for (XElement e : keySet) {
                        param_count = convert(param_map.get(e), pstm, param_count);
                    }

                    pstm.executeUpdate();

                    dispatch(OUT, resolver().create(true));
                    return;
                } catch (Exception ex) {
                    log(ex);
                }
            }
        }

        dispatch(OUT, resolver().create(false));
    }

    private int convert(XElement value, PreparedStatement pstm, int counter) throws Exception {

        final String val = value.name;
        if (val.equalsIgnoreCase("integer")) {
            pstm.setInt(counter, resolver().getInt(value));
            counter++;
        } else if (val.equalsIgnoreCase("integer")) {
            pstm.setDouble(counter, resolver().getDouble(value));
            counter++;
        } else if (val.equalsIgnoreCase("boolean")) {
            pstm.setBoolean(counter, resolver().getBoolean(value));
            counter++;
        } else if (val.equalsIgnoreCase("timestamp")) {
            pstm.setTimestamp(counter, new Timestamp(resolver().getTimestamp(value).getTime()));
            counter++;
        } else if (val.equalsIgnoreCase("bigdecimal")) {
            pstm.setBigDecimal(counter, resolver().getBigDecimal(value));
            counter++;
        } else if (val.equalsIgnoreCase("float")) {
            pstm.setFloat(counter, resolver().getFloat(value));
            counter++;
        } else {
            throw new Exception("Unknown parameter type " + val);
        }

        return counter;
    }
}
