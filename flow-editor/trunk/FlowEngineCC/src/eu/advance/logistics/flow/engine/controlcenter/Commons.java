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

import eu.advance.logistics.flow.engine.api.AdvanceControlException;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import eu.advance.logistics.flow.engine.api.AdvanceUser;
import eu.advance.logistics.flow.engine.api.AdvanceUserRealmRights;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.Icon;
import javax.swing.JTable;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.openide.util.ImageUtilities;

/**
 *
 * @author TTS
 */
class Commons {

    private Commons() {
    }

    static void fixRights(AdvanceEngineControl engine, AdvanceRealm realm, AdvanceUserRealmRights rights) throws IOException, AdvanceControlException {
        AdvanceUser u = engine.getUser();
        if (!u.realmRights.containsEntry(realm, rights)) {
            u.realmRights.put(realm.name, rights);
            engine.datastore().updateUser(u);
        }
    }

    static FileFilter createFileFilter() {
        return new FileNameExtensionFilter("Flow description (*.xml)", "xml");
    }

    static int syncTableSelection(JTable table, MouseEvent e) {
        int r = table.rowAtPoint(e.getPoint());
        if (r >= 0 && r < table.getRowCount()) {
            table.setRowSelectionInterval(r, r);
        } else {
            table.clearSelection();
        }

        return table.getSelectedRow();
    }

    static Icon getNotificationIcon(boolean ok) {
        if (ok) {
            return ImageUtilities.loadImageIcon("eu/advance/logistics/flow/engine/controlcenter/ok_16.png", false);
        } else {
            return ImageUtilities.loadImageIcon("eu/advance/logistics/flow/engine/controlcenter/alert_16.png", false);
        }
    }
}
