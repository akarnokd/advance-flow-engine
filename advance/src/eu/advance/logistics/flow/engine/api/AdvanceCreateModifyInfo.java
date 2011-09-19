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

package eu.advance.logistics.flow.engine.api;

import java.sql.Timestamp;

/**
 * The creation/modification time and user information.
 * @author karnokd, 2011.09.19.
 */
public interface AdvanceCreateModifyInfo {
	/** @return the creation timestamp of the object. */
	Timestamp createdAt();
	/** @return the last modification timestamp of the object. */
	Timestamp modifiedAt();
	/** @return The user who created the object. */
	String createdBy();
	/** @return The user who modified the object the last time. */
	String modifiedBy();
}
