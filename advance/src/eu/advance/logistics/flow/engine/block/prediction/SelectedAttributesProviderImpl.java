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

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import eu.advance.logistics.flow.engine.api.core.Pool;
import eu.advance.logistics.flow.engine.block.AdvanceBlock;
import eu.advance.logistics.flow.engine.comm.LocalConnection;
import eu.advance.logistics.flow.engine.runtime.DataResolver;
import eu.advance.logistics.flow.engine.xml.XElement;
import eu.advance.logistics.prediction.support.attributes.SelectedAttribute;
import eu.advance.logistics.prediction.support.attributes.SelectedAttributes;
import eu.advance.logistics.prediction.support.attributes.SelectedAttributesProvider;

/**
 * Implements a selected attributes provider loading the contents from an XML
 * file specified by a local connection data store.
 *
 */
final class SelectedAttributesProviderImpl implements SelectedAttributesProvider {

    /**
     * Map of selected attributes.
     */
    private Map<String, SelectedAttributes> attributesSet;

    /**
     * Implements a selected attributes provider. The contents can be loaded by
     * a local data store or from an XML element.
     */
    SelectedAttributesProviderImpl() {
    }

    @Override
    public Map<String, SelectedAttributes> load() {
        return attributesSet;
    }

    /**
     * Parse an attribute set from XML.
     *
     * @param attributesSet the collections of attributes sets
     * @param x the root XML element
     * @throws Exception if unable to parse the XML
     */
    private void parse(Map<String, SelectedAttributes> attributesSet, XElement x) throws Exception {
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
     *
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

    /**
     * Loads selected attributes form an XML element.
     *
     * @param root the XML element containing the attributes
     * @throws Exception if unable to parse the XML elements
     */
    public void load(XElement root) throws Exception {
        attributesSet = new HashMap<String, SelectedAttributes>();
        parse(attributesSet, root);
        if (!attributesSet.containsKey("default")) {
            attributesSet.put("default", createDefault());
        }
    }

    /**
     * Loads selected attributes from the contents from an XML file specified by
     * a local connection data store.
     *
     * @param owner owner block (to ge the connection pool)
     * @param selectedAttributesFile the local connection data store
     */
    public void loadFromLocalConnection(AdvanceBlock owner, String selectedAttributesFile) {
        attributesSet = new HashMap<String, SelectedAttributes>();
        Pool<LocalConnection> p = null;
        LocalConnection conn = null;
        try {
            p = owner.getPool(LocalConnection.class, selectedAttributesFile);
            if (p != null) {
                conn = p.get();
                parse(attributesSet, XElement.parseXML(conn.file()));
            }
        } catch (Exception ex) {
            Logger.getLogger(DuringDayTraining.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (p != null && conn != null) {
                p.put(conn);
            }
        }
        if (!attributesSet.containsKey("default")) {
            attributesSet.put("default", createDefault());
        }
    }
    /** Load the default attributes. */
    public void loadDefault() {
        attributesSet.put("default", createDefault());
    }

    /**
     * Return the attribute sets.
     *
     * @return a map of attibutes
     */
    public Map<String, SelectedAttributes> getAttributesSet() {
        return attributesSet;
    }

    /**
     * Converts the selected attributes list to an XML representation.
     *
     * @param resolver the data resolver used to convert data to XML
     * @return the XML representation of the selected attributes list
     */
    public XElement toXml(DataResolver<XElement> resolver) {
        XElement root = new XElement("attributes-set");
        for (Map.Entry<String, SelectedAttributes> e : attributesSet.entrySet()) {
            XElement attributes = new XElement("attributes");
            attributes.set("key", e.getKey());
            for (SelectedAttribute sa : e.getValue()) {
                XElement attribute = new XElement("attribute");
                attribute.set("name", sa._name);
                attributes.add(attribute);
            }
            root.add(attributes);
        }
        return root;
    }
}
