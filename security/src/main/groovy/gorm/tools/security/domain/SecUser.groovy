/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import java.time.LocalDateTime
import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode

import org.grails.datastore.gorm.validation.constraints.builder.ConstrainedPropertyBuilder

import gorm.tools.security.audit.AuditStampTrait
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
@EqualsAndHashCode(includes='username', useCanEqual=false)
class SecUser implements AuditStampTrait { //, Serializable {

    static transients = ['password'] //@Transient not working when mapping has same column name for passwordHash?

    // username –– also known as your handle –– what you put after the “@” symbol ala github or twitter
    // to mention others in comments or notes,
    // is unique to your account, and appears in your profile URL. username is used to log in to your account,
    // and is visible when sending and receiving. all lowercase and no spaces or special characters
    String username
    // lowercase property to be consitent as thats how everyone does it (twitter, facefck, github etc)
    String  name // the full name or display name, may come from contact or defaults to username if not populated
    String  email // users email for username or lost password
    String  passwordHash // the password hash
    Boolean passwordExpired = false // passwordExpired
    LocalDateTime passwordChangedDate //date when password was changed. passwordExpireDays is added to this to see if its time to change again
    Boolean inactive = false // !enabled
    String  resetPasswordToken // temp token for a password reset, TODO move to userToken once we have that setup?
    LocalDateTime resetPasswordDate // //date when user requested to reset password, adds resetPasswordExpireDays to see if its still valid

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
        table 'Users' // AppCtx.config.getProperty('gorm.tools.security.user.table', 'Users')
        // table Holders.config.getProperty('gorm.tools.security.user.table', 'Users')
        passwordHash column: "`password`"
        passwordExpired column: "mustChangePassword" // TODO change the column name in nine-db
        username column: "login"
    }

    //@CompileDynamic
    static constraints = {
        AuditStampTraitConstraints(delegate)
        //importFrom AuditStampTraitConstraints, include: AuditStampTraitConstraints.props
        // assert delegate instanceof ConstrainedPropertyBuilder
        username blank: false, nullable: false, unique: true, maxSize: 50
        name blank: false, nullable: false, maxSize: 50
        email nullable: false, blank: false, email: true, unique: true
        passwordHash blank: false, nullable: false, maxSize: 60, bindable: false, display:false, password: true
        passwordChangedDate nullable: true, bindable: false
        passwordExpired bindable: false
        resetPasswordToken nullable: true, bindable: false
        resetPasswordDate nullable: true, bindable: false
    }

    transient static SecUserRepo getRepo() {
        return (SecUserRepo)findRepo()
    }

    @CompileDynamic
    static SecUser getByUsername(String uname) {
        SecUser.findByUsername(uname.trim())
    }

    @CompileDynamic
    Set<SecRole> getRoles() {
        SecRoleUser.findAllByUser(this)*.role as Set
    }

}
