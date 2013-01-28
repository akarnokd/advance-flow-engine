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

import java.util.Date;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Filter a consignment.
 *
 * @author TTS
 */
@Block(id = "ConsignmentFilter", category = "prediction", scheduler = "IO",
description = "Filters consignment")
public class ConsignmentFilter extends AdvanceBlock {

    /**
     * Minimum date used to filter events.
     */
    @Input("advance:timestamp")
    protected static final String DATE_AFTER = "dateAfter";
    /**
     * Maximum date used to filter events.
     */
    @Input("advance:timestamp")
    protected static final String DATE_BEFORE = "dateBefore";
    /**
     * Stream of input consignment. The end of the stream is marked with a
     * consignment with id = -1.
     */
    @Input("advance:consignment")
    protected static final String IN = "in";
    /**
     * Stream of filtered consignment. The end of the stream is marked with a
     * consignment with id = -1.
     */
    @Output("advance:consignment")
    protected static final String OUT = "out";

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    protected void invoke() {
        // called on trigger
    }

    @Override
    public Observer<Void> run() {
        addCloseable(Reactive.observeOn(getInput(IN), scheduler()).register(new Observer<XElement>() {

            @Override
            public void next(XElement value) {
                try {
                    process(value);
                } catch (Exception ex) {
                    log(ex);
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
     * Process and filter a stream.
     * @param x XML element to process
     * @throws Exception if unable to parse dates
     */
    private void process(XElement x) throws Exception {
        Date dateAfter = getTimestamp(DATE_AFTER);
        Date dateBefore = getTimestamp(DATE_BEFORE);
        Consignment c = Consignment.parse(x);
        if (c != null) {
            if (dateAfter != null) {
                for (Event e : c.events) {
                    if (e.timestamp.before(dateAfter)) {
                        return;
                    }
                }
            }
            if (dateBefore != null) {
                for (Event e : c.events) {
                    if (e.timestamp.after(dateBefore)) {
                        return;
                    }
                }
            }
            dispatch(OUT, x);
        }
    }
}
