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

import javax.swing.JPanel;

import edu.umd.cs.findbugs.annotations.NonNull;
import eu.advance.logistics.flow.engine.api.AdvanceKeyStore;
import eu.advance.logistics.flow.engine.api.AdvanceSOAPChannel;

/**
 * The SOAP channel details.
 * @author karnokd, 2011.10.13.
 */
public class CCSOAPDetails extends JPanel implements
		CCLoadSave<AdvanceSOAPChannel> {
	/** */
	private static final long serialVersionUID = 447371924967453848L;
	/**
	 * Creates the panel GUI.
	 * @param labels the label manager
	 */
	public CCSOAPDetails(@NonNull final LabelManager labels) {
		
	}
	@Override
	public void load(AdvanceSOAPChannel value) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public AdvanceSOAPChannel save() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onAfterSave() {
		// TODO Auto-generated method stub
		
	}
	/**
	 * Set the keystores.
	 * @param keystores the keystore
	 */
	public void setKeyStores(Iterable<AdvanceKeyStore> keystores) {
		
	}
}
