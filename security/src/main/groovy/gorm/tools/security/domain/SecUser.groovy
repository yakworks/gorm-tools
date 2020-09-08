/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import gorm.tools.AuditStamp
import gorm.tools.beans.AppCtx
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@AuditStamp
@GrailsCompileStatic
@EqualsAndHashCode(includes='login', useCanEqual=false)
@ToString(includes='login', includeNames=true, includePackage=false)
@Entity
class SecUser implements Serializable {

    static transients = ['password', 'primaryRole', 'editedByName', 'enabled']

    String login // the login name
    String name // the display name, may come from contact
    String email // users email for login or lost password
    String passwordHash // the password hash
    Boolean mustChangePassword = false // passwordExpired
    Date    passwordChangedDate //date when password was changed. passwordExpireDays is added to this to see if its time to change again
    Boolean inactive = false // !enabled
    String  resetPasswordToken // temp token for a password reset, TODO move to userToken once we have that setup?
    Date    resetPasswordDate // //date when user requested to reset password, adds resetPasswordExpireDays to see if its still valid

    void setEnabled(Boolean val) { inactive = !val }
    boolean getEnabled() { !inactive }

    String password // raw password used to makehash, temporary transient, does not get saved,

    // other default fields
    // boolean accountExpired = false //not used right now
    // boolean accountLocked = false //not used right now

    static mapping = {
        cache true
        table 'Users'// AppCtx.config.getProperty('gorm.tools.security.user.table', 'Users')
        passwordHash column: '`password`'
        // password column: '`password`'
    }

    static constraints = {
        login blank: false, nullable: false, unique: true, maxSize: 50
        name blank: false, nullable: false, maxSize: 50
        email nullable: false, blank: false, email: true, unique: true
        passwordHash blank: false, nullable: false, maxSize: 60, bindable: false, password: true
        passwordChangedDate nullable: true, bindable: false
        mustChangePassword bindable: false
        resetPasswordToken nullable: true, bindable: false
        resetPasswordDate nullable: true, bindable: false
    }

    @CompileDynamic
    Set<SecRole> getRoles() {
        SecRoleUser.findAllByUser(this)*.role as Set
    }

    @CompileDynamic
    String getEditedByName() {
        SecUser.get(this.editedBy)?.name
    }
}
