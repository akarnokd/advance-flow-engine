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

package eu.advance.logistics.flow.engine.cc;

import hu.akarnokd.reactive4java.base.Action1;
import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Func2;
import hu.akarnokd.reactive4java.base.Pair;

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableRowSorter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import eu.advance.logistics.flow.engine.cc.CCFiltering.FilterItem;

/**
 * A generic listing frame containing engine information, a
 * freetext filter box, a table with records and
 * optional buttons (refresh, create, close, etc.).
 * @author akarnokd, 2011.10.07.
 * @param <T> the record element type
 */
public class CCListingFrame<T> extends JFrame {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(CCListingFrame.class);
	/** */
	private static final long serialVersionUID = -1243838082987540876L;
	/** Function to retrieve a cell value for the given element. */
	protected Func2<? super T, ? super Integer, ?> getCellValue;
	/** Function to retrieve the cell title and class. */
	protected Func1<? super Integer, Pair<String, ? extends Class<?>>> getCellTitle;
	/**
	 * Action to retrieve a list of elements in respect to the supplied filter.
	 */
	protected Action1<? super String> retrieve;
	/** Display the given item. */
	protected Action1<? super T> displayItem;
	/** The column count. */
	protected int columnCount;
	/** The rows. */
	protected List<T> rows;
	/** The table. */
	protected JTable table;
	/** The label manager. */
	protected final LabelManager labels;
	/** The refresh button. */
	protected JButton refresh;
	/** The filter function receiving the filter program, the current row and returns a boolean if the row should pass. */
	private Func2<List<FilterItem>, T, Boolean> filterFunction;
	/**
	 * The table model.
	 */
	protected AbstractTableModel model = new AbstractTableModel() {
		/** */
		private static final long serialVersionUID = 7270441459593329089L;

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return getCellValue.invoke(rows.get(rowIndex), columnIndex);
		}
		
		@Override
		public int getRowCount() {
			return rows != null ? rows.size() : 0;
		}
		
