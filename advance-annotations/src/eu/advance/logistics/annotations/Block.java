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
package eu.advance.logistics.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Defines an Advance Block which should be transformed into block-registry.xml entries.
 * @author szmarcell, 2011.11.07
 */
@Target(value = ElementType.TYPE)
public @interface Block {
	/** The block type identifier. */
    String id() default "";
    /** The preferred scheduler for the block execution. */
    String scheduler() default "IO";
    /** The short textual description of the block. */
    String description() default "";
    /** The block target category. */
    String category() default "";
    /** The block keywords. */
    String keywords() default "";
    /** The documentation URI. */
    String documentation() default "";
    /**
     * Syntax:
     *      name ([+]upper_bound | -lower_bound)? (, ([+]upper_bound | -lower_bound))*
     */
    String[] parameters() default { };
}
