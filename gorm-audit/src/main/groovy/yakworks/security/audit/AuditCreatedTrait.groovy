/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.audit

import java.time.LocalDateTime
import javax.persistence.Transient

import groovy.transform.CompileStatic

import yakworks.security.user.UserInfo

/**
 * flags a domain entity as stampable for events
 * the @AuditStamp ann adds this and can also be extended for events to pick
 */
@SuppressWarnings(['MethodName'])
@CompileStatic
trait AuditCreatedTrait {
    LocalDateTime createdDate
    Long createdBy

    /** comes from username - first section of email, if email is a username*/
    @Transient
    UserInfo getCreatedByUser() {
        AuditStampSupport.getUserInfo(getCreatedBy())
    }

    static constraintsMap = [
        createdDate:[ description: "created date/time",
            nullable: false, editable: false, bindable: false],
        createdBy:[   description: "created by user id",
            nullable: false, editable: false, bindable: false],
    ]

}
