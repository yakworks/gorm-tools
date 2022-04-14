/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.security.domain

import java.time.LocalDateTime
import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode

import gorm.tools.audit.AuditStampTrait
import gorm.tools.model.NamedEntity
import gorm.tools.repository.model.GormRepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity

@Entity
@GrailsCompileStatic
@EqualsAndHashCode(includes='username', useCanEqual=false)
class AppUser implements NamedEntity, AuditStampTrait, GormRepoEntity<AppUser, AppUserRepo>, Serializable {

    static Map includes = [
        qSearch: ['username', 'name', 'email'], // quick search includes
        stamp: ['id', 'username', 'name']  //picklist or minimal for joins
    ]

    String username
    String  name // the full name or display name, may come from contact or defaults to username if not populated
    String  email // users email for username or lost password
    String  passwordHash // the password hash
    Boolean passwordExpired = false // passwordExpired
    LocalDateTime passwordChangedDate //date when password was changed. passwordExpireDays is added to this to see if its time to change again
    Boolean inactive = false // !enabled
    String  resetPasswordToken // temp token for a password reset, TODO move to userToken once we have that setup?
    LocalDateTime resetPasswordDate // //date when user requested to reset password, adds resetPasswordExpireDays to see if its still valid

    Long orgId

    @Transient
    boolean getEnabled() { !inactive }
    void setEnabled(Boolean val) { inactive = !val }

    @Transient
    String password // raw password used to makehash, temporary transient, does not get saved,

    static transients = ['password', 'roles'] //@Transient not working when mapping has same column name for passwordHash?

    // getter is overriden but field is needed so EntityMap for json can get access to the SecRole generic type
    // @Transient
    // Set<SecRole> roles // here so

    // other default fields
    // boolean accountExpired = false //not used right now
    // boolean accountLocked = false //not used right now

    static mapping = {
        cache true
        table 'Users' // AppCtx.config.getProperty('gorm.tools.security.user.table', 'Users')
        // table Holders.config.getProperty('gorm.tools.security.user.table', 'Users')
        passwordHash column: "`password`"
    }

    static constraintsMap = [
        username:[ d: '''\
            The unique user name, also known as your handle –– what you put after the “@” symbol ala github or twitter
            to mention others in comments or notes. appears in your profile URL. username is used to log in to your account,
            and is visible when sending and receiving. All lowercase and no spaces or special characters.
            ''',
                   nullable: false, unique: true, maxSize: 50],
        name:[ d: "The full name, may come from contact, will default to username if not populated",
               nullable: false, required: false,  maxSize: 50],
        email:[ d: "The email",
                nullable: false, email: true, unique: true],
        inactive:[ d: 'True if user is inactive which means they cannot login but are still here for history',
                   editable: false],
        password:[ d: "The pwd", oapi:'CU', password: true],
        orgId:[ d: "The org to which this user belongs to", bindable: true, editable: false, nullable: true],
        roles:[ d: 'The roles assigned to this user', oapi: [read: true, edit: ['id']]],
        passwordHash:[ d: "The pwd hash, internal use only, never show this",
                       nullable: true, maxSize: 60, bindable: false, display:false, password: true],
        passwordChangedDate:[ d: "The date password was changed",
                              nullable: true, bindable: false, oapi:'R'],
        passwordExpired:[ d: "The password expired",
                          bindable: false, editable: false],
        resetPasswordToken:[ d: "temp token for a password reset, internal use only",
                             nullable: true, bindable: false, display:false],
        resetPasswordDate:[ d: "date when user requested to reset password, adds resetPasswordExpireDays to see if its still valid",
                            nullable: true, bindable: false, display:false]
    ]

    @CompileDynamic
    static AppUser getByUsername(String uname) {
        AppUser.findByUsername(uname.trim())
    }

    @CompileDynamic
    Set<SecRole> getRoles() {
        SecRoleUser.findAllByUser(this)*.role as Set
    }

}
