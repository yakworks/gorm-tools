/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model


import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
class OrgDynamicCalc implements Serializable {
    Integer contactCount

    static mapping = {
        table 'Org'
        version false
        contactCount formula:"(select count(c.id) from Contact c where c.orgId = id)", lazy: true
    }

    def beforeInsert() {
        return false
    }

}
