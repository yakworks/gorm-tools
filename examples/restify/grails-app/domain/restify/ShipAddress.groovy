/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
class ShipAddress {
    String city
    Long testId
    static constraints = {
        city nullable: false
    }
}

@GrailsCompileStatic
class Location {
    String city
    Long testId
    static constraints = {
        city nullable: false
    }
}
