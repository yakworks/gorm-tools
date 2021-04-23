/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

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
        table 'OrgCalc'
        id generator: 'assigned'
        version false
    }

}
