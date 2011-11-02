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

import com.google.common.eventbus.Subscribe;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import org.openide.util.NbBundle;

//@ActionID(category = "RemoteFlowEngine",
//id = "eu.advance.logistics.flow.engine.controlcenter.ManageRealmsAction")
//@ActionRegistration(displayName = "#CTL_ManageRealmsAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/RemoteFlowEngine", position = 600)
//})
public final class ManageRealmsAction extends AbstractAction {

    public ManageRealmsAction() {
        putValue(NAME, NbBundle.getMessage(ManageRealmsAction.class, "CTL_ManageRealmsAction"));
        setEnabled(false);
        EngineController.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEngine(EngineController ec) {
        setEnabled(ec.getEngine() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        AdvanceEngineControl engine = EngineController.getInstance().getEngine();
        if (engine != null) {
            final ManageRealmsDialog dlg = new ManageRealmsDialog(engine);
            dlg.setLocationRelativeTo(dlg.getOwner());
            dlg.setVisible(true);
        }
    }
}
