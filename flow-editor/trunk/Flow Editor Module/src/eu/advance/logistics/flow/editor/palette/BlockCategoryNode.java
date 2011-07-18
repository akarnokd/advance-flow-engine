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

import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import java.awt.EventQueue;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author TTS
 */
public class BlockCategoryNode extends AbstractNode implements PropertyChangeListener {

    public BlockCategoryNode(BlockCategory category) {
        super(new BlockCategoryChildren(category), Lookups.singleton(category));
        updateDisplayName();
        setIconBaseWithExtension("eu/advance/logistics/flow/editor/palette/images/" + category.getImage());
        category.addPropertyChangeListener(WeakListeners.propertyChange(this, category));
    }

    private void updateDisplayName() {
        BlockCategory category = getLookup().lookup(BlockCategory.class);
        setDisplayName(String.format("%s (%d)", category.getName(),
                category.getTypes().size()));
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
        if (BlockCategory.PROP_TYPE.equals(pname)) {
            ((BlockCategoryChildren) getChildren()).update();
            updateDisplayName();
        }
    }

    private static class BlockCategoryChildren extends Children.Keys<AdvanceBlockDescription> {

        private BlockCategory category;

        private BlockCategoryChildren(BlockCategory category) {
            this.category = category;
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            update();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            setKeys(Collections.EMPTY_SET);
        }

        void update() {
            List<AdvanceBlockDescription> list = new ArrayList<AdvanceBlockDescription>(
                    category.getTypes());
            Collections.sort(list, new Comparator<AdvanceBlockDescription>() {

                @Override
                public int compare(AdvanceBlockDescription o1, AdvanceBlockDescription o2) {
                    if (o1.displayName != null && o2.displayName != null) {
                    return o1.displayName.compareTo(o2.displayName);
                    } else if (o1.displayName != null) {
                        return 1;
                    } else if (o2.displayName != null) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });
            setKeys(list);
        }

        @Override
        protected Node[] createNodes(AdvanceBlockDescription key) {
            return new Node[]{new BlockNode(key)};
        }
    }
}
