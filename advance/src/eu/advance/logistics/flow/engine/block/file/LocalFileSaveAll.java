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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Save a set of files locally. Signature: LocalFileSaveAll(trigger,
 * localfiledatasource, map<string, string>) -> boolean
 *
 * @author TTS
 */
@Block(id = "LocalFileSaveAll", 
	category = "file", 
	scheduler = "IO", 
description = "Save a set of files locally.")
public class LocalFileSaveAll extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(LocalFileSave.class.getName());
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
    @Input("advance:map<advance:string, advance:string>")
    protected static final String CONTENT = "content";
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

    /**
     * Save a set of files locally.
     */
    private void execute() {
        try {
            final Map<XElement, XElement> map = resolver().getMap(get(CONTENT));
            final String dataSourceStr = getString(DATASOURCE);

            final Pool<LocalConnection> ds = getPool(LocalConnection.class, dataSourceStr);
            final LocalConnection conn = ds.get();
            try {

                for (Map.Entry<XElement, XElement> e : map.entrySet()) {
                    final XElement key = e.getKey();
                    final XElement value = e.getValue();

                    conn.send(resolver().getString(key), resolver().getString(value).getBytes());
                }

                dispatch(OUT, resolver().create(true));
            } finally {
                ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
            dispatch(OUT, resolver().create(false));
        }
    }
}
