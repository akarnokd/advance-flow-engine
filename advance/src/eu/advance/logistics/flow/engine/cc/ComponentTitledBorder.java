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

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * A titled border with custom component as its label.
 * @author akarnokd, 2011.10.13.
 */
public class ComponentTitledBorder implements Border, MouseListener, SwingConstants {
	/** The component-edge distance. */
	int offset = 5; 
	/** The wrapped component. */
	Component comp; 
	/** The inner container. */
	JComponent container;
	/** The base rectangle. */
	Rectangle rect; 
	/** The border. */
	Border border; 

	/**
	 * Constructor. Initializes the components.
	 * @param comp the component to display as title
	 * @param container the container within the borders
	 * @param border the border type
	 */
	public ComponentTitledBorder(Component comp, JComponent container, Border border) {
		this.comp = comp; 
		this.container = container; 
		this.border = border; 
		container.addMouseListener(this); 
	} 
	@Override
	public boolean isBorderOpaque() {
		return true; 
	} 

	@Override
	public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
		Insets borderInsets = border.getBorderInsets(c); 
		Insets insets = getBorderInsets(c); 
		int temp = (insets.top - borderInsets.top) / 2; 
		border.paintBorder(c, g, x, y + temp, width, height - temp); 
		Dimension size = comp.getPreferredSize(); 
		rect = new Rectangle(offset, 0, size.width, size.height); 
		SwingUtilities.paintComponent(g, comp, (Container)c, rect); 
	} 

	@Override
	public Insets getBorderInsets(Component c) {
		Dimension size = comp.getPreferredSize(); 
		Insets insets = border.getBorderInsets(c); 
		insets.top = Math.max(insets.top, size.height); 
		return insets; 
	} 
	/**
	 * Dispatch the mouse event to the title component or the inner component. 
	 * @param me the event
	 */
	private void dispatchEvent(MouseEvent me) { 
		if (rect != null && rect.contains(me.getX(), me.getY())) {
			Point pt = me.getPoint(); 
			pt.translate(-offset, 0); 
			comp.setBounds(rect); 
			comp.dispatchEvent(new MouseEvent(comp, me.getID() 
					, me.getWhen(), me.getModifiers() 
					, pt.x, pt.y, me.getClickCount() 
					, me.isPopupTrigger(), me.getButton())); 
			if (!comp.isValid()) {
				container.repaint();
			}
		} 
	} 

	@Override
	public void mouseClicked(MouseEvent me) {
		dispatchEvent(me); 
	} 

	@Override
	public void mouseEntered(MouseEvent me) {
		dispatchEvent(me); 
	} 

	@Override
	public void mouseExited(MouseEvent me) {
		dispatchEvent(me); 
	} 

	@Override
	public void mousePressed(MouseEvent me) {
		dispatchEvent(me); 
	} 

	@Override
	public void mouseReleased(MouseEvent me) {
		dispatchEvent(me); 
	} 
}