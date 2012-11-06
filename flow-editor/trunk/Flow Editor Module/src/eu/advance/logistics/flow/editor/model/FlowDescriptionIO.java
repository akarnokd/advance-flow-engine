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

import java.awt.Point;
import java.util.List;
import java.util.Map;

import org.openide.util.NbBundle;

import eu.advance.logistics.flow.editor.BlockRegistry;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockBind;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockReference;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockVisuals;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;

/**
 *
 * @author TTS
 */
class FlowDescriptionIO {

    private FlowDescriptionIO() {
    }

    static FlowDescription create(AdvanceCompositeBlock compositeBlock) {
        FlowDescription fd = new FlowDescription(compositeBlock.id);
        read(fd, compositeBlock);
        return fd;
    }

    private static void read(CompositeBlock parent, AdvanceCompositeBlock advCompositeBlock) {
        for (AdvanceCompositeBlockParameterDescription e : advCompositeBlock.inputs()) {
            parent.createInput(e);
        }

        for (AdvanceCompositeBlockParameterDescription e : advCompositeBlock.outputs()) {
            parent.createOutput(e);
        }

        BlockRegistry r = BlockRegistry.getInstance();
        for (Map.Entry<String, AdvanceBlockReference> e : advCompositeBlock.blocks.entrySet()) {
            AdvanceBlockReference advBlock = e.getValue();
            if (!e.getKey().equals(advBlock.id)) {
                System.err.println("ID " + e.getKey() + " != " + advBlock.id);
            }
            AdvanceBlockDescription advBlockDesc = r.findType(advBlock.type);
            if (advBlockDesc != null) {
                advBlockDesc = advBlockDesc.derive(advBlock);
                SimpleBlock block = parent.createBlock(advBlock.id, advBlockDesc);
                block.varargs = advBlock.varargs;
                readLocation(advBlock.keywords, advBlock.visuals, block);
            }
        }

        for (Map.Entry<String, AdvanceCompositeBlock> e : advCompositeBlock.composites.entrySet()) {
            AdvanceCompositeBlock advComposite = e.getValue();
            if (!e.getKey().equals(advComposite.id)) {
                System.err.println("ID " + e.getKey() + " != " + advComposite.id);
            }
            CompositeBlock composite = parent.createComposite(advComposite.id);
            readLocation(advComposite.keywords, advComposite.visuals, composite);
            read(composite, advComposite);
        }

        for (Map.Entry<String, AdvanceConstantBlock> e : advCompositeBlock.constants.entrySet()) {
            AdvanceConstantBlock advConstant = e.getValue();
            if (!e.getKey().equals(advConstant.id)) {
                System.err.println("ID " + e.getKey() + " != " + advConstant.id);
            }
            ConstantBlock constant = parent.createConstant(advConstant.id);
            readLocation(advConstant.keywords, advConstant.visuals, constant);
            constant.setConstant(advConstant);
        }

        for (AdvanceBlockBind advBind : advCompositeBlock.bindings) {
            BlockParameter src = parent.findBlockParameter(advBind.sourceBlock, advBind.sourceParameter);
            if (src == null) {
                src = parent.findBlockParameter(advBind.sourceBlock, ConstantBlock.DEFAULT_PARAMETER_NAME);
            }
            BlockParameter dst = parent.findBlockParameter(advBind.destinationBlock, advBind.destinationParameter);
            if (src != null && dst != null) {
                parent.createBind(advBind.id, src, dst);
            } else {
                try {
                System.out.println(NbBundle.getBundle(FlowDescriptionIO.class).getString("UNABLE_CREATE_BIND") + advBind.sourceBlock + "." + advBind.sourceParameter + " -> " + advBind.destinationBlock + "." + advBind.destinationParameter);
                } catch (Throwable t) {
                    t.printStackTrace();
                }
            }
        }
    }

