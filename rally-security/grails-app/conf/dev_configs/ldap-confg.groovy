//this is currently unused but should be put back into the testing picture

grails {
    plugin{
        springsecurity {
            active = true
            //===== LDAP =======================
            // COMMENT OUT THIS ENTIRE SECTION IF YOU DON'T USE LDAP!
            // LDAP installations need to add this entire thing in the config!
            providerNames = ['ldapAuthProvider']
            ldap{
                active = true
                context.managerDn = 'svclgk@nine.local'
                context.managerPassword = 'BotFly!'
                context.server = 'ldap://dc1vm2k8:389'
                authorities.retrieveGroupRoles = false //don't get roles from LDAP
                authorities.retrieveDatabaseRoles = true //get roles from the database
                search.filter = '(&(objectClass=user)(sAMAccountName={0})(memberOf=CN=9ci-app-users,DC=nine,DC=local))' // MUST BE THIS VALUE FOR WINDOWS DOMAIN
                // base is the context name to search in relative to the base of the configured ContextSource
                search.base = 'cn=users,dc=nine,dc=local'

                // groupSearchBase is the base DN from which search for group membership should be performed
                // is this really needed since we are not using roles from LDAP??????
                //authorities.groupSearchBase = 'CN=DL - 9Ci,OU=IS,OU=Domain Local Groups,OU=Groups,DC=mouser,DC=lan' // xx
            }
            //===== END LDAP ===================
        }//END springsecurity
    }//END plugins
}
