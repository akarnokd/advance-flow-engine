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
package eu.advance.logistics.flow.editor.diagram;

import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.datatransfer.Transferable;
import java.lang.ref.WeakReference;

import org.netbeans.api.visual.action.AcceptProvider;
import org.netbeans.api.visual.action.ConnectorState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.nodes.Node;
import org.openide.nodes.NodeTransfer;

import eu.advance.logistics.flow.editor.model.FlowDescription;

/**
 *
 * @author TTS
 */
class PaletteAcceptProvider implements AcceptProvider {

    protected FlowScene scene;
    private FlowDescription flowDescription;
    protected Rectangle dirty;
    private WeakReference<Transferable> activeTransferableRef;
    private WeakReference<Image> activeImageRef;

    PaletteAcceptProvider(FlowScene scene, FlowDescription flowDescription) {
        this.scene = scene;
        this.flowDescription = flowDescription;
    }

    @Override
    public ConnectorState isAcceptable(Widget widget, Point point, Transferable transferable) {
        Image dragImage = null;
        Transferable t = (activeTransferableRef != null) ? activeTransferableRef.get() : null;
        if (!transferable.equals(t)) {
            dragImage = getDropImage(transferable);
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
//        final JComponent view = scene.getView();
//        final Graphics2D g2 = (Graphics2D) view.getGraphics();
//        int w = dragImage.getWidth(null);
//        int h = dragImage.getHeight(null);
//        final int border = 4;
//        Point p = scene.convertSceneToView(point);
//        int x = p.x - w + border;
//        int y = p.y - h + border;
//        if (dirty != null) {
//            //Rectangle visRect = view.getVisibleRect();
//            if (x != dirty.x || y != dirty.y) {
//                view.paintImmediately(dirty.x, dirty.y, dirty.width, dirty.height);
//            }
//        } else {
//            dirty = new Rectangle();
//            dirty.width = w;
//            dirty.height = h;
//        }
//        dirty.x = x;
//        dirty.y = y;
//        g2.drawImage(dragImage, AffineTransform.getTranslateInstance(dirty.x, dirty.y), null);

        return ConnectorState.ACCEPT;
    }

    @Override
    public void accept(Widget widget, Point point, Transferable transferable) {
//        final JComponent view = scene.getView();
//        if (dirty != null) {
//            view.paintImmediately(dirty.x, dirty.y, dirty.width, dirty.height);
//            dirty = null;
//        }

        // prendo la categoria dal D&D
        SceneDropAction dropAction = get(transferable, NodeTransfer.DND_COPY_OR_MOVE);
        if (dropAction != null) {
            dropAction.accept(flowDescription, widget.convertLocalToScene(point));
        }
    }

    private Image getDropImage(Transferable t) {
        SceneDropAction dropAction = get(t, NodeTransfer.DND_COPY_OR_MOVE);
        return (dropAction != null) ? dropAction.getImage() : null;
    }

    private static SceneDropAction get(Transferable t, int type) {
        final Node[] nodes = NodeTransfer.nodes(t, type);
        if (nodes != null && nodes.length == 1) {
            return nodes[0].getLookup().lookup(SceneDropAction.class);
        }
        return null;
    }
}
