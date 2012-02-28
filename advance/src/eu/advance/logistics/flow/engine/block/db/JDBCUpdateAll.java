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
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Updates multiple values at the same time with the given SQL query on the
 * datasource and emits an update count once the batch update completes.
 * Signature: JDBCUpdateAll(datasource, string, collection<map<string, object>>)
 * -> integer
 *
 * @author TTS
 */
@Block(id = "JDBCUpdateAll", category = "db", scheduler = "IO", description = "Issues the given update SQL query into the datasource and fills in the query parameters from the supplied mapping from column name to column value. Once the update succeeded, a Boolean TRUE is emitted by the operator")
public class JDBCUpdateAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCUpdateAll.class.getName());
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
     * Out.
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
            try {
                final String query = getString(QUERY);
                final List<XElement> paramList = resolver().getList(get(LIST));
                final PreparedStatement pstm = conn.getConnection().prepareStatement(query);
                try {
	                int results = 0;
	
	                for (XElement listElement : paramList) {
	                	Map<XElement, XElement> row = resolver().getMap(listElement);
	                    int paramCount = 1;
	                	for (XElement value : row.values()) {
	                        paramCount = JDBCConverter.convert(resolver(), value, pstm, paramCount);
	                	}
	                	pstm.addBatch();
	                }
	
	                for (int i : pstm.executeBatch()) {
	                	results += i > 0 ? i : 0;
	                }
	                conn.commit();
	                dispatch(OUT, resolver().create(results));
                } catch (SQLException ex) {
                	conn.rollbackSilently();
                	log(ex);
                } finally {
                	pstm.close();
                }
                
            } finally {
            	ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
        }

        dispatch(OUT, resolver().create(0));
    }
}
