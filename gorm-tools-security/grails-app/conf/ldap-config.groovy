grails {
    plugin {
        springsecurity {
            providerNames = ['ldapAuthProvider']
            ldap {
                active = true
                context.managerDn = 'svclgk@nine.local'
                context.managerPassword = 'BotFly!'
                context.server = 'ldap://dc1vm2k8:389'
                authorities.retrieveGroupRoles = false //don't get roles from LDAP
                authorities.retrieveDatabaseRoles = true //get roles from the database
                search.filter = '(&(objectClass=user)(sAMAccountName={0})(memberOf=CN=9ci-app-users,DC=nine,DC=local))' // MUST BE THIS VALUE FOR WINDOWS DOMAIN
                // base is the context name to search in relative to the base of the configured ContextSource
                search.base = 'cn=users,dc=nine,dc=local'
            }
        }
    }
}
