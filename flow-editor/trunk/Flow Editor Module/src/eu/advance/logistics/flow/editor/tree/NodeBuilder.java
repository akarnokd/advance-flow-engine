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
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 * @author TTS
 */
public class NodeBuilder {

    public static Node create(final CompositeBlock block) {
        final List<NodeFactory> list = Lists.newArrayList();
        list.add(new NodeFactory() {

            SimpleBlockChildren children;

            @Override
            public Node createNode() {
                children = new SimpleBlockChildren(block.getChildren());
                AbstractNode node = new AbstractNode(children);
                node.setDisplayName(NbBundle.getBundle(NodeBuilder.class).getString("BLOCKS"));
                return node;
            }

            @Override
            public void updateChildren() {
                if (children != null) {
                    children.update();
                }
            }
        });
        list.add(new NodeFactory() {

            BindChildren children;

            @Override
            public Node createNode() {
                children = new BindChildren(block.getBinds());
                AbstractNode node = new AbstractNode(children);
                node.setDisplayName(NbBundle.getBundle(NodeBuilder.class).getString("BINDS"));
                return node;
            }

            @Override
            public void updateChildren() {
                if (children != null) {
                    children.update();
                }
            }
        });
        NodeFactory.Children ch = new NodeFactory.Children(list);
        AbstractNode node = new AbstractNode(ch);
        node.setDisplayName(block.getId());
        block.getFlowDiagram().addListener(new FlowDescriptionListener() {

            @Override
            public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
                switch (event) {
                    case SIMPLE_BLOCK_ADDED:
                    case SIMPLE_BLOCK_REMOVED:
                    case COMPOSITE_BLOCK_ADDED:
                    case COMPOSITE_BLOCK_REMOVED:
                        CompositeBlock parent = (CompositeBlock) params[0];
                        if (parent == block) {
                            for (NodeFactory f : list) {
                                f.updateChildren();
                            }
                        }
                        break;
                }
            }
        });
        return node;
    }
}
