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

import com.google.common.collect.Lists;
import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.prediction.support.IntGenerator;
import eu.advance.logistics.prediction.support.SourceSeries;
import eu.advance.logistics.prediction.support.TestSet;
import eu.advance.logistics.prediction.support.WekaClassifierName;
import eu.advance.logistics.prediction.support.attributes.CumulativeSeriesAttributes;
import eu.advance.logistics.prediction.support.attributes.SelectedAttributes;
import eu.advance.logistics.prediction.support.attributes.SelectedAttributesProvider;
import eu.advance.logistics.prediction.support.collections.CollectionUtils;
import eu.advance.logistics.prediction.support.collections.TreeMapStrStr;
import eu.advance.logistics.prediction.support.utils.Time;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 *
 * @author TTS
 */
public class DuringDayConfigData {

    /**
     * Target type: NbRemaining, Total, FractionEntered.
     */
    public String targetType;
    /**
     * List of target depots IDs.
     */
    public String[] targetDepotIDs;
    /**
     * Weka classifier class.
     */
    public String wekaClassifier;
    /**
     * Value type: pallets, size, (consignments).
     */
    public String valueType;
    /**
     * Normalise.
     */
    public boolean normalise = true;
    /**
     * Duration type: fold or fold+time.
     */
    public boolean durationIsFoldTime;
    /**
     * Min entered date.
     */
    public Date minEnteredDate;
    /**
     * Max entered date.
     */
    public Date maxEnteredDate;
    /**
     * List of events (first is the main event).
     */
    public String[] events;

    /**
     * Parse from XML.
     *
     * @param r data resolver
     * @param x XML element
     * @throws ParseException if unable to convert dates
     * @return configuration from XML description
     */
    public static DuringDayConfigData parse(DataResolver<XElement> r, XElement x) throws ParseException {
        DuringDayConfigData ddc = new DuringDayConfigData();
        ddc.targetType = x.get("targetType");
        ddc.wekaClassifier = x.get("wekaClassifier");
        ddc.valueType = x.get("valueType");
        ddc.normalise = x.getBoolean("normalise");
        ddc.durationIsFoldTime = x.getBoolean("durationIsFoldTime");
        ddc.minEnteredDate = r.getTimestamp(x.childElement("minEnteredDate"));
        ddc.maxEnteredDate = r.getTimestamp(x.childElement("maxEnteredDate"));
        ddc.targetDepotIDs = adapt(r, x.childElement("targetDepotIDs"));
        ddc.events = adapt(r, x.childElement("events"));
        return ddc;
    }

    /**
     * Convert to XML.
     *
     * @param r data resolver
     * @param name the name of the XML element
     * @return the XML representation
     */
    public XElement toXml(DataResolver<XElement> r, String name) {
        XElement x = new XElement(name);
        x.set("targetType", targetType);
        x.set("wekaClassifier", wekaClassifier);
        x.set("valueType", valueType);
        x.set("normalise", normalise);
        x.set("durationIsFoldTime", durationIsFoldTime);
        x.add(new XElement("minEnteredDate", r.create(minEnteredDate)));
        x.add(new XElement("maxEnteredDate", r.create(maxEnteredDate)));
        x.add(adapt(r, targetDepotIDs, "targetDepotIDs"));
        x.add(adapt(r, events, "events"));
        return x;
    }

    /**
     * Adapt a string array to XML.
     *
     * @param r data resolver
     * @param values array of string
     * @param name name of the element
     * @return representation of the string array
     */
    private static XElement adapt(DataResolver<XElement> r, String[] values, String name) {
        List<XElement> list = new ArrayList<XElement>();
        for (String value : values) {
            list.add(r.create(value));
        }
        return new XElement(name, r.create(list));
    }

    /**
     * Adapt an XML element to a string array.
     *
     * @param r data resolver
     * @param x XML element
     * @return the string array
     */
    private static String[] adapt(DataResolver<XElement> r, XElement x) {
        List<XElement> list = r.getList(x);
        String[] values = new String[list.size()];
        for (int i = 0; i < list.size(); i++) {
            values[i] = r.getString(list.get(i));
        }
        return values;
    }

