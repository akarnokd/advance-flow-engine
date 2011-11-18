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

package eu.advance.logistics.flow.engine.block;

import hu.akarnokd.reactive4java.base.Option;
import hu.akarnokd.reactive4java.base.Pair;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;

import com.google.common.collect.Lists;

import eu.advance.logistics.annotations.Block;
import eu.advance.logistics.annotations.Input;
import eu.advance.logistics.annotations.Output;
import eu.advance.logistics.flow.engine.cc.CCDebugRow;
import eu.advance.logistics.flow.engine.cc.CCValueDialog;
import eu.advance.logistics.flow.engine.cc.CCWatchSettings;
import eu.advance.logistics.flow.engine.cc.LabelManager;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlock;
import eu.advance.logistics.flow.engine.model.rt.AdvanceBlockSettings;
import eu.advance.logistics.flow.engine.model.rt.AdvanceConstantPort;
import eu.advance.logistics.flow.engine.model.rt.AdvancePort;
import eu.advance.logistics.flow.engine.xml.typesystem.XElement;

/**
 * Displays a timestamped list of values it receives.
 * @author akarnokd, 2011.10.27.
 */
@Block(parameters = { "T" }, 
description = "Displays a table with timestamps and the received data.",
category = "user-interface")
public class Log extends AdvanceBlock {
	/** In. */
    @Input("?T")
    private static final String IN = "in";

    /** In. */
    @Input(value = "advance:string", required = false)
    private static final String TITLE = "title";
    /** Out. */
    @Output("?T")
    private static final String OUT = "out";
    
	/** The peer frame. */
	protected JInternalFrame frame;
	/** The log entries. */
	protected final List<Pair<Date, XElement>> rows = Lists.newArrayList();
	/** The model. */
	protected AbstractTableModel model;
	/** The table. */
	protected JTable table;
	/** The number of rows. */
	protected JLabel rowcount;
	@Override
	public void init(AdvanceBlockSettings settings) {
		super.init(settings);
                AdvancePort titlePort = getInput(TITLE);
                final String title = titlePort instanceof AdvanceConstantPort 
                        ? ((AdvanceConstantPort)titlePort).value.content 
                        : settings.id;
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				createGUI(title);
			}
		});
	}
	/**
	 * Create the GUI.
	 */
	protected void createGUI(String title) {
		frame = new JInternalFrame(title, true);
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		rowcount = new JLabel("Rows: 0");
		model = new AbstractTableModel() {
			/** */
			private static final long serialVersionUID = -3454726369103170368L;
			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				Pair<Date, XElement> pair = rows.get(rowIndex);
				if (columnIndex == 0) {
					return pair.first;
				}
				return pair.second;
			}
			
			@Override
			public int getRowCount() {
				return rows.size();
			}
			
			@Override
			public int getColumnCount() {
				return 2;
			}
			@Override
			public Class<?> getColumnClass(int columnIndex) {
				if (columnIndex == 0) {
					return Date.class;
				}
				return String.class;
			}
			@Override
			public String getColumnName(int column) {
				if (column == 0) {
					return "Timestamp";
				}
				return "Value";
			}
		};
		model.addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				rowcount.setText(String.format("Rows %d, Total %d", table.getRowCount(), rows.size()));
			}
		});
		
		table = new JTable(model);
		table.setAutoCreateRowSorter(true);
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					int idx = table.getSelectedRow();
					if (idx >= 0) {
						idx = table.convertRowIndexToModel(idx);
						Pair<Date, XElement> row = rows.get(idx);
						
						CCDebugRow drow = new CCDebugRow();
						drow.timestamp = row.first;
						drow.value = Option.some(row.second);
						drow.watch = new CCWatchSettings();
						
						LabelManager labels = new LabelManager() {
							@Override
							public String format(String key, Object... values) {
								return String.format(key, values);
							}
							@Override
							public String get(String key) {
								return key;
							}
						};
						
						CCValueDialog d = new CCValueDialog(labels, drow);
						d.setLocationRelativeTo(frame);
						d.setVisible(true);
					}
				}
			}
		});
		table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -7615930689752608644L;

			@Override
			public Component getTableCellRendererComponent(JTable table,
					Object value, boolean isSelected, boolean hasFocus,
					int row, int column) {
				JLabel label = (JLabel)super.getTableCellRendererComponent(table, value, isSelected, hasFocus,
						row, column);
				
				label.setText(value.toString());
				return label;
			}
		});
		JScrollPane sp = new JScrollPane(table);
		
		Container c = frame.getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setAutoCreateContainerGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(sp, 0, 250, Short.MAX_VALUE)
			.addComponent(rowcount)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(sp, 0, 250, Short.MAX_VALUE)
			.addComponent(rowcount)
		);
		frame.pack();
		frame.setVisible(true);
		BlockVisualizer.getInstance().add(frame);
	}
	@Override
	protected void invoke() {
		final XElement in = params.get(IN);
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				rows.add(Pair.of(new Date(), in));
				model.fireTableRowsInserted(rows.size() - 1, rows.size() - 1);
			}
		});
		dispatchOutput(Collections.singletonMap(OUT, in));
	}
	@Override
	public void done() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				if (frame != null) {
					frame.dispose();
					BlockVisualizer.getInstance().remove(frame);
				}
			}
		});
		super.done();
	}
}
