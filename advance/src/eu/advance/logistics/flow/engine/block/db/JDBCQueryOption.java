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


import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.utils.pool.Pool;
import hu.akarnokd.utils.xml.XNElement;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.AdvanceData;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;

/**
 * Issues an SQL query into the datasource once a trigger object arrives and
 * returns the rows converted into a mapping from column name to column value.
 * Signature: JDBCQuery(trigger, datasource, string) ->map<string, object>
 *
 * @author TTS
 */
@Block(id = "JDBCQueryOption", category = "db", 
scheduler = "IO", 
description = "Issues an SQL query into the datasource once a trigger object arrives and returns the rows converted into a mapping from column name to column value as an option.")
public class JDBCQueryOption extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(JDBCQueryOption.class.getName());
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
    @Output("advance:option<advance:map<advance:string,advance:object>>")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
    	// called on trigger
    }
    
    @Override
    public Observer<Void> run() {
    	return new RunObserver() {
    		@Override
    		public void next(Void value) {
		    	observeInput(TRIGGER, new Action1<XNElement>() {
		    		@Override
		    		public void invoke(XNElement value) {
		                if (resolver().getBoolean(value)) {
		                    execute();
		                }
		    		}    		
		    	});
	       }
       };
    }
    
    /**
     * Execute.
     */
    private void execute() {
    	
    	try {
    	
    		String dataSourceStr = getString(DATASOURCE);
    		
	        final Pool<JDBCConnection> ds = getPool(JDBCConnection.class, dataSourceStr);
	        
	        if (ds != null) {
		        JDBCConnection conn = ds.get();
		        try {
		            final String query = getString(QUERY);
		            
		            if (query != null) {
		                try {
		                    final PreparedStatement stm = conn.db().prepare(query);
		                    try {
			                    final ResultSet rs = stm.executeQuery();
			                    try {
				                    if (rs != null) {
				                        final ResultSetMetaData rsmd = rs.getMetaData();
				                        while (rs.next()) {
				                            dispatch(OUT, AdvanceData.createSome(JDBCConverter.create(resolver(), rs, rsmd)));
				                        }
				                        dispatch(OUT, AdvanceData.createNone());
				                        rs.close();
				                    }
			                    } finally {
			                    	rs.close();
			                    }
		                    } finally {
		                    	stm.close();
		                    }
		                } catch (SQLException ex) {
		                    log(ex);
		                }
		            }
		        } finally {
		        	ds.put(conn);
		        }
	        } else {
	        	log(new IllegalArgumentException("No pool for JDBCConnection & " + dataSourceStr));
	        }
    	} catch (Exception ex) {
    		log(ex);
    	}
    }
}
