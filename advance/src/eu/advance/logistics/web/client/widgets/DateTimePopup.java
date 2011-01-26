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
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import eu.advance.logistics.web.model.LabelProvider;

/**
 * A generic date time picker as popup.
 * The label provider should provide the following labels:
 * "Close", "Choose a date and time", "Clear date and time", "Undefined"
 * @author karnokd
 */
public class DateTimePopup extends Composite {
	/** The underlying date time picker. */
	private DateTimePicker dtPicker;
	/**
	 * The constructor.
	 * @param ctx the client context
	 * @param date the initial date
	 * @param clearable allow clearing the value?
	 * @param formatter the date formatter for the label
	 */
	public DateTimePopup(@NonNull final LabelProvider ctx, @Nullable Date date, boolean clearable, @NonNull final DateTimeFormat formatter) {
		HorizontalPanel panel = new HorizontalPanel();
		final PopupPanel popup = new PopupPanel(true);
		dtPicker = new DateTimePicker(true);
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
		dateLabel.setTitle(ctx.getLabel("Choose a date and time"));
		dateLabel.getElement().getStyle().setProperty("cursor", "hand");
		dateLabel.getElement().getStyle().setProperty("cursor", "pointer");
		final Image imageDown = new Image("images/popup.png");
		imageDown.getElement().getStyle().setProperty("cursor", "hand");
		imageDown.getElement().getStyle().setProperty("cursor", "pointer");
		imageDown.setTitle(ctx.getLabel("Choose a date and time"));

		panel.add(dateLabel);
		panel.add(new HTML("&nbsp;"));
		panel.add(imageDown);
		
		if (clearable) {
			final Image clearImage = new Image("images/clear.png");
			clearImage.setTitle(ctx.getLabel("Clear date and time"));
			clearImage.getElement().getStyle().setProperty("cursor", "hand");
			clearImage.getElement().getStyle().setProperty("cursor", "pointer");
			panel.add(new HTML("&nbsp;"));
			panel.add(clearImage);
			clearImage.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					dateLabel.setText(ctx.getLabel("Undefined"));
					dtPicker.getDatePicker().setValue(null);
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
			dtPicker.getTimePicker().setDateTime(date);
			dtPicker.getDatePicker().setValue(date, true);
			dtPicker.getDatePicker().setCurrentMonth(date);
			dateLabel.setText(formatter.format(date));
		} else {
			dateLabel.setText(ctx.getLabel("Undefined"));
		}
		dtPicker.getDatePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				dtPicker.getTimePicker().setDate(event.getValue());
				dateLabel.setText(formatter.format(dtPicker.getTimePicker().getDateTime()));
			}
		});
		dtPicker.getTimePicker().addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				dateLabel.setText(formatter.format(dtPicker.getTimePicker().getDateTime()));
			}
		});
		initWidget(panel);
	}
	/**
	 * @return returns the selected date or null if no selection has been made yet
	 */
	@CheckForNull
	public Date getDateTime() {
		if (dtPicker.getDatePicker().getValue() != null) {
			return dtPicker.getTimePicker().getDateTime();
		}
		return null;
	}
	/**
	 * Add value change event handler.
	 * @param event the event handler
	 * @return the handler registration
	 */
	public HandlerRegistration addValueChangeHandler(ValueChangeHandler<Date> event) {
		final HandlerRegistration h1 = dtPicker.getDatePicker().addValueChangeHandler(event);
		final HandlerRegistration h2 = dtPicker.getTimePicker().addValueChangeHandler(event);
		
		return new HandlerRegistration() {
			@Override
			public void removeHandler() {
				h1.removeHandler();
				h2.removeHandler();
			}
		}; 
	}
	/**
	 * Set the current selected date and time.
	 * @param datetime the value
	 */
	public void setDateTime(@Nullable Date datetime) {
		if (datetime != null) {
			dtPicker.getTimePicker().setDateTime(datetime);
		}
		dtPicker.getDatePicker().setValue(datetime, true);
		if (datetime != null) {
			dtPicker.getDatePicker().setCurrentMonth(datetime);
		}
	}
}
