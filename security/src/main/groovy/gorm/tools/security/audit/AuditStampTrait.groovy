/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.audit

import java.time.LocalDateTime
import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.security.SecUtils

/**
 * flags a domain entity as stampable for events
 * the @AuditStamp ann adds this and can also be extended for events to pick
 */
@CompileStatic
trait AuditStampTrait {
    LocalDateTime createdDate
    LocalDateTime editedDate

    Long createdBy
    Long editedBy

    @Transient
    String getEditedByName() {
        SecUtils.getUsername(getEditedBy())
    }

    @Transient
    String getCreatedByName() {
        SecUtils.getUsername(getCreatedBy())
    }

}

@CompileDynamic
class AuditStampTraitConstraints implements AuditStampTrait {
    // use the props when doing importFrom so that it doesn't pick up the getCreatedByName and getEditedByName
    static props = ['createdDate', 'editedDate', 'createdBy', 'editedBy']

    static constraints = {
        createdDate nullable:false, display:false, editable:false, bindable:false
        editedDate nullable:false, display:false, editable:false, bindable:false
        createdBy nullable:false, display:false, editable:false, bindable:false
        editedBy nullable:false, display:false, editable:false, bindable:false
    }
}
