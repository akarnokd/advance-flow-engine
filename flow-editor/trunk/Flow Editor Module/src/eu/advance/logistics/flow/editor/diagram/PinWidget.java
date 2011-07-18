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
import org.netbeans.api.visual.anchor.Anchor;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.model.ObjectState;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.LabelWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author TTS
 */
public class PinWidget extends Widget {

    private ColorScheme scheme;
    private ImageWidget iconWidget;
    private LabelWidget nameWidget;
    private BlockAnchor anchor;

    /**
     * Creates a pin widget with a specific color scheme.
     * @param scene the scene
     * @param scheme the color scheme
     */
    public PinWidget(Scene scene, ColorScheme scheme) {
        super(scene);
        assert scheme != null;
        this.scheme = scheme;
        
        setLayout(LayoutFactory.createHorizontalFlowLayout(LayoutFactory.SerialAlignment.CENTER, 8));
        addChild(iconWidget = new ImageWidget(scene));
        addChild(nameWidget = new LabelWidget(scene));

        scheme.installUI(this);
        notifyStateChanged(ObjectState.createNormal(), ObjectState.createNormal());
    }

    /**
     * Called to notify about the change of the widget state.
     * @param previousState the previous state
     * @param state the new state
     */
    protected void notifyStateChanged(ObjectState previousState, ObjectState state) {
        scheme.updateUI(this, previousState, state);
    }

    /**
     * Returns a pin name widget.
     * @return the pin name widget
     */
    public Widget getPinNameWidget() {
        return nameWidget;
    }

    /**
     * Sets a pin name.
     * @param name the pin name
     */
    public void setPinName(String name) {
        nameWidget.setLabel(name);
    }

    /**
     * Returns a pin name.
     * @return the pin name
     */
    public String getPinName() {
        return nameWidget.getLabel();
    }

    public void setLabelIcon(Image image) {
        iconWidget.setImage(image);
    }

    /**
     * Creates a horizontally oriented anchor similar to VMDNodeWidget.createAnchorPin
     * @return the anchor
     */
    public Anchor createAnchor() {
        if (anchor == null) {
            anchor = new BlockAnchor(this, false, scheme);
        }
        return anchor;
    }
}
