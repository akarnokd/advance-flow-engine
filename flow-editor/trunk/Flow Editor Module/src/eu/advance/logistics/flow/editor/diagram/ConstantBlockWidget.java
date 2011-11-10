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
import java.awt.Dimension;
import java.awt.Font;

import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

import eu.advance.logistics.flow.editor.model.ConstantBlock;

/**
 *
 * @author TTS
 */
public class ConstantBlockWidget extends Widget {

    private Widget header;
    private LabelWidget valueWidget;
    private LabelWidget typeWidget;
    private Anchor nodeAnchor;
    private ColorScheme scheme;
    private ConstantBlock block;

    ConstantBlockWidget(Scene scene, ColorScheme scheme, ConstantBlock constBlock) {
        super(scene);
        this.scheme = scheme;
        this.block = constBlock;

        nodeAnchor = new BlockAnchor(this, false, scheme);

        setLayout(LayoutFactory.createVerticalFlowLayout());
        setMinimumSize(new Dimension(8, 8));

        header = new Widget(scene);
        header.setLayout(LayoutFactory.createVerticalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 4));
        addChild(header);

        typeWidget = new LabelWidget(scene);
        typeWidget.setForeground(Color.DARK_GRAY);
        typeWidget.setFont(scene.getDefaultFont().deriveFont(10.0f));
        header.addChild(typeWidget);

        valueWidget = new LabelWidget(scene);
        valueWidget.setFont(scene.getDefaultFont().deriveFont(Font.BOLD));
        header.addChild(valueWidget);

        update();

        scheme.installUI(this);
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    void update() {
        setNodeType(block.getTypeAsString());
        setNodeValue(block.getValueAsString());
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    @Override
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        scheme.updateUI(this, previousState, state);
    }

    private void setNodeValue(String value) {
        valueWidget.setLabel(value != null ? value.trim() : null);
    }

    private void setNodeType(String type) {
        typeWidget.setLabel(type != null ? "[" + type + "]" : null);
    }

    /**
     * Returns a node anchor.
     * @return the node anchor
     */
    Anchor getNodeAnchor() {
        return nodeAnchor;
    }

    /**
     * Returns a header widget.
     * @return the header widget
     */
    Widget getHeader() {
        return header;
    }
}
