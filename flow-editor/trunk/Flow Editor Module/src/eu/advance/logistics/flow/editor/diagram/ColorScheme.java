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
import java.awt.Image;
import java.awt.Stroke;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;

/**
 *
 * @author TTS
 */
public class ColorScheme {

    static final Color COLOR_NORMAL = new Color(0x56B230); // connections
    static final Color COLOR_NORMAL_ERROR = new Color(0xE61717); // connections
    static final Color COLOR_NORMAL_WARNING = new Color(0xE6C000); // connections
    private static final Color COLOR_HOVERED = Color.BLACK;
    private static final Color COLOR_SELECTED = new Color(0xA0EC80);
    private static final Color COLOR_SELECTED_ERROR = new Color(0xF66262);
    private static final Color COLOR_SELECTED_WARNING = new Color(0xF66200);
    static final Color COLOR_HIGHLIGHTED = new Color(0xC4ECB4);
    static final Color COLOR_HIGHLIGHTED_ERROR = new Color(0xFF7373);
    static final Color COLOR_HIGHLIGHTED_WARNING = new Color(0xFF7300);
    static final Color COLOR_BORDER = new Color(0x56B230);
    static final Color COLOR1 = new Color(0xFFFFFF);
    static final Color COLOR2 = new Color(0xACEB95);
    static final Border BORDER_PARAM = new ParamBorder(Color.WHITE, 4, new Color(0xC0C0C0));
    static final Border BORDER_BLOCK = new BlockBorder(COLOR_BORDER, 1,
            COLOR1, COLOR2);
    static final Color CONST_COLOR_SELECTED = new Color(0x7D9C9F);
    static final Border CONST_BORDER_BLOCK = new BlockBorder(CONST_COLOR_SELECTED, 1,
            new Color(0xDFEFF0), new Color(0xBDD8DA));
    static final Color BORDER_CATEGORY_BACKGROUND = new Color(0x56B230);
    static final Border BORDER_MINIMIZE =
            BorderFactory.createRoundedBorder(2, 2, null, COLOR_NORMAL);
    static final Border BORDER_PIN = BorderFactory.createOpaqueBorder(2, 4, 2, 4);
    private static final Border BORDER_PIN_HOVERED =
            BorderFactory.createLineBorder(2, 4, 2, 4, Color.BLACK);
    public static javax.swing.border.Border PAGE_BORDER;
    static final PointShape POINT_SHAPE_IMAGE = PointShapeFactory.createImagePointShape(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/pin.png")); // NOI18N
    static final Stroke connectionStroke = new BasicStroke(2);

    {
        javax.swing.border.Border outer = javax.swing.BorderFactory.createEmptyBorder(4, 4, 4, 4);
        javax.swing.border.Border inner = javax.swing.BorderFactory.createLineBorder(Color.LIGHT_GRAY);
        PAGE_BORDER = javax.swing.BorderFactory.createCompoundBorder(outer, inner);
    }

    public ColorScheme() {
    }

    void installUI(ParamWidget widget) {
        widget.setBorder(PAGE_BORDER);
        widget.setOpaque(true);
    }

    void installUI(BlockWidget widget) {
        widget.setBorder(BORDER_BLOCK);
        widget.setOpaque(false);

        Widget header = widget.getHeader();
        header.setBorder(BORDER_PIN);
        header.setBackground(COLOR_SELECTED);
        header.setOpaque(false);

        Widget minimize = widget.getMinimizeButton();
        minimize.setBorder(BORDER_MINIMIZE);

        Widget pinsSeparator = widget.getPinsSeparator();
        pinsSeparator.setForeground(BORDER_CATEGORY_BACKGROUND);
    }

    void updateUI(BlockWidget widget, ObjectState previousState, ObjectState state) {
        if (!previousState.isSelected() && state.isSelected()) {
            widget.bringToFront();
        } else if (!previousState.isHovered() && state.isHovered()) {
            widget.bringToFront();
        }

        Widget header = widget.getHeader();
        header.setOpaque(state.isSelected());
        header.setBorder(state.isFocused() || state.isHovered() ? BORDER_PIN_HOVERED : BORDER_PIN);
    }

    void installUI(ConstantBlockWidget widget) {
        widget.setBorder(CONST_BORDER_BLOCK);
        widget.setOpaque(false);

        Widget header = widget.getHeader();
        header.setBorder(BORDER_PIN);
        header.setBackground(CONST_COLOR_SELECTED);
        header.setOpaque(false);
    }

    void updateUI(ConstantBlockWidget widget, ObjectState previousState, ObjectState state) {
        if (!previousState.isSelected() && state.isSelected()) {
            widget.bringToFront();
        } else if (!previousState.isHovered() && state.isHovered()) {
            widget.bringToFront();
        }

        Widget header = widget.getHeader();
        header.setOpaque(state.isSelected());
        header.setBorder(state.isFocused() || state.isHovered() ? BORDER_PIN_HOVERED : BORDER_PIN);
    }

    boolean isNodeMinimizeButtonOnRight(BlockWidget widget) {
        return true;
    }

    Image getMinimizeWidgetImage(BlockWidget widget) {
        return widget.isMinimized()
                ? ImageUtilities.loadImage("org/netbeans/modules/visual/resources/vmd-expand.png") // NOI18N
                : ImageUtilities.loadImage("org/netbeans/modules/visual/resources/vmd-collapse.png"); // NOI18N
    }

    void installUI(BlockConnectionWidget widget) {
        widget.setStroke(connectionStroke);
        widget.setSourceAnchorShape(AnchorShape.NONE);
        widget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints(true);
    }

    void updateUI(BlockConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isHovered()) {
            widget.setForeground(COLOR_HOVERED);
        } else if (state.isSelected()) {
            widget.setForeground(widget.isError() ? COLOR_SELECTED_ERROR : (widget.isWarning() ? COLOR_SELECTED_WARNING : COLOR_SELECTED));
        } else if (state.isHighlighted()) {
            widget.setForeground(widget.isError() ? COLOR_HIGHLIGHTED_ERROR : (widget.isWarning() ? COLOR_HIGHLIGHTED_WARNING : COLOR_HIGHLIGHTED));
        } else if (state.isFocused()) {
            widget.setForeground(COLOR_HOVERED);
        } else {
            widget.setForeground(widget.isError() ? COLOR_NORMAL_ERROR : (widget.isWarning() ? COLOR_NORMAL_WARNING : COLOR_NORMAL));
        }

        if (state.isSelected()) {
            widget.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        } else {
            widget.setControlPointShape(PointShape.NONE);
            widget.setEndPointShape(POINT_SHAPE_IMAGE);
        }
    }

    void installUI(PinWidget widget) {
        widget.setBorder(BORDER_PIN);
        widget.setBackground(COLOR_SELECTED);
        widget.setOpaque(false);
    }

    void updateUI(PinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque(state.isSelected());
        widget.setBorder(state.isFocused() || state.isHovered() ? BORDER_PIN_HOVERED : BORDER_PIN);
    }

    int getNodeAnchorGap(BlockAnchor anchor) {
        return 8;
    }
}