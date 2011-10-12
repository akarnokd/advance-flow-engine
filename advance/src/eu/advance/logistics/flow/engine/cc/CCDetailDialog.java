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

import java.awt.Container;
import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.Group;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JSeparator;

import eu.advance.logistics.flow.engine.api.Identifiable;

/**
 * Edit/create a web data source.
 * @author karnokd, 2011.10.11.
 * @param <T> the paging element type
 */
public class CCDetailDialog<T extends Identifiable<?>> extends JDialog {
	/** */
	private static final long serialVersionUID = 558752187801196404L;
	/** The pager. */
	public final Pager<T> pager;
	/** The create modify panel. */
	CreateModifyPanel createModify;
	/** The engine info panel. */
	public final EngineInfoPanel engineInfo;
	/** The help panel. */
	public final HelpPanel helpPanel;
	/** The button panel. */
	public final DetailButtonPanel buttons;
	/** The top separator below the engine info. */
	protected JSeparator topSeparator;
	/**
	 * Create the GUI.
	 * @param labels the label manager
	 * @param body the list of components which will be laid out at each line.
	 */
	public CCDetailDialog(LabelManager labels, JComponent... body) {
		setModalityType(ModalityType.APPLICATION_MODAL);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		
		pager = new Pager<T>();
		createModify = new CreateModifyPanel(labels);
		engineInfo = new EngineInfoPanel(labels);
		helpPanel = new HelpPanel();
		buttons = new DetailButtonPanel(labels);
		
		topSeparator = new JSeparator(JSeparator.HORIZONTAL);
		
		Container c = getContentPane();
		GroupLayout gl = new GroupLayout(c);
		c.setLayout(gl);
		gl.setAutoCreateContainerGaps(true);
		gl.setAutoCreateGaps(true);

		Group hg = gl.createParallelGroup(Alignment.CENTER)
		.addComponent(engineInfo)
		.addComponent(topSeparator)
		.addComponent(pager)
		.addComponent(createModify)
		;
		Group vg = gl.createSequentialGroup()
		.addComponent(engineInfo)
		.addComponent(topSeparator, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE, GroupLayout.PREFERRED_SIZE)
		.addComponent(pager)
		.addComponent(createModify)
		;
		
		for (JComponent jc : body) {
			hg.addComponent(jc);
			vg.addComponent(jc);
		}
		
		hg.addComponent(helpPanel)
		.addComponent(buttons)
		;
		vg
		.addComponent(helpPanel)
		.addComponent(buttons)
		;
		
		gl.setHorizontalGroup(hg);
		gl.setVerticalGroup(vg);
		pack();
		setResizable(false);
	}
	/**
	 * Show the create/modify labels?
	 * @param visible if visible
	 */
	public void showCreateModify(boolean visible) {
		createModify.setVisible(visible);
	}
	/**
	 * Show the engine info?
	 * @param visible if visible
	 */
	public void showEngineInfo(boolean visible) {
		engineInfo.setVisible(visible);
		topSeparator.setVisible(visible);
	}
	/**
	 * Show the pager?
	 * @param visible if visible
	 */
	public void showPager(boolean visible) {
		pager.setVisible(visible);
	}
	/**
	 * Close the window by sending the window closing event to execute listeners.
	 */
	public void close() {
		WindowEvent we = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(we);
	}
}
