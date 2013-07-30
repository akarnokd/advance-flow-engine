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

import java.util.Map;

import com.google.common.io.Files;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.LocalConnection;

/**
 * Move/rename a set of local files. Signature: LocalFileMoveAll(trigger,
 * localfiledatastore, map<string, string>) -> boolean
 *
 * @author TTS
 */
@Block(id = "LocalFileMoveAll", category = "file", scheduler = "IO", description = "Move/rename a set of local files.")
public class LocalFileMoveAll extends AdvanceBlock {

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
    protected static final String MAP = "map";
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
            final Map<XNElement, XNElement> map = resolver().getMap(get(MAP));
            
            final int size = map.size();
            int count = 0;
            
            for (XNElement key : map.keySet()) {
                final XNElement value = map.get(key);
                final String keyStr = resolver().getString(key);
                final String valueStr = resolver().getString(value);

                final Pool<LocalConnection> dsKey = getPool(LocalConnection.class, keyStr);
                final Pool<LocalConnection> dsValue = getPool(LocalConnection.class, valueStr);
                final LocalConnection connKey = dsKey.get();
                final LocalConnection connValue = dsValue.get();

                try {
                    Files.move(connKey.file(), connValue.file());
                    count++;
                } catch (Exception ex) {
                    log(ex);
                } finally {
                    dsKey.put(connKey);
                    dsValue.put(connValue);
                }
            }

            dispatch(OUT, resolver().create(count == size));
        } catch (Exception ex) {
            log(ex);
            dispatch(OUT, resolver().create(false));
        }
    }
}
