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

import eu.advance.logistics.flow.engine.xml.XElement;
import java.text.ParseException;
import java.util.Date;

/**
 * Event represented by consignment, type and date.
 * @author TTS
 */
public class Event {

	 /** Unique event id. */
    public int id;
    /** Reference to the consignment id. */
    public int consignmentId;
    /** Reference to the event type id. */
    public int eventType;
    /** Time stamp of the event. */
    public Date timestamp;
    /** Free text notes. */
    public String notes;

    /**
     * Convert to XML element.
     * @param name name of the XML element
     * @return XML representation
     */
    public XElement toXML(String name) {
        XElement x = new XElement(name);
        x.set("id", id);
        x.set("consignmentId", consignmentId);
        x.set("eventType", eventType);
        x.set("timestamp", XElement.formatDateTime(timestamp));
        x.set("notes", notes);
        return x;
    }

    /**
     * Creates a new event object from an XML representation.
     * @param x XML element
     * @return new event object
     * @throws ParseException exception while parsing date
     */
    public static Event parse(XElement x) throws ParseException {
        Event e = new Event();
        e.id = Integer.parseInt(x.get("id"));
        e.consignmentId = Integer.parseInt(x.get("consignmentId"));
        e.eventType = Integer.parseInt(x.get("eventType"));
        e.timestamp = XElement.parseDateTime(x.get("timestamp"));
        e.notes = x.get("notes");
        return e;
    }
}
