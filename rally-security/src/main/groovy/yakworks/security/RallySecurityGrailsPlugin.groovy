/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileDynamic

import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import yakworks.rally.security.RallyUserService
import yakworks.security.rest.NineOauthUserDetailsService
import yakworks.security.rest.RestAuthenticationProvider
import yakworks.security.rest.RestAuthenticationSuccessHandler
import yakworks.security.rest.token.GormTokenStorageService
import yakworks.security.rest.token.HeaderTokenReader
import yakworks.security.rest.token.PostgresTokenStorageService
import yakworks.security.tenant.UserRequest

// import yakworks.security.tenant.UserTenantResolver

@SuppressWarnings(['Indentation', 'Println'])
@CompileDynamic //ok
class RallySecurityGrailsPlugin extends Plugin {
    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest',
                     'gorm-tools', 'rally-domain', 'datasource', 'jasper-reports', 'hibernate5']

    boolean getShiroActive(){ return config.getProperty('shiro.active', Boolean, true) }

    Closure doWithSpring() { { ->
        // xmlns context:"http://www.springframework.org/schema/context"
        // context.'component-scan'('base-package': 'nine.security')

        rallyUserService(RallyUserService, autowireLazy())
        // currentUser(CurrentUser, autowireLazy())

        // userTenantResolver(UserTenantResolver)

        userRequest(UserRequest){ bean ->
            bean.scope = 'request'
        }

        def secConf = SpringSecurityUtils.securityConfig

        if (secConf.active) {

            if(secConf.rest.active) {
                registerBeans(restSecurityBeans, delegate)
            }
        }
    }}

    @Override
    void doWithApplicationContext() {
        // FIXME This is not working, See line 222ish of AbstractSecurityInterceptor, that should blow error
        // but it seems that is gets the last logged in user. Seems to work with setting in rally in domain9 but not
        // with rest-api example here
        // this make sure the any threads that are spun off also get the user who is logged in already
        // SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
        def conf = SpringSecurityUtils.securityConfig
        if (conf && conf.active ) {
            applicationContext.logoutHandlers.add 0, applicationContext.shiroLogoutHandler // must be before SecurityContextLogoutHandler
            if(shiroActive) {
                applicationContext.shiroSecurityManager.subjectDAO.sessionStorageEvaluator.sessionStorageEnabled = false
            }
        }

    }

    Closure getRestSecurityBeans() { { ->
        println "Configuring 9ci's rest security setup"
        //add only if sec plugins enabled, or else it would fail.
        oauthUserDetailsService(NineOauthUserDetailsService)
        // tokenStorageService(PostgresTokenStorageService)
        tokenReader(HeaderTokenReader)
        restAuthenticationProvider(RestAuthenticationProvider) {
            useJwt = false
        }
        if(config.getProperty('dataSource.driverClassName').contains('postgre')){
            tokenStorageService(PostgresTokenStorageService)
        } else {
            tokenStorageService(GormTokenStorageService)
        }

        //stores the token in a secure cookie. and enables the logout clearing. its all under jwt even if its not technically a jwt
        restAuthenticationSuccessHandler(RestAuthenticationSuccessHandler)
        cookieClearingLogoutHandler(CookieClearingLogoutHandler, ['jwt'])

        // securityContextRepository(org.springframework.security.web.context.NullSecurityContextRepository)
    }}

    Closure getLdapBeans() { { ->
        //LDAP Setup
        // def ldapConf = securityConf.ldap
        // if (ldapConf?.active) {
        //     ldapUserDetailsMapper(NineLdapUserDetailsMapper) { bean ->
        //         bean.lazyInit = true
        //         userDetailsService = ref('userDetailsService')
        //     }
        // }
    }}
    Closure autowireLazy() {{ bean ->
        bean.lazyInit = true
        bean.autowire = true
    }}

    Closure lazy() {{ bean ->
        bean.lazyInit = true
    }}

    void registerBeans(Closure beanClosure, Object delegate) {
        beanClosure.delegate = delegate
        beanClosure()
    }
}
