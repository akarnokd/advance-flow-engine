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

import eu.advance.logistics.flow.engine.api.AdvanceRealm;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.swing.table.AbstractTableModel;

/**
 *
 * @author TTS
 */
class RealmTableModel extends AbstractTableModel {

    private List<AdvanceRealm> data;

    RealmTableModel(Collection<AdvanceRealm> data) {
        this.data = new ArrayList<AdvanceRealm>(data);
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 4;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        switch (columnIndex) {
            case 0:
                return data.get(rowIndex).name;
            case 1:
                return data.get(rowIndex).createdAt;
            case 2:
                return data.get(rowIndex).modifiedAt;
            case 3:
                return data.get(rowIndex).status;
            default:
                return null;
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
                return Integer.toString(column);
        }
    }
    
    AdvanceRealm getRealm(int index) {
        return data.get(index);
    }

}
