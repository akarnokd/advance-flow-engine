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

import eu.advance.logistics.prediction.support.ConsignmentAccessor;
import java.util.Date;

/**
 *
 * @author TTS
 */
/**
 * Internal class used to access the Consignment data.
 */
final class ConsignmentAccessorImpl implements ConsignmentAccessor {
    /** Underlying consignment. */
    private Consignment c;

    /**
     * Construct the accessor to the consignment.
     * @param c the object containing the data
     */
    ConsignmentAccessorImpl(Consignment c) {
        this.c = c;
    }

    @Override
    public int getId() {
        return c.id;
    }

    @Override
    public int getPalletCount() {
        return c.palletCount;
    }

    @Override
    public double getWeight() {
        return c.weight;
    }

    @Override
    public double getVolume() {
        return c.volume;
    }

    @Override
    public int getDeliveryDepotId() {
        return c.deliveryDepotId;
    }

    @Override
    public int getCollectionDepotId() {
        return c.collectionDepotId;
    }

    @Override
    public Date getEventDate(String string) {
        return null;
    }
    
}
