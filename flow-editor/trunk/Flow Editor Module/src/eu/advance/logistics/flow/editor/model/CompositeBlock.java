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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.openide.util.NbBundle;

/**
 * <b>CompositeBlock</b>
 *
 * @author TTS
 */
public class CompositeBlock extends AbstractBlock {

    protected Map<String, BlockBind> binds = Maps.newHashMap();
    protected Map<String, AbstractBlock> children = Maps.newHashMap();

    public CompositeBlock(String id) {
        this.id = id;
    }

    public Collection<BlockBind> getBinds() {
        return binds.values();
    }

    public Collection<AbstractBlock> getChildren() {
        return children.values();
    }

    public BlockBind createBind(BlockParameter src, BlockParameter dst) {
        return createBind(generateId(binds.keySet(), "bind"), src, dst);
    }

    public BlockBind createBind(String bindId, BlockParameter src, BlockParameter dst) {
        if (bindId == null || binds.containsKey(bindId)) {
            throw new IllegalArgumentException(NbBundle.getBundle(CompositeBlock.class).getString("INVALID_ID") + bindId);
        }

        // check if the bind already exists
        if (find(src, dst) != null) {
            return null;
        }

        BlockBind c = new BlockBind(this, bindId, src, dst);
        addBind(c);
        return c;
    }
    
    public void addBind(BlockBind c) {
        binds.put(c.id, c);
        getFlowDiagram().fire(FlowDescriptionChange.BIND_CREATED, this, c);
        
        FlowDescription fd = getFlowDiagram();
        if (fd != null) {
            AdvanceCompositeBlock build = fd.build();
            getFlowDiagram().setCompilationResult(BlockRegistry.getInstance()
                    .verify(build));
        }
    }

    public void removeBind(BlockBind bind) {
        if (binds.remove(bind.id) == bind) {
            getFlowDiagram().fire(FlowDescriptionChange.BIND_REMOVED, this, bind);
        } else {
            // something wrong
        }
        getFlowDiagram().setCompilationResult(BlockRegistry.getInstance()
                .verify(getFlowDiagram().build()));
    }

    public BlockParameter findBlockParameter(String blockId, String paramId) {
        if (blockId == null || blockId.isEmpty()) {
            return getInputOrOutputParameter(paramId);
        }
        AbstractBlock block = children.get(blockId);
        return (block != null) ? block.getInputOrOutputParameter(paramId) : null;
    }

    public BlockBind find(BlockParameter src, BlockParameter dst) {
        for (BlockBind bind : binds.values()) {
            if (bind.equals(src, dst)) {
                return bind;
            }
        }
        return null;
    }

    public List<BlockBind> getActiveBinds(AbstractBlock child) {
        List<BlockBind> result = Lists.newArrayList();
        for (BlockBind bind : binds.values()) {
            if (bind.source.owner == child || bind.destination.owner == child) {
                result.add(bind);
            }
        }
        return result;
    }

    public SimpleBlock createBlock(AdvanceBlockDescription desc) {
        return createBlock(generateId(children.keySet(), desc.id), desc);
    }

    public SimpleBlock createBlock(String blockId, AdvanceBlockDescription desc) {
        if (blockId == null || children.containsKey(blockId)) {
            throw new IllegalArgumentException(NbBundle.getBundle(CompositeBlock.class).getString("INVALID_ID") + blockId);
        }
        SimpleBlock block = new SimpleBlock(blockId, desc);
        addBlock(block);
        return block;
    }

    public void addBlock(SimpleBlock block) {
        block.setParent(this);
        children.put(block.id, block);
        getFlowDiagram().fire(FlowDescriptionChange.SIMPLE_BLOCK_ADDED, this, block);
    }

    public void removeBlock(SimpleBlock block) {
        if (children.remove(block.getId()) == block) {
            getFlowDiagram().fire(FlowDescriptionChange.SIMPLE_BLOCK_REMOVED, this, block);
        } else {
            // something wrong
        }
    }

    public CompositeBlock createComposite() {
        return createComposite(generateId(children.keySet(), "composite"));
    }

