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

package eu.advance.logistics.flow.engine.model.rt;

/**
 * <p>When a block body needs to be executed, which scheduler pool is preferred for that.</p>
 * <p>Computations are typically CPU or I/O bound with the potential overlapping of both.</p>
 * @author akarnokd, 2011.06.30.
 */
public enum AdvanceSchedulerPreference {
	/** The block uses computation-intensive body and should use the n-CPU based scheduler. */
	CPU,
	/** 
	 * The block uses an I/O intensive (e.g., database access, running external programs, web requests, etc.)
	 * body and should use a larger thread pool.
	 */
	IO,
	/** The block should be run on a single threaded scheduler. */
	SEQUENTIAL,
	/** 
	 * The now thread scheduler, which means the schedule() methods will simply execute in the current thread immediately.
	 * Useful for blocks which route/filter/project their input without any actual concurrency.  
	 */
	NOW
}
