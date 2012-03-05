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
import eu.advance.logistics.flow.engine.api.ds.AdvanceJDBCDataSource;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.comm.JDBCConnection;
import eu.advance.logistics.flow.engine.comm.JDBCPoolManager;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Get a consignment form a database.
 * @author TTS
 */
@Block(id = "ConsignmentGet", category = "prediction", scheduler = "IO",
description = "Gets consignment from the DB")
public class ConsignmentGet extends AdvanceBlock {

	/** Query to select the unique consignment id from the events table based on min and max dates. */
    private static final String QUERY_1 = "SELECT DISTINCT consignment FROM adb.events WHERE timestamp > ? AND timestamp < ?";
    
    /** Query to select a single consigmnet corresponding to a specific id. */
    private static final String QUERY_2 = "SELECT * FROM adb.consignment WHERE idConsignment = ?";
    
    /** Query to select all the events associated to a consignment. */
    private static final String QUERY_3 = "SELECT * FROM adb.events WHERE consignment = ? ORDER BY timestamp DESC";

    /** Trigger for activating the data fetch. */
    @Input("advance:boolean")
    protected static final String TRIGGER = "trigger";
    
    /** Name of the JDBC data source. */
    @Input("advance:string")
    protected static final String DATASOURCE = "datasource";
    
    /** Minimum date used to filter events. */
    @Input("advance:timestamp")
    protected static final String DATE_AFTER = "dateAfter";

    /** Maximum date used to filter events. */
    @Input("advance:timestamp")
    protected static final String DATE_BEFORE = "dateBefore";
    
    /** Stream of consignment fetched from the database. 
     * The end of the stream is marked with a consignment with id = -1.
     */
    @Output("advance:consignment")
    protected static final String OUT = "consignment";

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
        AdvanceJDBCDataSource ds = new AdvanceJDBCDataSource();
        ds.load(get(DATASOURCE));
        JDBCPoolManager pm = new JDBCPoolManager(ds);
        JDBCConnection conn = null;
        try {
            conn = pm.create();
        } catch (Exception ex) {
        	LOG.error(null, ex);
        }
        if (conn != null) {

            try {
                Date dateAfter = getTimestamp(DATE_AFTER);
                Date dateBefore = getTimestamp(DATE_BEFORE);
                if (dateAfter != null && dateBefore != null) {
                    PreparedStatement ps1 = (PreparedStatement) conn.getConnection().prepareStatement(QUERY_1);
                    PreparedStatement ps2 = (PreparedStatement) conn.getConnection().prepareStatement(QUERY_2);
                    PreparedStatement ps3 = (PreparedStatement) conn.getConnection().prepareStatement(QUERY_3);
                    ps1.setDate(1, new java.sql.Date(dateAfter.getTime()));
                    ps1.setDate(2, new java.sql.Date(dateBefore.getTime()));
                    ResultSet rs = ps1.executeQuery();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        ps2.setInt(1, id);
                        ps3.setInt(1, id);
                        ResultSet rs2 = ps2.executeQuery();
                        if (rs2.next()) {
                            ResultSet rs3 = ps3.executeQuery();
                            dispatch(OUT, create(rs2, rs3));
                            rs3.close();
                        }
                        rs2.close();
                    }
                    rs.close();
                    
                    Consignment eof = new Consignment();
                    eof.id = -1;
                    dispatch(OUT, eof.toString());
                }

            } catch (Exception ex) {
            	LOG.error(null, ex);
            }

        }
    }

    /**
     * Creates a XML representation of a consignment from a database row.
     * @param crs row containing the consignment data
     * @param ers row containing the events data
     * @return the XML representation of a Consignment object
     */
    private XElement create(ResultSet crs, ResultSet ers) {
        Consignment c = new Consignment();
        try {
            c.id = crs.getInt("ConsignmentId");
            c.hubId = crs.getInt("Hub");
            c.collectionDepotId = crs.getInt("CollectionDepot");
            c.collectionLocationId = crs.getInt("CollectionLocation");
            c.deliveryDepotId = crs.getInt("DeliveryDepot");
            c.deliveryLocationId = crs.getInt("DeliveryLocation");
            c.payingDepotId = crs.getInt("PayingDepot");
            c.palletCount = crs.getInt("PalletCount");
            c.weight = crs.getInt("Weight");
            c.number = crs.getString("Number");
            c.volume = crs.getDouble("Volume");
            c.events = new ArrayList<Event>();
            while (ers.next()) {
                Event e = new Event();
                e.eventType = ers.getInt("eventType");
                e.notes = ers.getString("notes");
                e.timestamp = ers.getTimestamp("timestamp");
                c.events.add(e);
            }
        } catch (SQLException ex) {
            LOG.error(null, ex);
        }
        return c.toXML("Consignment");
    }
}
