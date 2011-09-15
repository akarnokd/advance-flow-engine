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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
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
import org.openide.windows.WindowManager;

/**
 * 
 * @author TTS
 */
@ActionID(category = "File",
id = "eu.advance.logistics.flow.editor.OpenFlowAction")
@ActionRegistration(displayName = "#CTL_OpenFlowAction",
iconBase = "eu/advance/logistics/flow/editor/images/openProject.png")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 100),
    @ActionReference(path = "Shortcuts", name = "D-O"),
    @ActionReference(path = "Toolbars/File", position = 350)
})
public final class OpenFlowAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        final OpenFlowDialog dialog = new OpenFlowDialog(WindowManager.getDefault().getMainWindow(), true);
        dialog.setLocationRelativeTo(dialog.getOwner());
        dialog.setVisible(true);
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
        final String userHome = System.getProperty("user.home");
        final File workspace = new File(userHome, ".advance-flow-editor-ws");

        if (!workspace.exists()) {
            workspace.mkdir();
        }

        return workspace;
    }
}
