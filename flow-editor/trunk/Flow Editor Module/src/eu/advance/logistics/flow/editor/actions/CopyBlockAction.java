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

import eu.advance.logistics.flow.editor.FlowSceneClipboard;
import eu.advance.logistics.flow.editor.diagram.FlowScene;
import java.awt.event.ActionEvent;
import java.util.Set;
import javax.swing.AbstractAction;

/**
 *
 * @author TTS
 */
public class CopyBlockAction extends AbstractAction {

    private FlowScene scene;

    public void setScene(FlowScene scene) {
        this.scene = scene;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Set selection = scene.getSelectedObjects();

        if (selection != null) {
            FlowSceneClipboard.getInstance().setSelection(selection);
        }
    }
}