    /**
     * Create a TestSet using the current configuration.
     *
     * @param selectedAttributesProvider provides the selected attributes set
     * @return the configuration for a ML model
     */
    TestSet createTestSet(SelectedAttributesProvider selectedAttributesProvider) {
        TestSet ts = new TestSet();

        TestSet testSet = new TestSet();
        testSet.durationIsFoldTime = durationIsFoldTime;
        testSet.targetDepotIDs = targetDepotIDs;
        testSet.wekaClassifier = WekaClassifierName.getOrCreate(wekaClassifier);
        testSet.targetType = TestSet.getTargetType(targetType);
        testSet.valueType = TestSet.getValueType(valueType);

        // Create primary (and only) series, pointing towards the choice of event
        testSet.primarySeries = new SourceSeries();
        testSet.primarySeries.attributeFlags = CumulativeSeriesAttributes.Flag_DateTime | CumulativeSeriesAttributes.Flag_Target;
        testSet.primarySeries.eventName = events[0];
        testSet.primarySeries.attributes = selectedAttributesProvider.createDefault();

        boolean includeEmbedded = false;
        Map<String, SelectedAttributes> attributesMap = selectedAttributesProvider.load();

        testSet.secondarySeries = new SourceSeries[events.length - 1];
        for (int i = 1; i < events.length; i++) {
            TreeMapStrStr lookupKey = new TreeMapStrStr();
            lookupKey.put("Cls", testSet.wekaClassifier.shortName);
            lookupKey.put("Duration", testSet.DurationStr());
            lookupKey.put("MultiDepot", testSet.MultiDepotStr());
            lookupKey.put("Event", events[i]);
            lookupKey.put("ValueType", valueType.toString());

            SourceSeries series = new SourceSeries();
            testSet.secondarySeries[i] = series;
            System.out.println("get attributes for " + lookupKey);
            series.attributes = attributesMap.get(lookupKey.toString());
            if (series.attributes == null) {
                System.out.println("Using defaults attributes!");
                series.attributes = selectedAttributesProvider.createDefault();
            }
            series.eventName = events[i];
            series.attributePrefix = events[i].substring(0, 1);
            series.attributeFlags = CumulativeSeriesAttributes.Flag_RefSeriesWaiting;
            if (includeEmbedded) {
                series.attributeFlags |= CumulativeSeriesAttributes.Flag_RefSeriesRemainingPredictor;
                series.embeddedPredictorFlags = CumulativeSeriesAttributes.Flag_DateTime | CumulativeSeriesAttributes.Flag_Target;
            }
        }



        // Init the generators for test and training times
        int[] testTimes = Time.IntTimeToMinsInDay(new int[]{1200, 1300, 1400, 1500, 1600, 1700, 1800, 1900, 2000});
        if (durationIsFoldTime) {
            testSet.minsInDay = IntGenerator.CreateReturnArray(testTimes);
        } else {
            int minsBuffer = 60;
            int min = CollectionUtils.Min(testTimes) - minsBuffer;
            int max = CollectionUtils.Max(testTimes) + minsBuffer;
            testSet.minsInDay = IntGenerator.CreateGenerateUniqueRandomWithinRange(min, max, testTimes.length);
        }
        // disable to reduce memory usage
        //testSet._testSetMinsInDayGenerator = IntGenerator.CreateReturnArray(TestTimes);

        return ts;
    }

    /**
     * Convert to XML.
     *
     * @param r data resolver
     * @param name the name of the XML element
     * @param config the test-set configuration
     * @return the XML representation
     */
    public static XElement toXml(DataResolver<XElement> r, String name, TestSet config) {
        XElement x = new XElement(name);
        x.set("targetType", config.targetType.name());
        x.set("wekaClassifier", config.wekaClassifier.className);
        x.set("valueType", config.valueType.name());
        x.set("normalise", config.normalise);
        x.set("durationIsFoldTime", config.durationIsFoldTime);
        x.add(new XElement("minEnteredDate", r.create(config.minEnteredDate)));
        x.add(new XElement("maxEnteredDate", r.create(config.maxEnteredDate)));
        x.add(adapt(r, config.targetDepotIDs, "targetDepotIDs"));
        List<String> eventNames = Lists.newArrayList();
        eventNames.add(config.primarySeries.eventName);
        for (SourceSeries ss : config.secondarySeries) {
            eventNames.add(ss.eventName);
        }
        x.add(adapt(r, eventNames.toArray(new String[eventNames.size()]), "events"));
        return x;
    }
}