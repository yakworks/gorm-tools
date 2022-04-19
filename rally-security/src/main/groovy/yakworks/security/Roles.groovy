package yakworks.security

import groovy.transform.CompileStatic

/**
 * Constants for role names, NOT restricted to these just for easier and cleaner usage.
 */
@CompileStatic
class Roles {
    // enabled by default in base data
    static final String ADMINISTRATOR = "ROLE_ADMIN" //full access, system user
    static final String ADMIN = "ROLE_ADMIN" //alias
    static final String POWER_USER = "Power User"
    //default user, access to all the screens, not manager (cannot approve, view other's tasks or delete cash system data)
    static final String MANAGER = "Manager" //access to all user's tasks, approval, can delete cash system data
    static final String GUEST = "Guest" //read only
    static final String CUSTOMER = "Customer" //greenbill single customer user

    // disabled by default in base data, more speific roles
    static final String COLLECTIONS = "Collections" //non cash
    static final String COLLECTIONS_MANAGER = "Collections Manager"  //can see other collector tasks, approvals

    static final String AUTOCASH = "Autocash" //cash, cannot delete system data
    static final String AUTOCASH_MANAGER = "Autocash Manager" //cash, can delete system data
    static final String AUTOCASH_OFFSET = "Autocash Offset" //cash, can only do $0 payments

    static final String ADMIN_CONFIG = "Admin Config" //setup
    static final String ADMIN_SEC = "Admin Sec" //user sec. management (acl)

    static final String SALES = "Sales" //review, approve disputes
    static final String BRANCH = "Branch"
    //branch, store user with limited access to customer and transaction screen only.

}
