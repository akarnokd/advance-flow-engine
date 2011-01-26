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

import java.awt.Image;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.TransferHandler;

/**
 * Image selection for copy-pasting images to and from clipboard.
 * http://www.java2s
 * .com/Code/Java/Development-Class/SendingImageObjectsthroughtheClipboard.htm
 * 
 * @author karnokd, 2009.08.03.
 * @version $Revision 1.0$
 */
public class ImageSelection extends TransferHandler implements Transferable {
	/** */
	private static final long serialVersionUID = -371601312296712557L;
	/** The data flawor array. */
	private static final DataFlavor[] FLAVORS = { DataFlavor.imageFlavor };
	/** The source of the image. */
	private JLabel source;
    /** The actual image. */
	private Image image;
	/**
	 * {@inheritDoc}
	 */
	@Override
	public int getSourceActions(JComponent c) {
		return TransferHandler.COPY;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean canImport(JComponent comp, DataFlavor[] flavor) {
		if (!(comp instanceof JLabel)) {
			return false;
		}
		for (int i = 0, n = flavor.length; i < n; i++) {
			for (int j = 0, m = FLAVORS.length; j < m; j++) {
				if (flavor[i].equals(FLAVORS[j])) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Transferable createTransferable(JComponent comp) {
		// Clear
		source = null;
		image = null;

		if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			Icon icon = label.getIcon();
			if (icon instanceof ImageIcon) {
				image = ((ImageIcon) icon).getImage();
				source = label;
				return this;
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean importData(JComponent comp, Transferable t) {
		if (comp instanceof JLabel) {
			JLabel label = (JLabel) comp;
			if (t.isDataFlavorSupported(FLAVORS[0])) {
				try {
					image = (Image) t.getTransferData(FLAVORS[0]);
					ImageIcon icon = new ImageIcon(image);
					label.setIcon(icon);
					return true;
				} catch (UnsupportedFlavorException ignored) {
				} catch (IOException ignored) {
				}
			}
		}
		return false;
	}

	// Transferable
	/**
	 * {@inheritDoc}
	 */
	@Override
	public Object getTransferData(DataFlavor flavor) {
		if (isDataFlavorSupported(flavor)) {
			return image;
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public DataFlavor[] getTransferDataFlavors() {
		return FLAVORS;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isDataFlavorSupported(DataFlavor flavor) {
		return flavor.equals(DataFlavor.imageFlavor);
	}
	/**
	 * Returns the source object.
	 * @return the source object
	 */
	public JLabel getSource() {
		return source;
	}
}
