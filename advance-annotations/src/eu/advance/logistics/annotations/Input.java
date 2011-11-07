/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.advance.logistics.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 *
 * @author szmarcell
 */
@Target(value = ElementType.FIELD)
public @interface Input {
    /** Syntax:
     *      simple type: 'advance:real'
     *      type parameter: '?T'
     *      parameterized type: advance:collection&lt;...&gt;
     *   e.g.
     *      advance:collection&lt;advance:collection&lt;?T&gt;, advance:real&gt;
     */
    public String value();
}
