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

import java.util.Collections;
import java.util.Map;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.api.Pool;
import eu.advance.logistics.flow.engine.comm.LocalConnection;

/**
 * Load a set of local files. Signature: LocalFileLoadAll(trigger,
 * localfiledatasource, collection<string>) -> map<string, string>
 *
 * @author TTS
 */
@Block(id = "LocalFileLoadAll", 
	category = "file", 
	scheduler = "IO", 
description = "Load a set of local files.")
public class LocalFileLoadAll extends AdvanceBlock {

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
    @Input("advance:collection<advance:string>")
    protected static final String COLLECTION = "collection";
    /**
     * Out.
     */
    @Output("advance:map<advance:string, advance:string>")
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
     * Load a set of local files.
     */
    private void execute() {
        final Map<XNElement, XNElement> result = Collections.emptyMap();
        try {
            final String dataSourceStr = getString(DATASOURCE);
            final Pool<LocalConnection> ds = getPool(LocalConnection.class, dataSourceStr);
            final LocalConnection conn = ds.get();
            try {
                for (XNElement e : resolver().getItems(get(COLLECTION))) {
                    final String key = resolver().getString(e);
                    final String value = new String(conn.retrieve(key));

                    result.put(resolver().create(key), resolver().create(value));
                }
            } finally {
                ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
        }
        dispatch(OUT, resolver().create(result));
    }
}
