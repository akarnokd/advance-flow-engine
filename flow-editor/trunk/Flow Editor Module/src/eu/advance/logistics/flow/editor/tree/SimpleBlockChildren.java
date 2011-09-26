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
package eu.advance.logistics.flow.editor.tree;

import com.google.common.collect.Lists;
import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;

/**
 *
 * @author TTS
 */
class SimpleBlockChildren extends Children.Keys<AbstractBlock> {

    private Collection<AbstractBlock> keys;

    SimpleBlockChildren(Collection<AbstractBlock> keys) {
        this.keys = keys;
    }

    void update() {
        List<AbstractBlock> sorted = Lists.newArrayList(keys);
        Collections.sort(sorted);
        setKeys(sorted);
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

    @Override
    protected Node[] createNodes(AbstractBlock key) {
        AbstractNode node = new AbstractNode(LEAF);
        node.setDisplayName(key.getId());
        return new Node[]{node};
    }
}
