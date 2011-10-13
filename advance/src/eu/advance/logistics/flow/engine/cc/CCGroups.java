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

import java.awt.Toolkit;
import java.awt.event.WindowEvent;

import javax.swing.JDialog;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The Notification groups editor.
 * @author karnokd, 2011.10.13.
 */
public class CCGroups extends JDialog {
	/** */
	private static final long serialVersionUID = 5984112593977949929L;
	/** The label manager. */
	protected final LabelManager labels;
	/**
	 * Constructor. Creates the dialog.
	 * @param labels the label manager
	 */
	public CCGroups(@NonNull final LabelManager labels) {
		this.labels = labels;
		setTitle(labels.get("Notification group"));
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	/** Close the window. */
	public void close() {
		WindowEvent e = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
		Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(e);
	}
	/** Retrieve the current data. */
	public void refresh() {
		// TODO
	}
}
