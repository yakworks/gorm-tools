/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import gorm.tools.audit.AuditStamp
import gorm.tools.model.NameNum
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@AuditStamp
@GrailsCompileStatic
class Client implements NameNum, RepoEntity<Client>, Serializable {

    String appUrl
    String subDomain

    Org org
    //static belongsTo = [ org: Org ]

    static mapping = {
        cache true
        id generator: 'assigned'
        //org insertable: false, updateable: false, column:'id'
        org column: 'orgId'
    }

    static constraintsMap = [
        appUrl:[ description: 'The unique url prefix', nullable: false, maxSize: 100],
        org:[ description: 'The org this client is tied to', nullable: false]
    ]

    //returns the first and what should be the only client record
    static Client get() {
        Client.find("from Client as c ", [cache: true])
    }
}
