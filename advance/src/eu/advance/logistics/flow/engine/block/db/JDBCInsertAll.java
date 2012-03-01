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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
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
 * Inserts a collection of values into the datastore via the given SQL statement
 * and fills in the parameters from the supplied map. Once the batch insert is
 * complete, the operator returns a collection with the auto generated keys for
 * each of the input maps. Signature: JDBCUpdateAll(datasource, string,
 * map<k, v>) -> collection<map<k, v>>
 *
 * @author TTS
 */
@Block(id = "JDBCInsertAll", 
	category = "db", 
	scheduler = "IO", 
	description = "Inserts a collection of values into the datastore via the given SQL statement and fills in the parameters from the supplied map. Once the batch insert is complete, the operator returns a collection with the auto generated keys for each of the input maps", 
	parameters = { "K", "V" }
)
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
    @Input("advance:collection<advance:map<?K, ?V>>")
    protected static final String LIST = "collection";
    /**
     * The auto-generated keys if any.
     */
    @Output("advance:collection<advance:map<?K, ?V>>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        final List<XElement> paramList = resolver().getList(get(LIST));
        if (paramList.isEmpty()) {
        	return;
        }
        JDBCConnection conn = null;
        try {
            final String dataSourceStr = getString(DATASOURCE);
            final Pool<JDBCConnection> ds = getPool(JDBCConnection.class, dataSourceStr);
            conn = ds.get();
            try {
                final String query = getString(QUERY);

                
                
                final List<XElement> resultList = new ArrayList<XElement>();

                // prepare columns
                final Map<XElement, XElement> paramMap = resolver().getMap(paramList.get(0));

                final Set<XElement> keySet = paramMap.keySet();
                final String[] columns = new String[keySet.size()];

                //retrieve columns names
                int count = 0;
                for (XElement e : keySet) {
                    columns[count] = resolver().getString(e);
                    count++;
                }

                final PreparedStatement pstm = conn.getConnection().prepareStatement(query, columns);

                
                for (XElement el : paramList) {
                    Map<XElement, XElement> row = resolver().getMap(el);
                   // basing on types fill the prepared_statement
                    int paramCount = 1;
                    for (XElement e : row.values()) {
                        paramCount = JDBCConverter.convert(resolver(), e, pstm, paramCount);
                        pstm.addBatch();
                    }
                }
                
                pstm.executeBatch();
                
                final ResultSet rs = pstm.getGeneratedKeys();
                if (rs != null) {
                    final ResultSetMetaData rsmd = rs.getMetaData();
                    while (rs.next()) {
                        resultList.add(JDBCConverter.create(resolver(), rs, rsmd));
                    }
                    rs.close();
                }

                conn.commit();
                
                dispatch(OUT, resolver().create(resultList));
            } catch (SQLException ex) {
            	conn.rollbackSilently();
            	log(ex);
            } finally {
            	ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
        }

    }
}
