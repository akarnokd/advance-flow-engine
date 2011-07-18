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

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import org.netbeans.api.visual.anchor.AnchorShape;
import org.netbeans.api.visual.anchor.PointShape;
import org.netbeans.api.visual.anchor.PointShapeFactory;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.border.BorderFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;
import org.openide.util.ImageUtilities;

/**
 *
 * @author TTS
 */
public class ColorScheme {

    static final Color COLOR_NORMAL = new Color(0x56B230); // connections
    private static final Color COLOR_HOVERED = Color.BLACK;
    private static final Color COLOR_SELECTED = new Color(0xA0EC80);
    static final Color COLOR_HIGHLIGHTED = new Color(0xC4ECB4);
    static final Color COLOR_BORDER = new Color(0x56B230);
    static final Color COLOR1 = new Color(0xFFFFFF);
    static final Color COLOR2 = new Color(0xEDFFE6);
    public static final Border BORDER_NODE = new BlockBorder(COLOR_BORDER, 1, 
            COLOR1, COLOR2);
    static final Color BORDER_CATEGORY_BACKGROUND = new Color(0x56B230);
    static final Border BORDER_MINIMIZE =
            BorderFactory.createRoundedBorder(2, 2, null, COLOR_NORMAL);
    static final Border BORDER_PIN = BorderFactory.createOpaqueBorder(2, 8, 2, 8);
    private static final Border BORDER_PIN_HOVERED =
            BorderFactory.createLineBorder(2, 8, 2, 8, Color.BLACK);
    static final PointShape POINT_SHAPE_IMAGE = PointShapeFactory.createImagePointShape(ImageUtilities.loadImage("eu/advance/logistics/flow/editor/images/pin.png")); // NOI18N

    public ColorScheme() {
    }

    public void installUI(BlockWidget widget) {
        widget.setBorder(BORDER_NODE);
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

    public void updateUI(BlockWidget widget, ObjectState previousState, ObjectState state) {
        if (!previousState.isSelected() && state.isSelected()) {
            widget.bringToFront();
        } else if (!previousState.isHovered() && state.isHovered()) {
            widget.bringToFront();
        }

        Widget header = widget.getHeader();
        header.setOpaque(state.isSelected());
        header.setBorder(state.isFocused() || state.isHovered() ? BORDER_PIN_HOVERED : BORDER_PIN);
    }

    public boolean isNodeMinimizeButtonOnRight(BlockWidget widget) {
        return false;
    }

    public Image getMinimizeWidgetImage(BlockWidget widget) {
        return widget.isMinimized()
                ? ImageUtilities.loadImage("org/netbeans/modules/visual/resources/vmd-expand.png") // NOI18N
                : ImageUtilities.loadImage("org/netbeans/modules/visual/resources/vmd-collapse.png"); // NOI18N
    }

    public Widget createPinCategoryWidget(BlockWidget widget, String categoryDisplayName) {
        return createPinCategoryWidgetCore(widget, categoryDisplayName, true);
    }

    public void installUI(BlockConnectionWidget widget) {
        widget.setSourceAnchorShape(AnchorShape.NONE);
        widget.setTargetAnchorShape(AnchorShape.TRIANGLE_FILLED);
        widget.setPaintControlPoints(true);
    }

    public void updateUI(BlockConnectionWidget widget, ObjectState previousState, ObjectState state) {
        if (state.isHovered()) {
            widget.setForeground(COLOR_HOVERED);
        } else if (state.isSelected()) {
            widget.setForeground(COLOR_SELECTED);
        } else if (state.isHighlighted()) {
            widget.setForeground(COLOR_HIGHLIGHTED);
        } else if (state.isFocused()) {
            widget.setForeground(COLOR_HOVERED);
        } else {
            widget.setForeground(COLOR_NORMAL);
        }

        if (state.isSelected()) {
            widget.setControlPointShape(PointShape.SQUARE_FILLED_SMALL);
            widget.setEndPointShape(PointShape.SQUARE_FILLED_BIG);
        } else {
            widget.setControlPointShape(PointShape.NONE);
            widget.setEndPointShape(POINT_SHAPE_IMAGE);
        }
    }

    public void installUI(PinWidget widget) {
        widget.setBorder(BORDER_PIN);
        widget.setBackground(COLOR_SELECTED);
        widget.setOpaque(false);
    }

    public void updateUI(PinWidget widget, ObjectState previousState, ObjectState state) {
        widget.setOpaque(state.isSelected());
        widget.setBorder(state.isFocused() || state.isHovered() ? BORDER_PIN_HOVERED : BORDER_PIN);
    }

    int getNodeAnchorGap(BlockAnchor anchor) {
        return 8;
    }

    private static Widget createPinCategoryWidgetCore(BlockWidget widget, String categoryDisplayName, boolean changeFont) {
        Scene scene = widget.getScene();
        LabelWidget label = new LabelWidget(scene, categoryDisplayName);
        label.setOpaque(true);
        label.setBackground(BORDER_CATEGORY_BACKGROUND);
        label.setForeground(Color.GRAY);
        if (changeFont) {
            Font fontPinCategory = scene.getDefaultFont().deriveFont(10.0f);
            label.setFont(fontPinCategory);
        }
        label.setAlignment(LabelWidget.Alignment.CENTER);
        label.setCheckClipping(true);
        return label;
    }
}