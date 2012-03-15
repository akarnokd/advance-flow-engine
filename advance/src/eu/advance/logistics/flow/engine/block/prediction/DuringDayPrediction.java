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

import com.google.common.collect.Maps;
import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.util.Base64;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.prediction.support.MLModel;
import eu.advance.logistics.prediction.support.MLPrediction;
import hu.akarnokd.reactive4java.reactive.Observer;
import java.util.Map;

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
    @Input("advance:collection<advance:consignment>")
    protected static final String CONSIGNMENT = "consignments";
    /**
     * Previous trained model (optional, not used now).
     */
    @Input("duringdaymodel")
    protected static final String TRAINED_MODEL = "trainedModel";
    /**
     * Forecast.
     */
    @Output("advance:map<advance:string,advance:real>")
    protected static final String FORECAST = "forecast";
    
    /**
     * Provides the list of selected attributes.
     */
    private SelectedAttributesProviderImpl selectedAttributesProvider = new SelectedAttributesProviderImpl();

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    protected void invoke() {
        final MLPrediction prediction = new MLPrediction();
        try {
            MLModel model = fromXml(resolver(), get(TRAINED_MODEL));
            prediction.init(model);
        } catch (Exception ex) {
            LOG.error(null, ex);
        }
        getInput(CONSIGNMENT).register(new Observer<XElement>() {

            @Override
            public void next(XElement value) {
                process(prediction, value);
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
     * Trains the model using the consignment.
     *
     * @param prediction the model
     * @param x the XML representation of the consignment
     */
    private void process(MLPrediction prediction, XElement x) {
        try {
            Consignment c = Consignment.parse(x);
            if (c.id != -1) {
                prediction.process(new ConsignmentAccessorImpl(c));
            } else {
                Map<String, Double> results = prediction.done();
                Map<XElement, XElement> forecast = Maps.newHashMap();
                for (Map.Entry<String, Double> e : results.entrySet()) {
                    forecast.put(resolver().create(e.getKey()),
                            resolver().create(e.getValue()));
                }
                dispatch(FORECAST, forecast);
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
     * @return
     */
    private MLModel fromXml(DataResolver<XElement> resolver, XElement root) throws Exception {
        DuringDayConfigData cfg = DuringDayConfigData.parse(resolver, root.childElement("config"));
        MLModel model = new MLModel();
        model.config = cfg.createTestSet(null);

        XElement classifiers = root.childElement("classifiers");
        model.classifiers = Maps.newHashMap();
        for (XElement classifier : classifiers.children()) {
            model.classifiers.put(classifier.get("name"), Base64.decode(classifier.content));
        }

        selectedAttributesProvider.load(root.childElement("attributes-set"));

        return model;
    }
}
