/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.advance.logistics.flow.editor.util;

/**
 * Utility methods.
 * @author karnokd
 */
public final class Util {
    /** Utility class. */
    private Util() { }
    /** Escape characters. */
    private static final String WR_ESCAPE = ".|+(){}[]^$\\";

    /**
     * Convert a wildcard representation into regular expression.
     * @param wildcard the wildcard string
     * @return the regular expression
     */
    public static String wildcardToRegex(String wildcard) {
        StringBuilder result = new StringBuilder();
        result.append("^");
        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            if (c == '*') {
                result.append(".*");
            } else if (c == '?') {
                result.append('.');
            } else if (WR_ESCAPE.indexOf(c) >= 0) {
                result.append('\\').append(c);
            } else {
                result.append(c);
            }
        }
        result.append("$");
        return result.toString().toUpperCase();
    }
}
