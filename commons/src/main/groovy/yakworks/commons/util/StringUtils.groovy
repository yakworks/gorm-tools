/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.util

import groovy.transform.CompileStatic

/**
 * String utilities.
 *
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class StringUtils {

    /**
     * Check whether the given {@code String} contains actual <em>text</em>.
     * <p>More specifically, this method returns {@code true} if the
     * {@code String} is not {@code null}, its length is greater than 0,
     * and it contains at least one non-whitespace character.
     * @param str the {@code String} to check (may be {@code null})
     * @return {@code true} if the {@code String} is not {@code null}, its
     * length is greater than 0, and it does not contain whitespace only
     */
    static boolean hasText(String str) {
        return (str != null && !str.isEmpty() && containsText(str));
    }

    static boolean containsText(CharSequence str) {
        int strLen = str.length();
        for (int i = 0; i < strLen; i++) {
            if (!Character.isWhitespace(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Classic string join
     */
    static String join(Collection<String> collection, String separator) {
        return collection.join(separator)
    }

    /**
     * Capitalizes string
     */
    static String capitalize(String input) {
        return input.capitalize()
    }

    /**
     * Checks if input is empty, if input is not null its 'toString()' value will be used.
     */
    static boolean isEmpty(Object input) {
        return input == null || isEmpty(input.toString())
    }

    /**
     * Return whether the given string is empty.
     *
     * @param str The string
     * @return True if str is empty or null
     */
    public static boolean isEmpty(CharSequence str) {
        return str == null || str.length() == 0;
    }

    /**
     * Return whether the given string is not empty.
     *
     * @param str The string
     * @return True if str is not null and not empty
     */
    public static boolean isNotEmpty(CharSequence str) {
        return !isEmpty(str);
    }
}
