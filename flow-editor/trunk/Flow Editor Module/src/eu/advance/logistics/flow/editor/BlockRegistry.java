/*
 * Copyright 2010-2012 The Advance EU 7th Framework project consortium
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
package eu.advance.logistics.flow.editor;

import com.google.common.collect.Maps;
import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.util.Collection;
import java.util.Map;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author TTS
 */
public class BlockRegistry {

    public static final String PROP_ID = "id";
    public static final String PROP_CATEGORY = "category";
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private String id;
    private Map<String, BlockCategory> categories = Maps.newHashMap();
    private Map<AdvanceBlockDescription, BlockCategory> descriptions = Maps.newHashMap();
    private Map<String, AdvanceBlockDescription> types = Maps.newHashMap();

    private BlockRegistry() {
    }

    /**
     * Get the value of id
     *
     * @return the value of id
     */
    public String getId() {
        return id;
    }

    /**
     * Set the value of id
     *
     * @param id new value of id
     */
    public void setId(String id) {
        String old = this.id;
        this.id = id;
        propertyChangeSupport.firePropertyChange(PROP_ID, old, id);
    }

    public BlockCategory addCategory(String id, String name, String imageUrl) {
        BlockCategory category = new BlockCategory(this, id, name, imageUrl);
        category.addPropertyChangeListener(WeakListeners.propertyChange(catChangeListener, category));
        categories.put(category.getId(), category);
        propertyChangeSupport.firePropertyChange(PROP_CATEGORY, null, category);
        return category;
    }

    public void removeCategory(String name) {
        BlockCategory category = categories.remove(name);
        if (category != null) {
            propertyChangeSupport.firePropertyChange(PROP_CATEGORY, category, null);
        }
    }

    public BlockCategory find(String name) {
        return categories.get(name);
    }

    public BlockCategory findOrCreate(String id) {
        BlockCategory cat = find(id);
        if (cat == null) {
            String name = id;
            if (name == null) {
                name = "(" + NbBundle.getMessage(getClass(), "UNCATEGORIZED") + ")";
            }
            cat = addCategory(id, name, "uncategorized.png");
        }
        return cat;
    }

    public BlockCategory findByType(AdvanceBlockDescription description) {
        return descriptions.get(description);
    }

    public AdvanceBlockDescription findType(String type) {
        return types.get(type);
    }

    public Collection<BlockCategory> getCategories() {
        return categories.values();
    }

    /**
     * Add PropertyChangeListener.
     *
     * @param listener
     */
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.addPropertyChangeListener(listener);
    }

    /**
     * Remove PropertyChangeListener.
     *
     * @param listener
     */
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        propertyChangeSupport.removePropertyChangeListener(listener);
    }
    private static BlockRegistry INSTANCE;

    public static BlockRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new BlockRegistry();
        }
        return INSTANCE;
    }
    private PropertyChangeListener catChangeListener = new PropertyChangeListener() {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String pname = evt.getPropertyName();
            if (BlockCategory.PROP_TYPE.equals(pname)) {
                if (evt.getNewValue() != null) {
                    AdvanceBlockDescription d = (AdvanceBlockDescription) evt.getNewValue();
                    descriptions.put(d, (BlockCategory) evt.getSource());
                    types.put(d.id, d);
                } else if (evt.getOldValue() != null) {
                    AdvanceBlockDescription d = (AdvanceBlockDescription) evt.getOldValue();
                    descriptions.remove(d);
                    types.remove(d.id);
                }
            }
        }
    };

    public void readCategories(File file) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(file);
            NodeList nodeList = doc.getElementsByTagName("category");
            for (int i = 0, n = nodeList.getLength(); i < n; i++) {
                Element e = (Element) nodeList.item(i);
                addCategory(e.getAttribute("id"), e.getAttribute("displayName"), e.getAttribute("icon"));
            }
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
    public int getCategoryCount() {
        return categories.size();
    }
    
    public int getBlockDescriptionCount() {
        return descriptions.size();
    }
}