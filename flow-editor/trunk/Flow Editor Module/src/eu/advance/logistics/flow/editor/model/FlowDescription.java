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
import com.google.common.io.Closeables;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceCompilationResult;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.List;
import org.openide.util.Exceptions;

/**
 *
 * @author TTS
 */
public class FlowDescription extends CompositeBlock {

    public static FlowDescription create(AdvanceCompositeBlock compositeBlock) {
        return FlowDescriptionIO.create(compositeBlock);
    }
    private List<FlowDescriptionListener> listeners = Lists.newArrayList();
    private CompositeBlock activeBlock;
    private AdvanceCompilationResult compilationResult;

    public FlowDescription(String id) {
        super(id);
    }

    public CompositeBlock getActiveBlock() {
        return activeBlock;
    }

    public void setActiveBlock(CompositeBlock activeBlock) {
        if (this.activeBlock != activeBlock) {
            CompositeBlock old = this.activeBlock;
            this.activeBlock = activeBlock;
            fire(FlowDescriptionChange.ACTIVE_COMPOSITE_BLOCK_CHANGED, old, activeBlock);
        }
    }

    @Override
    public FlowDescription getFlowDiagram() {
        return this;
    }

    public void fire(FlowDescriptionChange event, Object... params) {
        int n = listeners.size();
        FlowDescriptionListener[] temp = listeners.toArray(new FlowDescriptionListener[n]);
        for (int i = 0; i < n; i++) {
            temp[i].flowDescriptionChanged(event, params);
        }
    }

    public void addListener(FlowDescriptionListener l) {
        listeners.add(l);
    }

    public void removeListener(FlowDescriptionListener l) {
        listeners.remove(l);
    }

    public AdvanceCompositeBlock build() {
        return FlowDescriptionIO.build(this);
    }

    public static void save(OutputStream s, AdvanceCompositeBlock compositeBlock) throws IOException {
        XElement root = AdvanceCompositeBlock.serializeFlow(compositeBlock);
        BufferedWriter out = null;
        try {
            out = new BufferedWriter(new OutputStreamWriter(s));

            // temporary header fix
            String header = "<?xml version='1.0' encoding='UTF-8'?>\n<flow-description xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:noNamespaceSchemaLocation=\"flow-description.xsd\">";
            // Fix serialization bug: we replace the wrong root element 
            // <flow-descriptor> with the correct <flow-description>.
            String str = root.toString();
            str = str.replace("<flow-descriptor>", header);
            str = str.replace("</flow-descriptor>", "</flow-description>");

            out.write(str);
            out.flush();
        } finally {
            Closeables.closeQuietly(out);
        }

    }

    public static AdvanceCompositeBlock load(InputStream s) throws IOException {
        XElement root = null;
        InputStream in = null;
        try {
            in = new BufferedInputStream(s);
            root = XElement.parseXML(in);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Closeables.closeQuietly(in);
        }
        return (root != null) ? AdvanceCompositeBlock.parseFlow(root) : null;
    }

    public void setCompilationResult(AdvanceCompilationResult result) {
        compilationResult = result;
        fire(FlowDescriptionChange.COMPILATION_RESULT, result);
    }

    public AdvanceCompilationResult getCompilationResult() {
        return compilationResult;
    }
    
}
