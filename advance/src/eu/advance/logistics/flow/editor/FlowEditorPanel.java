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

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import edu.umd.cs.findbugs.annotations.NonNull;

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
		
		URL logo = getClass().getResource("advlogo_192x128.png");
		
		add(new JLabel(new ImageIcon(logo)), BorderLayout.CENTER);

		buildMenu();
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
}
