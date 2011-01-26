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
import java.awt.event.ActionListener;

import javax.swing.JApplet;

/**
 * The flow editor implementation in applet style.
 * @author karnokd
 */
public class AdvanceFlowEditorApplet extends JApplet implements MainWindowCallback {
	/** */
	private static final long serialVersionUID = 3482252575254036925L;
	/** The flow editor component. */
	protected FlowEditorPanel editor;

	@Override
	public void init() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void start() {
		getContentPane().removeAll();
		editor = new FlowEditorPanel(this);
		getContentPane().add(editor, BorderLayout.CENTER);
		
		// TODO anything else?

	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void setCloseHandler(ActionListener onClose) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void exit() {
		// TODO Auto-generated method stub
		
	}

}
