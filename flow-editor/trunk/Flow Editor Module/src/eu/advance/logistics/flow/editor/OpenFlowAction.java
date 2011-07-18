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
package eu.advance.logistics.flow.editor;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileFilter;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JScrollPane;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.WindowManager;

@ActionID(category = "File",
id = "eu.advance.logistics.flow.editor.OpenFlowAction")
@ActionRegistration(displayName = "#CTL_OpenFlowAction")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 100),
    @ActionReference(path = "Shortcuts", name = "D-O")
})
@Messages("CTL_OpenFlowAction=Open flow...")
public final class OpenFlowAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        File workspace = getWorkspaceDir();
        final File[] files = workspace.listFiles(new FileFilter() {

            @Override
            public boolean accept(File pathname) {
                return pathname.isFile() && pathname.getName().toLowerCase().endsWith(".xml");
            }
        });
        String[] names = new String[files.length];
        for (int i = 0, n = files.length; i < n; i++) {
            names[i] = files[i].getName();
        }
        final JDialog dlg = new JDialog(WindowManager.getDefault().getMainWindow(), true);
        final JList list = new JList(names);
        list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int index = list.locationToIndex(e.getPoint());
                    if (index != -1) {
                        open(files[index]);
                        dlg.dispose();
                    }
                }
            }
        });
        dlg.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dlg.setLayout(new BorderLayout());
        dlg.add(new JScrollPane(list), BorderLayout.CENTER);
        dlg.pack();
        dlg.setLocationRelativeTo(dlg.getOwner());
        dlg.setVisible(true);
    }

    static void open(File file) {
        FileObject fo = FileUtil.toFileObject(file);
        if (fo != null) {
            try {
                DataObject dataObject = DataObject.find(fo);
                OpenCookie open = dataObject.getLookup().lookup(OpenCookie.class);
                if (open != null) {
                    open.open();
                }
            } catch (DataObjectNotFoundException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            // something wrong!
        }
    }

    static File getWorkspaceDir() {
        String userHome = System.getProperty("user.home");
        File workspace = new File(userHome, ".advance-flow-editor-ws");
        if (!workspace.exists()) {
            workspace.mkdir();
        }
        return workspace;

    }
}
