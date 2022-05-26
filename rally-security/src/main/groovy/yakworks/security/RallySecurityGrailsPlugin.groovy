/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import groovy.transform.CompileDynamic

import org.apache.shiro.cache.MemoryConstrainedCacheManager
import org.apache.shiro.spring.LifecycleBeanPostProcessor
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler
import org.springframework.security.web.authentication.logout.CookieClearingLogoutHandler

import gorm.tools.security.domain.AppUser
import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import grails.util.Environment
import yakworks.security.rest.NineOauthUserDetailsService
import yakworks.security.rest.RestAuthenticationProvider
import yakworks.security.rest.RestAuthenticationSuccessHandler
import yakworks.security.rest.token.GormTokenStorageService
import yakworks.security.rest.token.HeaderTokenReader
import yakworks.security.rest.token.PostgresTokenStorageService
import yakworks.security.shiro.GormShiroPermissionResolver
import yakworks.security.shiro.GormShiroRolePermissionResolver
import yakworks.security.shiro.ShiroGrailsExceptionResolver
import yakworks.security.shiro.ShiroLogoutHandler
import yakworks.security.shiro.ShiroSpringSecurityEventListener
import yakworks.security.shiro.ShiroSubjectBindingFilter
import yakworks.security.shiro.SpringSecurityRealm
import yakworks.security.tenant.UserRequest
import yakworks.security.tenant.UserTenantResolver

@SuppressWarnings(['Indentation', 'Println'])
@CompileDynamic //ok
class RallySecurityGrailsPlugin extends Plugin {
    //FIXME This is not working
    // !!! THIS IS IMPORTANT FOR SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    // We set that in doWithApplicationContext so thread spun off get the same security.
    // needs to be called after spring-security-core
    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest',
                     'gorm-tools', 'rally-domain', 'datasource', 'jasper-reports', 'hibernate5']

    boolean getShiroActive(){ return config.getProperty('shiro.active', Boolean, true) }

    Closure doWithSpring() { { ->
        // xmlns context:"http://www.springframework.org/schema/context"
        // context.'component-scan'('base-package': 'nine.security')

        rallyUserService(RallyUserService, autowireLazy())
        currentUser(CurrentUser, autowireLazy())

        userTenantResolver(UserTenantResolver)

        userRequest(UserRequest){ bean ->
            bean.scope = 'request'
        }

        def secConf = SpringSecurityUtils.securityConfig

        if (secConf.active) {

            if(shiroActive) {
                registerBeans(shiroBeans, delegate)
            }

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

        //replace so we can set the role prefix to be blank and not ROLE_
        webExpressionHandler(DefaultWebSecurityExpressionHandler) {
            expressionParser = ref('voterExpressionParser')
            permissionEvaluator = ref('permissionEvaluator')
            roleHierarchy = ref('roleHierarchy')
            trustResolver = ref('authenticationTrustResolver')
            defaultRolePrefix = ''
        }
        // securityContextRepository(org.springframework.security.web.context.NullSecurityContextRepository)
    }}

    Closure getShiroBeans() { { ->
        println ".. Integrate Shiro Permissions with Spring Security"
        //Shiro Permission integration, do 2 after ANONYMOUS_FILTER so it runs after the restTokenValidationFilter
        SpringSecurityUtils.registerFilter 'shiroSubjectBindingFilter',
            SecurityFilterPosition.ANONYMOUS_FILTER.order + 2

        //override the secService
        secService(SpringShiroSecService, AppUser){ bean -> bean.lazyInit = true}

        shiroLifecycleBeanPostProcessor(LifecycleBeanPostProcessor)

        shiroAdvisorAutoProxyCreator(DefaultAdvisorAutoProxyCreator) { bean ->
            bean.dependsOn = 'shiroLifecycleBeanPostProcessor'
            proxyTargetClass = true
        }

        shiroAttributeSourceAdvisor(AuthorizationAttributeSourceAdvisor) {
            securityManager = ref('shiroSecurityManager')
        }

        shiroPermissionResolver(GormShiroPermissionResolver)

        shiroRolePermissionResolver(GormShiroRolePermissionResolver)

        //override to replace so the shiro annotation exceptions can be handled properly
        // since they fire outside the normal grails handling
        exceptionHandler(ShiroGrailsExceptionResolver) {
            exceptionMappings = [
                'java.lang.Exception': '/error',
                'org.apache.shiro.authz.UnauthorizedException': '/forbidden'
            ]
            // statusCodes = [ '/unauthorized': 403 ]
        }

        boolean useCache = Environment.getCurrent() != Environment.TEST // conf.shiro.useCache

        if (useCache) {
            shiroCacheManager(MemoryConstrainedCacheManager)
        }

        springSecurityRealm(SpringSecurityRealm) {
            authenticationTrustResolver = ref('authenticationTrustResolver')
            shiroPermissionResolver = ref('shiroPermissionResolver')
            rolePermissionResolver = ref('shiroRolePermissionResolver')
            if (useCache) {
                cacheManager = ref('shiroCacheManager')
            }
        }

        shiroSecurityManager(DefaultWebSecurityManager) { bean ->
            realm = ref('springSecurityRealm')
            if (useCache) {
                cacheManager = ref('shiroCacheManager')
            }
        }

        shiroSpringSecurityEventListener(ShiroSpringSecurityEventListener) {
            realm = ref('springSecurityRealm')
            securityManager = ref('shiroSecurityManager')
        }

        shiroSubjectBindingFilter(ShiroSubjectBindingFilter) {
            authenticationTrustResolver = ref('authenticationTrustResolver')
            realm = ref('springSecurityRealm')
            securityManager = ref('shiroSecurityManager')
        }

        shiroLogoutHandler(ShiroLogoutHandler)
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
