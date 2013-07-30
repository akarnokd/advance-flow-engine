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

import hu.akarnokd.utils.xml.XNElement;

/**
 * Flag represented by consignment and type.
 *
 * @author TTS
 */
public class Flag {
	/** Unique flag id. */
    int id;
    /** Reference to the flag type. */
    int type;
    /** Reference to the consignment id. */
    int consignmentId;
    
    /**
     * Convert to XML element.
     * @param name name of the XML element
     * @return XML representation
     */
    public XNElement toXML(String name) {
        XNElement x = new XNElement(name);
        x.set("id", id);
        x.set("type", type);
        x.set("consignmentId", consignmentId);
        return x;
    }

    /**
     * Creates a new flag object from an XML representation.
     * @param x XML element
     * @return new flag object
     */
    public static Flag parse(XNElement x) {
        Flag f = new Flag();
        f.id = Integer.parseInt(x.get("id"));
        f.type = Integer.parseInt(x.get("type"));
        f.consignmentId = Integer.parseInt(x.get("consignmentId"));
        return f;
    }
}
