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
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.comm.JDBCPoolManager;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Issues an SQL query into the datasource once a trigger object arrives and
 * returns the rows converted into a mapping from column name to column value.
 * Signature: JDBCQuery(trigger, datasource, string, schema<t>) ->map<string,
 * object>
 *
 * @author TTS
 */
@Block(id = "JDBCQuery", category = "db", scheduler = "IO", description = "Issues an SQL query into the datasource once a trigger object arrives and returns the rows converted into a mapping from column name to column value.")
public class JDBCQuery extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCQuery.class.getName());
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
    @Output("advance:map<advance:string,advance:object>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
    	// called on trigger
    }
    
    @Override
    public Observer<Void> run() {
       addCloseable(Reactive.observeOn(getInput(TRIGGER), scheduler()).register(new Observer<XElement>() {

            @Override
            public void next(XElement value) {
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
    
    private void execute() {
        final AdvanceJDBCDataSource ds = new AdvanceJDBCDataSource();
        ds.load(get(DATASOURCE));
        final JDBCPoolManager pm = new JDBCPoolManager(ds);
        JDBCConnection conn = null;
        try {
            conn = pm.create();
        } catch (Exception ex) {
            log(ex);
        }
        
        if (conn != null) {
            final String query = getString(QUERY);
            
            if (query != null) {
                try {
                    final Statement stm = conn.getConnection().createStatement();
                   final ResultSet rs = stm.executeQuery(query);
                    if (rs != null) {
                        final ResultSetMetaData rsmd = rs.getMetaData();
                        while (rs.next()) {
                            dispatch(OUT, create(rs, rsmd));
                        }
                        rs.close();
                    }
                } catch (SQLException ex) {
                    log(ex);
                }
            }

        }
    }

    private XElement create(ResultSet rs, ResultSetMetaData rsmd) throws SQLException {
        final Map<XElement,XElement> data = new HashMap<XElement,XElement>();
        
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
