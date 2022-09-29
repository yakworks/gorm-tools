/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security.user;


import gorm.tools.model.NameCode;
import gorm.tools.model.NamedEntity;

import java.io.Serializable;
import java.security.Principal;
import java.util.*;
import org.springframework.security.core.GrantedAuthority;

/**
 * Trait for a User. We depend on this so that we are not locked into a specific security framework such as Spring or Shiro.
 * Roughly based on Springs UserDetails
 * Both the AppUser domain and the SpringUserInfo implement this.
 * @see org.springframework.security.core.userdetails.UserDetails
 */
public interface UserInfo extends Principal, Serializable {

    /** The unique id for the user, be default will be the unique generated id from db */
    Serializable getId();

    /** Returns the username used to authenticate the user. Cannot return `null` */
    String getUsername();

    String getPasswordHash();

    /** the full name, may come from contact or defaults to username if not populated */
    default String getName() {
        return getUsername();
    }

    /** The short display name or nickname for the user, can returns username or first part of email if username contains @, etc..*/

    default String getDisplayName() {
        String uname = getUsername();
        if(uname != null && uname.indexOf("@") != -1){
            return uname.substring(0, uname.indexOf("@"));
        } else {
            return uname;
        }
    }

    /** users email for username or lost password*/
    String  getEmail();

    /** the organization ID */
    Serializable getOrgId();

    /** Indicates whether the user is enabled or disabled. A disabled user cannot be authenticated. */
    boolean isEnabled();

    /** the organization ID */
    Map getUserProfile();

    Set getRoles();

}
