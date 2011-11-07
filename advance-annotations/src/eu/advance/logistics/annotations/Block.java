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
@Target(value = ElementType.TYPE)
public @interface Block {
    public String id() default "";
    public String scheduler() default "NOW";
    public String description() default "";
    /**
     * Syntax:
     *      name ([+]upper_bound | -lower_bound)? (, ([+]upper_bound | -lower_bound))*
     */
    public String[] parameters() default {};
}
