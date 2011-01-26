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
package eu.advance.logistics.applet;

import java.awt.Container;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JDialog;
import javax.swing.JLabel;

/**
 * The usage dialog.
 * @author karnokd
 */
public class UsageDialog extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3563332521365631248L;

	/** 
	 * Constructor.
	 * @param model the model for the labels
	 */
	public UsageDialog(DataDiagram model) {
		setModal(true);
		setTitle(model.get("Help"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		Container c = getContentPane();
		
		GroupLayout gl = new GroupLayout(c);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);
		c.setLayout(gl);

		JLabel lbl1 = new JLabel(model.get("ScadaDiagramApplet.Line.1"), JLabel.CENTER);		
		JLabel lbl2 = new JLabel(model.get("ScadaDiagramApplet.Line.2"), JLabel.CENTER);		
		JLabel lbl3 = new JLabel(model.get("ScadaDiagramApplet.Line.3"), JLabel.CENTER);		
		JLabel lbl4 = new JLabel(model.get("ScadaDiagramApplet.Line.4"), JLabel.CENTER);		
		JLabel lbl5 = new JLabel(model.get("ScadaDiagramApplet.Line.5"), JLabel.CENTER);		
		JLabel lbl6 = new JLabel(model.get("ScadaDiagramApplet.Line.6"), JLabel.CENTER);		
		
		gl.setHorizontalGroup(
			gl.createParallelGroup(Alignment.CENTER)
			.addComponent(lbl1)
			.addComponent(lbl2)
			.addComponent(lbl3)
			.addComponent(lbl4)
			.addComponent(lbl5)
			.addComponent(lbl6)
		);
		
		gl.setVerticalGroup(
			gl.createSequentialGroup()
			.addComponent(lbl1)
			.addComponent(lbl2)
			.addComponent(lbl3)
			.addComponent(lbl4)
			.addComponent(lbl5)
			.addComponent(lbl6)
		);
		
		setResizable(false);
		pack();
	}
}
