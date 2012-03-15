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
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.util.Base64;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.prediction.support.MLModel;
import java.util.Map;

/**
 * During day model writer.
 *
 * @author TTS
 */
@Block(id = "DuringDayModelReader", category = "prediction", scheduler = "IO",
description = "Reads a during-day model.")
public class DuringDayModelReader extends AdvanceBlock {

    /**
     * Location from where the DuringDay model will be loades.
     * It must rappresent a file.
     */
    @Input("advance:string")
    protected static final String LOCATION = "location";
    /**
     * DuringDay model loaded from the location.
     */
    @Output("duringdaymodel")
    protected static final String MODEL = "model";

    @Override
    protected void invoke() {
        Pool<LocalConnection> ds = null;
        LocalConnection conn = null;
        try {
            ds = getPool(LocalConnection.class, getString(LOCATION));
            conn = ds.get();
            XElement root = XElement.parseXML(conn.file());
            dispatch(root);
        } catch (Exception ex) {
            log(ex);
        } finally {
            if (ds != null && conn != null) {
                ds.put(conn);
            }
        }
    }

    /**
     * Converts a model to XML.
     * @param resolver used to resolve data to XML
     * @param model the model to convert
     * @return 
     */
    static XElement toXml(DataResolver<XElement> resolver, MLModel model) {
        XElement root = new XElement("DuringDayModel");
        root.add(DuringDayConfigData.toXml(resolver, "config", model.config));
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
