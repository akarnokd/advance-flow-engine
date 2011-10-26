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
import eu.advance.logistics.flow.engine.model.fd.AdvanceBlockDescription;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
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

public class BlockRegistryDataObject extends MultiDataObject {
        private final static String ICON_PATH = "eu/advance/logistics/flow/editor/palette/images/block.png";

    public BlockRegistryDataObject(FileObject pf, MultiFileLoader loader) throws DataObjectExistsException, IOException {
        super(pf, loader);
        CookieSet cookies = getCookieSet();
        //cookies.add((Node.Cookie) DataEditorSupport.create(this, getPrimaryEntry(), cookies));
        cookies.add(new Open());
    }

    @Override
    protected Node createNodeDelegate() {
        DataNode node = new DataNode(this, Children.LEAF, getLookup());
        node.setIconBaseWithExtension(ICON_PATH);
        return node;
    }

    @Override
    public Lookup getLookup() {
        return getCookieSet().getLookup();
    }

    private class Open implements OpenCookie {

        @Override
        public void open() {
            FileObject fo = getPrimaryFile();
            StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(BlockRegistryDataObject.class, "READING",
                    FileUtil.getFileDisplayName(fo)));
            try {
                read(fo.getInputStream());
            } catch (FileNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
    }

    public static void read(InputStream in) {
        XElement root = null;
        try {
            root = XElement.parseXML(in);
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        } finally {
            Closeables.closeQuietly(in);
        }
        if (root != null) {
            BlockRegistry r = BlockRegistry.getInstance();
            List<AdvanceBlockDescription> blocks = AdvanceBlockDescription.parse(root);
            for (AdvanceBlockDescription bd : blocks) {
                if (bd.displayName == null) {
                    bd.displayName = bd.id;
                }
                r.findOrCreate(bd.category).addType(bd);
            }
                        StatusDisplayer.getDefault().setStatusText(
                    NbBundle.getMessage(BlockRegistryDataObject.class, "REGISTRY_STATUS",
                    r.getBlockDescriptionCount(), r.getCategoryCount()));

        }
    }

}
