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

import hu.akarnokd.utils.pool.Pool;
import hu.akarnokd.utils.xml.XNElement;
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.LocalConnection;

/**
 * During day model writer.
 *
 * @author TTS
 */
@Block(id = "DuringDayModelReader", category = "prediction", scheduler = "IO",
description = "Reads a during-day model.")
public class DuringDayModelReader extends AdvanceBlock {

    /**
     * Location from where the DuringDay model will be loades.
     * It must rappresent a file.
     */
    @Input("advance:string")
    protected static final String LOCATION = "location";
    /**
     * DuringDay model loaded from the location.
     */
    @Output("advance:duringdaymodel")
    protected static final String MODEL = "model";

    @Override
    protected void invoke() {
        Pool<LocalConnection> ds = null;
        LocalConnection conn = null;
        try {
            ds = getPool(LocalConnection.class, getString(LOCATION));
            conn = ds.get();
            XNElement root = XNElement.parseXML(conn.file());
            dispatch(MODEL, root);
        } catch (Exception ex) {
            log(ex);
        } finally {
            if (ds != null && conn != null) {
                ds.put(conn);
            }
        }
    }

}
