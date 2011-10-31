package eu.advance.logistics.flow.editor.model;

import com.google.common.collect.Lists;
import com.google.common.io.Closeables;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
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
        for (FlowDescriptionListener l : listeners) {
            l.flowDescriptionChanged(event, params);
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
}
