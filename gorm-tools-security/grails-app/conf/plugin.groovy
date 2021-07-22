grails {
    plugin {
        springsecurity {
            active = true
            interceptUrlMap = [
                // all accesible anoymously by default
                [pattern: '/**', access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
            ]
            //MAPPING and AUTH
            userLookup.authoritiesPropertyName = 'roles'
            userLookup.authorityJoinClassName = 'gorm.tools.security.domain.SecRoleUser'
            userLookup.enabledPropertyName = 'enabled'
            userLookup.passwordPropertyName = 'passwordHash'
            userLookup.userDomainClassName = 'gorm.tools.security.domain.AppUser'
            userLookup.usernamePropertyName = 'username'
            userLookup.accountExpiredPropertyName = null
            userLookup.accountLockedPropertyName = null
            userLookup.passwordExpiredPropertyName = null

            authority.nameField = 'springSecRole'
            securityConfigType = "InterceptUrlMap"
            adh.errorPage = null //null out so it can be custom
            logout.handlerNames = ['rememberMeServices', 'secLogoutHandler']

            //events
            useSecurityEventListener = true
            onInteractiveAuthenticationSuccessEvent = { e, appCtx ->
                // handle AuthenticationSuccessEvent
                def userService = appCtx.getBean('userService')
                userService.trackUserLogin()
            }
            // rest {
            //     token.storage.jwt.secret = 'sWXY3VMm4wKAGoRZg8r3ftZcjrKvmExghY'
            // }
        }
    }
}
