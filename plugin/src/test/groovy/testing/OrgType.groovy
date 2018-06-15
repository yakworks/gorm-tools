/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package testing

import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class OrgType {
    String name

    static constraints = {
        name blank: false, nullable: false
    }
}
