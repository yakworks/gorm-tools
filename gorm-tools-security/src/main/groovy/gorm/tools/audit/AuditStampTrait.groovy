/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.audit

import java.time.LocalDateTime
import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import gorm.tools.security.SecUtils

/**
 * flags a domain entity as stampable for events
 * the @AuditStamp ann adds this and can also be extended for events to pick
 */
@SuppressWarnings(['MethodName'])
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

    //TODO copy/paste code from getCreatedByName, we should refactor it
    /** comes from username - first section of email, if email is a username*/
    @Transient
    String getEditedByDisplayName() {
        SecUtils.getDisplayName(getEditedBy())
    }

    /** comes from username - first section of email, if email is a username*/
    @Transient
    String getCreatedByDisplayName() {
        SecUtils.getDisplayName(getCreatedBy())
    }

    @CompileDynamic
    static AuditStampTraitConstraints(Object delegate) {
        def c = {
            createdDate description: "created date/time",
                        nullable: false, editable: false, bindable: false
            createdBy   description: "created by user id",
                        nullable: false, editable: false, bindable: false
            editedDate  description: "last edit date/time",
                        nullable: false, editable: false, bindable: false
            editedBy    description: "edited by user id",
                        nullable: false, editable: false, bindable: false
        }
        c.delegate = delegate
        c()
    }

    static constraintsMap = [
        createdDate:[ description: "created date/time",
            nullable: false, editable: false, bindable: false],
        createdBy:[   description: "created by user id",
            nullable: false, editable: false, bindable: false],
        editedDate:[  description: "last edit date/time",
            nullable: false, editable: false, bindable: false],
        editedBy:[    description: "edited by user id",
            nullable: false, editable: false, bindable: false]
    ]

}
