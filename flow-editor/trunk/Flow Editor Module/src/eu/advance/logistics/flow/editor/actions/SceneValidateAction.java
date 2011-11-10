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

@ActionID(category = "View",
id = "eu.advance.logistics.flow.editor.actions.SceneValidateAction")
@ActionRegistration(iconBase = "eu/advance/logistics/flow/editor/actions/testSingle.png",
displayName = "#CTL_SceneValidateAction")
@ActionReferences({
    @ActionReference(path = "Menu/View", position = 350),
    @ActionReference(path = "Toolbars/File", position = 600)
})
public final class SceneValidateAction implements ActionListener {

    private final FlowScene context;

    public SceneValidateAction(FlowScene context) {
        this.context = context;
    }

    @Override
    public void actionPerformed(ActionEvent ev) {
        context.layoutScene();
    }
}
