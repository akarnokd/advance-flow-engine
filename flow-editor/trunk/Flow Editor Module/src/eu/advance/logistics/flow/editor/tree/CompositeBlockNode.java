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

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.openide.nodes.AbstractNode;
import org.openide.nodes.Node;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.CompositeBlock;
import eu.advance.logistics.flow.editor.model.ConstantBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import java.util.regex.Pattern;

/**
 *
 * @author TTS
 */
public class CompositeBlockNode extends AbstractNode implements FlowDescriptionListener {

    private CompositeBlock block;
    private String htmlDisplayName;

    protected CompositeBlockNode(CompositeBlock block) {
        super(new CompositeBlockChildren(block));
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

    private void updateMyChildren() {
        ((CompositeBlockChildren) getChildren()).update();
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
                    updateMyChildren();
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
    /**
     * The children of a composite block.
     */
    public static class CompositeBlockChildren extends org.openide.nodes.Children.Keys<AbstractBlock> {

        private CompositeBlock block;
        /** The optional filtering pattern. */
        protected Pattern pattern;
        /**
         * Constructor.
         */
        private CompositeBlockChildren(CompositeBlock block) {
            this.block = block;
        }

        private void update() {
            setKeys(Collections.<AbstractBlock>emptySet());
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
            setKeys(Collections.<AbstractBlock>emptySet());
        }

        @Override
        protected Node[] createNodes(AbstractBlock key) {
            if (key instanceof CompositeBlock) {
                CompositeBlockNode cbn = new CompositeBlockNode((CompositeBlock) key);
                CompositeBlockChildren bc = (CompositeBlockChildren)cbn.getChildren();
                bc.setPattern(pattern);
                if (pattern == null || bc.getNodesCount() > 0) {
                    return new Node[]{ cbn };
                }
            } else if (key instanceof SimpleBlock) {
                final SimpleBlock bk = (SimpleBlock) key;
                final SimpleBlockNode b = new SimpleBlockNode(bk);
                
                boolean r = false;
                if (pattern == null) {
                    r = true;
                } else {
                    if (pattern.matcher(bk.getId().toUpperCase()).matches()) {
                        r = true;
                    } else
                    if (pattern.matcher(bk.getTooltip().toUpperCase()).matches()) {
                        r = true;
                    }
                }
                if (r) {
                    return new Node[]{ b };
                }
            } else if (key instanceof ConstantBlock) {
                final ConstantBlock bk = (ConstantBlock) key;
                final ConstantBlockNode b = new ConstantBlockNode(bk);
                boolean r = false;
                if (pattern == null) {
                    r = true;
                } else {
                    String s = bk.toString().toUpperCase();
                    r = pattern.matcher(s).matches();
                }                
                if (r) {
                    return new Node[]{ b };
                }
            }
            return new Node[0];
        }
        /**
         * Set the filtering pattern.
         * @param pattern the filtering pattern
         */
        public void setPattern(Pattern pattern) {
            this.pattern = pattern;
            update();
        }
        /**
         * @return the current filtering.
         */
        public Pattern getPattern() {
            return pattern;
        }
    }
}
