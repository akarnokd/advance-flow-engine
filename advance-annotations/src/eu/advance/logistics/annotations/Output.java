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
 *
 * @author szmarcell
 */
@Target(value = ElementType.FIELD)
public @interface Output {
    /** 
     *   <pre>Syntax:
     *      simple type: 'advance:real'
     *      type parameter: '?T'
     *      parameterized type: advance:collection&lt;...&gt;
     *   e.g.
     *      advance:collection&lt;advance:collection&lt;?T&gt;, advance:real&gt;
     *   </pre>
     */
    String value();
    /** The output represents a varargs type outputs, where the output 
     * names are derived from the field name plus a running counter from 1. */
    boolean variable() default false;
}
