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

import hu.akarnokd.reactive4java.base.Action0;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JPanel;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * Contains common buttons used by dialogs.
 * @author akarnokd, 2011.10.11.
 */
public class DetailButtonPanel extends JPanel {
	/** */
	private static final long serialVersionUID = 3767455018968092786L;
	/** Refresh current view. */
	JButton refresh;
	/** Save. */
	JButton save;
	/** Save and close. */
	JButton saveAndClose;
	/** Close. */
	JButton close;
	/** The action for refresh. */
	Action0 onRefresh;
	/** The action for save. */
	Action0 onSave;
	/** The action for save and close. */
	Action0 onSaveAndClose;
	/** The action for close. */
	Action0 onClose;
	/**
	 * Creates the panel GUI.
	 * @param labels the label manager
	 */
	public DetailButtonPanel(@NonNull final LabelManager labels) {
		refresh = new JButton(labels.get("Refresh"));
		refresh.addActionListener(GUIUtils.createFromMethod(this, "doRefresh"));
		save = new JButton(labels.get("Save"));
		save.addActionListener(GUIUtils.createFromMethod(this, "doSave"));
		saveAndClose = new JButton(labels.get("Save & Close"));
		saveAndClose.addActionListener(GUIUtils.createFromMethod(this, "doSaveAndClose"));
		close = new JButton(labels.get("Close"));
		close.addActionListener(GUIUtils.createFromMethod(this, "doClose"));
		
		GroupLayout gl = new GroupLayout(this);
		setLayout(gl);
		gl.setAutoCreateGaps(true);
		
		gl.setHorizontalGroup(
			gl.createSequentialGroup()
			.addComponent(refresh)
			.addGap(20)
			.addComponent(save)
			.addComponent(saveAndClose)
			.addGap(20)
			.addComponent(close)
		);
		gl.setVerticalGroup(
			gl.createParallelGroup(Alignment.BASELINE)
			.addComponent(refresh)
			.addComponent(save)
			.addComponent(saveAndClose)
			.addComponent(close)
		);
		gl.setHonorsVisibility(refresh, false);
	}
	/** The refresh action. */
	void doRefresh() {
		if (onRefresh != null) {
			onRefresh.invoke();
		}
	}
	/** The save action. */
	void doSave() {
		if (onSave != null) {
			onSave.invoke();
		}
	}
	/** The save and close action. */
	void doSaveAndClose() {
		if (onSaveAndClose != null) {
			onSaveAndClose.invoke();
		}
	}
	/** The close action. */
	void doClose() {
		if (onClose != null) {
			onClose.invoke();
		}
	}
	/**
	 * Set the refresh action.
	 * @param action the action
	 */
	public void setRefresh(Action0 action) {
		onRefresh = action;
	}
	/**
	 * Set the save action.
	 * @param action the action
	 */
	public void setSave(Action0 action) {
		onSave = action;
	}
	/**
	 * Set the save and close action.
	 * @param action the action
	 */
	public void setSaveAndClose(Action0 action) {
		onSaveAndClose = action;
	}
	/**
	 * Set the close action.
	 * @param action the action
	 */
	public void setClose(Action0 action) {
		onClose = action;
	}
	/**
	 * Show the refresh button?
	 * @param visible is visible?
	 */
	public void showRefresh(boolean visible) {
		refresh.setVisible(visible);
	}
}