    static AdvanceCompositeBlock build(CompositeBlock parent) {
        AdvanceCompositeBlock aCompositeBlock = new AdvanceCompositeBlock();
        aCompositeBlock.id = parent.getId();
        for (BlockParameter param : parent.getInputs()) {
            aCompositeBlock.addInput(convert(param.getDescription()));
        }
        for (BlockParameter param : parent.getOutputs()) {
            aCompositeBlock.addOutput(convert(param.getDescription()));
        }
        for (AbstractBlock block : parent.getChildren()) {
            if (block instanceof SimpleBlock) {
                SimpleBlock sb = ((SimpleBlock) block);
                AdvanceBlockReference aBlockRef = new AdvanceBlockReference();
                aBlockRef.id = block.getId();
                aBlockRef.parent = aCompositeBlock;
                aBlockRef.type = sb.description.id;
                saveLocation(aBlockRef.keywords, aBlockRef.visuals, block);
                aCompositeBlock.blocks.put(aBlockRef.id, aBlockRef);
                if (sb.varargs != null && !sb.varargs.isEmpty()) {
                    aBlockRef.varargs.putAll(sb.varargs);
                }
            } else if (block instanceof CompositeBlock) {
                AdvanceCompositeBlock aCompositeBlockChild = build((CompositeBlock) block);
                saveLocation(aCompositeBlockChild.keywords, aCompositeBlockChild.visuals, block);
                aCompositeBlock.composites.put(aCompositeBlockChild.id, aCompositeBlockChild);
            } else if (block instanceof ConstantBlock) {
                ConstantBlock cb = (ConstantBlock) block;
                AdvanceConstantBlock aConstantBlock = new AdvanceConstantBlock();
                aConstantBlock.id = cb.getId();
                //aConstantBlock.displayName = null;
                //aConstantBlock.documentation = null;
                aConstantBlock.typeString = cb.getConstant().typeString;
                aConstantBlock.type = cb.getConstant().type;
                aConstantBlock.value = cb.getConstant().value;
                saveLocation(aConstantBlock.keywords, aConstantBlock.visuals, block);
                aCompositeBlock.constants.put(aConstantBlock.id, aConstantBlock);
            }
        }
        for (BlockBind bind : parent.getBinds()) {
            AdvanceBlockBind aBlockBind = new AdvanceBlockBind();
            aBlockBind.id = bind.id;
            aBlockBind.parent = aCompositeBlock;
            if (bind.source.owner != parent) {
                aBlockBind.sourceBlock = bind.source.owner.getId();
            }
            aBlockBind.sourceParameter = bind.source.getId();
            if (bind.destination.owner != parent) {
                aBlockBind.destinationBlock = bind.destination.owner.getId();
            }
            aBlockBind.destinationParameter = bind.destination.getId();
            aCompositeBlock.bindings.add(aBlockBind);
        }
        return aCompositeBlock;
    }

    private static AdvanceCompositeBlockParameterDescription convert(AdvanceBlockParameterDescription s) {
        AdvanceCompositeBlockParameterDescription d = new AdvanceCompositeBlockParameterDescription();
        d.displayName = s.displayName;
        d.documentation = s.documentation;
        d.id = s.id;
        d.type = s.type;
        return d;
    }

    private static AdvanceBlockVisuals saveLocation(List<String> keywords, AdvanceBlockVisuals abv, AbstractBlock block) {
        for (String s : keywords) {
            if (s.startsWith("location(")) {
                keywords.remove(s);
                break;
            }
        }
        Point p = block.getLocation();
        if (p != null) {
            abv.x = p.x;
            abv.y = p.y;
        }
        return abv;
    }

    private static void readLocation(List<String> keywords, AdvanceBlockVisuals abv, AbstractBlock block) {
        for (String s : keywords) {
            if (s.startsWith("location(")) {
                String[] v = s.substring("location(".length(), s.length() - 1).split(";");
                block.setLocation(new Point(Integer.parseInt(v[0]), Integer.parseInt(v[1])));
                return;
            }
        }
        block.setLocation(new Point(abv.x, abv.y));
    }
}
