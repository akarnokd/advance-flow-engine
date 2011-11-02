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
import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.NotificationDisplayer;
import org.openide.awt.StatusDisplayer;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.windows.WindowManager;

//@ActionID(category = "RemoteFlowEngine",
//id = "eu.advance.logistics.flow.engine.controlcenter.UploadFlowAction")
//@ActionRegistration(displayName = "#CTL_UploadFlowAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/RemoteFlowEngine", position = 400)
//})
public final class UploadFlowAction extends AbstractAction {

    public UploadFlowAction() {
        putValue(NAME, NbBundle.getMessage(UploadFlowAction.class, "CTL_UploadFlowAction"));
        setEnabled(false);
        EngineController.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEngine(EngineController ec) {
        setEnabled(ec.getEngine() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        final Item item = chooseFlowDescription();
        if (item == null || item.flow == null) {
            return;
        }

        List<AdvanceRealm> data = null;
        final AdvanceEngineControl engine = EngineController.getInstance().getEngine();
        if (engine != null && engine.datastore() != null) {
            try {
                data = engine.datastore().queryRealms();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (data == null) {
            return;
        }

        final RealmTableModel tableModel = new RealmTableModel(false);
        final ListDialog dlg = new ListDialog(tableModel);
        final JButton btn = dlg.getConfirmButton();
        dlg.setTitle(NbBundle.getMessage(UploadFlowAction.class, "UploadFlowAction.title"));
        btn.setText(NbBundle.getMessage(UploadFlowAction.class, "UploadFlowAction.button"));
        btn.setEnabled(false);
        final JTable table = dlg.getTable();
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                btn.setEnabled(table.getSelectedRow() != -1);
            }
        });
        final ActionListener uploadAction = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int index = table.getSelectedRow();
                if (index == -1) {
                    // should not happen
                    return;
                }
                AdvanceRealm realm = tableModel.getRealm(index);
                boolean ok = false;
                try {
                    Commons.fixRights(engine, realm, AdvanceUserRealmRights.WRITE);
                    engine.updateFlow(realm.name, item.flow, engine.getUser().name);
                    ok = true;
                    dlg.dispose();
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                }

                String text = item.name + (ok ? " uploaded" : " not uploaded");
                NotificationDisplayer.getDefault().notify(dlg.getTitle(),
                        Commons.getNotificationIcon(ok), text, null);
                StatusDisplayer.getDefault().setStatusText(text);
            }
        };
        btn.addActionListener(uploadAction);
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                int index = Commons.syncTableSelection(table, e);
                if (index != -1 && e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                    uploadAction.actionPerformed(null);
                }

            }
        });
        dlg.addWindowListener(new WindowAdapter() {

            @Override
            public void windowOpened(WindowEvent e) {
                tableModel.refresh();
            }
            
        });

        dlg.setLocationRelativeTo(dlg.getOwner());
        dlg.setVisible(true);
    }

    private Item chooseFlowDescription() {
        FlowDescription fd = Utilities.actionsGlobalContext().lookup(FlowDescription.class);
        if (fd != null) {
            String name;
            DataObject dataObject = Utilities.actionsGlobalContext().lookup(DataObject.class);
            if (dataObject != null) {
                name = FileUtil.getFileDisplayName(dataObject.getPrimaryFile());
            } else {
                name = fd.getId();
            }
            String msg = MessageFormat.format("<html>{0}<br><br>Do you want to upload the current flow?<br>Unsaved changes will be uploaded, too.</html>", name);
            NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
            nd.setTitle("Upload");
            if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION) {
                Item item = new Item();
                item.flow = fd.build();
                item.name = name;
                return item;
            }
        }
        File file = chooseFile();
        if (file != null && file.exists()) {
            try {
                Item item = new Item();
                item.flow = FlowDescription.load(new FileInputStream(file));
                item.name = file.getCanonicalPath();
                return item;
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return null;
    }

    private static File chooseFile() {
        File dir = EngineController.getWorkspaceDir();
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(dir);
        fc.setFileFilter(Commons.createFileFilter());
        int r = fc.showDialog(WindowManager.getDefault().getMainWindow(), "Upload");
        return (r == JFileChooser.APPROVE_OPTION) ? fc.getSelectedFile() : null;
    }

    private static class Item {

        AdvanceCompositeBlock flow;
        String name;
    }
}
