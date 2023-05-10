/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode
import yakworks.rally.orgs.repo.OrgMemberRepo
import yakworks.security.audit.AuditStamp

@Entity @AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class OrgMember implements GormRepoEntity<OrgMember, OrgMemberRepo>, Serializable {
    static belongsTo = [Org]
    static transients = ['org']

    Org org //transient ref back to org for OrgDimensionConstraint
    Org getOrg() { this.org ?: Org.get(id) }

    //denormalized
    Org branch //at CED this is Profit Center and comes from CustAcct
    Org division //at CED this is credit center
    Org business
    Org sales
    Org region
    Org factory
    Org company

//    static mappedBy = [branch: "none", division: "none", business: "none", sales: "none",
//                       region: "none", factory: "none", org: "member"]

    static mapping = {
        id generator: 'assigned'
        branch column: 'branchId', lazy: true
        division column: 'divisionId', lazy: true
        business column: 'businessId', lazy: true
        sales column: 'salesId', lazy: true
        region column: 'regionId', lazy: true
        factory column: 'factoryId', lazy: true
        company column: 'companyId', lazy: true
    }

    static constraintsMap = [
        branch:[ description: 'The branch for the Org',
                 oapi:[read: true, create: ['id', 'num']]
        ],
        division:[ description: 'The division for the Org',
                 oapi:[read: true, create: ['id', 'num']]
        ],
        business:[ description: 'The business for the Org',
                   oapi:[read: true, create: ['id', 'num']]
        ],
        sales:[ description: 'The sales for the Org',
                   oapi:[read: true, create: ['id', 'num']]
        ],
        region:[ description: 'The region for the Org',
                   oapi:[read: true, create: ['id', 'num']]
        ],
        factory:[ description: 'The factory for the Org',
                 oapi:[read: true, create: ['id', 'num']]
        ],
    ]

    OrgMember copy() {
        return new OrgMember(branch: this.branch, division: this.division, business: this.business,
            sales: this.sales, region: this.region, factory: this.factory, company: this.company)
    }

    static OrgMember make(Org org){
        //assert org.id
        return new OrgMember(id: org.getId(), org: org)
    }

    /**
     * get the id for the property based on the passed in orgType
     */
    Long getMemberOrgId(OrgType orgType){
        this[orgType.idFieldName] as Long
    }

    /**
     * get the org property based on the passed in orgType
     */
    Org getMemberOrg(OrgType orgType){
        this[orgType.propertyName] as Org
    }
}
