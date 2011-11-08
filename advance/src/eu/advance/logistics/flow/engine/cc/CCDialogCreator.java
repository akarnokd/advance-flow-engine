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

package eu.advance.logistics.flow.engine.cc;

import hu.akarnokd.reactive4java.base.Func1;
import hu.akarnokd.reactive4java.base.Option;

import java.util.List;

import javax.swing.JComponent;

import eu.advance.logistics.flow.engine.api.core.Identifiable;
import eu.advance.logistics.flow.engine.api.ds.AdvanceCreateModifyInfo;

/**
 * The dialog creator function.
 * @author akarnokd, 2011.10.24.
 */
public interface CCDialogCreator {
	/**
	 * Construct a detailed dialog.
	 * @param <K> the identifier type of the record
	 * @param <T> the expected record type
	 * @param <V> the dialog type
	 * @param list the available list
	 * @param selected the selected item or null to indicate a new item should be created
	 * @param detailPanel the panel containing the detail fields
	 * @param namer the function to convert an entry into string
	 * @param retriever the function to retrieve a record through its id
	 * @param saver the function to save a record and report back an error
	 * @return the dialog created
	 */
	<K, T extends AdvanceCreateModifyInfo & Identifiable<K>, V extends JComponent & CCLoadSave<T>> 
	CCDetailDialog<T> createDetailDialog(
			final List<T> list, 
			final T selected,
			final V detailPanel,
			final Func1<T, String> namer,
			final Func1<? super K, ? extends Option<? extends T>> retriever,
			final Func1<T, Throwable> saver
			);
}