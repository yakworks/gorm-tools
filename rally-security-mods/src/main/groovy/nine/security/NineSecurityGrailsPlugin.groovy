package nine.security

import groovy.transform.CompileDynamic

import org.springframework.security.core.context.SecurityContextHolder

import grails.plugin.springsecurity.SpringSecurityUtils
import grails.plugins.Plugin
import nine.security.oauth.NineOauthUserDetailsService

@SuppressWarnings('Indentation')
@CompileDynamic //ok
class NineSecurityGrailsPlugin extends Plugin {
    def loadAfter = ['rally']

    Closure doWithSpring() { { ->
        rallyUserService(RallyUserService, autowireLazy())

        def secConf = SpringSecurityUtils.securityConfig
        if(secConf && secConf.active && secConf.rest.active) {
            println "9ci's rest security setup"
            //add only if sec plugins enabled, or else it would fail.
            oauthUserDetailsService(NineOauthUserDetailsService)
            tokenStorageService(AppUserTokenStorageService)
            tokenReader(HeaderTokenReader)
            restAuthenticationProvider(RestAuthenticationProvider) {
                useJwt = false
            }
        }

        if (secConf.active) {
            log.info "Spring Security is enabled"
            //LDAP Setup
            // def ldapConf = securityConf.ldap
            // if (ldapConf?.active) {
            //     ldapUserDetailsMapper(NineLdapUserDetailsMapper) { bean ->
            //         bean.lazyInit = true
            //         userDetailsService = ref('userDetailsService')
            //     }
            // }
        } else {
            log.warn "Spring Security is disabled, skip Rally config for Spring Security"
        }
    }}

    @Override
    void doWithApplicationContext() {
        // this make sure the any threads that are spun off also get the user who is logged in already
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL)
    }

    Closure autowireLazy() {{ bean ->
        bean.lazyInit = true
        bean.autowire = true
    }}

    Closure lazy() {{ bean ->
        bean.lazyInit = true
    }}
}
