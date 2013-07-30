/*
 * Copyright 2010-2013 The Advance EU 7th Framework project consortium
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

package eu.advance.logistics.flow.engine.api.ds;

import hu.akarnokd.reactive4java.base.Func0;
import hu.akarnokd.utils.xml.XNElement;
import hu.akarnokd.utils.xml.XNSerializable;

import java.util.Set;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import eu.advance.logistics.flow.engine.api.core.Copyable;
import eu.advance.logistics.flow.engine.api.core.HasPassword;
import eu.advance.logistics.flow.engine.api.core.Identifiable;

/**
 * User settings.
 * @author akarnokd, 2011.09.19.
 */
public class AdvanceUser extends AdvanceCreateModifyInfo 
implements XNSerializable, HasPassword, Copyable<AdvanceUser>, Identifiable<String> {
	/** Is the user enabled? */
	public boolean enabled;
	/** The user's name. */
	public String name;
	/** The email address. */
	public String email;
	/** The pager number. */
	public String pager;
	/** The sms number. */
	public String sms;
	/** The date format string. */
	public String dateFormat;
	/** The date-time format string. */
	public String dateTimeFormat;
	/** The number format string. */
	public String numberFormat;
	/** The thousand separator character. */
	public char thousandSeparator = ',';
	/** The decimal separator character. */
	public char decimalSeparator = '.';
	/** Is the user logging in via username/password? */
	public boolean passwordLogin;
	/** 
	 * The password.
	 * <p>Note that passwords are never returned from the 
	 * control API calls and are always {@code null}.</p> 
	 * <p>An empty password should be an empty {@code char} array. To keep
	 * the current password, use {@code null}.</p>
	 */
	private char[] password;
	/** The keystore where the user certificate is located. */
	public String keyStore;
	/** The certificate alias. */
	public String keyAlias;
	/** Set of enabled user rights. */
	public final Set<AdvanceUserRights> rights = Sets.newHashSet();
	/**
	 * A multimap of realms and set of rights.
	 */
	public final Multimap<String, AdvanceUserRealmRights> realmRights = HashMultimap.create();
	/** The function to create an instance of this class. */
	public static final Func0<AdvanceUser> CREATOR = new Func0<AdvanceUser>() {
		@Override
		public AdvanceUser invoke() {
			return new AdvanceUser();
		}
	};
	@Override
	public void load(XNElement source) {
		enabled = source.getBoolean("enabled");
		name = source.get("name");
		email = source.get("email");
		pager = source.get("pager");
		sms = source.get("sms");
		dateFormat = source.get("date-format");
		dateTimeFormat = source.get("date-time-format");
		numberFormat = source.get("number-format");
		thousandSeparator = source.get("thousand-separator").charAt(0);
		decimalSeparator = source.get("decimal-separator").charAt(0);
		passwordLogin = source.getBoolean("password-login");
		password = getPassword(source, "password");
		keyStore = source.get("keystore");
		keyAlias = source.get("keyalias");
		rights.clear();
		for (XNElement xe : source.childElement("rights").childrenWithName("right")) {
			rights.add(AdvanceUserRights.valueOf(xe.get("value")));
		}
		realmRights.clear();
		for (XNElement xe : source.childElement("realm-rights").childrenWithName("realm")) {
			for (XNElement xe2 : xe.childrenWithName("right")) {
				realmRights.put(xe.get("name"), AdvanceUserRealmRights.valueOf(xe2.get("value")));
			}
		}
		super.load(source);
	}
	@Override
	public void save(XNElement destination) {
		destination.set("name", name);
		destination.set("enabled", enabled);
		destination.set("email", email);
		destination.set("pager", pager);
		destination.set("sms", sms);
		destination.set("date-format", dateFormat);
		destination.set("date-time-format", dateTimeFormat);
		destination.set("number-format", numberFormat);
		destination.set("thousand-separator", thousandSeparator);
		destination.set("decimal-separator", decimalSeparator);
		destination.set("password-login", passwordLogin);
		setPassword(destination, "password", password);
		destination.set("keystore", keyStore);
		destination.set("keyalias", keyAlias);
		XNElement xr = destination.add("rights");
		for (AdvanceUserRights r : rights) {
			xr.add("right").set("value", r);
		}
		XNElement rr = destination.add("realm-rights");
		for (String r : realmRights.keySet()) {
			XNElement xrr = rr.add("realm");
			xrr.set("name", r);
			for (AdvanceUserRealmRights urr : realmRights.get(r)) {
				xrr.add("right").set("value", urr);
			}
		}
		super.save(destination);
	}
	@Override
	public AdvanceUser copy() {
		AdvanceUser result = new AdvanceUser();
		
		result.enabled = enabled;
		result.name = name;
		result.email = email;
		result.pager = pager;
		result.sms = sms;
		result.dateFormat = dateFormat;
		result.dateTimeFormat = dateTimeFormat;
		result.numberFormat = numberFormat;
		result.thousandSeparator = thousandSeparator;
		result.decimalSeparator = decimalSeparator;
		result.passwordLogin = passwordLogin;
		result.keyStore = keyStore;
		result.keyAlias = keyAlias;
		result.rights.addAll(rights);
		result.realmRights.putAll(realmRights);
		result.password(password);
		
		assignTo(result);
		
		return result;
	}
	/**
	 * @return check if the user could modify users
	 */
	public boolean mayModifyUser() {
		return enabled
		&& rights.contains(AdvanceUserRights.LIST_USERS)
		&& rights.contains(AdvanceUserRights.MODIFY_USER);
	}
	@Override
	public char[] password() {
		return password != null ? password.clone() : null;
	}
	@Override
	public void password(char[] newPassword) {
		password = newPassword != null ? newPassword.clone() : null;
	}
	@Override
	public String id() {
		return name;
	}
}
