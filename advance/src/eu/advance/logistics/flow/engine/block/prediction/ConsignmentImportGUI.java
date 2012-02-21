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

package eu.advance.logistics.flow.engine.block.prediction;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipInputStream;

import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileFilter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Closeables;

import eu.advance.logistics.flow.engine.util.U;

/**
 * Imports the consignment CSV files into a MySQL database.
 * @author karnokd, 2012.02.21.
 */
public class ConsignmentImportGUI extends JFrame {
	/** */
	private static final long serialVersionUID = -8233409580796614654L;
	/**
	 * Main program.
	 * @param args no arguments.
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				ConsignmentImportGUI g = new ConsignmentImportGUI();
				g.setVisible(true);
			}
		});
	}
	/** The list model. */
	DefaultListModel model;
	/** Field. */
	JList files;
	/** Field. */
	JButton add;
	/** Field. */
	JButton remove;
	/** Field. */
	JButton start;
	/** Field. */
	JButton stop;
	/** Field. */
	JProgressBar filesProgress;
	/** Field. */
	JProgressBar currentProgress;
	/** Field. */
	JLabel filesLabel;
	/** Field. */
	JLabel currentLabel;
	/** The background worker. */
	SwingWorker<Void, Void> worker;
	/** The current directory. */
	File currentDir = new File(".");
	/** The connection URL. */
	JTextField url;
	/** The user. */
	JTextField user;
	/** The password. */
	JPasswordField password;
	/** Truncate target first? */
	JCheckBox truncate;
	/**
	 * Constructor. Creates the GUI.
	 */
	public ConsignmentImportGUI() {
		setTitle("Import Consignment CSV data");
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				doClose();
			}
		});
		
		model = new DefaultListModel();
		files = new JList(model);
		add = new JButton("Add...");
		add.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doAdd();
			}
		});
		remove = new JButton("Remove");
		remove.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doRemove();
			}
		});
		start = new JButton("Start");
		start.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doStart();
			}
		});
		stop = new JButton("Stop");
		stop.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				doStop();
			}
		});
		stop.setEnabled(false);
		
		truncate = new JCheckBox("Truncate target?", true);
		
		filesProgress = new JProgressBar(JProgressBar.HORIZONTAL);
		currentProgress = new JProgressBar(JProgressBar.HORIZONTAL);
		filesLabel = new JLabel();
		currentLabel = new JLabel();
		
		JLabel urlLabel = new JLabel("URL:");
		JLabel userLabel = new JLabel("User:");
		JLabel passwordLabel = new JLabel("Password:");
		
		url = new JTextField("localhost/adb");
		user = new JTextField("root");
		password = new JPasswordField("advance");
		
		JScrollPane sp = new JScrollPane(files);
		
		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createParallelGroup()
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(sp)
				.addGroup(
					gl.createParallelGroup()
					.addComponent(add)
					.addComponent(remove)
				)
			)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(urlLabel)
				.addComponent(url)
				.addComponent(userLabel)
				.addComponent(user)
				.addComponent(passwordLabel)
				.addComponent(password)
			)
			.addComponent(truncate)
			.addGroup(
				gl.createSequentialGroup()
				.addComponent(start)
				.addComponent(stop)
			)
			.addComponent(filesLabel)
			.addComponent(filesProgress)
			.addComponent(currentLabel)
			.addComponent(currentProgress)
		);
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addGroup(
				gl.createParallelGroup()
				.addComponent(sp)
				.addGroup(
					gl.createSequentialGroup()
					.addComponent(add)
					.addComponent(remove)
				)
			)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(urlLabel)
				.addComponent(url)
				.addComponent(userLabel)
				.addComponent(user)
				.addComponent(passwordLabel)
				.addComponent(password)
			)
			.addComponent(truncate)
			.addGroup(
				gl.createParallelGroup(Alignment.BASELINE)
				.addComponent(start)
				.addComponent(stop)
			)
			.addComponent(filesLabel)
			.addComponent(filesProgress)
			.addComponent(currentLabel)
			.addComponent(currentProgress)
		);
		
		pack();
		setLocationRelativeTo(null);
	}
	/** Remove the selected elements from the list. */
	void doRemove() {
		int[] idxs = files.getSelectedIndices();
		for (int i = idxs.length - 1; i >= 0; i--) {
			model.remove(idxs[i]);
		}
		
	}
	/** Add files to list. */
	void doAdd() {
		JFileChooser fc = new JFileChooser(currentDir);
		fc.setMultiSelectionEnabled(true);
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "ZIP files";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".zip");
			}
		});
		fc.addChoosableFileFilter(new FileFilter() {
			@Override
			public String getDescription() {
				return "CSV files";
			}
			
			@Override
			public boolean accept(File f) {
				return f.getName().toLowerCase().endsWith(".csv");
			}
		});
		
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			File[] fs = fc.getSelectedFiles();
			if (fs.length > 0) {
				currentDir = fs[0].getParentFile();
				
				for (File f : fs) {
					model.addElement(f.toString());
				}
			}
		}
	}
	/**
	 * Close the window.
	 */
	void doClose() {
		if (worker != null) {
			worker.cancel(true);
		}
		// TODO implement
		dispose();
	}
	/** Start the upload. */
	void doStart() {
		stop.setEnabled(true);
		start.setEnabled(false);
		
		final List<String> fileList = Lists.newArrayList();

		final String dbURL = url.getText();
		final String dbUser = user.getText();
		final String dbPw = new String(password.getPassword());
		
		for (int i = 0; i < model.size(); i++) {
			fileList.add((String)model.getElementAt(i));
		}
		
		final boolean tr = truncate.isSelected();
		
		worker = new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground() throws Exception {
				ConsignmentImportGUI.this.process(fileList, this, dbURL, dbUser, dbPw, tr);
				return null;
			}
			@Override
			protected void done() {
				stop.setEnabled(false);
				start.setEnabled(true);
				currentProgress.setIndeterminate(false);
		}
		};
		worker.execute();
	}
	/** Stop the upload. */
	void doStop() {
		if (worker != null) {
			worker.cancel(true);
			worker = null;
		}
		stop.setEnabled(false);
		start.setEnabled(true);
		currentProgress.setIndeterminate(false);
	}
	/**
	 * Process files.
	 * @param files the list of files
	 * @param worker the parent worker
	 * @param dbURL the database url
	 * @param dbUser the user
	 * @param dbPw the password
	 * @param trunc truncate?
	 */
	void process(List<String> files, SwingWorker<?, ?> worker, 
			String dbURL, String dbUser, String dbPw, boolean trunc) {
		final int count = files.size();
		Map<String, Integer> strings = Maps.newLinkedHashMap();
		ConsignmentRow r = new ConsignmentRow();
		int i = 0;
		
		// ***********************************
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
	        conn = DriverManager.getConnection("jdbc:mysql://" + dbURL, dbUser, dbPw);
			
	        conn.setAutoCommit(false);
	        
	        if (trunc) {
	        	PreparedStatement pstmt = conn.prepareStatement("TRUNCATE TABLE adb.Consignment_Raw");
	        	pstmt.executeUpdate();
	        	pstmt.close();
	        }
	        
			PreparedStatement pstmtCons = conn.prepareStatement(
					"INSERT INTO adb.Consignment_Raw ("
					+ "entered, manifested, hub, "
					+ "collecting_depot, collection_postcode, collection_lat, "
					+ "collection_long, delivery_depot, delivery_postcode, "
					+ "delivery_lat, delivery_long, flags, "
					+ "lifts, paying_depot, weight, "
					+ "q, h, f "
					+ ") VALUES ("
					+ "?, ?, ?, "
					+ "?, ?, ?, "
					+ "?, ?, ?, "
					+ "?, ?, ?, "
					+ "?, ?, ?, "
					+ "?, ?, ? "
					+ ")"
			);
			
			// ***********************************
			int bigtotal = 0;
	
			for (final String s : files) {
				final int j = i;
	
				if (worker.isCancelled()) {
					return;
				}
				
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						filesLabel.setText("Files: " + j + " / " + count + ": " + s);
						filesProgress.setMaximum(0);
						filesProgress.setMaximum(count);
						filesProgress.setValue(j);
						currentLabel.setText("");
						currentProgress.setIndeterminate(true);
					}
				});
				
				BufferedReader in = null;
				try {
					if (s.toLowerCase().endsWith(".zip")) {
	
						ZipInputStream zip = new ZipInputStream(
								new BufferedInputStream(new FileInputStream(s), 64 * 1024));
						zip.getNextEntry();
	
						in = new BufferedReader(
								new InputStreamReader(zip, "ISO-8859-1"), 64 * 1024);
					} else {
						in = new BufferedReader(new FileReader(s), 64 * 1024);
					}
					int total = 0;
					while (!worker.isCancelled()) {
						List<String> line = U.csvLine(in);
						if (line == null) {
							break;
						}
						if (line.size() > 0) {
							try {
								if (!"Entered Date".equals(line.get(0))) {
									ConsignmentRow.parseRow(i, line, r, strings);
									
									// ***********************************
	
									int p = 1;
									
									pstmtCons.setTimestamp(p++, new Timestamp(r.enteredDateTime.getMillis()));
									pstmtCons.setTimestamp(p++, new Timestamp(r.manifestedDateTime.getMillis()));
									
									pstmtCons.setString(p++, r.hubFacility);
									
									pstmtCons.setInt(p++, r.collectingDepot);
									if (r.collectionPostCode.length() > 10) {
										r.collectionPostCode = r.collectionPostCode.substring(0, 10);
									}
									pstmtCons.setString(p++, r.collectionPostCode);
									
									if (r.collectionGPS != null) {
										pstmtCons.setDouble(p++, r.collectionGPS.x);
										pstmtCons.setDouble(p++, r.collectionGPS.y);
									} else {
										pstmtCons.setNull(p++, Types.DOUBLE);
										pstmtCons.setNull(p++, Types.DOUBLE);
									}

									pstmtCons.setInt(p++, r.deliveryDepot);
									if (r.deliveryPostCode.length() > 10) {
										r.deliveryPostCode = r.deliveryPostCode.substring(0, 10);
									}
									pstmtCons.setString(p++, r.deliveryPostCode);
									
									if (r.deliveryGPS != null) {
										pstmtCons.setDouble(p++, r.deliveryGPS.x);
										pstmtCons.setDouble(p++, r.deliveryGPS.y);
									} else {
										pstmtCons.setNull(p++, Types.DOUBLE);
										pstmtCons.setNull(p++, Types.DOUBLE);
									}

									pstmtCons.setInt(p++, r.getFlags());
									pstmtCons.setInt(p++, r.lifts);
									pstmtCons.setInt(p++, r.payingDepot);
									pstmtCons.setInt(p++, r.consignmentWeight);
									pstmtCons.setInt(p++, r.q);
									pstmtCons.setInt(p++, r.h);
									pstmtCons.setInt(p++, r.f);
									
									pstmtCons.addBatch();
									
									// ***********************************
									total++;
									bigtotal++;
									
									if (total % 10000 == 0) {
										pstmtCons.executeBatch();
										conn.commit();
									}
								}
							} catch (NumberFormatException ex) {
								ex.printStackTrace();
							}
						}
						if (total % 10000 == 0) {
							final int ftotal = total;
							final int fbigtotal = bigtotal;
							SwingUtilities.invokeLater(new Runnable() {
								@Override
								public void run() {
									currentLabel.setText(String.format("%,d ; %,d", ftotal, fbigtotal));
								}
							});
						}
					}
	
					
				} catch (IOException ex) {
					ex.printStackTrace();
				} finally {
					if (in != null) {
						Closeables.closeQuietly(in);
					}
				}
				i++;
			}
			pstmtCons.executeBatch();
			conn.commit();
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException ex) {
					ex.printStackTrace();
				}
			}
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					filesLabel.setText("Files: " + count + " / " + count);
					filesProgress.setValue(count);
					currentLabel.setText("");
					currentProgress.setIndeterminate(false);
				}
			});
		}
	}
}
