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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

import com.google.common.io.Files;
import com.google.common.io.InputSupplier;

/**
 * 
 * @author TTS
 */
@ActionID(id = "eu.advance.logistics.flow.editor.NewFlowAction", category = "File")
@ActionRegistration(iconInMenu = true, displayName = "#CTL_NewFlowAction",
        iconBase = "eu/advance/logistics/flow/editor/images/newProject.png")
@ActionReferences(value = {
    @ActionReference(path = "Shortcuts", name = "D-N"),
    @ActionReference(path = "Menu/File", position = 100),
    @ActionReference(path = "Toolbars/File", position=300)
})
public final class NewFlowAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        File dest = null;
        try {
            File workspace = OpenFlowAction.getWorkspaceDir();

            for (int i = 1; i < Integer.MAX_VALUE; i++) {
                dest = new File(workspace, "flow-description-" + i + ".xml");
                if (!dest.exists()) {
                    break;
                }
            }
            Files.copy(new InputSupplier<InputStream>() {

                @Override
                public InputStream getInput() throws IOException {
                    FileObject template = FileUtil.getConfigFile("Templates/Other/FlowDescriptionTemplate.xml");
                    if (template == null) {
                        throw new FileNotFoundException();
                    }
                    return template.getInputStream();
                }
            }, dest);
            OpenFlowAction.open(dest);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
}
