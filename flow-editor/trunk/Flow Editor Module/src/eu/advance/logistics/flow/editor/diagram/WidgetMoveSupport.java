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

import java.awt.Point;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.netbeans.api.visual.action.AlignWithMoveDecorator;
import org.netbeans.api.visual.action.AlignWithWidgetCollector;
import org.netbeans.api.visual.action.MoveProvider;
import org.netbeans.api.visual.action.MoveStrategy;
import org.netbeans.api.visual.widget.LayerWidget;
import org.netbeans.api.visual.widget.Widget;
import org.netbeans.modules.visual.action.AlignWithSupport;

import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.undo.BlockMoved;
import eu.advance.logistics.flow.editor.undo.UndoRedoSupport;

/**
 *
 * @author TTS
 */
public class WidgetMoveSupport extends AlignWithSupport implements MoveStrategy, MoveProvider {

    private FlowScene scene;

    public WidgetMoveSupport(FlowScene scene, LayerWidget collectionLayer, LayerWidget interractionLayer, AlignWithMoveDecorator decorator) {
        super(new SingleLayerAlignWithWidgetCollector(collectionLayer), interractionLayer, decorator);
        this.scene = scene;
    }

    @Override
    public Point locationSuggested(Widget widget, Point originalLocation, Point suggestedLocation) {
        Point widgetLocation = widget.getLocation();
        Rectangle widgetBounds = widget.getBounds();
        Rectangle bounds = widget.convertLocalToScene(widgetBounds);
        bounds.translate(suggestedLocation.x - widgetLocation.x, suggestedLocation.y - widgetLocation.y);
        Point point = super.locationSuggested(widget, bounds, widget.getParentWidget().convertLocalToScene(suggestedLocation), true, true, true, true);
        return widget.getParentWidget().convertSceneToLocal(point);
    }

    @Override
    public void movementStarted(Widget widget) {
        show();
    }

    @Override
    public void movementFinished(Widget widget) {
        hide();

        UndoRedoSupport urs = scene.getUndoRedoSupport();
        Point loc = widget.getPreferredLocation();
        if (loc != null) {
            Object obj = scene.findObject(widget);
            if (obj instanceof AbstractBlock) {
                AbstractBlock block =((AbstractBlock) obj);
                urs.start();
                Point old = block.getLocation();
                block.setLocation(loc);
                urs.commit(new BlockMoved(block, old, loc));
            }
        }
    }

    @Override
    public Point getOriginalLocation(Widget widget) {
        return widget.getPreferredLocation();
    }

    @Override
    public void setNewLocation(Widget widget, Point location) {
        widget.setPreferredLocation(location);
    }

    private static class SingleLayerAlignWithWidgetCollector implements AlignWithWidgetCollector {

        private LayerWidget collectionLayer;

        public SingleLayerAlignWithWidgetCollector(LayerWidget collectionLayer) {
            this.collectionLayer = collectionLayer;
        }

        @Override
        public java.util.List<Rectangle> getRegions(Widget movingWidget) {
            List<Widget> children = collectionLayer.getChildren();
            List<Rectangle> regions = new ArrayList<Rectangle>(children.size());
            for (Widget widget : children) {
                if (widget != movingWidget) {
                    regions.add(widget.convertLocalToScene(widget.getBounds()));
                }
            }
            return regions;
        }
    }
}
