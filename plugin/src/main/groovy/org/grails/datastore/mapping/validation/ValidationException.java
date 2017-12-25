/* Copyright (C) 2010 SpringSource
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.grails.datastore.mapping.validation;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.validation.Errors;
import org.springframework.validation.ObjectError;

/**
 * Exception thrown when a validation error occurs
 *
 * @author Graeme Rocher
 */

/**
 *
 * Overridden to avoid grails.validation.ValidationException to be blowed up, temporary hack without it,
 * grails.validation.ValidationException will be thrown by default
 *
 * see issue https://github.com/grails/grails-data-mapping/issues/1038
 */
public class ValidationException extends DataIntegrityViolationException {

    private static final long serialVersionUID = 1;

    public static final Class<? extends RuntimeException> VALIDATION_EXCEPTION_TYPE = ValidationException.class;

    private final String fullMessage;
    private final Errors errors;

    public ValidationException(String msg, Errors errors) {
        super(msg);
        fullMessage = formatErrors(errors, msg);
        this.errors = errors;
    }

    /**
     * @return The errors object
     * @since 6.1.3
     */
    public Errors getErrors() {
        return errors;
    }

    @Override
    public String getMessage() {
        return fullMessage;
    }

    public static String formatErrors(Errors errors, String msg ) {
        String ls = System.getProperty("line.separator");
        StringBuilder b = new StringBuilder();
        if (msg != null) {
            b.append(msg).append(" : ").append(ls);
        }

        for (ObjectError error : errors.getAllErrors()) {
            b.append(ls)
             .append(" - ")
             .append(error)
             .append(ls);
        }
        return b.toString();
    }

    public static RuntimeException newInstance(String message, Errors errors) {
         return new ValidationException(message, errors);
    }
}
