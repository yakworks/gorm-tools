/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import groovy.transform.CompileStatic

/**
 * similiar to org.apache.commons.lang3.Validate but throws IllegalArgumentException instead of
 * a NullPointer
 *
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class Validate {

    private static final String DEFAULT_IS_TRUE_EX_MESSAGE = "The validated expression is false"
    //private static final String DEFAULT_NOT_EMPTY =  "The validated object must not be null, blank or empty";
    //private static final String DEFAULT_NOT_NULL = "The validated object must not be null";

    /**
     * Validate that the specified argument is no {@code null}
     *
     * @param obj the object to validate
     * @param message  the message to use to populate default message, if the string is wrapped in [ ] then it
     *   builds the default message with the descriptor
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
     * Validate that the specified argument is no {@code null}
     *
     * For performance reasons, the Object... values is passed as a separate parameter and
     * appended to the exception message only in the case of an error.
     *
     * @param expression  the boolean expression to check
     * @param message  the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param msgArgs  the optional message args for the formatted exception message, null array not recommended
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static <T> T notNull(final T obj, final String message, final Object... msgArgs) {
        if (obj == null) {
            throw new IllegalArgumentException(String.format(message, msgArgs))
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

    /**
     * <p>Validate that the argument condition is {@code true}; otherwise
     * throwing an exception with the specified message. This method is useful when
     * validating according to an arbitrary boolean expression, such as validating a
     * primitive number or using your own custom validation expression.</p>
     *
     * <pre>
     * Validate.isTrue(i &gt;= min &amp;&amp; i &lt;= max, "The value must be between &#37;d and &#37;d", min, max);
     * Validate.isTrue(myObject.isOk(), "The object is not okay");</pre>
     *
     * For performance reasons, the Object... values is passed as a separate parameter and
     * appended to the exception message only in the case of an error.
     *
     * @param expression  the boolean expression to check
     * @param message  the {@link String#format(String, Object...)} exception message if invalid, not null
     * @param values  the optional values for the formatted exception message, null array not recommended
     * @throws IllegalArgumentException if expression is {@code false}
     */
    public static void isTrue(final boolean expression, final String message, final Object... values) {
        if (!expression) {
            throw new IllegalArgumentException(String.format(message, values))
        }
    }

    /**
     * <p>Validate that the argument condition is {@code true}; otherwise
     * throwing an exception. This method is useful when validating according
     * to an arbitrary boolean expression, such as validating a
     * primitive number or using your own custom validation expression.</p>
     *
     * <pre>
     * Validate.isTrue(i &gt; 0);
     * Validate.isTrue(myObject.isOk());</pre>
     *
     * <p>The message of the exception is &quot;The validated expression is
     * false&quot;.</p>
     *
     * @param expression  the boolean expression to check
     * @throws IllegalArgumentException if expression is {@code false}
     * @see #isTrue(boolean, String, long)
     * @see #isTrue(boolean, String, double)
     * @see #isTrue(boolean, String, Object...)
     */
    public static void isTrue(final boolean expression) {
        if (!expression) {
            throw new IllegalArgumentException(DEFAULT_IS_TRUE_EX_MESSAGE)
        }
    }
}
