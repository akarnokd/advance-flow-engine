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

package eu.advance.logistics.web.model;

import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasVerticalAlignment.VerticalAlignmentConstant;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Widget;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * The common application context.
 * @author karnok, 2010.12.15.
 * @version $Revision 1.0$
 */
public class AdvanceContext implements LabelProvider {
	/** The service object. */
	@NonNull
	public AdvanceProjectServiceAsync service;
	/** The current labels. */
	@NonNull
	public Map<String, String> labels;
	/** The list of available language codes. */
	@NonNull
	public List<String> languageCodes;
	/**
	 * Retrieve a translation for the given label.
	 * @param label the label to translate
	 * @return the translated label or the {@code label} parameter if no translation is found
	 */
	public String get(String label) {
		String translation = labels.get(label);
		if (translation == null) {
			GWT.log("Missing label for: " + label);
			return label;
		}
		return translation;
	}
	/**
	 * Create a horizontal panel with the given centered objects.
	 * @param widgets the list of widgets to center
	 * @return the horizontal panel
	 */
	public HorizontalPanel center(Widget... widgets) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.getElement().getStyle().setProperty("marginLeft", "auto");
		hp.getElement().getStyle().setProperty("marginRight", "auto");
		for (Widget w : widgets) {
			hp.add(w);
		}
		return hp;
	}
	/**
	 * Create a horizontal panel with the given vertical alignment and given objects centered.
	 * @param align the vertical alignment: HasVerticalAlignment.* constants
	 * @param widgets the list of widgets
	 * @return the horizontal panel
	 */
	public HorizontalPanel center(VerticalAlignmentConstant align, Widget... widgets) {
		HorizontalPanel hp = new HorizontalPanel();
		hp.getElement().getStyle().setProperty("marginLeft", "auto");
		hp.getElement().getStyle().setProperty("marginRight", "auto");
		hp.setVerticalAlignment(align);
		for (Widget w : widgets) {
			hp.add(w);
		}
		return hp;
	}
	/**
	 * Set the cursor of the widget to the hand cursor.
	 * @param widget the target widget
	 */
	public void setHandCursor(Widget widget) {
		widget.getElement().getStyle().setProperty("cursor", "pointer");
		widget.getElement().getStyle().setProperty("cursor", "hand");
	}
	/**
	 * Create a fixed space region.
	 * @param n the number of spaces.
	 * @return the HTML element
	 */
	public HTML space(int n) {
		StringBuilder b = new StringBuilder();
		for (int i = 0; i < n; i++) {
			b.append("&nbsp;");
		}
		return new HTML(b.toString());
	}
	/**
	 * Create a simple line break.
	 * @return the HTML element with a line break
	 */
	public HTML br() {
		return new HTML("<br/>");
	}
	/**
	 * Create a horizontla ruler.
	 * @return the HTML element with the horizontal ruler
	 */
	public HTML hr() {
		return new HTML("<hr/>");
	}
	/**
	 * Create a fixed column-width table with the given list of Widgets and other objects as labels.
	 * @param columns the number of columnbs
	 * @param cells the cells to fill in, can be Widget elements or any other data types.
	 * These data types are converted to a string.
	 * @return the Flex table containing the cells
	 */
	public FlexTable table(int columns, Object... cells) {
		FlexTable result = new FlexTable();
		for (int i = 0; i < cells.length; i++) {
			int row = i / columns;
			int col = i % columns;
			Object cell = cells[i];
			if (cell instanceof Widget) {
				result.setWidget(row, col, (Widget)cell);
			} else 
			if (cell != null) {
				result.setText(row, col, String.valueOf(cell));
			}
		}
		return result;
	}
	@Override
	public String getLabel(String key) {
		return get(key);
	}
	
}
