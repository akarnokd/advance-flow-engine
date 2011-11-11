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
package eu.advance.logistics.flow.engine.block.demo;

import java.util.Map;
import java.util.Random;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 *.
 * @author szmarcell
 */
@Block(description = "Creates a new half pallet with random destination once the trigger arrives.", category = "demo")
public class HalfPalletDispenser extends AdvanceBlock {
	/** The logger. */
    protected static final Logger LOGGER = Logger.getLogger(HalfPalletDispenser.class.getName());
    /** Trigger the block. */
    @Input("advance:object")
    protected static final String TRIGGER = "trigger";
    /** Out. */
    @Output("advance:half-pallet")
    protected static final String HALF_PALLET = "half-pallet";
    /** The random number generator. */
    protected ThreadLocal<Random> rnd = new ThreadLocal<Random>() {
    	@Override
    	protected Random initialValue() {
    		return new Random();
    	}
    };
	/**
	 * Constructor.
	 * @param settings the block settings
	 */
	public HalfPalletDispenser(AdvanceBlockSettings settings) {
		super(settings);
	}
    @Override
    protected void invoke(Map<String, XElement> map) {
    	DemoDatastore ds = DemoDatastore.instance();
    	int n = ds.getMaxDestinations();
    	XElement fullPallet = DemoTypes.createHalfPallet(rnd.get().nextInt(n));
    	dispatch(HALF_PALLET, fullPallet);
    }
    
}
