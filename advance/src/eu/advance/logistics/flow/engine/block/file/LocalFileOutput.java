/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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

import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import hu.akarnokd.reactive4java.reactive.Observer;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockDiagnostic;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockState;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.PrintWriter;

/**
 * Save the data into a local file, appending the received input at the end of file or overwriting the file.
 * Signature: LocalFileOutput(string, string, string) -> void
 * @author szmarcell
 */
@Block(id = "LocalFileOutput", category = "file", scheduler = "IO", description = "Save the data into a local file, appending the received input at the end of file or overwriting the file.")
public class LocalFileOutput extends AdvanceBlock {

    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(LocalFileOutput.class.getName());
    /** Filename input. */
    @Input("advance:string")
    protected static final String PATH = "path";
    /** Append input. */
    @Input("advance:string")
    protected static final String APPEND = "append";
    /** Write input. */
    @Input("advance:string")
    protected static final String WRITE = "write";
    @Override
    protected Observer<Void> runReactiveBlock(List<AdvancePort> reactivePorts) {
        for (AdvancePort port : reactivePorts) {
            if (APPEND.equals(port.name())) {
                register(port, new InvokeObserver<XElement>() {

                    @Override
                    public void next(XElement value) {
                        diagnostic.next(new AdvanceBlockDiagnostic("", description().id, Option.some(AdvanceBlockState.START)));
                        try {
                            FileWriter fw = new FileWriter(file, true);
                            fw.append(value.content);
                            fw.close();
                        } catch (Throwable t) {
                            diagnostic.next(new AdvanceBlockDiagnostic("", description().id, Option.<AdvanceBlockState>error(t)));
                        }
                    }
                });
            } else if (WRITE.equals(port.name())) {
                register(port, new InvokeObserver<XElement>() {

                    @Override
                    public void next(XElement value) {
                        diagnostic.next(new AdvanceBlockDiagnostic("", description().id, Option.some(AdvanceBlockState.START)));
                        try {
                            FileWriter fw = new FileWriter(file, false);
                            fw.append(value.content);
                            fw.close();
                        } catch (Throwable t) {
                            diagnostic.next(new AdvanceBlockDiagnostic("", description().id, Option.<AdvanceBlockState>error(t)));
                        }
                    }
                });
            } else if (PATH.equals(port.name())) {
                register(port, new InvokeObserver<XElement>() {

                    @Override
                    public void next(XElement value) {
                        file = value.content;
                    }
                });
            }
        }
        return new RunObserver();

    }
    private void register(AdvancePort port, Observer<XElement> observer) {
        addCloseable(Reactive.observeOn(port, scheduler()).register(observer));
    }
    /** The filename. */
    private String file;
    @Override
    protected void invoke() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
