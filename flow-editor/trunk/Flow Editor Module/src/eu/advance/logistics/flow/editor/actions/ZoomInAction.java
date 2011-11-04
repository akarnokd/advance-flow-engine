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
package eu.advance.logistics.flow.editor.actions;

import eu.advance.logistics.flow.editor.diagram.FlowScene;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;

/**
 * 
 * @author TTS
 */
@ActionID(category = "View",
id = "eu.advance.logistics.flow.editor.actions.ZoomInAction")
@ActionRegistration(iconBase = "eu/advance/logistics/flow/editor/actions/zoomIn.png",
displayName = "#CTL_ZoomInAction")
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 0),
    @ActionReference(path = "Toolbars/Zoom", position = 800)
})
public final class ZoomInAction implements ActionListener {

    private final static double FACTOR = 4;
    private final static double STEP = 2;
    private final static double MAX = 10;
    private final FlowScene context;

    public ZoomInAction(FlowScene context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ZoomAction.getInstance().apply(context, Math.min((int) (context.getZoomFactor() * FACTOR) * STEP / FACTOR, MAX));
    }
}
