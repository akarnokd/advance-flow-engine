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

import eu.advance.logistics.flow.editor.diagram.FlowDiagramScene;
import eu.advance.logistics.flow.editor.model.BlockCategory;
import eu.advance.logistics.flow.editor.model.SimpleBlock;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.awt.geom.AffineTransform;
import java.lang.ref.WeakReference;
import javax.swing.JComponent;
import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ActionFactory;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;
import org.openide.util.ImageUtilities;

/**
 *
 * @author TTS
 */
public class FlowDiagramFactory {

    private FlowDiagramFactory() {
    }

    static void configure(final FlowDiagramScene scene) {
        final FlowDiagramController controller = scene.getController();
        scene.getActions().addAction(ActionFactory.createAcceptAction(new AcceptProvider() {

            private Rectangle dirty;
            private WeakReference<Transferable> activeTransferableRef;
            private WeakReference<Image> activeImageRef;

            @Override
            public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
                Image dragImage = null;
                Transferable t = (activeTransferableRef != null) ? activeTransferableRef.get() : null;
                if (!transferable.equals(t)) {
                    dragImage = getImageFromCategory(get(transferable, NodeTransfer.DND_COPY_OR_MOVE));
                    if (dragImage != null) {
                        activeImageRef = new WeakReference<Image>(dragImage);
                        activeTransferableRef = new WeakReference<Transferable>(transferable);
                    } else {
                        activeImageRef = null;
                        activeTransferableRef = null;
                    }
                    dirty = null;
                } else if (activeImageRef != null) {
                    dragImage = activeImageRef.get();
                }

                if (dragImage == null) {
                    return ConnectorState.REJECT;
                }

                //
                final JComponent view = scene.getView();
                final Graphics2D g2 = (Graphics2D) view.getGraphics();
                int w = dragImage.getWidth(null);
                int h = dragImage.getHeight(null);
                final int border = 4;
                int x = (int) point.getLocation().getX() - w + border;
                int y = (int) point.getLocation().getY() - h + border;
                if (dirty != null) {
                    //Rectangle visRect = view.getVisibleRect();
                    if (x != dirty.x || y != dirty.y) {
                        view.paintImmediately(dirty.x, dirty.y, dirty.width, dirty.height);
                    }
                } else {
                    dirty = new Rectangle();
                    dirty.width = w;
                    dirty.height = h;
                }
                dirty.x = x;
                dirty.y = y;
                g2.drawImage(dragImage, AffineTransform.getTranslateInstance(dirty.x, dirty.y), null);

                return ConnectorState.ACCEPT;
            }

            @Override
            public void accept(Widget widget, Point point, Transferable transferable) {
                final JComponent view = scene.getView();
                if (dirty != null) {
                    view.paintImmediately(dirty.x, dirty.y, dirty.width, dirty.height);
                    dirty = null;
                }
                // prendo la categoria dal D&D
                final AdvanceBlockDescription desc = get(transferable, NodeTransfer.DND_COPY_OR_MOVE);

                // creo il blocco
                final SimpleBlock block = controller.createBlock(desc);

                // posiziona il blocco
                controller.setLocation(block, widget.convertLocalToScene(point));
            }
        }));
    }

    private static AdvanceBlockDescription get(Transferable t, int type) {
        final Node[] nodes = NodeTransfer.nodes(t, type);
        if (nodes != null && nodes.length == 1) {
            return nodes[0].getLookup().lookup(AdvanceBlockDescription.class);
        }
        return null;
    }

    private static Image getImageFromCategory(AdvanceBlockDescription desc) {
        BlockCategory category = BlockRegistry.getInstance().findByType(desc);
        if (category != null) {
            String url = category.getImage();
            url = url.substring(0, url.length() - 4) + "24.png";
            return ImageUtilities.loadImage("eu/advance/logistics/flow/editor/palette/images/" + url);
        } else {
            return null;
        }
    }
}
