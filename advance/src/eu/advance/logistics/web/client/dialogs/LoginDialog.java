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

package eu.advance.logistics.web.client.dialogs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyPressEvent;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.web.model.AdvanceContext;
import eu.advance.logistics.web.model.KeyPressEnterHandler;

/**
 * @author karnok, 2010.12.15.
 * @version $Revision 1.0$
 */
public class LoginDialog extends DialogBox {
	/** The application context. */
	final AdvanceContext ctx;
	/** The user name. */
	TextBox user;
	/** The password. */
	PasswordTextBox password;
	/**
	 * Constructor. Creates the GUI.
	 * @param ctx the context
	 */
	public LoginDialog(@NonNull AdvanceContext ctx) {
		this.ctx = ctx;
		setText(ctx.get("Advance Login"));
		setGlassEnabled(true);
		setAnimationEnabled(true);
		setModal(true);
		
		VerticalPanel vp = new VerticalPanel();
		
		KeyPressEnterHandler enter = new KeyPressEnterHandler() {
			@Override
			public void onEnter(KeyPressEvent event) {
				doLogin();
			}
		};
		
		user = new TextBox();
		user.addKeyPressHandler(enter);
		password = new PasswordTextBox();
		password.addKeyPressHandler(enter);
		
		Button login = new Button(ctx.get("Login"));
		login.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				doLogin();
			}
		});
		
		FlexTable table = ctx.table(2, 
				ctx.get("User name:"), user, 
				ctx.get("Password:"), password);
		
		vp.add(ctx.center(new Image("images/advlogo_192x128.png")));
		vp.add(table);
		vp.add(ctx.hr());
		vp.add(ctx.center(login));
		
		setWidget(vp);
	}
	/** Perform the login procedure. */
	void doLogin() {
		
	}
}
