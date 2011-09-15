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

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.geom.RoundRectangle2D;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author TTS
 */
public class ParamWidget extends Widget {

    ParamWidget(Scene scene, ColorScheme scheme) {
        super(scene);

        setLayout(LayoutFactory.createVerticalFlowLayout());
        setMinimumSize(new Dimension(8, 8));

        scheme.installUI(this);
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    /**
     * Attaches a pin widget to the node widget.
     * @param widget the pin widget
     */
    public void attachPinWidget(Widget widget) {
        widget.setCheckClipping(true);
        addChild(widget);
    }

//    @Override
//    protected void paintWidget() {
//        Rectangle bounds = getBounds();
//        if (bounds != null) {
//            Graphics2D gr = getGraphics();
//            gr.setPaint(getBackground());
//            gr.fill(new RoundRectangle2D.Float(bounds.x, bounds.y, bounds.width, bounds.height, 8, 8));
//        }
//    }
}
