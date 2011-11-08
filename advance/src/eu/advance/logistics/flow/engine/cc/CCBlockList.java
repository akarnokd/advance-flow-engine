/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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

import java.awt.Container;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.WindowEvent;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceEngineControl;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockRegistryEntry;

/**
 * The list and description of blocks.
 * @author akarnokd, 2011.10.14.
 */
public abstract class CCBlockList extends JFrame {
	/** */
	private static final long serialVersionUID = 831429874824695251L;
	/** The label manager. */
	protected final LabelManager labels;
	/** The block table. */
	protected JTable table;
	/** The block list model. */
	protected AbstractTableModel model;
	/** Refresh the view. */
	protected JButton refresh;
	/** Close the window. */
	protected JButton close;
	/** Export the block registry. */
	protected JButton export;
	/** The block description. */
	protected JTextArea description;
	/** The engine control. */
	protected final AdvanceEngineControl control;
	/** The engine information panel. */
	protected EngineInfoPanel engineInfo;
	/** The filter. */
	protected JTextField filter;
	/** The block rows. */
	protected final List<AdvanceBlockRegistryEntry> rows = Lists.newArrayList();
	/**
	 * Create the block list GUI.
	 * @param labels the label manager
	 * @param control the engine control
	 */
	public CCBlockList(@NonNull final LabelManager labels, @NonNull final AdvanceEngineControl control) {
		super(labels.get("Blocks"));
		this.control = control;
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.labels = labels;
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		refresh = new JButton(labels.get("Refresh"));
		refresh.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refresh();
			}
		});
		export = new JButton(labels.get("Export..."));
		export.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				export();
			}
		});
		close = new JButton(labels.get("Close"));
		close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				close();
			}
		});
		
		engineInfo = new EngineInfoPanel(labels);
		
		description = new JTextArea();
		description.setEditable(false);
		description.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
		description.addMouseWheelListener(new MouseAdapter() {
			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.isControlDown()) {
					Font f = description.getFont();
					if (e.getUnitsToScroll() < 0) {
						description.setFont(new Font(f.getName(), f.getStyle(), f.getSize() + 1));
					} else 
					if (f.getSize() > 4) {
						description.setFont(new Font(f.getName(), f.getStyle(), f.getSize() - 1));
					}
					e.consume();
				} else {
					description.getParent().dispatchEvent(e);
				}
			}
		});

		
		model = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = -2411793652521877153L;
			/** Column names. */
			String[] cols = { labels.get("Name"), labels.get("Category"), 
					labels.get("Keywords"), labels.get("Documentation") };
			/** Classes. */
			Class<?>[] clss = { String.class, String.class, String.class, String.class };
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				AdvanceBlockRegistryEntry e = rows.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return e.id;
				case 1:
					return e.category;
				case 2:
					return Joiner.on(", ").join(e.keywords);
				case 3:
					return e.documentation;
				default:
					return null;
				}
			}
			
			@Override
			public int getRowCount() {
				return rows.size();
			}
			
			@Override
			public int getColumnCount() {
				return cols.length;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				return clss[columnIndex];
			}
			@Override
			public String getColumnName(int column) {
				return cols[column];
			}
		};
		table = new JTable(model);
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				doDisplayBlock();
			}
		});
		table.setAutoCreateRowSorter(true);
		
		JScrollPane sp = new JScrollPane(table);
		
		JScrollPane ds = new JScrollPane(description);
		
		JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, sp, ds);

		JLabel filterLabel = new JLabel(labels.get("Filter:"));
		
		final JLabel records = new JLabel(labels.format("Records: %d", 0));
		
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				records.setText(labels.format("Records: %d", table.getRowCount()));
			}
		});
		filter = new JTextField();
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(engineInfo)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(filterLabel)
				.addComponent(filter)
			)
			.addComponent(split)
			.addComponent(records)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(refresh)
				.addComponent(export)
				.addComponent(close)
			)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(engineInfo)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(filterLabel)
				.addComponent(filter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
			)
			.addComponent(split)
			.addComponent(records)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(refresh)
				.addComponent(export)
				.addComponent(close)
			)
		);
		split.setDividerLocation(250);
	}
	/**
	 * Close the frame.
	 */
	public void close() {
		WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
	}
	/**
	 * Refresh the view.
	 */
	public void refresh() {
		GUIUtils.getWorker(new WorkItem() {
			/** The exception. */
			Throwable t;
			/** The registry list. */
			List<AdvanceBlockRegistryEntry> result;
			@Override
			public void run() {
				try {
					result = control.queryBlocks();
				} catch (Throwable t) {
					this.t = t;
				}
			}
			@Override
			public void done() {
				if (t != null) {
					GUIUtils.errorMessage(CCBlockList.this, t);
				} else {
					rows.clear();
					rows.addAll(result);
					model.fireTableDataChanged();
					GUIUtils.autoResizeColWidth(table, model);
				}
			}
		}).execute();
	}
	/**
	 * Export the block list.
	 */
	public abstract void export();
	/** Display the selected block definition. */
	void doDisplayBlock() {
		description.setText("");
		int idx = table.getSelectedRow();
		if (idx >= 0) {
			idx = table.convertRowIndexToModel(idx);
			AdvanceBlockRegistryEntry e = rows.get(idx);
			description.setText(e.toString());
		}
	}
}
