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
package eu.advance.logistics.flow.engine.controlcenter;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;

/**
 * Opens the ECC program.
 * 
 * @author TTS
 */
@ActionID(category = "Edit",
id = "eu.advance.logistics.flow.engine.controlcenter.ControlCenterAction")
@ActionRegistration(displayName = "#CTL_ControlCenterAction")
@ActionReferences({
    @ActionReference(path = "Menu/RemoteFlowEngine", position = 0, separatorAfter = 50)
})
public final class ControlCenterAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        eu.advance.logistics.flow.engine.cc.CCMain.main(new String[0]);
    }
}
