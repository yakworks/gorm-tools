/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import gorm.tools.rest.RestApi
import grails.compiler.GrailsCompileStatic

@GrailsCompileStatic
// @RestApi(description = "Book test domain")
class Book {

    String title


    static constraints = {
        title nullable: false
    }

}
