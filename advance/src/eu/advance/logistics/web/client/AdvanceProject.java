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

package eu.advance.logistics.web.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.web.client.dialogs.LoginDialog;
import eu.advance.logistics.web.model.AdvanceContext;
import eu.advance.logistics.web.model.AdvanceProjectService;

/**
 * The main entry point of the application.
 * @author karnokd
 */
public class AdvanceProject implements EntryPoint {
	/** The service caller. */
	@NonNull
	public AdvanceContext ctx;
	@Override
	public void onModuleLoad() {
		ctx = new AdvanceContext();
		ctx.service = GWT.create(AdvanceProjectService.class);
		
		retrieveLanguages();
	}
	/**
	 * Retrieve the list of languages.
	 */
	void retrieveLanguages() {
		ctx.service.getLanguages(new AsyncCallback<List<String>>() {
			@Override
			public void onFailure(Throwable caught) {
				Window.alert(caught.toString());
			}
			@Override
			public void onSuccess(List<String> result) {
				ctx.languageCodes = result;
				retrieveDefaultLabels();
			}
		});
	}
	/**
	 * Retrieve the default labels.
	 */
	void retrieveDefaultLabels() {
		String languageCode = "en";
		if (!ctx.languageCodes.contains(languageCode) && ctx.languageCodes.size() > 0) {
			languageCode = ctx.languageCodes.get(0);
		}
		if (ctx.languageCodes.size() == 0) {
			ctx.labels = new HashMap<String, String>();
			Window.alert("No suitable user interface language found.");
			DOM.getElementById("loading").removeFromParent();
		} else {
			ctx.service.getLabels(languageCode, new AsyncCallback<Map<String, String>>() {
				@Override
				public void onFailure(Throwable caught) {
					Window.alert(caught.toString());
				}
				@Override
				public void onSuccess(Map<String, String> result) {
					ctx.labels = result;
					createLoginPage();
				}
			});
		}
	}
	/** Create the login page. */
	void createLoginPage() {
		DOM.getElementById("loading").removeFromParent();
		LoginDialog dialog = new LoginDialog(ctx);
		dialog.center();
	}
	/** Create the application menu. */
	void createMenu() {
		
	}

}
