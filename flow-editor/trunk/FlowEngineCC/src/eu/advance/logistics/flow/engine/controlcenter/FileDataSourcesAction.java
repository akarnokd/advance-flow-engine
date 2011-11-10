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

import javax.swing.AbstractAction;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.openide.util.NbBundle;

import com.google.common.eventbus.Subscribe;

//@ActionID(category = "RemoteFlowEngine",
//id = "eu.advance.logistics.flow.engine.controlcenter.FileDataSourcesAction")
//@ActionRegistration(displayName = "#CTL_FileDataSourcesAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/RemoteFlowEngine", position = 1400)
//})
public final class FileDataSourcesAction  extends AbstractAction {

    /** */
	private static final long serialVersionUID = 8191100191501426637L;

	public FileDataSourcesAction() {
        putValue(NAME, NbBundle.getMessage(FileDataSourcesAction.class, "CTL_FileDataSourcesAction"));
        setEnabled(false);
        EngineController.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEngine(EngineController ec) {
        setEnabled(ec.getEngine() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
       final TableModel tableModel = new DefaultTableModel();
        final ListDialog dlg = new ListDialog(tableModel);
        dlg.getConfirmButton();
        dlg.setTitle(NbBundle.getMessage(FileDataSourcesAction.class, "FileDataSourcesAction.title"));
        dlg.getTable();


        dlg.setLocationRelativeTo(dlg.getOwner());
        dlg.setVisible(true);
    }
}
