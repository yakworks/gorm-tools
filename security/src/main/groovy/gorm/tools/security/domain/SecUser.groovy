/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode

import gorm.tools.security.stamp.AuditStampTrait
import gorm.tools.security.stamp.AuditStampTraitConstraints
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import grails.util.Holders

@Entity
@GrailsCompileStatic
@EqualsAndHashCode(includes='username', useCanEqual=false)
class SecUser implements Serializable, AuditStampTrait {

    static transients = ['password'] //@Transient not working when mapping has same column name for passwordHash?

    // username –– also known as your handle –– begins with the “@” symbol to reference others in comments or notes,
    // is unique to your account, and appears in your profile URL. username is used to log in to your account,
    // and is visible when sending and receiving. all lowercase and no spaces or special characters
    String username
    // lowercase property to be consitent as thats how everyone does it (twitter, facefck, github etc)
    String  name // the display name, may come from contact or defaults to username if not populated
    String  email // users email for username or lost password
    String  passwordHash // the password hash
    Boolean passwordExpired = false // passwordExpired
    Date    passwordChangedDate //date when password was changed. passwordExpireDays is added to this to see if its time to change again
    Boolean inactive = false // !enabled
    String  resetPasswordToken // temp token for a password reset, TODO move to userToken once we have that setup?
    Date    resetPasswordDate // //date when user requested to reset password, adds resetPasswordExpireDays to see if its still valid

    @Transient
    boolean getEnabled() { !inactive }
    void setEnabled(Boolean val) { inactive = !val }

    @Transient
    String password // raw password used to makehash, temporary transient, does not get saved,

    // other default fields
    // boolean accountExpired = false //not used right now
    // boolean accountLocked = false //not used right now

    static mapping = {
        cache true
        //table 'Users'// AppCtx.config.getProperty('gorm.tools.security.user.table', 'Users')
        table Holders.config.getProperty('gorm.tools.security.user.table', 'Users')
        passwordHash column: "`password`"
        passwordExpired column: "mustChangePassword" // TODO change the column name in nine-db
        username column: "login"
    }

    //@CompileDynamic
    static constraints = {
        importFrom AuditStampTraitConstraints, include: AuditStampTraitConstraints.includes
        username blank: false, nullable: false, unique: true, maxSize: 50
        name blank: false, nullable: false, maxSize: 50
        email nullable: false, blank: false, email: true, unique: true
        passwordHash blank: false, nullable: false, maxSize: 60, bindable: false, password: true
        passwordChangedDate nullable: true, bindable: false
        passwordExpired bindable: false
        resetPasswordToken nullable: true, bindable: false
        resetPasswordDate nullable: true, bindable: false

    }

    @CompileDynamic
    Set<SecRole> getRoles() {
        SecRoleUser.findAllByUser(this)*.role as Set
    }

}
