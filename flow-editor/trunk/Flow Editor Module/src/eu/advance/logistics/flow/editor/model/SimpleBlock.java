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

import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockParameterDescription;
import java.util.Map;

/**
 * <b>SimpleBlock</b>
 *
 * @author TTS
 */
public class SimpleBlock extends AbstractBlock {

    public final AdvanceBlockDescription description;
    /**
     * The saved varargs counts used by this block.
     */
    public Map<String, Integer> varargs;

    private SimpleBlock(AdvanceBlockDescription desc){
        this.description = desc;
    }

    public SimpleBlock(String id, AdvanceBlockDescription desc) {
        this.id = id;
        this.description = desc;
        
        setTooltip(desc.tooltip);

        for (AdvanceBlockParameterDescription param : desc.inputs.values()) {
            inputParameters.put(param.id, new BlockParameter(this, param, BlockParameter.Type.INPUT));
        }
        for (AdvanceBlockParameterDescription param : desc.outputs.values()) {
            outputParameters.put(param.id, new BlockParameter(this, param, BlockParameter.Type.OUTPUT));
        }
    }

    @Override
    public void destroy() {
        if (parent != null) {
            parent.removeBlock(this);            
        }
    }

    @Override
    public SimpleBlock createClone(CompositeBlock newParent) {
        final SimpleBlock sb = new SimpleBlock(description.copy());
        sb.setParent(newParent);
        sb.copyValues(this);

        return sb;
    }
}
