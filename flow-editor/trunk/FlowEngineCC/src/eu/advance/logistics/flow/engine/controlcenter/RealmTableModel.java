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

import com.google.common.collect.Lists;
import eu.advance.logistics.flow.engine.api.ds.AdvanceRealm;
import java.util.List;
import javax.swing.SwingWorker;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.openide.util.Exceptions;

/**
 *
 * @author TTS
 */
class RealmTableModel extends AbstractTableModel {

    private List<AdvanceRealm> data = Lists.newArrayList();
    private int columns;

    RealmTableModel(boolean extended) {
        columns = extended ? 6 : 4;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return columns;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        AdvanceRealm realm = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return realm.name;
            case 1:
                return realm.createdAt;
            case 2:
                return realm.modifiedAt;
            case 3:
                return realm.status;
            default:
                return realm;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Name";
            case 1:
                return "Created";
            case 2:
                return "Modified";
            case 3:
                return "Status";
            default:
                return "";
        }
    }

    AdvanceRealm getRealm(int index) {
        return data.get(index);
    }

    private void update(List<AdvanceRealm> list) {
        data.clear();
        data = list;
        fireTableDataChanged();
    }

    void refresh() {
        final ProgressHandle ph = ProgressHandleFactory.createHandle("Retrieving realms list...");
        ph.setInitialDelay(0);
        ph.start();
        new SwingWorker<List<AdvanceRealm>, Void>() {

            @Override
            protected List<AdvanceRealm> doInBackground() throws Exception {
                return EngineController.getInstance().getEngine().datastore().queryRealms();
            }

            @Override
            protected void done() {
                try {
                    update(get());
                } catch (Exception ex) {
                    Exceptions.printStackTrace(ex);
                } finally {
                    ph.finish();
                }
            }
        }.execute();
    }

    void remove(int row) {
        data.remove(row);
        fireTableRowsDeleted(row, row);
    }

    void update(int row) {
        fireTableRowsUpdated(row, row);
    }

    void update(int row, int column) {
        fireTableCellUpdated(row, column);
    }
}
