/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import groovy.transform.CompileStatic

/**
 * similiar to org.apache.commons.lang3.Validate but throws IllegalArgumentException instead of
 * a NullPointer
 */
@CompileStatic
class Validate {

    //private static final String DEFAULT_NOT_EMPTY =  "The validated object must not be null, blank or empty";
    //private static final String DEFAULT_NOT_NULL = "The validated object must not be null";

    /**
     * Validate that the specified argument is no {@code null}
     *
     * @param obj the object to validate
     * @param message  the message to use to populate default message, if the string is wrapped in [ ] then its
     *   build the message with the descriptor
     * @return the validated obj (never {@code null} method for chaining)
     * @throws IllegalArgumentException
     */
    public static <T> T notNull(T obj, String message = "The validated object must not be null") {
        if (obj == null) {
            if(message.startsWith('[')) message = "$message must not be null"
            throw new IllegalArgumentException(message)
        }
        return obj
    }

    /**
     * Validate that the specified argument is
     * neither {@code null} nor a length of zero (no characters) nor an empty collection
     * otherwise throwing an IllegalArgumentException with the specified message.
     *
     * @param obj the object to validate
     * @param objDescriptor  the descriptor to use for default message
     * @return the validated obj (never {@code null} method for chaining)
     * @throws IllegalArgumentException
     */
    public static <T> T notEmpty(T obj, String objDescriptor = "validated object") {
        if (!obj) {
            throw new IllegalArgumentException("The $objDescriptor must not be blank or empty")
        }
        return obj
    }
}
