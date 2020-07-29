/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import gorm.tools.rest.RestApi
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
@RestApi(description = "Company test domain, will be used for testing fields functionality")
class Company {

    String name
    String city
    int staffQuantity


    static constraints = {
        name nullable: false
    }

}
