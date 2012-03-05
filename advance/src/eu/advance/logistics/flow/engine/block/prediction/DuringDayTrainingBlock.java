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
import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.block.AdvanceRuntimeContext;
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.runtime.BlockSettings;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.prediction.support.ConsignmentAccessor;
import eu.advance.logistics.prediction.support.MLModelTraining;
import eu.advance.logistics.prediction.support.TestSet;
import eu.advance.logistics.prediction.support.attributes.SelectedAttribute;
import eu.advance.logistics.prediction.support.attributes.SelectedAttributes;
import eu.advance.logistics.prediction.support.attributes.SelectedAttributesProvider;
import hu.akarnokd.reactive4java.reactive.Observer;
import java.io.File;
import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * During day training block.
 * @author TTS
 */
@Block(id = "DuringDayTraining", category = "prediction", scheduler = "IO",
description = "Training for during-day prediction algorithm.")
public class DuringDayTrainingBlock extends AdvanceBlock {

    /**
     * Stream of consignments.
     */
    @Input("advance:collection<advance:consignment>")
    protected static final String CONSIGNMENT = "consignments";
    /**
     * Previous trained model (optional, not used now).
     */
    @Input("advance:object")
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
    @Output("advance:object")
    protected static final String TRAINED_MODEL = "trainedModel";

    @Override
    public void init(BlockSettings<XElement, AdvanceRuntimeContext> settings) {
        super.init(settings);
    }

    @Override
    protected void invoke() {
        DuringDayConfig cfg = null;
        try {
            cfg = DuringDayConfig.parse(resolver(), get(CONFIG));
        } catch (ParseException ex) {
            Logger.getLogger(DuringDayTrainingBlock.class.getName()).log(Level.SEVERE, null, ex);
        }
        TestSet testSet = cfg.createTestSet(new SelectedAttributesProviderImpl(getString(SELECTED_ATTRIBUTES_FILE)));
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
     * @param mt the model
     * @param x the XML representation of the consignment
     */
    private void process(MLModelTraining mt, XElement x) {
        try {
            Consignment c = Consignment.parse(x);
            if (c.id != -1) {
                mt.process(new ConsignmentAccessorImpl(c));
            } else {
                mt.done();
            }
        } catch (Exception ex) {
            LOG.error(null, ex);
        }
    }

    /**
     * Internal class used to load the selected attributes.
     */
    final class SelectedAttributesProviderImpl implements SelectedAttributesProvider {
        /** Local connection data store of the XML file. */
        private final String selectedAttributesFile;

        /**
         * Implements a selected attributes provider loading the contents from
         * an XML file specified by a local connection data store.         * 
         * @param selectedAttributesFile the local connection data store
         */
        private SelectedAttributesProviderImpl(String selectedAttributesFile) {
            this.selectedAttributesFile = selectedAttributesFile;
        }
        
        @Override
        public Map<String, SelectedAttributes> load() {
            Map<String, SelectedAttributes> attributesSet = new HashMap<String, SelectedAttributes>();
            Pool<LocalConnection> p = null;
            LocalConnection conn = null;
            try {
                p = getPool(LocalConnection.class, selectedAttributesFile);
                conn = p.get();
                parse(attributesSet, conn.file());
            } catch (Exception ex) {
                Logger.getGlobal().log(Level.SEVERE, null, ex);
            } finally {
                if (p != null && conn != null) {
                    p.put(conn);
                }
            }
            if (!attributesSet.containsKey("default")) {
                attributesSet.put("default", createDefault());
            }
            return attributesSet;
        }

        /**
         * Parse an atttibute set from XML.
         * @param attributesSet the collections of attributes sets
         * @param file XML with a map of attributes sets
         * @throws Exception if unable to parse the XML
         */
        private void parse(Map<String, SelectedAttributes> attributesSet, File file) throws Exception {
            XElement x = XElement.parseXML(file);
            for (XElement cx : x.childrenWithName("attributes")) {
                String key = cx.get("key");
                if (key != null) {
                    SelectedAttributes sa = parse(cx);
                    if (!sa.isEmpty()) {
                        attributesSet.put(key, sa);
                    }
                }
            }
        }

        /**
         * Parse a single attribute.
         * @param x the XML element representing the attribute 
         * @return a named attibute
         */
        private SelectedAttributes parse(XElement x) {
            SelectedAttributes sa = new SelectedAttributes();
            for (XElement cx : x.childrenWithName("attribute")) {
            	String name = cx.get("name");
                if (name != null) {
                    sa.add(new SelectedAttribute(name));
                }
            }
            return sa;
        }

        @Override
        public SelectedAttributes createDefault() {
            SelectedAttributes a = new SelectedAttributes();
            a.add(new SelectedAttribute("(C0-C2)"));
            a.add(new SelectedAttribute("(EC0-EC2)"));
            a.add(new SelectedAttribute("(EC0-EC5)"));
            a.add(new SelectedAttribute("(ER3-ER4)"));
            a.add(new SelectedAttribute("(R4-R5)"));
            a.add(new SelectedAttribute("C2"));
            a.add(new SelectedAttribute("CTime"));
            a.add(new SelectedAttribute("DW"));
            a.add(new SelectedAttribute("ER1"));
            a.add(new SelectedAttribute("ER3"));
            a.add(new SelectedAttribute("ER5"));
            a.add(new SelectedAttribute("EW1"));
            a.add(new SelectedAttribute("EW4"));
            a.add(new SelectedAttribute("FR4"));
            a.add(new SelectedAttribute("FW0"));
            a.add(new SelectedAttribute("R1"));
            a.add(new SelectedAttribute("R2"));
            a.add(new SelectedAttribute("R5"));
            return a;
        }
    }
    
    /**
     * Internal class used to access the Consignment data.
     */
    private static final class ConsignmentAccessorImpl implements ConsignmentAccessor {
        /** Underlying consignment. */
        private Consignment c;

        /**
         * Construct the accessor to the consignment.
         * @param c the object containing the data
         */
        private ConsignmentAccessorImpl(Consignment c) {
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
}
