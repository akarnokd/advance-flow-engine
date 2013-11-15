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

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceConstantBlock;
import hu.akarnokd.utils.xml.XNElement;

/**
 * <b>ConstantBlock</b>
 *
 * @author TTS
 */
public class ConstantBlock extends AbstractBlock {

    public final static String DEFAULT_PARAMETER_NAME = "constant";
    private AdvanceConstantBlock constant;
    private BlockParameter parameter;

    public ConstantBlock(String id) {
        this.id = id;

        AdvanceBlockParameterDescription dummy = new AdvanceBlockParameterDescription();
        dummy.id = DEFAULT_PARAMETER_NAME;
        parameter = new BlockParameter(this, dummy, BlockParameter.Type.OUTPUT);
        outputParameters.put(dummy.id, parameter);
    }

    public AdvanceConstantBlock getConstant() {
        return constant;
    }

    public void setConstant(AdvanceConstantBlock constant) {
        AdvanceConstantBlock old = this.constant;
        this.constant = constant;
        getFlowDiagram().fire(FlowDescriptionChange.CONSTANT_BLOCK_CHANGED, this, old, constant);
    }
    
    public BlockParameter getParameter() {
        return parameter;
    }
    
    public String getTypeAsString() {
        if (constant != null) {
            return constant.value.name;
        }
        return org.openide.util.NbBundle.getBundle(ConstantBlock.class).getString("NO_TYPE");
    }

    public String getValueAsString() {
        if (constant != null) {
            if (constant.value.content != null) {
                return constant.value.content;
            }
            return constant.typeString;
        }
        return org.openide.util.NbBundle.getBundle(ConstantBlock.class).getString("NO_VALUE");
    }

    @Override
    public String toString() {
        return getTypeAsString() + "=" + getValueAsString();
    }

    @Override
    public void destroy() {
        if (parent != null) {
            parent.removeConstant(this);
        }
    }

    @Override
    public ConstantBlock createClone(CompositeBlock newParent) {
        final ConstantBlock cb = new ConstantBlock(id + " (copy)");
        cb.setParent(newParent);
        cb.setTooltip(tooltip);
        cb.setLocation(getLocation());
        cb.constant = clone(constant);
        return cb;
    }

    private static AdvanceConstantBlock clone(AdvanceConstantBlock src) {
        XNElement temp = new XNElement("temp");
        src.save(temp);
        AdvanceConstantBlock cloned  = new AdvanceConstantBlock();
        cloned.load(temp);
        return cloned;
    }
}
