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

import hu.akarnokd.utils.Base64;

import java.io.IOException;
import java.util.logging.Logger;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;

/**
 * Convert the string into a Base64 representation. Signature:
 * EncodeBase64(string) -> string
 *
 * @author TTS
 */
@Block(id = "EncodeBase64", category = "string", scheduler = "NOW", description = "Convert the string into a Base64 representation")
public class EncodeBase64 extends AdvanceBlock {

    /**
     * The logger.
     */
    protected static final Logger LOGGER = Logger.getLogger(EncodeBase64.class.getName());
    /**
     * In.
     */
    @Input("advance:string")
    protected static final String IN = "in";
    /**
     * Out.
     */
    @Output("advance:string")
    protected static final String OUT = "out";

    @Override
    protected void invoke() {
        try {
            dispatch(OUT, resolver().create(Base64.encodeBytes(getString(IN).getBytes("ISO-8859-1"))));
        } catch (IOException ex) {
            log(ex);
        }
    }
}
