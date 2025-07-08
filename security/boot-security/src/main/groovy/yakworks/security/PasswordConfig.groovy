/*
* Copyright 2025 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import org.springframework.boot.context.properties.ConfigurationProperties

@CompileStatic
@ConfigurationProperties(prefix="yakworks.security.password")
class PasswordConfig {

    //XXX @SUD add javadocs to each of these. All our ConfigurationProperties classes should have good docs.
    int passwordExpireDays = 90
    boolean expiryEnabled = false
    int warnDays = 30
    int minLength = 3
    boolean mustContainNumbers = false
    boolean mustContainSymbols = false
    boolean mustContainUppercaseLetter = false
    boolean mustContainLowercaseLetter = false
    boolean historyEnabled = false
    int historyLength = 4
}
