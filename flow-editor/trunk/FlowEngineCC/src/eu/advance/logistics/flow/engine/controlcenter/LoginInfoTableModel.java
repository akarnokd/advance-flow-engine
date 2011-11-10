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

import java.util.List;

import javax.swing.table.AbstractTableModel;

/**
 *
 * @author TTS
 */
class LoginInfoTableModel extends AbstractTableModel {

    /** */
	private static final long serialVersionUID = -6394245658681942794L;
	private List<LoginInfo> data;

    LoginInfoTableModel(List<LoginInfo> data) {
        this.data = data;
    }

    @Override
    public int getRowCount() {
        return data.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        LoginInfo info = data.get(rowIndex);
        switch (columnIndex) {
            case 0:
                return info.address;
            case 1:
                return info.lastLogin;
            default:
                return null;
        }
    }

    @Override
    public String getColumnName(int column) {
        switch (column) {
            case 0:
                return "Address";
            case 1:
                return "Last login";
            default:
                return Integer.toString(column);
        }
    }

    LoginInfo getLoginInfo(int index) {
        return data.get(index);
    }

    void remove(int index) {
        data.remove(index);
        fireTableRowsDeleted(index, index);
    }

    void add(LoginInfo info) {
        int index = data.size();
        data.add(info);
        fireTableRowsInserted(index, index);
    }

    List<LoginInfo> getData() {
        return data;
    }
}
