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

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URI;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Contains a help button and a horizontal divider.
 * @author karnokd, 2011.10.11.
 *
 */
public class HelpPanel extends JPanel {
	/** The logger. */
	protected static final Logger LOG = LoggerFactory.getLogger(HelpPanel.class);
	/** */
	private static final long serialVersionUID = -3965821161538542815L;
	/** The help button. */
	protected JButton help;
	/** The bottom separator. */
	protected JSeparator separator;
	/** Create the GUI. */
	public HelpPanel() {
		separator = new JSeparator(JSeparator.HORIZONTAL);
		help = new JButton(new ImageIcon(getClass().getResource("help.png")));
		int h = help.getPreferredSize().height;

		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(help, h, h, h)
			.addComponent(separator)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(help)
			.addComponent(separator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		);
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
