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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

import eu.advance.logistics.flow.editor.diagram.FlowScene;

/**
 * 
 * @author TTS
 */
@ActionID(category = "View",
id = "eu.advance.logistics.flow.editor.actions.ZoomOutAction")
@ActionRegistration(iconBase = "eu/advance/logistics/flow/editor/actions/zoomOut.png",
displayName = "#CTL_ZoomOutAction")
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 0),
    @ActionReference(path = "Toolbars/Zoom", position = 900)
})
public final class ZoomOutAction implements ActionListener {

    private final static double FACTOR = 4;
    private final static double STEP = 0.5;
    private final static double MIN = 0.1;
    private final FlowScene context;

    public ZoomOutAction(FlowScene context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        ZoomAction.getInstance().apply(context, Math.max((int) (context.getZoomFactor() * FACTOR) * STEP / FACTOR, MIN));
    }
}
