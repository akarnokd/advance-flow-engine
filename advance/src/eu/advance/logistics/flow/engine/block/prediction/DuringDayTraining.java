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

import hu.akarnokd.reactive4java.reactive.Observer;

import java.util.Map;

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
    @Input("advance:consignment")
    protected static final String CONSIGNMENT = "consignments";
    /**
     * Previous trained model (optional, not used now).
     */
    @Input("advance:duringdaymodel")
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
    @Output("advance:duringdaymodel")
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
    /** The configuration. */
    private DuringDayConfigData duringDayConfig;
    /** Is finished? */
    private boolean finished;

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    public Observer<Void> run() {
        getInput(CONFIG).register(new Observer<XElement>() {
            @Override
            public void next(XElement value) {
                duringDayConfig = value.get();
            }

            @Override
            public void error(Throwable ex) {
            }

            @Override
            public void finish() {
            }
        });
        getInput(CONSIGNMENT).register(new Observer<XElement>() {
            private MLModelTraining training;
            private long lastTime = 0;
            private long count = 0;

            @Override
            public void next(XElement value) {
                if (finished) {
                	return;
                }
                if (training == null) {
                    training = init();
                }
                if (training != null) {
                    process(training, value);
                    count++;
                }
                long time = System.currentTimeMillis();
                if (time - lastTime > 2000) {
                    LOG.info("Training - processed " + count + "...");
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
     * Initialize.
     * @return the training model
     */
    private MLModelTraining init() {
        LOG.info("Starting TRAINING");
        if (duringDayConfig == null) {
            LOG.info("Invalid config");
            return null;
        }
        try {
            selectedAttributesProvider = new SelectedAttributesProviderImpl();
            if (get(SELECTED_ATTRIBUTES_FILE) == null || getString(SELECTED_ATTRIBUTES_FILE).isEmpty()) {
                selectedAttributesProvider.loadDefault();
            } else {
                selectedAttributesProvider.loadFromLocalConnection(this, getString(SELECTED_ATTRIBUTES_FILE));
            }
            TestSet testSet = duringDayConfig.createTestSet(selectedAttributesProvider);

            // testing output
//            LOG.info("MODEL WRITE TEST STARTED");
//            MLModel dummy = new MLModel();
//            dummy.config = testSet;
//            dummy.classifiers = Maps.newHashMap();
//            dummy.classifiers.put("TEST", new byte[1]);
//            dummy.mae = 0.25;
//            dummy.smape = 0.35;
//            dispatch(TRAINED_MODEL, toXml(dummy));
//            LOG.info("MODEL WRITE TEST COMPLETED");
            //

            final MLModelTraining trainingBlock = new MLModelTraining(testSet);
            trainingBlock.init();
            return trainingBlock;

        } catch (Throwable ex) {
            log(ex);
        }

        return null;
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
                LOG.info("TRAINING - finished");
                MLModel result = mt.done();
                dispatch(OUTPUT_MAE, resolver().create(result.mae));
                dispatch(OUTPUT_SMAPE, resolver().create(result.smape));
                dispatch(TRAINED_MODEL, toXml(result));
                finished = true;
            }
        } catch (Exception ex) {
            LOG.error(null, ex);
        }
    }

    /**
     * Converts a model to XML.
     *
     * @param model the model to convert
     * @return the XML representation of the model
     */
    /**
     * Converts a model to XML.
     *
     * @param model the model to convert
     * @return the XML representation of the model
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
            classifiers.add(classifier);
        }
        root.add(classifiers);

        return root;
    }
    /*
     private XElement toXml(MLModel model) {
     XElement root = new XElement("DuringDayModel");
     ByteArrayDataOutput out = ByteStreams.newDataOutput();
     // write config
     DuringDayConfigData.write(out, model.config);
     // write classifiers
     out.writeInt(model.classifiers.size());
     for (Map.Entry<String, byte[]> e : model.classifiers.entrySet()) {
     out.writeUTF(e.getKey());
     out.writeInt(e.getValue().length);
     out.write(e.getValue());
     }
     // write selected attributes
     //selectedAttributesProvider.write(out); //TODO
     root.content = Base64.encodeBytes(out.toByteArray());
     return root;
     }*/
}
