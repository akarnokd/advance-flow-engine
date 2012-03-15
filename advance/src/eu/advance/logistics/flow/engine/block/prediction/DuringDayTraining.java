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
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.util.Base64;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.prediction.support.MLModel;
import eu.advance.logistics.prediction.support.MLModelTraining;
import eu.advance.logistics.prediction.support.TestSet;
import hu.akarnokd.reactive4java.reactive.Observer;
import java.text.ParseException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * During day training block.
 *
 * @author TTS
 */
@Block(id = "DuringDayTraining", category = "prediction", scheduler = "IO",
description = "Training for during-day prediction algorithm.")
public class DuringDayTraining extends AdvanceBlock {

    /**
     * Stream of consignments.
     */
    @Input("advance:collection<advance:consignment>")
    protected static final String CONSIGNMENT = "consignments";
    /**
     * Previous trained model (optional, not used now).
     */
    @Input("duringdaymodel")
    protected static final String PREVIOUS_MODEL = "previousTrainedModel";
    /**
     * During day prediction configuration.
     */
    @Input("advance:duringdayconfig")
    protected static final String CONFIG = "config";
    /**
     * Selected attributes XML file (local data store).
     */
    @Input("advance:string")
    protected static final String SELECTED_ATTRIBUTES_FILE = "selectedAttributesFile";
    /**
     * During day trained model.
     */
    @Output("duringdaymodel")
    protected static final String TRAINED_MODEL = "trainedModel";
    /**
     * Mean Absolute Error of the trained model.
     */
    @Output("advance:real")
    protected static final String OUTPUT_MAE = "mae";
    /**
     * Symmetric Mean Absolute Percentage Error of the trained model.
     */
    @Output("advance:real")
    protected static final String OUTPUT_SMAPE = "smape";
    
    /**
     * Provides the list of selected attributes.
     */
    private SelectedAttributesProviderImpl selectedAttributesProvider;

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    protected void invoke() {
        DuringDayConfigData cfg = null;
        try {
            cfg = DuringDayConfigData.parse(resolver(), get(CONFIG));
        } catch (ParseException ex) {
            Logger.getLogger(DuringDayTraining.class.getName()).log(Level.SEVERE, null, ex);
        }
        selectedAttributesProvider = new SelectedAttributesProviderImpl();
        selectedAttributesProvider.loadFromLocalConnection(this, getString(SELECTED_ATTRIBUTES_FILE));
        TestSet testSet = cfg.createTestSet(selectedAttributesProvider);
        final MLModelTraining trainingBlock = new MLModelTraining(testSet);
        try {
            trainingBlock.init();
        } catch (Exception ex) {
            LOG.error(null, ex);
        }
        getInput(CONSIGNMENT).register(new Observer<XElement>() {

            @Override
            public void next(XElement value) {
                process(trainingBlock, value);
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
     * @param mt the model
     * @param x the XML representation of the consignment
     */
    private void process(MLModelTraining mt, XElement x) {
        try {
            Consignment c = Consignment.parse(x);
            if (c.id != -1) {
                mt.process(new ConsignmentAccessorImpl(c));
            } else {
                MLModel result = mt.done();
                dispatch(OUTPUT_MAE, resolver().create(result.mae));
                dispatch(OUTPUT_SMAPE, resolver().create(result.smape));
                dispatch(TRAINED_MODEL, toXml(result));
            }
        } catch (Exception ex) {
            LOG.error(null, ex);
        }
    }

    /**
     * Converts a model to XML.
     *
     * @param resolver used to resolve data to XML
     * @param model the model to convert
     * @return
     */
    private XElement toXml(MLModel model) {
        XElement root = new XElement("DuringDayModel");
        
        root.add(DuringDayConfigData.toXml(resolver(), "config", model.config));
        root.add(selectedAttributesProvider.toXml(resolver()));
        
        XElement classifiers = new XElement("classifiers");
        for (Map.Entry<String, byte[]> e : model.classifiers.entrySet()) {
            XElement classifier = new XElement("classifier");
            classifier.set("name", e.getKey());
            classifier.content = Base64.encodeBytes(e.getValue());
        }
        root.add(classifiers);
        
        return root;
    }
}
