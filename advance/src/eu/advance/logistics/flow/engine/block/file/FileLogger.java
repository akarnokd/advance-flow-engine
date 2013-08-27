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
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.reactive.Reactive;
import hu.akarnokd.utils.xml.XNElement;

import java.io.FileWriter;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceType;
import eu.advance.logistics.flow.engine.runtime.BlockDiagnostic;
import eu.advance.logistics.flow.engine.runtime.BlockState;
import eu.advance.logistics.flow.engine.runtime.Port;

/**
 * Save the data into a local file, appeding the received input at the end of the file with a timestamp.
 * Signature: FileLogger(string, string) -> void
 * @author szmarcell
 */
@Block(id = "FileLogger", category = "file", scheduler = "IO", description = "Save the data into a local file, appeding the received input at the end of the file with a timestamp.")
public class FileLogger extends AdvanceBlock {
	/** The datetime format. */
    private static final DateFormat DATETIME = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(FileLogger.class.getName());
    /** Filename input. */
    @Input("advance:string")
    protected static final String PATH = "path";
    /** Append input. */
    @Input("advance:string")
    protected static final String APPEND = "append";
    @Override
    protected Observer<Void> runReactiveBlock(List<Port<XNElement, AdvanceType>> reactivePorts) {
        for (Port<XNElement, AdvanceType> port : reactivePorts) {
            if (APPEND.equals(port.name())) {
                addCloseable(Reactive.observeOn(port, scheduler()).register(new InvokeObserver<XNElement>() {

                    @Override
                    public void next(XNElement value) {
                        diagnostic.next(new BlockDiagnostic("", description().id, Option.some(BlockState.START)));
                        FileWriter fw = null;
                        try {
                            fw = new FileWriter(file, true);
                            PrintWriter pw = new PrintWriter(fw, true);
                            pw.println(DATETIME.format(new Date()) + " | " + value.content);
                            pw.close();
                        } catch (Throwable t) {
                            diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(t)));
                        } finally {
                            if (fw != null) {
                                try {
                                    fw.close();
                                } catch (Exception ex) {
                                    diagnostic.next(new BlockDiagnostic("", description().id, Option.<BlockState>error(ex)));
                                }
                            }
                        }
                    }
                }));
            } else if (PATH.equals(port.name())) {
                addCloseable(Reactive.observeOn(port, scheduler()).register(new InvokeObserver<XNElement>() {

                    @Override
                    public void next(XNElement value) {
                        file = value.content;
                    }
                }));
            }
        }
        return new RunObserver();
    }
    /** The filename. */
    private String file;
    @Override
    protected void invoke() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
