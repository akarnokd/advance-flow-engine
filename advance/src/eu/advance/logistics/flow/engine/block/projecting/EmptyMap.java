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
package eu.advance.logistics.flow.engine.block.projecting;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.xml.XElement;
import hu.akarnokd.reactive4java.reactive.Observer;
import hu.akarnokd.reactive4java.reactive.Reactive;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * Returns an empty collection. Signature: EmptyMap(trigger) -> map<t, u>
 *
 * @author TTS
 */
@Block(id = "EmptyMap", 
	category = "projection", 
	scheduler = "IO", 
	description = "Returns an empty collection", 
	parameters = { "K", "V" }
)
public class EmptyMap extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(EmptyMap.class.getName());
    /**
     * In.
     */
    @Input("advance:boolean")
    protected static final String TRIGGER = "trigger";
    /**
     * Out.
     */
    @Output("advance:map<?K, ?V>")
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
                    dispatch(OUT, resolver().create(new HashMap<XElement, XElement>()));
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
}
