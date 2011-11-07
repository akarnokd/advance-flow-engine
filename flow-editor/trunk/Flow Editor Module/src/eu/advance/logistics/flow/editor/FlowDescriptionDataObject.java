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
package eu.advance.logistics.flow.editor;

import eu.advance.logistics.flow.editor.diagram.FlowScene;
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
import eu.advance.logistics.flow.editor.model.FlowDescriptionListener;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import java.awt.EventQueue;
import java.io.IOException;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObjectExistsException;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.MultiFileLoader;
import org.openide.nodes.CookieSet;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.util.Lookup;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

public class FlowDescriptionDataObject extends MultiDataObject {

    private InstanceContent instanceContent = new InstanceContent();
    private Lookup lookup;

    public FlowDescriptionDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        final CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        cookies.add(new OpenSupport());


        lookup = new ProxyLookup(cookies.getLookup(), new AbstractLookup(instanceContent));
    }

    public InstanceContent getInstanceContent() {
        return instanceContent;
    }

    @Override
    protected Node createNodeDelegate() {
        DataNode node = new DataNode(this, Children.LEAF, getLookup());
        node.setDisplayName(getName());
        return node;
    }

    @Override
    public Lookup getLookup() {
        return lookup;
    }

    @Override
    public void setModified(boolean modified) {
        if (modified != isModified()) {
            if (modified) {
                getCookieSet().assign(SaveCookie.class, new SaveSupport());
            } else {
                SaveCookie save = getCookie(SaveCookie.class);
                if (save != null) {
                    getCookieSet().remove(save);
                }
            }
        }
        super.setModified(modified);
    }

    void save() throws IOException {
        ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getBundle(FlowDescriptionDataObject.class).getString("LOADING"));
        ph.start();
        try {
            FlowDescription fd = getLookup().lookup(FlowDescription.class);
            fd.fire(FlowDescriptionChange.SAVING, fd);
            try {
                FlowDescription.save(getPrimaryFile().getOutputStream(), fd.build());
                setModified(false);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        } finally {
            ph.finish();
        }
    }

    private class SaveSupport implements SaveCookie {

        @Override
        public void save() throws IOException {
            FlowDescriptionDataObject.this.save();
        }
    }

    private class OpenSupport implements OpenCookie {

        @Override
        public void open() {
            EditorTopComponent tc = getLookup().lookup(EditorTopComponent.class);
            if (tc != null && tc.getDataObject() != null) {
                tc.open();
                tc.requestActive();
                return;
            }
            ProgressHandle ph = ProgressHandleFactory.createHandle(NbBundle.getBundle(FlowDescriptionDataObject.class).getString("LOADING"));
            ph.start();
            try {
                final AdvanceCompositeBlock compositeBlock = FlowDescription.load(getPrimaryFile().getInputStream());
                if (compositeBlock != null) {
                    compositeBlock.id = getPrimaryFile().getName();
                    final FlowDescription flowDesc = FlowDescription.create(compositeBlock);
                    flowDesc.setActiveBlock(flowDesc);
                    flowDesc.addListener(new FlowDescriptionListener() {

                        @Override
                        public void flowDescriptionChanged(FlowDescriptionChange event, Object... params) {
                            if (!(event == FlowDescriptionChange.ACTIVE_COMPOSITE_BLOCK_CHANGED
                                    || event == FlowDescriptionChange.COMPILATION_RESULT
                                    || event == FlowDescriptionChange.CLOSED)) {
                                setModified(true);
                            }
                        }
                    });
                    getInstanceContent().add(flowDesc);

                    final UndoRedoSupport urs = new UndoRedoSupport();
                    flowDesc.addListener(urs);
                    getInstanceContent().add(urs);

                    setModified(false);
                    EventQueue.invokeLater(new Runnable() {

                        @Override
                        public void run() {
                            getInstanceContent().add(FlowScene.create(urs, flowDesc));
                            EditorTopComponent tc;
                            tc = getLookup().lookup(EditorTopComponent.class);
                            if (tc == null) {
                                tc = new EditorTopComponent();
                                getInstanceContent().add(tc);
                            }
                            tc.setDataObject(FlowDescriptionDataObject.this);
                            tc.setActivatedNodes(new Node[]{getNodeDelegate()});
                            tc.open();
                            tc.requestActive();
                            
                            flowDesc.setCompilationResult(BlockRegistry.getInstance()
                                    .verify(compositeBlock));
                        }
                    });
                } else {
                    final String msg = "<html>" + NbBundle.getBundle(FlowDescriptionDataObject.class).getString("ERROR_LOADING_HTML") + "<br>" + FileUtil.getFileDisplayName(getPrimaryFile()) + "</html>";
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                ph.finish();
            }
        }
    }
}
