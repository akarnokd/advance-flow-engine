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

import java.awt.Frame;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import com.google.common.eventbus.Subscribe;

import eu.advance.logistics.flow.engine.cc.CCDebugDialog;
import eu.advance.logistics.flow.engine.cc.LabelManager;

//@ActionID(category = "RemoteFlowEngine",
//id = "eu.advance.logistics.flow.engine.controlcenter.DebugFlowAction")
//@ActionRegistration(displayName = "#CTL_DebugFlowAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/RemoteFlowEngine", position = 500)
//})
public final class DebugFlowAction  extends AbstractAction {

    /** */
	private static final long serialVersionUID = -5088512955025742741L;

	public DebugFlowAction() {
        putValue(NAME, NbBundle.getMessage(DebugFlowAction.class, "CTL_DebugFlowAction"));
        setEnabled(false);
        EngineController.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEngine(EngineController ec) {
        setEnabled(ec.getEngine() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Frame mainWindow = WindowManager.getDefault().getMainWindow();
//        final DebugFlowDialog debugDialog = new DebugFlowDialog(mainWindow, true);
//        debugDialog.setLocationRelativeTo(mainWindow);
//        debugDialog.setVisible(true);
        
        LabelManager labels = new LabelManager()  {
            @Override
            public String get(String key) {
                return key;
            }

            @Override
            public String format(String key, Object... values) {
                return String.format(key, values);
            }
        };

        EngineController ec = EngineController.getInstance();

        CCDebugDialog dialog = new CCDebugDialog(labels, ec.getEngine());

        dialog.engineInfo.setEngineURL(ec.getEngineAddress());
        dialog.engineInfo.setEngineVersion(ec.getEngineVersion());

        dialog.pack();
        dialog.setLocationRelativeTo(mainWindow);
        dialog.setVisible(true);

    }
}
