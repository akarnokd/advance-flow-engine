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
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;

/**
 * Inserts a collection of values into the datastore via the given SQL statement
 * and fills in the parameters from the supplied map. Once the batch insert is
 * complete, the operator returns a collection with the auto generated keys for
 * each of the input maps. Signature: JDBCUpdateAll(datasource, string,
 * map<string, object>) -> collection<map<string, object>>
 *
 * @author TTS
 */
@Block(id = "JDBCInsertAll", category = "db", scheduler = "IO", description = "Inserts a collection of values into the datastore via the given SQL statement and fills in the parameters from the supplied map. Once the batch insert is complete, the operator returns a collection with the auto generated keys for each of the input maps")
public class JDBCInsertAll extends AdvanceBlock {

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
    @Input("advance:collection")
    protected static final String LIST = "collection";
    /**
     * Out.
     */
    @Output("advance:collection")
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
            final List<XElement> param_list = resolver().getList(get(LIST));

            if (query != null) {
                try {
                    final List<XElement> result_list = new ArrayList<XElement>();

                    for (XElement el : param_list) {
                        final Map<XElement, XElement> param_map = resolver().getMap(el);

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

                        final ResultSet rs = pstm.getGeneratedKeys();
                        if (rs != null) {
                            final ResultSetMetaData rsmd = rs.getMetaData();
                            while (rs.next()) {
                                result_list.add(create(rs, rsmd));
                            }
                            rs.close();
                        }
                    }

                    dispatch(OUT, resolver().create(result_list));
                } catch (Exception ex) {
                    log(ex);
                }
            }
        }
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

    private XElement create(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {

        final Map<XElement, XElement> data = new HashMap<XElement, XElement>();

        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            XElement value = null;

            switch (rsmd.getColumnType(i)) {
                case java.sql.Types.BOOLEAN:
                    value = resolver().create(rs.getBoolean(i));
                    break;
                case java.sql.Types.INTEGER:
                    value = resolver().create(rs.getInt(i));
                    break;
                case java.sql.Types.DOUBLE:
                    value = resolver().create(rs.getDouble(i));
                    break;
                case java.sql.Types.DATE:
                    value = resolver().create(rs.getDate(i));
                    break;
                case java.sql.Types.BIGINT:
                    value = resolver().create(rs.getBigDecimal(i));
                    break;
                case java.sql.Types.FLOAT:
                    value = resolver().create(rs.getFloat(i));
                    break;
                case java.sql.Types.TIME:
                    value = resolver().create(rs.getTime(i));
                    break;
                case java.sql.Types.TIMESTAMP:
                    value = resolver().create(rs.getTimestamp(i));
                    break;
                case java.sql.Types.VARCHAR:
                    value = resolver().create(rs.getString(i));
                    break;
            }

            if (value != null) {
                data.put(resolver().create(rsmd.getColumnName(i)), value);
            }
        }

        return resolver().create(data);
    }
}
