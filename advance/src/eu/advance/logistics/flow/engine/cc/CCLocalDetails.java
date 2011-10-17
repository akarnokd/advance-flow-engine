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

import hu.akarnokd.reactive4java.base.Pair;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Group;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceLocalFileDataSource;

/**
 * The Local file data source details.
 * @author akarnokd, 2011.10.13.
 */
public class CCLocalDetails extends JPanel implements
		CCLoadSave<AdvanceLocalFileDataSource> {
	/** */
	private static final long serialVersionUID = 6201768105859035639L;
	/** The name. */
	protected JTextField name;
	/** The remote directory. */
	protected JTextField directory;
	/** The label manager. */
	protected final LabelManager labels;
	/**
	 * Create the GUI panel.
	 * @param labels the label manager
	 */
	public CCLocalDetails(@NonNull final LabelManager labels) {
		this.labels = labels;
		
		name = new JTextField();
		directory = new JTextField();
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		Pair<Group, Group> g = GUIUtils.createForm(gl, 2,
				labels.get("Name:"), name,
				labels.get("Directory:"), directory
		);
		gl.setHorizontalGroup(g.first);
		gl.setVerticalGroup(g.second);
	}
	@Override
	public void load(AdvanceLocalFileDataSource value) {
		name.setText(value.name);
		name.setEditable(false);
		directory.setText(value.directory);
	}

	@Override
	public AdvanceLocalFileDataSource save() {
		AdvanceLocalFileDataSource result = new AdvanceLocalFileDataSource();
		
		result.name = name.getText();
		if (result.name.isEmpty()) {
			GUIUtils.errorMessage(this, labels.get("Please enter a name!"));
			return null;
		}
		result.directory = directory.getText();
		
		return result;
	}

	@Override
	public void onAfterSave() {
		name.setEditable(false);
	}
}
