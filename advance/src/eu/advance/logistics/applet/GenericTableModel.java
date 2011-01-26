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
package eu.advance.logistics.applet;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * A generic table model to contain an arraylist of Ts.
 * @author karnokd
 * @param <T> the row element type.
 */
public abstract class GenericTableModel<T> extends AbstractTableModel implements Iterable<T> {
	/** */
	private static final long serialVersionUID = -2666581445449459598L;
	/** The array of column names. */
//	@NonNull
	protected String[] colNames = { };
	/** The array of column classes. */
//	@NonNull
	protected Class<?>[] colClasses = { };
	/** The list of row elements. */
	protected final List<T> rows = new ArrayList<T>();
	/**
	 * Set the column names.
	 * @param colNames the column names to set
	 */
	public void setColumnNames(String... colNames) {
		if (colNames == null || colNames.length == 0) {
			this.colNames = new String[0];
		} else {
			this.colNames = colNames.clone();
		}
	}
	/**
	 * Sets the column classes.
	 * @param colClasses the column classes to set
	 */
	public void setColumnClasses(Class<?>... colClasses) {
		if (colClasses == null || colClasses.length == 0) {
			this.colClasses = new Class<?>[0];
		} else {
			this.colClasses = colClasses.clone();
		}
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<?> getColumnClass(int columnIndex) {
		return colClasses[columnIndex];
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getColumnCount() {
		return colNames.length;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getRowCount() {
		return rows.size();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return getValueFor(rowIndex, columnIndex, rows.get(rowIndex));
	}
	/**
	 * Return the value to the given column index. The row index and the record at that row is
	 * available for convenience.
	 * @param rowIndex the row index
	 * @param columnIndex the column index
	 * @param entry the entry
	 * @return the object to display
	 */
	public abstract Object getValueFor(int rowIndex, int columnIndex, T entry);
	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		setValueAt(rowIndex, columnIndex, rows.get(rowIndex), aValue);
	}
	/**
	 * Set the value at a given row and column on the object. 
	 * Override this.
	 * @param rowIndex the row index
	 * @param columnIndex the column index
	 * @param t the content of the row
	 * @param aValue the value to set
	 */
	public void setValueAt(int rowIndex, int columnIndex, T t, Object aValue) {
		
	}
	/**
	 * @return the array of column names
	 */
//	@NonNull
	public String[] getColumnNames() {
		return colNames.clone();
	}
	/**
	 * @return the array of column classes
	 */
//	@NonNull
	public Class<?>[] getColumnClasses() {
		return colClasses.clone();
	}
	/**
	 * Returns the indexth element from the rows list.
	 * @param index the index
	 * @return the entry
	 */
	public T get(int index) {
		return rows.get(index);
	}
	/**
	 * Update the given indexth element to the new value. Calls <code>set(int, T)</code> to perform the setting. 
	 * @param index the index
	 * @param newValue the new value
	 * @return the new value
	 */
	public T update(int index, T newValue) {
		T result = get(index);
		set(index, newValue);
		return result;
	}
	/**
	 * Sets the given indexth element to a new value.
	 * @param index the index
	 * @param newValue the new value 
	 */
	public void set(int index, T newValue) {
		rows.set(index, newValue);
		fireTableRowsUpdated(index, index);
	}
	/**
	 * Delete the given indexed rows.
	 * @param indexes the array of indexes to be deleted
	 */
	public void delete(int... indexes) {
		if (indexes.length < 1) {
			return;
		}
		int[] idxs = indexes.clone();
		Arrays.sort(idxs);
		for (int i = idxs.length - 1; i >= 0; i++) {
			rows.remove(idxs[i]);
		}
		fireTableRowsUpdated(idxs[0], idxs[idxs.length - 1]);
	}
	/**
	 * Delete one row from the table.
	 * @param index the row index
	 */
	public void delete(int index) {
		rows.remove(index);
		fireTableRowsDeleted(index, index);
	}
	/**
	 * Add a new element to the rows.
	 * @param element the element to add
	 */
	public void add(T element) {
		rows.add(element);
		fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
	}
	/**
	 * Add an array of elements to the rows.
	 * @param elements the elements
	 */
	public void add(T... elements) {
		for (T e : elements) {
			rows.add(e);
		}
		if (elements.length > 0) {
			fireTableRowsInserted(rows.size() - 1 - elements.length, rows.size() - 1);
		}
	}
	/**
	 * Add the contents of the iterable.
	 * @param elements the element iterable
	 */
	public void add(Iterable<T> elements) {
		add(elements.iterator());
	}
	/**
	 * Add the contents of the iterator.
	 * @param elements the iterator of elements
	 */
	public void add(Iterator<T> elements) {
		int size = rows.size();
		while (elements.hasNext()) {
			rows.add(elements.next());
		}
		if (size != rows.size()) {
			fireTableRowsInserted(size, rows.size() - 1);
		}
	}
	/**
	 * Returns the index of the element or -1 if not in the rows.
	 * @param entry the entry to look for
	 * @return the index of the element or -1 if not found
	 */
	public int indexOf(Object entry) {
		return rows.indexOf(entry);
	}
	/**
	 * Returns the index of the element searched from the given start index or -1 if not in the rows.
	 * @param entry the entry to look for
	 * @param startIndex the start index of the search
	 * @return the index of the element or -1 if not found
	 */
	public int indexOf(Object entry, int startIndex) {
		for (int i = startIndex; i < rows.size(); i++) {
			Object o = rows.get(i);
			if (o == entry || (o != null && o.equals(entry))) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Returns the last occurrence index of the given object, or -1 if not found.
	 * @param entry the entry to look for
	 * @return the last occurrence index or -1 if not found
	 */
	public int lastIndexOf(Object entry) {
		return rows.lastIndexOf(entry);
	}
	/**
	 * Returns the last index of the given entry starting the search from the index and going backwards.
	 * @param entry the entry to look for
	 * @param startIndex the start index, if bigger than the number of items the search starts from the last entry.
	 * @return the index of the occurrence or -1 if not in the rows
	 */
	public int lastIndexOf(Object entry, int startIndex) {
		if (startIndex >= rows.size()) {
			startIndex = rows.size() - 1;
		}
		for (int i = startIndex; i >= 0; i--) {
			Object o = rows.get(i);
			if (o == entry || (o != null && o.equals(entry))) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Clear the entries of the rows list.
	 */
	public void clear() {
		int len = rows.size();
		if (len > 0) {
			rows.clear();
			fireTableRowsDeleted(0, len - 1);
		}
	}
	/**
	 * Returns the internal rows list as is. Changes made through it should be fireXXX() externally.
	 * @return the list of elements
	 */
	public List<T> getRows() {
		return rows;
	}
	/**
	 * Convert the array of view indexes into model indexes of the given table.
	 * @param table the table
	 * @param indexes the array of view indexes
	 * @return the array of model indexes
	 */
	public int[] toModelIndex(JTable table, int... indexes) {
		int[] result = new int[indexes.length];
		for (int i = 0; i < indexes.length; i++) {
			result[i] = table.convertRowIndexToModel(indexes[i]);
		}
		return result;
	}
	@Override
	public Iterator<T> iterator() {
		return rows.iterator();
	}
	/**
	 * Resizes the table columns based on the column and data preferred widths.
	 * @param table the original table
	 * @param model the data model
	 * @return the table itself
	 */
    public static JTable autoResizeColWidth(JTable table, AbstractTableModel model) {
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.setModel(model);
 
        int margin = 5;
 
        for (int i = 0; i < table.getColumnCount(); i++) {
            int                     vColIndex = i;
            DefaultTableColumnModel colModel  = (DefaultTableColumnModel) table.getColumnModel();
            TableColumn             col       = colModel.getColumn(vColIndex);
            int                     width     = 0;
 
            // Get width of column header
            TableCellRenderer renderer = col.getHeaderRenderer();
 
            if (renderer == null) {
                renderer = table.getTableHeader().getDefaultRenderer();
            }
 
            Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
 
            width = comp.getPreferredSize().width;
 
            // Get maximum width of column data
            for (int r = 0; r < table.getRowCount(); r++) {
                renderer = table.getCellRenderer(r, vColIndex);
                comp     = renderer.getTableCellRendererComponent(table, table.getValueAt(r, vColIndex), false, false,
                        r, vColIndex);
                width = Math.max(width, comp.getPreferredSize().width);
            }
 
            // Add margin
            width += 2 * margin;
 
            // Set the width
            col.setPreferredWidth(width);
        }
 
        ((DefaultTableCellRenderer) table.getTableHeader().getDefaultRenderer()).setHorizontalAlignment(
            SwingConstants.LEFT);
 
        // table.setAutoCreateRowSorter(true);
//        table.getTableHeader().setReorderingAllowed(false);
 
//        for (int i = 0; i < table.getColumnCount(); i++) {
//            TableColumn column = table.getColumnModel().getColumn(i);
// 
//            column.setCellRenderer(new DefaultTableColour());
//        }
 
        return table;
    }
}
