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
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.windows.WindowManager;

import com.google.common.eventbus.Subscribe;

import eu.advance.logistics.flow.editor.model.FlowDescription;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.core.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.ds.AdvanceUserRealmRights;
import eu.advance.logistics.flow.engine.model.fd.AdvanceCompositeBlock;
import hu.akarnokd.utils.xml.XNElement;

//@ActionID(category = "RemoteFlowEngine",
//id = "eu.advance.logistics.flow.engine.controlcenter.DownloadFlowAction")
//@ActionRegistration(displayName = "#CTL_DownloadFlowAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/RemoteFlowEngine", position = 300)
//})
public final class DownloadFlowAction extends AbstractAction {

    /** */
	private static final long serialVersionUID = 5297515763220164288L;

	public DownloadFlowAction() {
        putValue(NAME, NbBundle.getMessage(DownloadFlowAction.class, "CTL_DownloadFlowAction"));
        setEnabled(false);
        EngineController.getInstance().getEventBus().register(this);
    }

    @Subscribe
    public void onEngine(EngineController ec) {
        setEnabled(ec.getEngine() != null);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<AdvanceRealm> data = null;
        final AdvanceEngineControl engine = EngineController.getInstance().getEngine();
        if (engine != null && engine.datastore() != null) {
            try {
                data = engine.datastore().queryRealms();
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        if (data != null) {
            final RealmTableModel tableModel = new RealmTableModel(false);
            final ListDialog dlg = new ListDialog(tableModel);
            final JButton btn = dlg.getConfirmButton();
            dlg.setTitle(NbBundle.getMessage(DownloadFlowAction.class, "DownloadFlowAction.title"));
            btn.setText(NbBundle.getMessage(DownloadFlowAction.class, "DownloadFlowAction.button"));
            btn.setEnabled(false);
            final JTable table = dlg.getTable();
            table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

                @Override
                public void valueChanged(ListSelectionEvent e) {
                    btn.setEnabled(table.getSelectedRow() != -1);
                }
            });
            btn.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    int index = table.getSelectedRow();
                    if (index == -1) {
                        // should not happen
                        return;
                    }
                    AdvanceRealm realm = tableModel.getRealm(index);

                    // download the flow
                    AdvanceCompositeBlock flow = null;
                    try {
                        Commons.fixRights(engine, realm, AdvanceUserRealmRights.READ);
                        XNElement root = engine.datastore().queryFlow(realm.name);
                        if (root != null) {
                            flow = AdvanceCompositeBlock.parseFlow(root);
                        } else {
                            String msg = "Nothing to download!";
                            NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.WARNING_MESSAGE);
                            nd.setTitle("Download");
                            DialogDisplayer.getDefault().notify(nd);
                        }
                    } catch (AdvanceControlException ex) {
                        String msg = "<html>You don't have permissions to download the selected flow.<br><br>Please contact the engine administrator.</html>";
                        NotifyDescriptor nd = new NotifyDescriptor.Message(msg, NotifyDescriptor.ERROR_MESSAGE);
                        nd.setTitle("Access denied");
                        DialogDisplayer.getDefault().notify(nd);
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                    }
                    if (flow == null) {
                        return;
                    }

                    // save the flow
                    File file = chooseFile(flow.id);
                    if (file == null) {
                        return;
                    }
                    try {
                        FlowDescription.save(new FileOutputStream(file), flow);
                        StatusDisplayer.getDefault().setStatusText(file.getCanonicalPath() + " saved");
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
                        return;
                    }

                    // open the flow
                    try {
                        DataObject dataObject = DataObject.find(FileUtil.toFileObject(file));
                        OpenCookie open = dataObject.getLookup().lookup(OpenCookie.class);
                        if (open != null) {
                            open.open();
                            dlg.dispose();
                        }
                    } catch (Exception ex) {
                        Exceptions.printStackTrace(ex);
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
    }

    private static File chooseFile(String name) {
        File dir = EngineController.getWorkspaceDir();
        JFileChooser fc = new JFileChooser();
        fc.setCurrentDirectory(dir);
        fc.setSelectedFile(new File(dir, name + ".xml")); // NOI18N
        fc.setFileFilter(Commons.createFileFilter());

        while (true) {
            int r = fc.showDialog(WindowManager.getDefault().getMainWindow(), "Save");
            if (r != JFileChooser.APPROVE_OPTION) {
                return null;
            } else {
                File file = fc.getSelectedFile();
                if (!file.exists() || confirmOverwrite(file)) {
                    return file;
                }
            }
        }
    }

    private static boolean confirmOverwrite(File file) {
        String msg = String.format("Overwrite {0}?", file.getName());
        NotifyDescriptor nd = new NotifyDescriptor.Confirmation(msg, NotifyDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.YES_OPTION;
    }
}