    public CompositeBlock createComposite(String blockId) {
        if (blockId == null || children.containsKey(blockId)) {
            throw new IllegalArgumentException(NbBundle.getBundle(CompositeBlock.class).getString("INVALID_ID") + blockId);
        }
        CompositeBlock block = new CompositeBlock(blockId);
        addComposite(block);
        return block;
    }

    public void addComposite(CompositeBlock block) {
        block.setParent(this);
        children.put(block.id, block);
        getFlowDiagram().fire(FlowDescriptionChange.COMPOSITE_BLOCK_ADDED, this, block);
    }

    public void removeComposite(CompositeBlock block) {
        if (children.remove(block.getId()) == block) {
            getFlowDiagram().fire(FlowDescriptionChange.COMPOSITE_BLOCK_REMOVED, this, block);
        } else {
            // something wrong
        }
    }

    public String generateConstantId() {
        return generateId(children.keySet(), "constant");
    }

    public ConstantBlock createConstant() {
        return createConstant(generateId(children.keySet(), "constant"));
    }

    public ConstantBlock createConstant(String blockId) {
        if (blockId == null || children.containsKey(blockId)) {
            throw new IllegalArgumentException(NbBundle.getBundle(CompositeBlock.class).getString("INVALID_ID") + blockId);
        }
        ConstantBlock block = new ConstantBlock(blockId);
        addConstant(block);
        return block;
    }

    public void addConstant(ConstantBlock block) {
        block.setParent(this);
        children.put(block.id, block);
        getFlowDiagram().fire(FlowDescriptionChange.CONSTANT_BLOCK_ADDED, this, block);
    }

    public void removeConstant(ConstantBlock block) {
        if (children.remove(block.getId()) == block) {
            getFlowDiagram().fire(FlowDescriptionChange.CONSTANT_BLOCK_REMOVED, this, block);
        } else {
            // something wrong
        }
    }

    boolean updateBlockId(AbstractBlock target, String id) {
        if (children.containsKey(id)) {
            return false;
        }
        AbstractBlock block = children.remove(target.getId());
        if (block != target) {
            // something wrong!
        }
        children.put(id, target);
        return true;
    }

    @Override
    public void destroy() {
        if (parent != null) {
            parent.removeComposite(this);
        }
    }

    @Override
    public CompositeBlock createClone(CompositeBlock newParent) {
        final CompositeBlock cb = new CompositeBlock("");
        cb.setParent(newParent);
        cb.copyValues(this);

        final Set<String> keySet = children.keySet();
        for (String key : keySet) {
            final AbstractBlock ch_src = children.get(key);
            final AbstractBlock ch = ch_src.createClone(cb);
            cb.children.put(ch.getId(), ch);
        }

        final Set<String> bindsKeySet = binds.keySet();
        for (String key : bindsKeySet) {
            final BlockBind b_src = binds.get(key);

            final String srcParamID = b_src.source.getId();

            // if I'm the owner, then the id is null
            String srcBlockID = b_src.source.owner.getId();
            if (srcBlockID.equals(id)) {
                srcBlockID = null;
            } else {
                srcBlockID += " (copy)";
            }
            final BlockParameter source = cb.findBlockParameter(srcBlockID, srcParamID);
//            System.err.println("CompositeBlockClone SOURCE: " + source + " <=>  param: " + srcParamID + "  block: " + srcBlockID);

            final String dstParamID = b_src.destination.getId();
            // if I'm the owner, then the id is null
            String dstBlockID = b_src.destination.owner.getId();
            if (dstBlockID.equals(id)) {
                dstBlockID = null;
            } else {
                dstBlockID += " (copy)";
            }
            final BlockParameter destination = cb.findBlockParameter(dstBlockID, dstParamID);
//            System.err.println("CompositeBlockClone DEST: " + destination + " <=>  param: " + dstParamID + "  block: " + dstBlockID);

            //add the bind
            final BlockBind c = new BlockBind(cb, key, source, destination);
            cb.binds.put(generateId(cb.binds.keySet(), "bind"), c);
        }

        return cb;
    }
}
