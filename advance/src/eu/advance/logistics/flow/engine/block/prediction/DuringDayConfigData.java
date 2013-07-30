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

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.runtime.DataResolver;
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
     * @param x XML element
     * @throws ParseException if unable to convert dates
     */
    public void parse(XNElement x) throws ParseException {
        targetType = x.get("targetType");
        wekaClassifier = x.get("wekaClassifier");
        valueType = x.get("valueType");
        normalise = x.getBoolean("normalise");
        durationIsFoldTime = x.getBoolean("durationIsFoldTime");
        minEnteredDate = XNElement.parseDateTime(x.childElement("minEnteredDate").content);
        maxEnteredDate = XNElement.parseDateTime(x.childElement("maxEnteredDate").content);
        targetDepotIDs = adapt(x.childElement("targetDepotIDs"));
        events = adapt(x.childElement("events"));
    }

    /**
     * Convert to XML.
     *
     * @param name the name of the XML element
     * @return the XML representation
     */
    public XNElement toXml(String name) {
        XNElement x = new XNElement(name);
        x.set("targetType", targetType);
        x.set("wekaClassifier", wekaClassifier);
        x.set("valueType", valueType);
        x.set("normalise", normalise);
        x.set("durationIsFoldTime", durationIsFoldTime);
        x.add(new XNElement("minEnteredDate", XNElement.formatDateTime(minEnteredDate)));
        x.add(new XNElement("maxEnteredDate", XNElement.formatDateTime(maxEnteredDate)));
        x.add(adapt(targetDepotIDs, "targetDepotIDs"));
        x.add(adapt(events, "events"));
        return x;
    }

    /**
     * Adapt a string array to XML.
     *
     * @param values array of string
     * @param name name of the element
     * @return representation of the string array
     */
    private static XNElement adapt(String[] values, String name) {
        XNElement x = new XNElement(name);
        for (String value : values) {
            x.add(new XNElement("item", value));
        }
        return x;
    }

    /**
     * Adapt an XML element to a string array.
     *
     * @param x XML element
     * @return the string array
     */
    private static String[] adapt(XNElement x) {
        List<String> values = Lists.newArrayList();
        for (XNElement item : x.childrenWithName("item")) {
            values.add(item.content);
        }
        return values.toArray(new String[values.size()]);
    }

    /**
     * Parse from XML.
     *
     * @param r data resolver
     * @param x XML element
     * @throws ParseException if unable to convert dates
     * @return configuration from XML description
     */
//    public static DuringDayConfigData parse(XNElement root)  {
//        ByteArrayDataInput in = ByteStreams.newDataInput(Base64.decode(root.content));
//        return read(in);
//        return root.get();
//    }

    /**
     * Parse from XML.
     *
     * @param r data resolver
     * @param x XML element
     * @throws ParseException if unable to convert dates
     * @return configuration from XML description
     */
//    public static DuringDayConfigData read(ByteArrayDataInput in) {
//        DuringDayConfigData ddc = new DuringDayConfigData();
//        ddc.targetType = in.readUTF();
//        ddc.wekaClassifier = in.readUTF();
//        ddc.valueType = in.readUTF();
//        ddc.normalise = in.readBoolean();
//        ddc.durationIsFoldTime = in.readBoolean();
//        ddc.minEnteredDate = new Date(in.readLong());
//        ddc.maxEnteredDate = new Date(in.readLong());
//        int n = in.readInt();
//        ddc.targetDepotIDs = new String[n];
//        for (int i = 0; i < n; i++) {
//            ddc.targetDepotIDs[i] = in.readUTF();
//        }
//        n = in.readInt();
//        ddc.events = new String[n];
//        for (int i = 0; i < n; i++) {
//            ddc.events[i] = in.readUTF();
//        }
//        return ddc;
//    }

//    public XNElement toXml() {
//        XNElement root = new XNElement("DuringDayConfig");
//        ByteArrayDataOutput out = ByteStreams.newDataOutput();
//        write(out);
//        root.content = Base64.encodeBytes(out.toByteArray());
//        root.set(this);
//        return root;
//    }

    /**
     * Convert to XML.
     *
     * @param r data resolver
     * @param name the name of the XML element
     * @return the XML representation
     */
    /*
    public void write(ByteArrayDataOutput out) {
        out.writeUTF(targetType);
        out.writeUTF(wekaClassifier);
        out.writeUTF(valueType);
        out.writeBoolean(normalise);
        out.writeBoolean(durationIsFoldTime);
        out.writeLong(minEnteredDate.getTime());
        out.writeLong(maxEnteredDate.getTime());
        if (targetDepotIDs == null) {
            out.writeInt(0);
        } else {
            out.writeInt(targetDepotIDs.length);
            for (String s : targetDepotIDs) {
                out.writeUTF(s);
            }
        }
        if (events == null) {
            out.writeInt(0);
        } else {
            out.writeInt(events.length);
            for (String s : events) {
                out.writeUTF(s);
            }
        }
    }*/

    /**
     * Create a TestSet using the current configuration.
     *
     * @param selectedAttributesProvider provides the selected attributes set
     * @return the configuration for a ML model
     */
    public TestSet createTestSet(SelectedAttributesProvider selectedAttributesProvider) {
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

        return testSet;
    }

    /**
     * Convert to XML.
     *
     * @param r data resolver
     * @param name the name of the XML element
     * @param config the test-set configuration
     * @return the XML representation
     */
    public static XNElement toXml(DataResolver<XNElement> r, String name, TestSet config) {
        XNElement x = new XNElement(name);
        x.set("targetType", config.targetType.name());
        x.set("wekaClassifier", config.wekaClassifier.className);
        x.set("valueType", config.valueType.name());
        x.set("normalise", config.normalise);
        x.set("durationIsFoldTime", config.durationIsFoldTime);
        x.add(new XNElement("minEnteredDate", r.create(config.minEnteredDate).content));
        x.add(new XNElement("maxEnteredDate", r.create(config.maxEnteredDate).content));
        x.add(adapt(config.targetDepotIDs, "targetDepotIDs"));
        List<String> eventNames = Lists.newArrayList();
        eventNames.add(config.primarySeries.eventName);
        for (SourceSeries ss : config.secondarySeries) {
            eventNames.add(ss.eventName);
        }
        x.add(adapt(eventNames.toArray(new String[eventNames.size()]), "events"));
        return x;
    }
//    public static void write(ByteArrayDataOutput out, TestSet config) {
//        DuringDayConfigData cfg = new DuringDayConfigData();
//        cfg.targetType = config.targetType.name();
//        cfg.wekaClassifier = config.wekaClassifier != null ? config.wekaClassifier.className : "null";
//        cfg.valueType = config.valueType.name();
//        cfg.normalise = config.normalise;
//        cfg.durationIsFoldTime = config.durationIsFoldTime;
//        cfg.minEnteredDate = config.minEnteredDate;
//        cfg.maxEnteredDate = config.maxEnteredDate;
//        cfg.targetDepotIDs = config.targetDepotIDs;
//        List<String> eventNames = Lists.newArrayList();
//        if (config.primarySeries != null) {
//            eventNames.add(config.primarySeries.eventName);
//        } else {
//            eventNames.add("no_primary_series");
//        }
//        if (config.secondarySeries != null) {
//            for (SourceSeries ss : config.secondarySeries) {
//                eventNames.add(ss.eventName);
//            }
//        }
//        cfg.events = eventNames.toArray(new String[eventNames.size()]);
//        cfg.write(out);
//    }
}
