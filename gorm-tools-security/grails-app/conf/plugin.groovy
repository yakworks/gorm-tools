
grails.plugin.springsecurity.active = true
grails.plugin.springsecurity.userLookup.authoritiesPropertyName = 'roles'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'gorm.tools.security.domain.SecRoleUser'
grails.plugin.springsecurity.userLookup.enabledPropertyName = 'enabled'
grails.plugin.springsecurity.userLookup.passwordPropertyName = 'passwordHash'
grails.plugin.springsecurity.userLookup.userDomainClassName = 'gorm.tools.security.domain.AppUser'
grails.plugin.springsecurity.userLookup.usernamePropertyName = 'username'
grails.plugin.springsecurity.userLookup.accountExpiredPropertyName = null
grails.plugin.springsecurity.userLookup.accountLockedPropertyName = null
grails.plugin.springsecurity.userLookup.passwordExpiredPropertyName = null
grails.plugin.springsecurity.authority.className = 'gorm.tools.security.domain.SecRole'
grails.plugin.springsecurity.authority.nameField = 'springSecRole'

            // interceptUrlMap = [
            //     // all accesible anoymously by default
            //     [pattern: '/**', access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
            // ]
            //MAPPING and AUTH
            // userLookup {
            //     authoritiesPropertyName = 'roles'
            //     authorityJoinClassName = 'gorm.tools.security.domain.SecRoleUser'
            //     enabledPropertyName = 'enabled'
            //     passwordPropertyName = 'passwordHash'
            //     userDomainClassName = 'gorm.tools.security.domain.AppUser'
            //     usernamePropertyName = 'username'
            //     accountExpiredPropertyName = null
            //     accountLockedPropertyName = null
            //     passwordExpiredPropertyName = null
            // }
            // authority.nameField = 'springSecRole'

            // // securityConfigType = "InterceptUrlMap"
            // // adh.errorPage = null //null out so it send just 403 error
            // logout.handlerNames = ['rememberMeServices', 'secLogoutHandler']

            //events
            // useSecurityEventListener = true
            // onInteractiveAuthenticationSuccessEvent = { e, appCtx ->
            //     // handle AuthenticationSuccessEvent
            //     def userService = appCtx.getBean('userService')
            //     userService.trackUserLogin()
            // }
            // rest {
            //     token.storage.jwt.secret = 'sWXY3VMm4wKAGoRZg8r3ftZcjrKvmExghY'
            // }
//         }
//     }
// }
