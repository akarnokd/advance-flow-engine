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

import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.utils.xml.XNElement;
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.api.Pool;
import eu.advance.logistics.flow.engine.comm.LocalConnection;

/**
 * During day model writer.
 *
 * @author TTS
 */
@Block(id = "DuringDayModelWriter", category = "prediction", scheduler = "IO",
description = "Writes the during-day model.")
public class DuringDayModelWriter extends AdvanceBlock {

    /**
     * Location where the DuringDay model will be stores. It must rappresent a
     * file.
     */
    @Input("advance:string")
    protected static final String LOCATION = "location";
    /**
     * DuringDay model to save.
     */
    @Input("advance:duringdaymodel")
    protected static final String MODEL = "model";

    @Override
    public Observer<Void> run() {
        addCloseable(Reactive.observeOn(getInput(MODEL), scheduler()).register(new Observer<XNElement>() {

            @Override
            public void next(XNElement value) {
                write(value);
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
    /** 
     * Write the model.
     * @param root the document 
     */
    private void write(XNElement root) {
        if (get(LOCATION) == null) {
            LOG.error("DuringDayModelWriter: no location!");
            return;
        }
        Pool<LocalConnection> ds = null;
        LocalConnection conn = null;
        try {
            LOG.info("DuringDayModelWriter: writing to " + getString(LOCATION) + "...");
            ds = getPool(LocalConnection.class, getString(LOCATION));
            if (ds == null) {
                LOG.error("DuringDayModelWriter: unable to get local connection!");
            } else {
                conn = ds.get();
                root.save(conn.file());
                LOG.info("DuringDayModelWriter: done.");
            }
        } catch (Exception ex) {
            log(ex);
        } finally {
            if (ds != null && conn != null) {
                ds.put(conn);
            }
        }
    }

    @Override
    protected void invoke() {
    }
}
