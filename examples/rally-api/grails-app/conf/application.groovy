import grails.util.Environment
import yakworks.commons.util.BuildSupport

// securityConfig << "classpath:security/security-config.groovy"

grails.config.locations = ["classpath:restapi-config.yml", "classpath*:restapi/rally/*.yml"]

//grails.plugin.fields.disableLookupCache = true
//grails.converters.domain.include.version = true

if(Environment.getCurrent() == Environment.TEST ){
    grails.plugin.springsecurity.rest.active = false
    grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
    grails.plugin.springsecurity.interceptUrlMap = [
        // all accesible anoymously by default
        [pattern: '/**', access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
    ]
}
else {
    //PRODUCTION
    //enable shiro
    shiro.active = false
    grails.plugin.springsecurity.useSecurityEventListener = true
    // Added by the Spring Security Core plugin:
    grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
    grails.plugin.springsecurity.interceptUrlMap = [
        // [pattern: '/api/login',       access: ['permitAll']],
        [pattern: '/api/oauth/**',    access: ['permitAll']],
        [pattern: '/api/validate',    access: ['permitAll']],
        // [pattern: '/api/logout',      access: ['permitAll']],
        [pattern: '/api/actuator/health', access: ['permitAll']],
        [pattern: '/api/actuator/liveness', access: ['permitAll']],
        // [pattern: '/h2-console', 		 access: ['permitAll']],
        // [pattern: '/api/register', 	 access: ['permitAll']],
        [pattern: '/**',             access: ['isFullyAuthenticated()']]
    ]

    String restSecFilter = 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'
    String anonSecFilter = 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor'
    // String statefulSecFilter = 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'

    grails.plugin.springsecurity.filterChain.chainMap = [
        //We need anonymousAuthenticationFilter filter for /api/oauth/** urls for oauth login and callbacks to work - permitAll expects Anonymously authenticated user.
        [pattern: '/api/oauth/**', filters: anonSecFilter],
        [pattern: '/api/actuator/health', filters: anonSecFilter],
        [pattern: '/api/actuator/liveness', filters: anonSecFilter],
        // [pattern: '/api/**', filters: restSecFilter],
        //filter all through restSecFilter
        [pattern: '/**', filters: restSecFilter],
    ]


    // grails.plugin.springsecurity.rest.logout.endpointUrl = '/api/logout'
    // grails.plugin.springsecurity.rest.login.endpointUrl = '/api/login'
    // grails.plugin.springsecurity.rest.token.storage.grailsCacheName = 'authTokens'
    // grails.plugin.springsecurity.rest.token.generation.useSecureRandom = true
    grails.plugin.springsecurity.rest.token.storage.useJwt=false
    grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt=false
    // grails.plugin.springsecurity.rest.token.storage.jwt.secret='qrD6h8K6S9503Q06Y6Rfk21TErImPYqa'
    // grails.plugin.springsecurity.rest.token.validation.enableAnonymousAccess = true

}


String projectRoot = BuildSupport.gradleRootProjectDir
app {
    resources {
        rootLocation = "${projectRoot}/examples/resources"
        tempDir = "./build/rootLocation/tempDir"
        attachments.location = 'attachments'
    }
}
