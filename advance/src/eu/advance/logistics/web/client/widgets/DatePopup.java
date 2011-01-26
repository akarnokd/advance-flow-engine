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
package eu.advance.logistics.web.client.widgets;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.datepicker.client.DatePicker;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.web.model.LabelProvider;

/**
 * A generic date picker as popup.
 * The label provider should have the following entries:
 * "Close", "Choose a date", "Undefined", "Clear date"
 * @author karnokd
 */
public class DatePopup extends Composite {
	/** The underlying date time picker. */
	private DatePicker dtPicker;
	/**
	 * The constructor.
	 * @param ctx the client context
	 * @param date the initial date
	 * @param clearable allow clearing the value?
	 * @param formatter the date formatter for the label
	 */
	public DatePopup(@NonNull final LabelProvider ctx, @Nullable Date date, boolean clearable, @NonNull final DateTimeFormat formatter) {
		HorizontalPanel panel = new HorizontalPanel();
		final PopupPanel popup = new PopupPanel(true);
		dtPicker = new DatePicker();
		VerticalPanel vp = new VerticalPanel();
		vp.add(dtPicker);
		
		Button close = new Button(ctx.getLabel("Close"));
		close.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				popup.hide();
			}
		});
		HorizontalPanel hp = new HorizontalPanel();
		hp.getElement().getStyle().setProperty("marginLeft", "auto");
		hp.getElement().getStyle().setProperty("marginRight", "auto");
		hp.add(close);
		vp.add(hp);
		
		popup.setWidget(vp);
		
		final Label dateLabel = new Label();
		dateLabel.setTitle(ctx.getLabel("Choose a date"));
		dateLabel.getElement().getStyle().setProperty("cursor", "hand");
		dateLabel.getElement().getStyle().setProperty("cursor", "pointer");
		final Image imageDown = new Image("images/popup.png");
		imageDown.getElement().getStyle().setProperty("cursor", "hand");
		imageDown.getElement().getStyle().setProperty("cursor", "pointer");
		imageDown.setTitle(ctx.getLabel("Choose a date"));

		panel.add(dateLabel);
		panel.add(new HTML("&nbsp;"));
		panel.add(imageDown);
		
		if (clearable) {
			final Image clearImage = new Image("images/clear.png");
			clearImage.setTitle(ctx.getLabel("Clear date"));
			clearImage.getElement().getStyle().setProperty("cursor", "hand");
			clearImage.getElement().getStyle().setProperty("cursor", "pointer");
			panel.add(new HTML("&nbsp;"));
			panel.add(clearImage);
			clearImage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dateLabel.setText(ctx.getLabel("Undefined"));
					dtPicker.setValue(null);
				}
			});
		}
		
		ClickHandler labelClick = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				popup.showRelativeTo(dateLabel);
			}
		};
		dateLabel.addClickHandler(labelClick);
		imageDown.addClickHandler(labelClick);
		
		if (date != null) {
			dtPicker.setValue(date, true);
			dtPicker.setCurrentMonth(date);
			dateLabel.setText(formatter.format(date));
		} else {
			dateLabel.setText(ctx.getLabel("Undefined"));
		}
		dtPicker.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				dateLabel.setText(formatter.format(dtPicker.getValue()));
				popup.hide();
			}
		});
		initWidget(panel);
	}
	/**
	 * @return returns the selected date or null if no selection has been made yet
	 */
	@CheckForNull
	public Date getDate() {
		return dtPicker.getValue();
	}
	/**
	 * Set the selected date.
	 * @param date the date
	 */
	public void setDate(@Nullable Date date) {
		dtPicker.setValue(date, true);
	}
}
