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

package eu.advance.logistics.flow.editor;

import hu.akarnokd.reactive4java.interactive.Interactive;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.Comparator;

import javax.imageio.ImageIO;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import javax.xml.stream.XMLStreamException;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.model.AdvanceBlockDescription;
import eu.advance.logistics.xml.typesystem.XElement;

/**
 * The main flow editor panel used as the body within the editor application or applet.
 * @author karnokd
 */
public class FlowEditorPanel extends JPanel {
	/** */
	private static final long serialVersionUID = 518887456444732683L;
	/** The main window setup. */
	@NonNull
	protected MainWindowCallback mainWindow;
	/** The block renderer. */
	protected BlockRenderer blockRenderer;
	/** Block template panel. */
	private AdvanceBlockTemplatePanel blockTemplatePanel;
	/** Block tree panel. */
	private AdvanceBlockTreePanel blockTreePanel;
	/**
	 * Constructor.
	 * @param mainWindow 
	 */
	public FlowEditorPanel(@NonNull MainWindowCallback mainWindow) {
		this.mainWindow = mainWindow;
		createGUI();
	}
	/** Build up the GUI. */
	void createGUI() {
		setLayout(new BorderLayout());
		
		blockRenderer = new BlockRenderer();

		blockTemplatePanel = new AdvanceBlockTemplatePanel();
		blockTreePanel = new AdvanceBlockTreePanel();
		
		JSplitPane split0 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		split0.setOneTouchExpandable(true);

		final JSplitPane templateAndTree = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		
		split0.setLeftComponent(templateAndTree);
		split0.setRightComponent(blockRenderer);
		templateAndTree.setTopComponent(blockTemplatePanel);
		templateAndTree.setBottomComponent(blockTreePanel);
		
		
		add(split0, BorderLayout.CENTER);
		
		buildMenu();
		createBlocks();
		blockTemplatePanel.refreshTemplates();
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				templateAndTree.setDividerLocation(0.5);
			}
		});
	}
	/**
	 * The block tree view panel. 
	 */
	class AdvanceBlockTreePanel extends JPanel {
		/** */
		private static final long serialVersionUID = 1130320800613221782L;
		/** The root node. */
		DefaultMutableTreeNode root;
		/** The tree. */
		JTree tree;
		/**
		 * Constructs the panel GUI. 
		 */
		public AdvanceBlockTreePanel() {
			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			
			JTextField filterBox = new JTextField();
			JButton clearFilter = new JButton("X");
			filterBox.setPreferredSize(new Dimension(150, 25));
			
			root = new DefaultMutableTreeNode("Flow");
			tree = new JTree(root);
			tree.setEditable(false);
			tree.setRootVisible(false);

			DefaultTreeCellRenderer treeRender = new DefaultTreeCellRenderer();
			
			treeRender.setLeafIcon(new ImageIcon(getImageResource("res/SimpleBlock.png")));
			treeRender.setOpenIcon(new ImageIcon(getImageResource("res/CompositeBlock.png")));
			treeRender.setClosedIcon(new ImageIcon(getImageResource("res/CompositeBlock.png")));
			
			tree.setCellRenderer(treeRender);
			
			JScrollPane treeScroll = new JScrollPane(tree);
			
			gl.setHorizontalGroup(
				gl.createSequentialGroup()
				.addGap(3)
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(filterBox)
						.addComponent(clearFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addComponent(treeScroll)
				)
				.addGap(3)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGap(3)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(filterBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(clearFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addComponent(treeScroll)
				.addGap(3)
			);
			
		}
	}
	/**
	 * Load an image resource.
	 * @param name the image name and path
	 * @return the buffered image
	 */
	BufferedImage getImageResource(String name) {
		try {
			URL u = getClass().getResource(name);
			if (u != null) {
				return ImageIO.read(u);
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return null;
	}
	/** The block template panel. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	class AdvanceBlockTemplatePanel extends JPanel {
		/** */
		private static final long serialVersionUID = -6921023975304659151L;
		/** The text filter box. */
		JTextField filterBox;
		/** The clear button. */
		JButton clearFilter;
		/** The list of templates. */
		JList templates;
		/**
		 * Constructs the panel GUI. 
		 */
		public AdvanceBlockTemplatePanel() {
			GroupLayout gl = new GroupLayout(this);
			setLayout(gl);
			
			filterBox = new JTextField();
			clearFilter = new JButton("X");
			filterBox.setPreferredSize(new Dimension(150, 25));

			templates = new JList();
			JScrollPane templatesScroll = new JScrollPane(templates);

			gl.setHorizontalGroup(
				gl.createSequentialGroup()
				.addGap(3)
				.addGroup(
					gl.createParallelGroup()
					.addGroup(
						gl.createSequentialGroup()
						.addComponent(filterBox)
						.addComponent(clearFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					)
					.addComponent(templatesScroll)
				)
				.addGap(3)
			);
			gl.setVerticalGroup(
				gl.createSequentialGroup()
				.addGap(3)
				.addGroup(
					gl.createParallelGroup(Alignment.BASELINE)
					.addComponent(filterBox, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(clearFilter, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
				)
				.addComponent(templatesScroll)
				.addGap(3)
			);
		}
		/** Refresh templates. */
		void refreshTemplates() {
			DefaultListModel dlm = new DefaultListModel();
			try {
				for (AdvanceBlockDescription bd 
						: Interactive.orderBy(
							AdvanceBlockDescription.parse(XElement.parseXML("schemas/block-registry.xml")),
							new Comparator<AdvanceBlockDescription>() {
								@Override
								public int compare(AdvanceBlockDescription o1,
										AdvanceBlockDescription o2) {
									return o1.id.compareToIgnoreCase(o2.id);
								}
							}
					)) {
					dlm.addElement(bd.id);
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			} catch (XMLStreamException ex) {
				ex.printStackTrace();
			}
			templates.setModel(dlm);
		}
	}
	/** Build the menu structure. */
	void buildMenu() {
		JMenuBar menubar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("File");
		JMenuItem fileExit = new JMenuItem("Exit");
		fileExit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				mainWindow.exit();
			}
		});
		
		fileMenu.add(fileExit);
		menubar.add(fileMenu);
		
		mainWindow.setJMenuBar(menubar);
	}
	/**
	 * Create some sample blocks.
	 */
	void createBlocks() {
		Block b1 = new Block();
		b1.x = 50;
		b1.y = 50;
		b1.name = "Block 1";
		b1.outputs.add("out");
		
		Block b2 = new Block();
		b2.x = 150;
		b2.y = 150;
		b2.name = "Block 2";
		b2.inputs.add("in");
		b2.outputs.add("out");

		Block b3 = new Block();
		b3.x = 50;
		b3.y = 250;
		b3.name = "Block 3";
		b3.inputs.add("in1");
		b3.inputs.add("in2");

		Block b4 = new Block();
		b4.x = 0;
		b4.y = 150;
		b4.name = "Block 4";
		b4.outputs.add("out");
		
		Wire w1 = new Wire(b1, 0, b2, 0);
		Wire w2 = new Wire(b2, 0, b3, 0);
		Wire w3 = new Wire(b4, 0, b3, 1);
		
		blockRenderer.blocks.add(b1);
		blockRenderer.blocks.add(b2);
		blockRenderer.blocks.add(b3);
		blockRenderer.blocks.add(b4);
		
		blockRenderer.wires.add(w1);
		blockRenderer.wires.add(w2);
		blockRenderer.wires.add(w3);
		
		blockRenderer.autoHorizontalLayout();
		
		blockTreePanel.root.add(new DefaultMutableTreeNode(b1.name));
		blockTreePanel.root.add(new DefaultMutableTreeNode(b2.name));
		blockTreePanel.root.add(new DefaultMutableTreeNode(b3.name));
		blockTreePanel.root.add(new DefaultMutableTreeNode(b4.name));
		
		blockTreePanel.tree.expandPath(new TreePath(blockTreePanel.root));

	}
}
