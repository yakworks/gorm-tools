/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.gorm.model

import java.time.LocalDateTime
import javax.persistence.Transient

import groovy.transform.CompileDynamic
import groovy.transform.EqualsAndHashCode

import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.RepoEntity
import grails.compiler.GrailsCompileStatic
import grails.persistence.Entity
import yakworks.security.audit.AuditStampTrait
import yakworks.security.user.UserInfo

import static grails.gorm.hibernate.mapping.MappingBuilder.orm

@Entity
@GrailsCompileStatic
@EqualsAndHashCode(includes='username', useCanEqual=false)
class AppUser implements UserInfo, AuditStampTrait, RepoEntity<AppUser>, Serializable {

    static Map includes = [
        //get: [ 'id', 'version', 'username' , 'name', 'email', 'inactive', 'roles.$*'],
        qSearch: ['username', 'name', 'email'], // quick search includes
        stamp: ['id', 'username', 'name']  //picklist or minimal for joins
    ]

    /** the unique username, may be same as email. see displayName for the short handle */
    String  username

    /** the full name, may come from contact or defaults to username if not populated */
    String  name

    /** users email for username or lost password*/
    String  email

    /** will be true when user is inactivated !enabled */
    Boolean inactive = false

    /** the organization ID */
    Long orgId

    /** the hashed password  */
    String  passwordHash
    Boolean passwordExpired = false // passwordExpired
    LocalDateTime passwordChangedDate //date when password was changed. passwordExpireDays is added to this to see if its time to change again
    String  resetPasswordToken // temp token for a password reset, TODO move to userToken once we have that setup?
    LocalDateTime resetPasswordDate // //date when user requested to reset password, adds resetPasswordExpireDays to see if its still valid

    @Transient
    boolean isEnabled() { !inactive }
    void setEnabled(Boolean val) { inactive = !val }

    @Transient
    String password // raw password used to makehash, temporary transient, does not get saved,

    static transients = ['password', 'roles'] //@Transient not working when mapping has same column name for passwordHash?

    static AppUserRepo getRepo() { RepoLookup.findRepo(this) as AppUserRepo }

    static mapping = orm {
        // cache "nonstrict-read-write"
        table 'Users'
        columns (
            passwordHash: property([column: "`password`"])
        )
    }

    static constraintsMap = [
        username:[ d: '''\
            The unique user name, also known as your handle –– what you put after the “@” symbol ala github or twitter
            to mention others in comments or notes. appears in your profile URL. username is used to log in to your account,
            and is visible when sending and receiving. All lowercase and no spaces or special characters.
            ''', nullable: false, unique: true, maxSize: 50],
        name:[ d: "The full name, may come from contact, will default to username if not populated",
               nullable: false, required: false,  maxSize: 50],
        email:[ d: "The email",
                nullable: false, email: true, unique: true],
        inactive:[ d: 'True if user is inactive which means they cannot login but are still here for history',
                   editable: false],
        password:[ d: "The pwd", oapi:'CU', password: true],
        orgId:[ d: "The org to which this user belongs to", bindable: true, editable: false, nullable: true],
        roles:[ d: 'The string list of roles assigned to this user', validate: false, editable: false ],
        secRoles:[ d: 'The roles assigned to this user', validate: false, oapi: [read: true, edit: ['id']]],
        passwordHash:[ d: "The pwd hash, internal use only, never show this",
                       nullable: true, bindable: false, display:false, password: true],
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
        Collection<AppUser> results = AppUser.executeQuery("from AppUser where lower(username) = :uname",
            [uname:uname.trim().toLowerCase(), max:1])

        AppUser user = results ? results[0] : null
        return user
    }

    @Override
    Set<String> getRoles() {
       return getRepo().getRoles(this)
    }

    List<SecRole> getSecRoles() {
        // List res = SecRoleUser.executeQuery('select role.code from SecRoleUser where user.id = :uid',
        //     [uid: this.id] )
        // SecRoleUser.findAllByUser(this).collect{ ((SecRole) it.role).code } as Set<String>
        def res = SecRoleUser.query {
            eq "user", this
        }.projections { property("role") }.list()

        return res as List<SecRole>
    }

    @Override
    Set getPermissions() {
        return getRepo().getPermissions(this)
    }

    /**
     * Implements the displayName for short handle. currently returns username or first part of email if username contains @
     * FIXME displayName should be stored in db so it can be changed. can default to whats happening below to parse out first part of email.
     * @return the display name
     */
    //
    @Override //UserInfo
    String getDisplayName() {
        if(username.indexOf("@") != -1){
            return username.substring(0, username.indexOf("@"))
        } else {
            return username
        }
    }

    @Override //UserInfo
    Map getAttributes() {
        throw new UnsupportedOperationException("Not yet supported here")
    }

    SecRoleUser addRole(String roleCode, boolean flushAfterPersist) {
        getRepo().addUserRole(this, roleCode, flushAfterPersist)
    }
}
