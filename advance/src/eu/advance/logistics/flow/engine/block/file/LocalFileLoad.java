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

import com.google.common.io.Files;
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.nio.charset.Charset;

/**
 * Load the data from the local file. Signature: LocalFileLoad(trigger,
 * localfiledatasource) -> string
 *
 * @author TTS
 */
@Block(id = "LocalFileLoad", category = "file", scheduler = "IO", description = "Load the data from the local file.")
public class LocalFileLoad extends AdvanceBlock {

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
     * Out.
     */
    @Output("advance:string")
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

    /** Load the data from the local file. */
    private void execute() {
        try {
            final String dataSourceStr = getString(DATASOURCE);
            final Pool<LocalConnection> ds = getPool(LocalConnection.class, dataSourceStr);
            final LocalConnection conn = ds.get();
            try {
                final String resultStr = Files.toString(conn.file(), Charset.defaultCharset());
                
                dispatch(OUT, resolver().create(resultStr));
            } finally {
                ds.put(conn);
            }
        } catch (Exception ex) {
            log(ex);
            dispatch(OUT, resolver().create(""));
        }
    }
}
