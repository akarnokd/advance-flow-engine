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
package eu.advance.logistics.flow.engine.block.streaming;

import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;

import java.util.LinkedList;
import java.util.logging.Logger;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceConstantPort;
import eu.advance.logistics.flow.engine.model.rt.AdvanceData;
import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Buffers the incoming values into a collection with a maximum size and forwards this collection once fully filled.
 * Signature: BufferWithSize(t, integer, boolean) -> collection<t>
 * @author szmarcell
 */
@Block(id = "BufferWithSize", category = "streaming", scheduler = "IO", parameters = "T", description = "Buffers the incoming values into a collection with a maximum size and forwards this collection. Forwards the collection only when fully filled (the default behaviour), unless eagerness is set.")
public class BufferWithSize extends AdvanceBlock {
    /** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(BufferWithSize .class.getName());
    /** In element. */
    @Input("?T")
    protected static final String ELEMENT = "element";
    /** In size. */
    @Input("advance:integer")
    protected static final String SIZE = "size";
    /** In eagerness. */
    @Input("advance:boolean")
    protected static final String EAGER = "eager";
    /** Out. */
    @Output("advance:collection<?T>")
    protected static final String OUT = "out";
    /** The elements. */
    private LinkedList<XElement> elements = Lists.newLinkedList();
    /** The maximum size. */
    private int maxSize = 0;
    /** The actual size. */
    private int actualSize = 0;
    /** Should be eager? */
    private boolean eager = false;
    @Override
    public Observer<Void> run() {
        AdvancePort sizePort = inputs.get(SIZE);
        AdvancePort eagerPort = inputs.get(EAGER);
        AdvancePort elementPort = inputs.get(ELEMENT);
        if (sizePort instanceof AdvanceConstantPort) {
            maxSize = AdvanceData.getInt(((AdvanceConstantPort)sizePort).value);
        } else if (sizePort != null) {
            addCloseable(Reactive.observeOn(sizePort, scheduler()).register(new InvokeObserver<XElement>() {

                @Override
                public void next(XElement value) {
                    int nSize = AdvanceData.getInt(value);
                    if (actualSize > nSize) {
                        for (int i = 0; i < actualSize - nSize; i++) {
                            elements.poll();
                        }
                        actualSize = nSize;
                    }
                    maxSize = nSize;
                }
            }));
        }
        if (eagerPort instanceof AdvanceConstantPort) {
            eager = Boolean.parseBoolean(((AdvanceConstantPort)eagerPort).value.content);
        } else if (eagerPort != null) {
            addCloseable(Reactive.observeOn(sizePort, scheduler()).register(new InvokeObserver<XElement>() {

                @Override
                public void next(XElement value) {
                    eager = Boolean.parseBoolean(value.content);
                }
            }));
        }
        if (elementPort instanceof AdvanceConstantPort) {
            invoke(((AdvanceConstantPort)elementPort).value);
        } else if (eagerPort != null) {
            addCloseable(Reactive.observeOn(sizePort, scheduler()).register(new InvokeObserver<XElement>() {

                @Override
                public void next(XElement value) {
                    invoke(value);
                }
            }));
        }
        return new RunObserver();
    }
    
    /**
     * Invoke the computation.
     * @param element the element value
     */
    private void invoke(XElement element) {
        if (actualSize == maxSize) {
            elements.poll();
            actualSize--;
        }
        elements.add(element);
        actualSize++;
        if (maxSize == actualSize || eager) {
            dispatch(OUT, AdvanceData.create(elements));
        }
    }
    
    @Override
    protected void invoke() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
