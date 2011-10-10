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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.AbstractTableModel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A generic listing frame containing engine information, a
 * freetext filter box, a table with records and
 * optional buttons (refresh, create, close, etc.).
 * @author karnokd, 2011.10.07.
 * @param <T> the record element type
 */
public class GenericListingFrame<T> extends JFrame {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(GenericListingFrame.class);
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
	/** The column count. */
	protected int columnCount;
	/** The rows. */
	protected List<T> rows;
	/** The table. */
	protected JTable table;
	/** The engine url. */
	protected JLabel engineURL;
	/** The engine version. */
	protected JLabel engineVersion;
	/** The label manager. */
	protected final LabelManager labels;
	/** The refersh button. */
	protected JButton refresh;
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
	/** First extra button. */
	protected JButton extra1;
	/** Second extra button. */
	protected JButton extra2;
	/** Third extra button. */
	protected JButton extra3;
	/** The extra button array. */
	protected JButton[] extra;
	/** The filter. */
	protected JTextField filter;
	/** The help button. */
	protected JButton help;
	/** Close this window. */
	protected JButton close;
	/** The top separator. */
	protected JSeparator topSeparator;
	/** The bottom separator. */
	protected JSeparator bottomSeparator;
	/** Engine URL label. */
	protected JLabel engineLabel;
	/** Version label. */
	protected JLabel versionLabel;
	/** The filter label. */
	protected JLabel filterLabel;
	/** The listing date. */
	protected JLabel listDate;
	/** The record count label. */
	private JLabel countLabel;
	/**
	 * Initialize the contents.
	 * @param labels the label manager
	 */
	public GenericListingFrame(final LabelManager labels) {
		this.labels = labels;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		JScrollPane tableScroll = new JScrollPane(table);
		
		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		engineLabel = new JLabel(labels.get("Engine:"));
		versionLabel = new JLabel(labels.get("Version:"));
		filterLabel = new JLabel(labels.get("Filter:"));
		countLabel = new JLabel();
		
		topSeparator = new JSeparator(JSeparator.HORIZONTAL);
		bottomSeparator = new JSeparator(JSeparator.HORIZONTAL);
		
		extra1 = new JButton();
		extra1.setVisible(false);
		extra2 = new JButton();
		extra2.setVisible(false);
		extra3 = new JButton();
		extra3.setVisible(false);
		
		extra = new JButton[] { extra1, extra2, extra3 };
		
		filter = new JTextField();
		filter.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		engineURL = new JLabel();
		engineVersion = new JLabel();
		refresh = new JButton(labels.get("Refresh"));
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		help = new JButton(new ImageIcon(getClass().getResource("help.png")));
		Dimension r = help.getPreferredSize();
		
		close = new JButton("Close");
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		listDate = new JLabel();
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(engineLabel)
				.addComponent(engineURL)
				.addGap(50)
				.addComponent(versionLabel)
				.addComponent(engineVersion)
			)
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
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(help, r.height, r.height, r.height)
				.addComponent(bottomSeparator, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(refresh)
				.addGap(30)
				.addComponent(extra1)
				.addComponent(extra2)
				.addComponent(extra3)
				.addGap(30)
				.addComponent(close)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(engineLabel)
				.addComponent(engineURL)
				.addComponent(versionLabel)
				.addComponent(engineVersion)
			)
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
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(help, r.height, r.height, r.height)
				.addComponent(bottomSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.CENTER)
				.addComponent(refresh)
				.addComponent(extra1)
				.addComponent(extra2)
				.addComponent(extra3)
				.addComponent(close)
			)
		);
		
		pack();
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
		engineURL.setText(url);
	}
	/**
	 * Set the engine version text.
	 * @param version the version text
	 */
	public void setEngineVersion(String version) {
		engineVersion.setText(version);
	}
	/**
	 * Show engine info labels?
	 * @param visible visible?
	 */
	public void showEngineInfo(boolean visible) {
		engineLabel.setVisible(visible);
		engineURL.setVisible(visible);
		versionLabel.setVisible(visible);
		engineVersion.setVisible(visible);
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
	/**
	 * Set the help uri to browse when clicking on the help button.
	 * @param helpURI the URI
	 */
	public void setHelpURI(@NonNull final URI helpURI) {
		help.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (Desktop.isDesktopSupported()) {
					Desktop d = Desktop.getDesktop();
					try {
						d.browse(helpURI);
					} catch (IOException ex) {
						LOG.error(ex.toString(), ex);
					}
				}
			}
		});
	}
}
