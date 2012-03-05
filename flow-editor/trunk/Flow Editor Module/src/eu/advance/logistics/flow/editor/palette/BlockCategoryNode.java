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
import java.util.Comparator;
import java.util.List;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import java.util.regex.Pattern;

/**
 *
 * @author TTS
 */
public class BlockCategoryNode extends AbstractNode implements PropertyChangeListener {
    /**
     * Constructs a node.
     */
    public BlockCategoryNode(BlockCategory category) {
        super(new BlockCategoryChildren(category), Lookups.singleton(category));
        updateDisplayName();
        setIconBaseWithExtension("eu/advance/logistics/flow/editor/palette/images/" + category.getImage());
        category.addPropertyChangeListener(WeakListeners.propertyChange(this, category));
    }

    private void updateDisplayName() {
        BlockCategory category = getLookup().lookup(BlockCategory.class);
        
        BlockCategoryChildren bcc = (BlockCategoryChildren) getChildren();
        
        setDisplayName(String.format("%s (%d)", category.getName(),
                bcc.pattern == null ? category.getTypes().size() : getChildren().getNodesCount()));
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
        /** The pattern for filtering. */
        protected Pattern pattern;
        
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
            setKeys(Collections.<AdvanceBlockDescription>emptySet());
        }

        void update() {
            List<AdvanceBlockDescription> list = new ArrayList<AdvanceBlockDescription>();
            for (AdvanceBlockDescription bd : category.getTypes()) {
                if (pattern == null) {
                    list.add(bd);
                } else
                if (pattern.matcher(bd.id.toUpperCase()).matches()) {
                    list.add(bd);
                } else
                if (pattern.matcher(bd.tooltip.toUpperCase()).matches()) {
                    list.add(bd);
                }
            }
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
    public void setPattern(Pattern pattern) {
        ((BlockCategoryChildren)getChildren()).pattern = pattern;
        ((BlockCategoryChildren)getChildren()).update();
        updateDisplayName();
    }
}
