package eu.advance.logistics.flow.editor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.ActionRegistration;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionID;
import org.openide.cookies.OpenCookie;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

//@ActionID(category = "File",
//id = "eu.advance.logistics.flow.editor.OpenAction")
//@ActionRegistration(displayName = "#CTL_OpenAction")
//@ActionReferences({
//    @ActionReference(path = "Menu/File", position = 200),
//    @ActionReference(path = "Shortcuts", name = "D-O")
//})
public final class OpenAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        FileChooserBuilder builder = new FileChooserBuilder(getClass()).setFilesOnly(true);
        File file = builder.showOpenDialog();
        if (file != null) {
            DataObject data = null;
            try {
                data = DataObject.find(FileUtil.toFileObject(file));
            } catch (DataObjectNotFoundException ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(
                        ex.getLocalizedMessage(), NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
            }
            if (data != null) {
                OpenCookie open = data.getCookie(OpenCookie.class);
                if (open != null) {
                    open.open();
                }
            }
        }
    }
}
