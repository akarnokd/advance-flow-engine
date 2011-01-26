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

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

import edu.umd.cs.findbugs.annotations.NonNull;

/**
 * A GWT service endpoint.
 * @author karnokd
 *
 */
@RemoteServiceRelativePath("AdvanceProjectService")
public interface AdvanceProjectService extends RemoteService {
	/**
	 * Retrieve the labels for the given language code.
	 * @param languageCode the language code, e.g., en, hu
	 * @return the label mapping for the requested language
	 */
	@NonNull
	Map<String, String> getLabels(@NonNull String languageCode);
	/**
	 * Retrieve the list of supported user interface languages.
	 * @return the list of supported user interface languages
	 */
	@NonNull
	List<String> getLanguages();
}
