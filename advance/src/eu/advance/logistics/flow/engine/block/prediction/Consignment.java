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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.xml.XElement;

/**
 * Consignment read from the database.
 *
 * @author TTS
 */
public class Consignment {

    /**
     * Unique consignment id.
     */
    public int id;
    /**
     * Id of the hub.
     */
    public int hubId;
    /**
     * Id of the collection depot.
     */
    public int collectionDepotId;
    /**
     * Id of the collection location.
     */
    public int collectionLocationId;
    /**
     * Id of the devlivery depot.
     */
    public int deliveryDepotId;
    /**
     * Id of the delivery location.
     */
    public int deliveryLocationId;
    /**
     * Id of the paying depot.
     */
    public int payingDepotId;
    /**
     * Number of pallets, i.e. "Lifts".
     */
    public int palletCount;
    /**
     * Weight of the consignment.
     */
    public double weight;
    /**
     * Consignment number (free text).
     */
    public String number;
    /**
     * Total volume of the consignment (computed as 1/4*Q+1/2*H+F).
     */
    public double volume;
    /**
     * Events associated to this consignment.
     */
    public List<Event> events = Lists.newArrayList();
    /**
     * Flags associated to this consignment.
     */
    public List<Flag> flags = Lists.newArrayList();

    /**
     * Serialize the class to XML.
     *
     * @param name the name of the element
     * @return an XElement representing the class
     */
    public XElement toXML(String name) {
        XElement x = new XElement(name);
        x.set("id", id);
        x.set("hubId", hubId);
        x.set("collectionDepotId", collectionDepotId);
        x.set("collectionLocationId", collectionLocationId);
        x.set("deliveryDepotId", deliveryDepotId);
        x.set("deliveryLocationId", deliveryLocationId);
        x.set("payingDepotId", payingDepotId);
        x.set("palletCount", palletCount);
        x.set("weight", weight);
        x.set("number", number);
        x.set("volume", volume);
        if (events != null) {
            for (Event e : events) {
                x.add(e.toXML("event"));
            }
        }
        if (flags != null) {
            for (Flag f : flags) {
                x.add(f.toXML("flag"));
            }
        }
        return x;
    }

    /**
     * Creates a new instance from an XML representation.
     *
     * @param x the XML element
     * @return the class represented by the element
     * @throws ParseException if the element cannot be parsed
     */
    public static Consignment parse(XElement x) throws ParseException {
        Consignment c = new Consignment();
        c.id = Integer.parseInt(x.get("id"));
        c.hubId = Integer.parseInt(x.get("hubId"));
        c.collectionDepotId = Integer.parseInt(x.get("collectionDepotId"));
        c.collectionLocationId = Integer.parseInt(x.get("collectionLocationId"));
        c.deliveryDepotId = Integer.parseInt(x.get("deliveryDepotId"));
        c.deliveryLocationId = Integer.parseInt(x.get("deliveryLocationId"));
        c.payingDepotId = Integer.parseInt(x.get("payingDepotId"));
        c.palletCount = Integer.parseInt(x.get("palletCount"));
        c.weight = Double.parseDouble(x.get("weight"));
        c.number = x.get("number");
        c.volume = Double.parseDouble(x.get("volume"));
        c.events = new ArrayList<Event>();
        for (XElement xe : x.childrenWithName("event")) {
            Event e = Event.parse(xe);
            if (e != null) {
                c.events.add(e);
            }
        }
        c.flags = new ArrayList<Flag>();
        for (XElement xe : x.childrenWithName("flag")) {
            Flag e = Flag.parse(xe);
            if (e != null) {
                c.flags.add(e);
            }
        }
        return c;
    }
}
