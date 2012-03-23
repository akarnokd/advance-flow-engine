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
package eu.advance.logistics.flow.engine.block.prediction;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Get a consignment form a database.
 * @author TTS
 */
@Block(id = "ConsignmentGet", category = "prediction", scheduler = "IO",
description = "Gets consignment from the DB")
public class ConsignmentGet extends AdvanceBlock {

    /** Trigger for activating the data fetch. */
    @Input("advance:boolean")
    protected static final String TRIGGER = "trigger";
    
    /** Name of the JDBC data source. */
    @Input("advance:string")
    protected static final String DATASOURCE = "datasource";
    
    /** Stream of consignment fetched from the database. 
     * The end of the stream is marked with a consignment with id = -1.
     */
    @Output("advance:consignment")
    protected static final String OUT = "consignment";
    
    /**
     * Special consignment instance that mark the end if stream.
     */
    public static final Consignment EOF = new Consignment();
    
    {
    	EOF.id = -1;
    }

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

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

    /** Activated on trigger. */
    private void execute() {
    	JDBCConnection conn = null;
		try {
			final String dataSourceStr = getString(DATASOURCE);
			final Pool<JDBCConnection> ds = getPool(JDBCConnection.class, dataSourceStr);
			conn = ds.get();
			try {
				final Session s = new Session(conn.getConnection());
				try {
					Consignment c;
					while ((c = s.next()) != null) {
						dispatch(OUT, c.toXML("consignment"));
					}
				} finally {
					s.close();
				}
			} finally {
				ds.put(conn);
			}
		} catch (Exception ex) {
			log(ex);
		}    	
    }
    
    /**
     * Internal class to handle a single query session.
     * @author TTS
     */
    private final class Session {
    	/** Query statement. */
        private Statement selectConsignment;
        /** Depots. */
        private Map<String, Integer> depots = new HashMap<String, Integer>();
        /** Events. */
        private Map<Integer, String> events = new HashMap<Integer, String>();
        /** Flag to single end-of-stream. */
        private boolean eof = false;
        /** Working result sets. */
   	 	private ResultSet rs;
        
   	 	/**
   	 	 * A query session built from a connection.
   	 	 * @param connection the JDBC connection
   	 	 * @throws SQLException if any error in queries
   	 	 */
        private Session(Connection connection) throws SQLException {
        	ResultSet rs;

        	 // fetch all depots
             PreparedStatement allDepots = connection.prepareStatement("SELECT Name, idDepot FROM depot", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             rs = allDepots.executeQuery();
             while (rs.next()) {
                 depots.put(rs.getString(1), rs.getInt(2));
             }
             rs.close();
             allDepots.close();

             // fetch all event types
             PreparedStatement allEventTypes = connection.prepareStatement("SELECT idEventType, Name FROM eventtype", ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
             rs = allEventTypes.executeQuery();
             while (rs.next()) {
                 events.put(rs.getInt(1), rs.getString(2));
             }
             rs.close();
             allEventTypes.close();

             // build the query
             StringBuilder sb = new StringBuilder();
             sb.append("SELECT consignment.*, ");
             for (Map.Entry<Integer, String> eventTypeName : events.entrySet()) {
                 sb.append("event").append(eventTypeName.getKey()).append(".timestamp AS '");
                 sb.append(eventTypeName.getValue()).append("', ");
             }
             int len = sb.length();
             sb.delete(len - 2, len);
             sb.append(" FROM consignment ");
             for (Map.Entry<Integer, String> eventTypeName : events.entrySet()) {
                 sb.append("LEFT JOIN event AS event").append(eventTypeName.getKey());
                 sb.append(" ON(consignment.idConsignment=event").append(eventTypeName.getKey());
                 sb.append(".consignment AND event").append(eventTypeName.getKey());
                 sb.append(".eventtype=").append(eventTypeName.getKey()).append(") ");
             }
             sb.deleteCharAt(sb.length() - 1);

             // TYPE_FORWARD_ONLY & CONCUR_READ_ONLY is the default but we set it 
             // explicitly as it is required to stream result sets row-by-row.
             selectConsignment = connection.createStatement(
                     ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);

             // magic value to tell the driver to stream the results one row at a time.
             // see http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-implementation-notes.html
             selectConsignment.setFetchSize(Integer.MIN_VALUE);
             rs = selectConsignment.executeQuery(sb.toString());
        }
        
        /**
         * Fetch the next consignment.
         * @return the consignment or EOF
         * @throws SQLException error accessing the result set
         */
        private Consignment next() throws SQLException {
            if (!rs.next()) {
                return EOF;
            }
            return eof ? EOF : adapt();
        }

        /**
         * Fetch as many rows as necessary to build a consignment with events.
         * @return the consignment (with events)
         * @throws SQLException error accessing the result set         * 
         */
        private Consignment adapt() throws SQLException {
            Consignment c = new Consignment();
            c.id = rs.getInt("idConsignment");
            c.palletCount = rs.getInt("palletCount");
            c.volume = rs.getInt("volume");
            c.weight = rs.getInt("weight");
            c.collectionDepotId = rs.getInt("collectionDepot");
            c.deliveryDepotId = rs.getInt("deliveryDepot");

            while (c.id == rs.getInt("idConsignment")) {
                for (String eventName : events.values()) {
                    parseEvent(c, eventName, rs);
                }
                if (!rs.next()) {
                    eof = true;
                    break;
                }
            }

            return c;
        }

        /**
         * Close the session.
         */
        private void close() {
            try {
                rs.close();
            } catch (SQLException ex) {
                log(ex);
            }
        }
        
        /**
         * Parse events and add them into the list sorted by date.
         * @param c consignment
         * @param name event name
         * @param rs result set
         * @throws SQLException error accessing the result set
         */
        private void parseEvent(Consignment c, String name, ResultSet rs) throws SQLException {
        	List<Event> events = c.events;
            Timestamp eventDateTime = rs.getTimestamp(name);
            if (eventDateTime != null) {
            	Event newEvent = new Event();
            	newEvent.name = name;
            	newEvent.timestamp = new java.util.Date(eventDateTime.getTime());
            	for (int i = 0, n = events.size(); i < n; i++) {
            		Event event = events.get(i);
            		if (newEvent.name.equals(event.name) && newEvent.timestamp.before(event.timestamp)) {
            			events.add(i, newEvent);
            			return;
            		}
            	}
            	events.add(newEvent);
            }
        }
    	
    }
    
}
