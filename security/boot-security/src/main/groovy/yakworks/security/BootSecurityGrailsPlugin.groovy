/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.security

import org.apache.shiro.cache.MemoryConstrainedCacheManager
import org.apache.shiro.spring.LifecycleBeanPostProcessor
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator

import grails.plugins.Plugin
import grails.util.Environment
import yakworks.security.audit.AuditStampBeforeValidateListener
import yakworks.security.audit.AuditStampPersistenceEventListener
import yakworks.security.audit.AuditStampSupport
import yakworks.security.audit.DefaultAuditUserResolver
import yakworks.security.config.SpringSecurityConfiguration
import yakworks.security.spring.AsyncSecureService

@SuppressWarnings(['Indentation'])
class BootSecurityGrailsPlugin extends Plugin {

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']

    Closure doWithSpring() { { ->

        // springSecurityConfiguration(SpringSecurityConfiguration)

        //figure out how to depend on this.
        // asyncService(AsyncSecureService)

        // def securityConf = SpringSecurityUtils.securityConfig
        // if (securityConf.active) {
        //
        //     asyncService(AsyncSecureService){ bean -> bean.lazyInit = true}
        //     passwordValidator(PasswordValidator){ bean -> bean.lazyInit = true}
        //
        //     // spring security uses an older deprecated interface security.authentication.encoding.PasswordEncoder
        //     // this one wraps the new one in the old interface as spring sec's DaoAuthenticationProvider needs it
        //     // once thats upgraded then we can fix this
        //     passwordEncoder(BCryptPasswordEncoder){ bean -> bean.lazyInit = true}
        //
        //     //overrrides the spring sec's userDetailsService
        //     userDetailsService(AppUserDetailsService){ bean -> bean.lazyInit = true}
        //
        //     secLoginHandler(SecLoginHandler){ bean -> bean.lazyInit = true}
        //     secLogoutHandler(SecLogoutHandler){ bean -> bean.lazyInit = true}
        //     // authenticationDetailsSource(RallyAuthenticationDetailsSource)
        //
        //     // ANONYMOUS SETUP
        //     // replace authenticationTrustResolver so we can set out own anonymousClass
        //     // authenticationTrustResolver( AuthenticationTrustResolverImpl) {
        //     //     anonymousClass = AnonToken
        //     // }
        //     // anonymousAuthenticationFilter(classFor('anonymousAuthenticationFilter', GrailsAnonymousAuthenticationFilter)) {
        //     //     authenticationDetailsSource = ref('authenticationDetailsSource')
        //     //     key = conf.anon.key
        //     // }
        //     // anonymousAuthenticationFilter(AnonymousAuthenticationFilter) {
        //     //     // authenticationDetailsSource = ref('authenticationDetailsSource')
        //     //     key = "Anon_key"
        //     //     userAttribute = "ANONYMOUS"
        //     // }
        //
        //     //replace so we can set the role prefix to be blank and not ROLE_
        //     webExpressionHandler(DefaultWebSecurityExpressionHandler) {
        //         expressionParser = ref('voterExpressionParser')
        //         permissionEvaluator = ref('permissionEvaluator')
        //         roleHierarchy = ref('roleHierarchy')
        //         trustResolver = ref('authenticationTrustResolver')
        //         //the default is the ROLE_, so we set it to nothing here.
        //         defaultRolePrefix = ''
        //     }
        // }


        //dont register beans if audit trail is disabled.
        // if (config.getProperty('gorm.tools.audit.enabled', Boolean, true)) {
        //     // auditStampEventListener(AuditStampEventListener)
        //     auditStampBeforeValidateListener(AuditStampBeforeValidateListener)
        //     auditStampPersistenceEventListener(AuditStampPersistenceEventListener)
        //     auditStampSupport(AuditStampSupport)
        //     auditUserResolver(DefaultAuditUserResolver)
        // }

        // if (secConf.active) {
        //
        //     if(shiroActive) {
        //         registerBeans(shiroBeans, delegate)
        //     }
        //
        //     if(secConf.rest.active) {
        //         registerBeans(restSecurityBeans, delegate)
        //     }
        // }

    } }


    Closure getShiroBeans() { { ->
        // println ".. Integrate Shiro Permissions with Spring Security"
        //Shiro Permission integration, do 2 after ANONYMOUS_FILTER so it runs after the restTokenValidationFilter
        SpringSecurityUtils.registerFilter 'shiroSubjectBindingFilter',
            SecurityFilterPosition.ANONYMOUS_FILTER.order + 2

        //override the SecService and CurrentUser for Shiro
        secService(SpringShiroSecService)
        currentUser(CurrentSpringShiroUser)

        // pulled what we need from ShiroBeanConfiguration, ShiroConfiguration, ShiroAnnotationProcessorConfiguration
        //see those for stock
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

        // override to replace so the shiro annotation exceptions can be handled properly
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
}
