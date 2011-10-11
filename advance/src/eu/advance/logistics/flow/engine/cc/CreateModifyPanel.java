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
import eu.advance.logistics.flow.engine.api.AdvanceCreateModifyInfo;

/**
 * Displays labels for the creation and modificationd dates.
 * @author karnokd, 2011.10.11.
 */
public class CreateModifyPanel extends JPanel {
	/** */
	private static final long serialVersionUID = 5887799713482515971L;
	/** The create value. */
	protected JLabel createdAtBy;
	/** The modify value. */
	protected JLabel modifiedAtBy;
	/**
	 * Create the GUI.
	 * @param labels the label manager
	 */
	public CreateModifyPanel(@NonNull LabelManager labels) {
		JLabel createdLabel = new JLabel(labels.get("Created:"));
		JLabel modifiedLabel = new JLabel(labels.get("Modified:"));
		createdAtBy = new JLabel();
		modifiedAtBy = new JLabel();
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(createdLabel)
			.addComponent(createdAtBy)
			.addGap(30)
			.addComponent(modifiedLabel)
			.addComponent(modifiedAtBy)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(createdLabel)
			.addComponent(createdAtBy)
			.addComponent(modifiedLabel)
			.addComponent(modifiedAtBy)
		);
	}
	/**
	 * Set the labels according to the info.
	 * @param info the info
	 */
	public void set(AdvanceCreateModifyInfo info) {
		if (info != null) {
			createdAtBy.setText(info.createdAt + " by " + info.createdBy);
			modifiedAtBy.setText(info.modifiedAt + " by " + info.modifiedBy);
		} else {
			createdAtBy.setText("-");
			modifiedAtBy.setText("-");
		}
	}
}
