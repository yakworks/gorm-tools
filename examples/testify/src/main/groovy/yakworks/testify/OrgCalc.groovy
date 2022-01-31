/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testify

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.model.Org

@Entity
@IdEqualsHashCode
@GrailsCompileStatic
class OrgCalc implements RepoEntity<OrgCalc>, Serializable {
    static belongsTo = [Org]
    //balance due fields
    BigDecimal curBal // whats not due yet
    BigDecimal pastDue //balance past due
    BigDecimal totalDue // total due, sum of all open items. will be the ending balance when month is closed


    static mapping = {
        id generator: 'assigned'
        version false
    }

}
