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
import eu.advance.logistics.flow.engine.xml.XElement;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * During day prediction configuration.
 * @author TTS
 */
@Block(id = "DuringDayPredictionConfig",
category = "prediction",
scheduler = "IO",
description = "Configuration for during day prediction.")
public class DuringDayPredictionConfig extends AdvanceBlock {
    /**
     * Target type: NbRemaining, Total, FractionEntered.
     */
    @Input("advance:string")
    protected static final String TARGET_TYPE = "targetType";
    /**
     * List of target depots IDs.
     */
    @Input("advance:collection<advance:string>")
    protected static final String TARGET_DEPOT_IDS = "targetDepotIds";
    /**
     * Weka classifier class.
     */
    @Input("advance:string")
    protected static final String WEKA_CLASSIFIER = "wekaClassifier";
    /**
     * Value type: pallets, size, (consignments).
     */
    @Input("advance:string")
    protected static final String VALUE_TYPE = "valueType";
    /**
     * Normalise.
     */
    @Input("advance:boolean")
    protected static final String NORMALISE = "normalise";
    /**
     * Duration type: fold or fold+time.
     */
    @Input("advance:boolean")
    protected static final String DURATION = "durationIsFoldTime";
    /**
     * Min entered date.
     */
    @Input("advance:date")
    protected static final String MIN_ENTERED_DATE = "minEnteredDate";
    /**
     * Max entered date.
     */
    @Input("advance:date")
    protected static final String MAX_ENTERED_DATE = "maxEnteredDate";
    /**
     * List of events (first is the main event).
     */
    @Input("advance:collection<advance:string>")
    protected static final String EVENTS = "events";
    /**
     * Out.
     */
    @Output("advance:duringdayconfig")
    protected static final String CONFIG = "config";

    @Override
    protected void invoke() {
        DuringDayConfig cfg = new DuringDayConfig();
        cfg.targetType = getString(TARGET_TYPE);
        cfg.targetDepotIDs = getStringArray(get(TARGET_DEPOT_IDS));
        cfg.wekaClassifier = getString(WEKA_CLASSIFIER);
        cfg.valueType = getString(VALUE_TYPE);
        cfg.normalise = getBoolean(NORMALISE);
        cfg.durationIsFoldTime = getBoolean(DURATION);
        try {
            cfg.minEnteredDate = getTimestamp(MIN_ENTERED_DATE);
        } catch (ParseException ex) {
            LOG.error(null, ex);
        }
        try {
            cfg.maxEnteredDate = getTimestamp(MAX_ENTERED_DATE);
        } catch (ParseException ex) {
            LOG.error(null, ex);
        }
        cfg.events = getStringArray(get(EVENTS));
    }

    /**
     * Get a string array form a XML element representing a collection.
     * @param collection the XML element
     * @return the string array
     */
    private String[] getStringArray(XElement collection) {
        List<String> names = new ArrayList<String>();
        for (XElement e : resolver().getItems(collection)) {
            names.add(resolver().getString(e));
        }
        return names.toArray(new String[names.size()]);
    }
}
