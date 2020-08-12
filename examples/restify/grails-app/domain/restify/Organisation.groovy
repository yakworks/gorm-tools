/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import java.time.LocalDateTime

import gorm.tools.rest.RestApi
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@RestApi(description = "Organisation domain")
class Organisation {
    String name
    String num
    ShipAddress address
    Date testDate
    LocalDateTime testDateTwo
    boolean isActive = true
    BigDecimal revenue = 0
    BigDecimal credit
    Long refId = 0L
    String event

    static qSearchFields = ["name", "num"]

    static constraints = {
        name blank: false
        num nullable: true
        address nullable: true
        testDate nullable: true
        testDateTwo nullable: true
        credit nullable: true
        event nullable: true
    }
}
