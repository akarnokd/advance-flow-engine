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
package eu.advance.logistics.flow.engine.block.file;

import hu.akarnokd.reactive4java.base.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.utils.xml.XNElement;

import com.google.common.io.Files;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.LocalConnection;

/**
 * Move/rename a local file. Signature: LocalFileMove(trigger,
 * localfiledatasource, string, string) -> boolean
 *
 * @author TTS
 */
@Block(id = "LocalFileMove", category = "file", scheduler = "IO", description = "Move/rename a local file")
public class LocalFileMove extends AdvanceBlock {

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
    protected static final String SOURCE = "source";
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String DESTINATION = "destination";
    /**
     * Out.
     */
    @Output("advance:boolean")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        // called on trigger
    }

    @Override
    public Observer<Void> run() {
        addCloseable(Reactive.observeOn(getInput(TRIGGER), scheduler()).register(new Observer<XNElement>() {

            @Override
            public void next(XNElement value) {
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

    /**
     * Load the data from the local file.
     */
    private void execute() {
        try {
//            final String dataSourceStr = getString(DATASOURCE);
            final String sourceStr = getString(SOURCE);
            final String destStr = getString(DESTINATION);

//            final Pool<LocalConnection> ds = getPool(LocalConnection.class, dataSourceStr);
            final Pool<LocalConnection> ds1 = getPool(LocalConnection.class, sourceStr);
            final Pool<LocalConnection> ds2 = getPool(LocalConnection.class, destStr);
//            final LocalConnection conn = ds.get();
            final LocalConnection conn1 = ds1.get();
            final LocalConnection conn2 = ds2.get();
            try {
                //rename
                Files.move(conn1.file(), conn2.file());

                dispatch(OUT, resolver().create(true));
            } catch (Exception ex) {
                log(ex);
                dispatch(OUT, resolver().create(false));
            } finally {
//                ds.put(conn);
                ds1.put(conn1);
                ds2.put(conn2);
            }
        } catch (Exception ex) {
            log(ex);
            dispatch(OUT, resolver().create(false));
        }
    }
}
