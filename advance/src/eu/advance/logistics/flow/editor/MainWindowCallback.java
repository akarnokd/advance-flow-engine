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

import java.awt.event.ActionListener;

import javax.swing.JMenuBar;

import edu.umd.cs.findbugs.annotations.Nullable;

/**
 * Interface to abstract away method calls on the JFrame and JApplet components.
 * This should allow the flow editor components to be easily integrated into a standalone
 * or applet container.
 * @author karnokd
 */
public interface MainWindowCallback {
	/**
	 * Set the main menu bar.
	 * @param menubar the menubar object or null if you want to remove the main menu
	 */
	void setJMenuBar(@Nullable JMenuBar menubar);
	/**
	 * Set a callback for the case the applet/window is closes to allow saving the current document.
	 * @param onClose the close handler or null if you want to remove the current handler
	 */
	void setCloseHandler(@Nullable ActionListener onClose);
	/** 
	 * Perform the exit procedure. 
	 */
	void exit();
}
