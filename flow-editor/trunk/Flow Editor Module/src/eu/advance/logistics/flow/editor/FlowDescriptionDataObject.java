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

import com.google.common.io.Closeables;
import eu.advance.logistics.flow.model.AdvanceCompositeBlock;
import eu.advance.logistics.xml.typesystem.XElement;
import java.awt.EventQueue;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

public class FlowDescriptionDataObject extends MultiDataObject {

    private FlowDiagramController controller = new FlowDiagramController(this);

    public FlowDescriptionDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        cookies.add(new OpenSupport());
    }

    @Override
    protected Node createNodeDelegate() {
        DataNode node = new DataNode(this, Children.LEAF, getLookup());
        node.setDisplayName(controller.getDisplayName());
        return node;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
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
        ProgressHandle ph = ProgressHandleFactory.createHandle("Loading...");
        ph.start();
        try {
            AdvanceCompositeBlock compositeBlock = controller.get();
            XElement root = new XElement("flow-description");
            compositeBlock.save(root);
            OutputStream out = null;
            try {
                //out = getPrimaryFile().getOutputStream();
                setModified(false);
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            } finally {
                Closeables.closeQuietly(out);
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
            ProgressHandle ph = ProgressHandleFactory.createHandle("Loading...");
            ph.start();
            try {
                XElement root = null;
                InputStream in = null;
                try {
                    in = getPrimaryFile().getInputStream();
                    root = XElement.parseXML(in);
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    Closeables.closeQuietly(in);
                }

                if (root != null) {
                    AdvanceCompositeBlock compositeBlock = new AdvanceCompositeBlock();
                    compositeBlock.load(root);
                    controller.set(compositeBlock);
                    setModified(false);
                } else {
                    String msg = "<html>Error loading flow diagram:<br>" + FileUtil.getFileDisplayName(getPrimaryFile()) + "</html>";
                    NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                }

                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        EditorTopComponent tc = new EditorTopComponent(controller);
                        tc.setActivatedNodes(new Node[]{getNodeDelegate()});
                        tc.open();
                        tc.requestActive();
                    }
                });

            } finally {
                ph.finish();
            }
        }
    }
}
