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
import hu.akarnokd.utils.Base64;
import hu.akarnokd.utils.xml.XNElement;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.Maps;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.AdvanceBlock;
import eu.advance.logistics.prediction.support.MLModel;
import eu.advance.logistics.prediction.support.MLPrediction;

/**
 * During day prediction block.
 *
 * @author TTS
 */
@Block(id = "DuringDayPrediction", category = "prediction", scheduler = "IO",
description = "During-day prediction algorithm.")
public class DuringDayPrediction extends AdvanceBlock {

    /**
     * Stream of consignments.
     */
    @Input("advance:consignment")
    protected static final String CONSIGNMENT = "consignments";
    /**
     * Targer date.
     */
    @Input("advance:timestamp")
    protected static final String TARGET_DATE = "targetDate";
    /**
     * Previous trained model (optional, not used now).
     */
    @Input("advance:duringdaymodel")
    protected static final String TRAINED_MODEL = "trainedModel";
    /**
     * Forecast.
     */
    @Output("advance:map<advance:string,advance:real>")
    protected static final String FORECAST = "forecast";
    /**
     * Number of consignment.
     */
    @Output("advance:integer")
    protected static final String COUNTER = "counter";
    /**
     * Provides the list of selected attributes.
     */
    private SelectedAttributesProviderImpl selectedAttributesProvider = new SelectedAttributesProviderImpl();
    /** The prediction. */
    private MLPrediction prediction;

    @Override
    public Observer<Void> run() {
        getInput(TRAINED_MODEL).register(new Observer<XNElement>() {
            @Override
            public void next(XNElement value) {
                LOG.info("Reading prediction model...");
                prediction = new MLPrediction();
                try {
                    MLModel model = fromXml(value);

                    Date targetDate = getTimestamp(TARGET_DATE);
                    prediction.setTargetDate(targetDate);
                    prediction.setTargetDepotIds(model.config.targetDepotIDs);
                    prediction.init(model);

                    LOG.info("Target date: " + prediction.getTargetDate());
                    LOG.info("Target depots: " + Arrays.toString(prediction.getTargetDepotIds()));

                } catch (Exception ex) {
                    LOG.error(null, ex);
                }
                LOG.info("Prediction model: " + prediction);
            }

            @Override
            public void error(Throwable ex) {
            }

            @Override
            public void finish() {
            }
        });
        getInput(CONSIGNMENT).register(new Observer<XNElement>() {
            private int counter;
            private long lastTime;

            @Override
            public void next(XNElement value) {
                if (prediction != null) {
                    process(value);
                    counter++;
                }
                long time = System.currentTimeMillis();
                if (time - lastTime > 2000) {
                    dispatch(COUNTER, resolver().create(counter));
                    lastTime = time;
                }
            }

            @Override
            public void error(Throwable ex) {
            }

            @Override
            public void finish() {
            }
        });
        return new RunObserver();
    }

    @Override
    protected void invoke() {
    }

    /**
     * Trains the model using the consignment.
     *
     * @param x the XML representation of the consignment
     */
    private void process(XNElement x) {
        try {
            Consignment c = Consignment.parse(x);
            if (c.id != -1) {
                prediction.process(new ConsignmentAccessorImpl(c));
            } else {
                Map<String, Double> results = prediction.done();
                Map<XNElement, XNElement> forecast = Maps.newHashMap();
                for (Map.Entry<String, Double> e : results.entrySet()) {
                    forecast.put(resolver().create(e.getKey()),
                            resolver().create(e.getValue()));
                }
                dispatch(FORECAST, resolver().create(forecast));
                prediction = null;
            }
        } catch (Exception ex) {
            LOG.error(null, ex);
        }
    }

    /**
     * Creates the model from XML.
     *
     * @param resolver used to resolve data to XML
     * @param root the root XML element
     * @return the model
     * @throws Exception if unable to convert base64 string
     */
    /**
     * Creates the model from XML.
     *
     * @param root the root XML element
     * @return the model
     * @throws Exception if unable to convert base64 string
     */
    private MLModel fromXml(XNElement root) throws Exception {
        MLModel model = new MLModel();

        DuringDayConfigData cfg = new DuringDayConfigData();
        cfg.parse(root.childElement("config"));

        selectedAttributesProvider.load(root.childElement("attributes-set"));
        model.config = cfg.createTestSet(selectedAttributesProvider);

        XNElement classifiers = root.childElement("classifiers");
        model.classifiers = Maps.newHashMap();
        for (XNElement classifier : classifiers.children()) {
            model.classifiers.put(classifier.get("name"), Base64.decode(classifier.content));
        }

        return model;
    }
}
