/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileStatic

import gorm.tools.model.NameCode
import gorm.tools.model.NamedEntity

/**
 * Trait for a User. We depend on this so that we are not locked into a specific security framework such as Spring or Shiro.
 * Roughly based on Springs UserDetails
 *
 * @see org.springframework.security.core.userdetails.UserDetails
 */
@CompileStatic
trait UserTrait<ID> extends NamedEntity {

    /** the unique username, may be same as email. see displayName for the short handle */
    String  username

    /** the full name, may come from contact or defaults to username if not populated */
    String  name

    /** users email for username or lost password*/
    String  email

    /** the hashed password  */
    String  passwordHash

    /** will be true when user is inactivated !enabled */
    Boolean inactive = false

    /** the organization ID */
    Long orgId

    /** The unique id for the user, be default will be the normal generated id from gorm */
    abstract ID getId()
    abstract void setId(ID id)

    /** The roles assigned to the User */
    abstract Set<? extends NameCode> getRoles()

    /** The short display name or nickname for the user, can returns username or first part of email if username contains @, etc..*/
    abstract String getDisplayName()

}
