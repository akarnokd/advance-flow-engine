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
package eu.advance.logistics.flow.editor.palette;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.editor.model.BlockCategory;

/**
 *
 * @author TTS
 */
public class PaletteRootChildren extends Children.Keys<BlockCategory> implements PropertyChangeListener {

    public PaletteRootChildren() {
        BlockRegistry registry = BlockRegistry.getInstance();
        registry.addPropertyChangeListener(WeakListeners.propertyChange(this, registry));
    }

    @Override
    protected Node[] createNodes(BlockCategory key) {
        return new Node[]{new BlockCategoryNode(key)};
    }

    @Override
    protected void addNotify() {
        super.addNotify();
        update();
    }

    @Override
    protected void removeNotify() {
        super.removeNotify();
        setKeys(Collections.<BlockCategory>emptySet());
    }

    private void update() {
        List<BlockCategory> list = new ArrayList<BlockCategory>(
                BlockRegistry.getInstance().getCategories());
        Collections.sort(list);
        setKeys(list);
    }

    @Override
    public void propertyChange(final PropertyChangeEvent evt) {
        if (EventQueue.isDispatchThread()) {

            propertyChangeEDT(evt);
        } else {
            EventQueue.invokeLater(new Runnable() {

                @Override
                public void run() {
                    propertyChangeEDT(evt);
                }
            });
        }
    }

    private void propertyChangeEDT(PropertyChangeEvent evt) {
        String pname = evt.getPropertyName();
        if (BlockRegistry.PROP_CATEGORY.equals(pname)) {
            update();
        }
    }
}
