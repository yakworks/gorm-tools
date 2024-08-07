/**
 * Deafaults for our domains. Deployed and automatically merged in with plugin
 */
grails.plugin.springsecurity.active = true
grails.plugin.springsecurity.userLookup.authoritiesPropertyName = 'roles'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'yakworks.security.gorm.model.SecRoleUser'
grails.plugin.springsecurity.userLookup.enabledPropertyName = 'enabled'
grails.plugin.springsecurity.userLookup.passwordPropertyName = 'passwordHash'
grails.plugin.springsecurity.userLookup.userDomainClassName = 'yakworks.security.gorm.model.AppUser'
grails.plugin.springsecurity.userLookup.usernamePropertyName = 'username'
grails.plugin.springsecurity.userLookup.accountExpiredPropertyName = null
grails.plugin.springsecurity.userLookup.accountLockedPropertyName = null
grails.plugin.springsecurity.userLookup.passwordExpiredPropertyName = null
grails.plugin.springsecurity.authority.className = 'yakworks.security.gorm.model.SecRole'
grails.plugin.springsecurity.authority.nameField = 'code'

//try to turn off sessions
// grails.plugin.springsecurity.scr.allowSessionCreation = false
// grails.plugin.springsecurity.scpf.forceEagerSessionCreation = false
// grails.plugin.springsecurity.apf.allowSessionCreation = false
