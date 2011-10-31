package eu.advance.logistics.flow.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.filechooser.FileFilter;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.filesystems.FileChooserBuilder;

@ActionID(category = "File",
id = "eu.advance.logistics.flow.editor.OpenAction")
@ActionRegistration(displayName = "#CTL_OpenAction",
iconBase = "eu/advance/logistics/flow/editor/images/openProject.png")
@ActionReferences({
    @ActionReference(path = "Menu/File", position = 200),
    @ActionReference(path = "Shortcuts", name = "D-O"),
    @ActionReference(path = "Toolbars/File", position = 300)
})
public final class OpenAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        File file = chooseFile();
        if (file != null) {
            OpenFlowAction.open(file);
        }
    }

    private static File chooseFile() {
        FileChooserBuilder fcb = new FileChooserBuilder("OpenFlowDescription");
        fcb.addFileFilter(createFileFilter());
        fcb.setDefaultWorkingDirectory(OpenFlowAction.getWorkspaceDir());
        fcb.setTitle("Open flow description");
        return fcb.showSaveDialog();
    }

    static FileFilter createFileFilter() {
        return new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".xml"); // NOI18N
            }

            @Override
            public String getDescription() {
                return "Flow description (*.xml)";
            }
        };
    }
}
