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
package eu.advance.logistics.flow.editor.model;

import eu.advance.logistics.flow.editor.BlockRegistry;
import com.google.common.base.Objects;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.List;
import org.openide.util.ImageUtilities;

/**
 * <b>BlockCategory</b>
 * 
 * @author TTS
 */
public class BlockCategory implements Comparable<BlockCategory> {

    public static final String PROP_TYPE = "type";
    private String id;
    private String name;
    private String image_url;
    private Image image;
    private PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
    private List<AdvanceBlockDescription> types;
    private BlockRegistry registry;

    public BlockCategory(BlockRegistry registry, String id, String name, String imageUrl) {
        this.registry = registry;
        this.id = id;
        this.types = new ArrayList<AdvanceBlockDescription>();
        this.name = name;
        this.image_url = imageUrl;
        this.image = ImageUtilities.loadImage("eu/advance/logistics/flow/editor/palette/images/" + imageUrl);
    }

    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image_url = image;
        this.image = ImageUtilities.loadImage(image);
    }

    public String getImage() {
        return image_url;
    }

    public Image getImageObject() {
        return image;
    }

    public void addType(AdvanceBlockDescription type) {
        int index = types.size();
        types.add(type);
        propertyChangeSupport.fireIndexedPropertyChange(PROP_TYPE, index, null, type);
    }

    public void addType(int index, AdvanceBlockDescription type) {
        types.add(index, type);
        propertyChangeSupport.fireIndexedPropertyChange(PROP_TYPE, index, null, type);
    }

    public void removeType(AdvanceBlockDescription type) {
        int index = types.indexOf(type);
        if (index != -1) {
            types.remove(index);
            propertyChangeSupport.fireIndexedPropertyChange(PROP_TYPE, index, type, null);
        }
    }

    public List<AdvanceBlockDescription> getTypes() {
        return types;
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof BlockCategory) {
            Objects.equal(id, ((BlockCategory) other).id);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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

    @Override
    public int compareTo(BlockCategory o) {
        return name.compareTo(o.name);
    }
}
