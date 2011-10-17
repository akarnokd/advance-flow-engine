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

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Displays engine and login information.
 * @author akarnokd, 2011.10.11.
 */
public class EngineInfoPanel extends JPanel {
	/** */
	private static final long serialVersionUID = -2958092401144868998L;
	/** The engine url. */
	protected JLabel engineURL;
	/** The engine version. */
	protected JLabel engineVersion;
	/**
	 * Create the GUI.
	 * @param labels the label manager.
	 */
	public EngineInfoPanel(@NonNull final LabelManager labels) {
		engineURL = new JLabel();
		engineVersion = new JLabel();
		JLabel engineLabel = new JLabel(labels.get("Engine:"));
		JLabel versionLabel = new JLabel(labels.get("Version:"));

		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
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
		);
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
}
