package yakworks.security

import groovy.transform.CompileDynamic

import org.apache.shiro.cache.MemoryConstrainedCacheManager
import org.apache.shiro.spring.LifecycleBeanPostProcessor
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor
import org.apache.shiro.web.mgt.DefaultWebSecurityManager
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator
import org.springframework.security.core.context.SecurityContextHolder

import grails.plugin.springsecurity.SecurityFilterPosition
import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import org.springframework.security.web.access.expression.DefaultWebSecurityExpressionHandler
import yakworks.rally.tenant.UserRequest
import yakworks.rally.tenant.UserTenantResolver
import yakworks.security.rest.NineOauthUserDetailsService
import yakworks.security.rest.RestAuthenticationProvider
import yakworks.security.rest.token.HeaderTokenReader
import yakworks.security.rest.token.PostgresTokenStorageService
import yakworks.security.shiro.GormShiroPermissionResolver
import yakworks.security.shiro.GormShiroRolePermissionResolver
import yakworks.security.shiro.ShiroGrailsExceptionResolver
import yakworks.security.shiro.ShiroLogoutHandler
import yakworks.security.shiro.ShiroSpringSecurityEventListener
import yakworks.security.shiro.ShiroSubjectBindingFilter
import yakworks.security.shiro.SpringSecurityRealm

@SuppressWarnings(['Indentation', 'Println'])
@CompileDynamic //ok
class RallySecurityGrailsPlugin extends Plugin {
    def loadAfter = ['rally', 'springSecurityCore']

    Closure doWithSpring() { { ->
        // xmlns context:"http://www.springframework.org/schema/context"
        // context.'component-scan'('base-package': 'nine.security')

        userTenantResolver(UserTenantResolver)

        userRequest(UserRequest){ bean ->
            bean.scope = 'request'
        }

        rallyUserService(RallyUserService, autowireLazy())

        def secConf = SpringSecurityUtils.securityConfig

        if (secConf.active) {
            registerBeans(shiroBeans, delegate)

            if(secConf.rest.active) {
                registerBeans(restBeans, delegate)
            }

            // registerBeans(ldapBeans, delegate)
        }
    }}

    @Override
    void doWithApplicationContext() {
        // this make sure the any threads that are spun off also get the user who is logged in already
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
        def conf = SpringSecurityUtils.securityConfig
        if (conf && conf.active ) {
            applicationContext.logoutHandlers.add 0, applicationContext.shiroLogoutHandler // must be before SecurityContextLogoutHandler
        }
    }

    Closure getRestBeans() { { ->
        println "Configuring 9ci's rest security setup"
        //add only if sec plugins enabled, or else it would fail.
        oauthUserDetailsService(NineOauthUserDetailsService)
        tokenStorageService(PostgresTokenStorageService)
        tokenReader(HeaderTokenReader)
        restAuthenticationProvider(RestAuthenticationProvider) {
            useJwt = false
        }
    }}

    Closure getShiroBeans() { { ->
        println "Integrate Shiro Permissions with Spring Security"
        //Shiro Permission integration
        SpringSecurityUtils.registerFilter 'shiroSubjectBindingFilter',
            SecurityFilterPosition.SECURITY_CONTEXT_FILTER.order + 1

        //replace so we can set the role prefix to be blank and not ROLE_
        // webExpressionHandler(DefaultWebSecurityExpressionHandler) {
        //     expressionParser = ref('voterExpressionParser')
        //     permissionEvaluator = ref('permissionEvaluator')
        //     roleHierarchy = ref('roleHierarchy')
        //     trustResolver = ref('authenticationTrustResolver')
        //     defaultRolePrefix = ''
        // }

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

        //override to replace so the shiro annotation exceptions can be handled properly since the fire outside the normal
        exceptionHandler(ShiroGrailsExceptionResolver) {
            exceptionMappings = [
                'java.lang.Exception': '/error',
                'org.apache.shiro.authz.UnauthorizedException': '/forbidden'
            ]
            // statusCodes = [ '/unauthorized': 403 ]
        }

        boolean useCache = true // conf.shiro.useCache

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
