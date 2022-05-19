/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.activity.model

import gorm.tools.audit.AuditStamp
import gorm.tools.model.NameCodeDescription
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.commons.transform.IdEqualsHashCode

@Entity
@AuditStamp
@IdEqualsHashCode
@GrailsCompileStatic
class TaskType implements NameCodeDescription, RepoEntity<TaskType>, Serializable {

    Activity.Kind kind = Activity.Kind.Todo

    Boolean validForSales //valid for Sales/CRM area of the app
    Boolean validForAr // valid  the Ar area of the app

    static mapping = {
        cache "nonstrict-read-write"
    }

    static constraints = {
        apiConstraints(delegate)
        kind nullable: false, inList: Activity.Kind.taskKinds as List
    }

    static TaskType getTODO() { return this.get(1) }

    static TaskType getCALL() { return this.get(2) }

    static TaskType getMEETING() { return this.get(3) }

    static TaskType getEMAIL() { return this.get(4) }

    static TaskType getFAX() { return this.get(5) }

    static TaskType getMAIL() { return this.get(6) }

    static TaskType getFEDEX() { return this.get(7) }

    static TaskType getFOLLOWUP_CALL() { return this.get(8) }

    static TaskType getFOLLOWUP_EMAIL() { return this.get(9) }

    static TaskType getREVIEW() { return this.get(10) }
}
