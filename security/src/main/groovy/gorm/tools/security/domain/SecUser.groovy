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

    static transients = ['pass', 'primaryRole', 'editedByName', 'enabled']

    String login // the login name
    String name // the display name, may come from contact
    String email // users email for login or lost password
    String passwd // the password
    Boolean mustChangePassword = false // passwordExpired
    Date    passwordChangedDate // FIXME whats this user for?
    Boolean inactive = false // !enabled

    String  resetPasswordToken // temp token for a password reset, TODO move to userToken once we have that setup?
    Date    resetPasswordDate // FIXME whats this for

    void setEnabled(Boolean val) { inactive = !val }
    boolean getEnabled() { !inactive }

    // FIXME change this design
    // temporary holder for unencrypted password before it gets encrypted and saved to passwd
    String pass

    // default fields
    // boolean accountExpired = false //not used right now
    // boolean accountLocked = false //not used right now
    // boolean passwordExpired = false

    static mapping = {
        cache true
        table 'Users'// AppCtx.config.getProperty('gorm.tools.security.user.table', 'Users')
        passwd column: '`password`'
        // password column: '`password`'
    }

    static constraints = {
        login blank: false, nullable: false, unique: true, maxSize: 50
        name blank: false, nullable: false, maxSize: 50
        email nullable: false, blank: false, email: true, unique: true
        passwd blank: false, nullable: false, maxSize: 60, bindable: false, password: true
        passwordChangedDate nullable: true, bindable: false
        mustChangePassword bindable: false
        resetPasswordToken nullable: true, bindable: false
        resetPasswordDate nullable: true, bindable: false
    }

    // XXX this seems funky, it just grabs the first role and thats the primary?
    SecRole getPrimaryRole() {
        if (!id) return null
        SecRoleUser.findByUser(this)?.role
    }

    @CompileDynamic
    Set<SecRole> getRoles() {
        SecRoleUser.findAllByUser(this)*.role as Set
    }

    @CompileDynamic
    void encodePassword() {
        passwd = repo.encodePassword(pass)
    }

    @CompileDynamic
    String getEditedByName() {
        SecUser.get(this.editedBy)?.name
    }
}
