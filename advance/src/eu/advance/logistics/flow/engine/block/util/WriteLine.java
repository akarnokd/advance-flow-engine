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
package eu.advance.logistics.flow.engine.block.util;

import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;

/**
 * Writes the incoming value to the console for debugging purposes. Signature:
 * WriteLine(t)
 *
 * @author TTS
 */
@Block(id = "WriteLine", category = "string", scheduler = "IO", description = "Writes the incoming value to the console for debugging purposes")
public class WriteLine extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(WriteLine.class.getName());
    /**
     * In.
     */
    @Input("advance:object")
    protected static final String IN = "in";

    @Override
    protected void invoke() {
        System.out.println(resolver().getString(get(IN)));
    }
}