		@Override
		public int getColumnCount() {
			return columnCount;
		}
		@Override
		public java.lang.Class<?> getColumnClass(int columnIndex) {
			return getCellTitle.invoke(columnIndex).second;
		};
		@Override
		public String getColumnName(int column) {
			return getCellTitle.invoke(column).first;
		}
	};
	/** The extra button array. */
	protected JButton[] extra = new JButton[5];
	/** The filter. */
	protected JTextField filter;
	/** Close this window. */
	protected JButton close;
	/** The top separator. */
	protected JSeparator topSeparator;
	/** The filter label. */
	protected JLabel filterLabel;
	/** The listing date. */
	protected JLabel listDate;
	/** The record count label. */
	private JLabel countLabel;
	/** The engine info panel. */
	public final EngineInfoPanel engineInfo;
	/** The help panel. */
	public final HelpPanel help;
	/**
	 * Initialize the contents.
	 * @param labels the label manager
	 */
	public CCListingFrame(final LabelManager labels) {
		this.labels = labels;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		JScrollPane tableScroll = new JScrollPane(table);
		
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					doDisplaySelectedItem();
				}
			}

		});
		table.addKeyListener(new KeyAdapter() {
			@Override
			public void keyTyped(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					doDisplaySelectedItem();
					e.consume();
				}
			}
		});
		
		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		filterLabel = new JLabel(labels.get("Filter:"));
		countLabel = new JLabel();
		
		topSeparator = new JSeparator(JSeparator.HORIZONTAL);

		for (int i = 0; i < extra.length; i++) {
			extra[i] = new JButton();
			extra[i].setVisible(false);
		}
		
		filter = new JTextField();
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doFilter();
			}
		});
		refresh = new JButton(labels.get("Refresh"));
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				WindowEvent we = new WindowEvent(CCListingFrame.this, WindowEvent.WINDOW_CLOSING);
				Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(we);
			}
		});
		
		listDate = new JLabel();
		
		engineInfo = new EngineInfoPanel(labels);
		help = new HelpPanel();
		
		Group hb = gl.createSequentialGroup()
				.addComponent(refresh)
				.addGap(30);
		Group vb = gl.createParallelGroup(Alignment.CENTER)
		.addComponent(refresh);
		for (JButton b : extra) {
			vb.addComponent(b);
			hb.addComponent(b);
		}
		hb.addGap(30)
		.addComponent(close)
		;
		vb.addComponent(close)
		;
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(engineInfo)
			.addComponent(topSeparator)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(filterLabel)
				.addComponent(filter)
			)
			.addComponent(tableScroll)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(countLabel)
				.addGap(30)
				.addComponent(listDate)
			)
			.addComponent(help)
			.addGroup(hb)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(engineInfo)
			.addComponent(topSeparator)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(tableScroll)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(countLabel)
				.addComponent(listDate)
			)
			.addComponent(help)
			.addGroup(vb)
		);
		
		pack();
	}
	/**
	 * Display the selected item.
	 */
	void doDisplaySelectedItem() {
		int idx = table.getSelectedRow();
		if (idx >= 0) {
			idx = table.convertRowIndexToModel(idx);
			if (displayItem != null) {
				displayItem.invoke(rows.get(idx));
			}
		}
	}
	/**
	 * Retrieve the rows and update the view.
	 */
	public void refresh() {
		if (retrieve != null) {
			retrieve.invoke(filter.getText());
		}
	}
	/**
	 * Set the cell value returning function.
	 * @param function the function
	 */
	public void setCellValueFunction(Func2<? super T, ? super Integer, ?> function) {
		this.getCellValue = function;
	}
	/**
	 * Set the cell title and type function.
	 * @param function the function
	 */
	public void setCellTitleFunction(Func1<? super Integer, Pair<String, ? extends Class<?>>> function) {
		this.getCellTitle = function;
	}
	/**
	 * Set the retrieve function.
	 * @param action the retrieve action
	 */
	public void setRetrieveFunction(Action1<? super String> action) {
		this.retrieve = action;
	}
	/**
	 * Update the list of rows.
	 * @param rows the new sequence of rows
	 */
	public void setRows(Iterable<? extends T> rows) {
		this.rows = Lists.newArrayList(rows);
		model.fireTableDataChanged();
		listDate.setText("@ " + new Date());
		countLabel.setText(labels.format("Records: %d", rows != null ? this.rows.size() : 0));
	}
	/**
	 * Set the column count.
	 * @param count the count
	 */
	public void setColumnCount(int count) {
		this.columnCount = count;
	}
	/**
	 * Set the engine URL text.
	 * @param url the URL
	 */
	public void setEngineURL(String url) {
		engineInfo.setEngineURL(url);
	}
	/**
	 * Set the engine version text.
	 * @param version the version text
	 */
	public void setEngineVersion(String version) {
		engineInfo.setEngineVersion(version);
	}
	/**
	 * Show engine info labels?
	 * @param visible visible?
	 */
	public void showEngineInfo(boolean visible) {
		engineInfo.setVisible(visible);
		topSeparator.setVisible(visible);
	}
	/**
	 * Show the filter label and box.
	 * @param visible the visibility
	 */
	public void showFilter(boolean visible) {
		filterLabel.setVisible(visible);
		filter.setVisible(visible);
	}
	/**
	 * Adjust the table cell sizes to the content.
	 */
	public void autoSizeTable() {
		GUIUtils.autoResizeColWidth(table, model);
	}
	/** Indicate the table structure changed. */
	public void fireTableStructureChanged() {
		model.fireTableStructureChanged();
	}
	/**
	 * Set the name and action on the given extra button and show it.
	 * @param index the button index (zero based)
	 * @param title the title label
	 * @param action the action
	 */
	public void setExtraButton(int index, String title, ActionListener action) {
		extra[index].setText(labels.get(title));
		extra[index].addActionListener(action);
		extra[index].setVisible(true);
	}
	/** @return the list of selected items. */
	public List<T> getSelectedItems() {
		List<T> result = Lists.newArrayList();
		int[] indices = table.getSelectedRows();
		for (int idx : indices) {
			int j = table.convertRowIndexToModel(idx);
			result.add(rows.get(j));
		}
		return result;
	}
	/**
	 * Remove the specific items from the rows.
	 * @param toRemove the items to remove
	 */
	public void removeItems(Collection<T> toRemove) {
		rows.removeAll(toRemove);
		model.fireTableDataChanged();
	}
	/**
	 * Set the action to display the selected item.
	 * @param displayItem the action
	 */
	public void setDisplayItem(Action1<? super T> displayItem) {
		this.displayItem = displayItem;
	}
	/**
	 * Return the displayed row objects according to the sorting and filtering.
	 * @return the list of items
	 */
	public List<T> getRows() {
		List<T> result = Lists.newArrayList();
		
		for (int i = 0; i < table.getRowCount(); i++) {
			int idx = table.convertRowIndexToModel(i);
			result.add(rows.get(idx));
		}
		
		return result;
	}
	/** Filter the rows. */
	void doFilter() {
		@SuppressWarnings("unchecked") TableRowSorter<AbstractTableModel> rs = (TableRowSorter<AbstractTableModel>)table.getRowSorter();
		if (filter.getText().isEmpty() || filterFunction == null) {
			rs.setRowFilter(null);
			return;
		}
		try {
			final List<FilterItem> fis = CCFiltering.parse(filter.getText());
			rs.setRowFilter(new RowFilter<AbstractTableModel, Integer>() {
				@Override
				public boolean include(
						javax.swing.RowFilter.Entry<? extends AbstractTableModel, ? extends Integer> entry) {
					Integer idx = entry.getIdentifier();
					T row = rows.get(idx);
					
					return filterFunction.invoke(fis, row);
				}
			});
			
			
		} catch (ParseException ex) {
			GUIUtils.errorMessage(this, ex);
		}
	}
	/**
	 * Sets the filter function.
	 * @param filterFunction the filter function receiving the filter program, the row and should 
	 * return a boolean indicating whether the row should stay
	 */
	public void setFilterFunction(Func2<List<FilterItem>, T, Boolean> filterFunction) {
		this.filterFunction = filterFunction;
	}
}
