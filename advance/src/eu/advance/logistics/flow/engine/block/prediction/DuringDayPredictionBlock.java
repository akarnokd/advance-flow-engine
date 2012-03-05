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

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;

/**
 * During day prediction block.
 * @author TTS
 */
@Block(id = "DuringDayPrediction", category = "prediction", scheduler = "IO",
description = "During-day prediction algorithm.")
public class DuringDayPredictionBlock extends AdvanceBlock {

    /**
     * In.
     */
    @Input("advance:collection<advance:consignment>")
    protected static final String IN1 = "consignments";
    /**
     * In.
     */
    @Input("advance:object")
    protected static final String IN2 = "trainedModel";
    /**
     * Out.
     */
    @Output("advance:integer")
    protected static final String OUT = "pallets";

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    protected void invoke() {
         getInput(IN1).register(new Observer<XElement>() {

            @Override
            public void next(XElement value) {
                process(value);
            }

            @Override
            public void error(Throwable ex) {
            }

            @Override
            public void finish() {
            }
        });
    }
    
    /**
     * Process a consignment.
     * @param x 
     */
    private void process(XElement x) {
        Consignment c = null;
        try {
            c = Consignment.parse(x);
        } catch (Exception ex) {
            // TODO
        }
        if (c != null) {
            // feed to model and get result
            int forecast = 0;
            dispatch(OUT, resolver().create(forecast));
        }
    }
}
