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
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

/**
 *
 * @author TTS
 */
class CompositeBlockNode extends AbstractNode implements FlowDescriptionListener {

    private CompositeBlock block;
    private String htmlDisplayName;

    protected CompositeBlockNode(CompositeBlock block) {
        super(new Ch(block));
        this.block = block;
        setIconBaseWithExtension("eu/advance/logistics/flow/editor/palette/images/block.png");
        updateName();
        block.getFlowDiagram().addListener(this);        
    }

    private void updateName() {
        String name = block.getId();
        setName(name);

        if (block.getFlowDiagram().getActiveBlock() == block) {
            htmlDisplayName = "<b>" + name + "</b>";
            setDisplayName("*" + name + "*");
        } else {
            htmlDisplayName = name;
            setDisplayName(name);
        }
    }

    @Override
    public String getHtmlDisplayName() {
        return htmlDisplayName;
    }

    private void updateChildren() {
        ((Ch) getChildren()).update();
    }

    @Override
    public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
        switch (event) {
            case BLOCK_RENAMED:
            case CONSTANT_BLOCK_CHANGED:
                if (params[0] == block) {
                    updateName();
                }
                break;
            case BLOCK_MOVED:
                break;
            case SIMPLE_BLOCK_ADDED:
            case SIMPLE_BLOCK_REMOVED:
            case COMPOSITE_BLOCK_ADDED:
            case COMPOSITE_BLOCK_REMOVED:
            case CONSTANT_BLOCK_ADDED:
            case CONSTANT_BLOCK_REMOVED:
                if (params[0] == block) {
                    updateChildren();
                }
                break;
            case ACTIVE_COMPOSITE_BLOCK_CHANGED:
                // check if old or new active block equals this block
                if (params[0] == block || params[1] == block) {
                    updateName();
                }
                break;
            case BIND_CREATED:
                break;
            case BIND_REMOVED:
                break;
            case BIND_ERROR_MESSAGE:
                break;
            case PARAMETER_CREATED:
                break;
            case PARAMETER_REMOVED:
                break;
            case PARAMETER_RENAMED:
                break;
            case PARAMETER_CHANGED:
                break;
            case CLOSED:
                break;
            case SAVING:
                break;
        }
    }

    @Override
    public void destroy() throws IOException {
        super.destroy();
        block = null;
    }

    private static class Ch extends org.openide.nodes.Children.Keys<AbstractBlock> {

        private CompositeBlock block;

        private Ch(CompositeBlock block) {
            this.block = block;
        }

        private void update() {
            List<AbstractBlock> list = Lists.newArrayList(block.getChildren());
            Collections.sort(list);
            setKeys(list);
        }

        @Override
        protected void addNotify() {
            super.addNotify();
            update();
        }

        @Override
        protected void removeNotify() {
            super.removeNotify();
            setKeys(Collections.EMPTY_LIST);
        }

        @Override
        protected Node[] createNodes(AbstractBlock key) {
            if (key instanceof CompositeBlock) {
                return new Node[]{new CompositeBlockNode((CompositeBlock) key)};
            } else if (key instanceof SimpleBlock) {
                return new Node[]{new SimpleBlockNode((SimpleBlock) key)};
            } else if (key instanceof ConstantBlock) {
                return new Node[]{new ConstantBlockNode((ConstantBlock) key)};
            } else {
                return new Node[0];
            }
        }
    }
}
