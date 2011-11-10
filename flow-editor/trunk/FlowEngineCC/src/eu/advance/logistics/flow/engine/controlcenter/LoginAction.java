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

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;

@ActionID(category = "RemoteFlowEngine",
id = "eu.advance.logistics.flow.engine.controlcenter.LoginAction")
@ActionRegistration(displayName = "#CTL_LoginAction")
@ActionReferences({
    @ActionReference(path = "Shortcuts", name = "D-L"),
    @ActionReference(path = "Menu/RemoteFlowEngine", position = 100)
})
public final class LoginAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.setLocationRelativeTo(loginDialog.getOwner());
        loginDialog.setVisible(true);
    }
}
