/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.advance.logistics.flow.editor.diagram;

import eu.advance.logistics.flow.editor.model.AbstractBlock;
import eu.advance.logistics.flow.editor.model.FlowDescriptionChange;
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

/**
 *
 * @author dalmaso
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

        Point loc = widget.getPreferredLocation();
        if (loc != null) {
            Object obj = scene.findObject(widget);
            if (obj instanceof AbstractBlock) {
                ((AbstractBlock) obj).setLocation(loc);
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
