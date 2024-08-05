/*
* Copyright 2023 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.mail

import javax.mail.internet.AddressException
import javax.mail.internet.InternetAddress

import groovy.transform.CompileStatic

import org.apache.commons.lang3.StringUtils

import yakworks.api.problem.Problem

/**
 * Misc utils for emails.
 */
@CompileStatic
class EmailUtils {

    /**
     * Accepts a string with 1 or more emails in a comma separated RFC 822 format. <br>
     * Examples: <br>
     * "joe@email.com" - simple single email <br>
     * "Account Services <tesla@greenbill.io>, "Galt, John" <galt@yak.works>, joe@email.com" - 3 valid emails
     * @return true is valid
     * @throw ThrowableProblem with code:validation.problem
     */
    static boolean validateEmail(String emails) {
        boolean isValid = false
        try {
            InternetAddress[] addys = InternetAddress.parse(emails)
            for (InternetAddress addr : addys) {
                try {
                    addr.validate()
                } catch (AddressException e) {
                    throw Problem.of("validation.problem")
                        .payload(addr)
                        .detail("Invalid email address [$addr], " + e.message)
                        .toException()
                }
            }
            isValid = true
        } catch (AddressException e) {
            throw Problem.of("validation.problem").payload(emails).detail(e.message).toException()
        }
        return isValid
    }

    public static String nameWithEmail(String name, String email) {
        return StringUtils.isBlank(name) ? email : name + " <" + email + ">"
    }
}
