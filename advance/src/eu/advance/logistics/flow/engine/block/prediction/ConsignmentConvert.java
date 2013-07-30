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
import hu.akarnokd.utils.xml.XNElement;

import java.util.Map;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;

/**
 * Convert a database row into a consignment.
 * @author TTS
 */
@Block(id = "ConsignmentConvert", category = "prediction", scheduler = "IO",
description = "Convert a database row into a consignment")
public class ConsignmentConvert extends AdvanceBlock {

    /**
     * A single database row.
     */
    @Input("advance:map<advance:string,advance:object>")
    protected static final String IN = "row";
    /**
     * A consignment object.
     */
    @Output("advance:consignment")
    protected static final String OUT = "consignment";

    @Override
    public void init(BlockSettings<XNElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    protected void invoke() {
        getInput(IN).register(new Observer<XNElement>() {

            @Override
            public void next(XNElement value) {
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
     * Gets a consignment for a database row. 
     * @param x the XML representation of a row
     */
    private void process(XNElement x) {
        Map<XNElement, XNElement> data = resolver().getMap(x);
        Consignment c = new Consignment();
        c.id = getInt(data, "ConsignmentId");
        c.hubId = getInt(data, "Hub");
        c.collectionDepotId = getInt(data, "CollectionDepot");
        c.collectionLocationId = getInt(data, "CollectionLocation");
        c.deliveryDepotId = getInt(data, "DeliveryDepot");
        c.deliveryLocationId = getInt(data, "DeliveryLocation");
        c.payingDepotId = getInt(data, "PayingDepot");
        c.palletCount = getInt(data, "PalletCount");
        c.weight = getInt(data, "Weight");
        c.number = getString(data, "Number");
        c.volume = getDouble(data, "Volume");
        dispatch(OUT, c.toXML("Consignment"));
    }
    
    /**
     * Gets an integer value from a table row.
     * @param data table row
     * @param key table column name
     * @return the value
     */
    private int getInt(Map<XNElement, XNElement> data, String key) {
        return resolver().getInt(data.get(resolver().create(key)));
    }

    /**
     * Gets a double value from a table row.
     * @param data table row
     * @param key table column name
     * @return the value
     */
    private double getDouble(Map<XNElement, XNElement> data, String key) {
        return resolver().getDouble(data.get(resolver().create(key)));
    }
    
    
    /**
     * Gets a string value from a table row.
     * @param data table row
     * @param key table column name
     * @return the value
     */
    private String getString(Map<XNElement, XNElement> data, String key) {
        return resolver().getString(data.get(resolver().create(key)));
    }
}
