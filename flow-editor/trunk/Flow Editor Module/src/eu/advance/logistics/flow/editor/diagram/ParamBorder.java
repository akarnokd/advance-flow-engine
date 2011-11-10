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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import org.netbeans.api.visual.border.Border;

/**
 *
 * @author TTS
 */
class ParamBorder implements Border {

    private Color colorBorder;
    private Insets insets;
    private Stroke stroke;
    private Color color1;

    ParamBorder(Color colorBorder, int thickness, Color color1) {
        this.colorBorder = colorBorder;
        this.insets = new Insets(thickness, thickness, thickness, thickness);
        this.stroke = new BasicStroke(thickness);
        this.color1 = color1;
    }

    @Override
    public Insets getInsets() {
        return insets;
    }

    @Override
    public void paint(Graphics2D gr, Rectangle bounds) {
        Shape s = new RoundRectangle2D.Float(bounds.x, bounds.y, bounds.width - 1, bounds.height - 1, 8, 8);
        Shape previousClip = gr.getClip();
        gr.setColor(color1);
        gr.fill(s);
        gr.setColor(colorBorder);
        Stroke previousStroke = gr.getStroke();
        gr.setStroke(stroke);
        gr.draw(new RoundRectangle2D.Float(bounds.x + 0.5f, bounds.y + 0.5f, bounds.width - 1, bounds.height - 1, 8, 8));
        gr.setStroke(previousStroke);
        gr.setClip(previousClip);
    }

    @Override
    public boolean isOpaque() {
        return true;
    }
}
