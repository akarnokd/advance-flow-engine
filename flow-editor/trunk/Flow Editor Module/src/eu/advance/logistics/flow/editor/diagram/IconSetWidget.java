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

import java.awt.Cursor;
import java.awt.Image;

import org.netbeans.api.visual.action.WidgetAction;
import org.netbeans.api.visual.border.Border;
import org.netbeans.api.visual.layout.LayoutFactory;
import org.netbeans.api.visual.widget.ImageWidget;
import org.netbeans.api.visual.widget.Scene;
import org.netbeans.api.visual.widget.Widget;

/**
 *
 * @author TTS
 */
public class IconSetWidget extends Widget {

    private Border border;

    /**
     * Creates a glyph set widget.
     * @param scene the scene
     */
    public IconSetWidget(Scene scene, Border border) {
        super(scene);
        this.border = border;
        setLayout(LayoutFactory.createHorizontalFlowLayout());
    }

    public void add(Image image, Runnable action) {
        ImageWidget imageWidget = new ImageWidget(getScene());
        imageWidget.setImage(image);
        imageWidget.setBorder(border);
        if (action != null) {
            imageWidget.getActions().addAction(new ActionAdapter(action));
            imageWidget.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
        addChild(imageWidget);
    }

    private class ActionAdapter extends WidgetAction.Adapter {

        private Runnable action;

        private ActionAdapter(Runnable action) {
            this.action = action;
        }

        @Override
        public State mouseClicked(Widget widget, WidgetMouseEvent event) {
            action.run();
            return State.CONSUMED;
        }
    }
}