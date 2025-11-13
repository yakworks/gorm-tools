/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

@CompileStatic
@ConfigurationProperties(prefix="app.security.password")
class PasswordConfig {

    /**
     * If passwords expiry is enabled
     */
    boolean expiryEnabled = false

    /**
     * The days after last password change date when password would expire
     */
    int passwordExpireDays = 90

    /**
     * Minimum password length
     */
    int minLength = 3

    /**
     * If password must contains numbers
     */
    boolean mustContainNumbers = false

    /**
     * If password must contains symbols
     */
    boolean mustContainSymbols = false

    /**
     * If password must contains upper case letters
     */
    boolean mustContainUppercaseLetter = false

    /**
     * If password must contains lower case letters
     */
    boolean mustContainLowercaseLetter = false

    /**
     * If password history is enabled
     */
    boolean historyEnabled = false

    /**
     * Number of password change history records which would be kept and checked against.
     */
    int historyLength = 4
}
