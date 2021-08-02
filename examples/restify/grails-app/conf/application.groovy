import grails.util.Environment

grails {
    gorm.failOnError = true
    gorm.default.mapping = {
        id generator: 'gorm.tools.hibernate.SpringBeanIdGenerator'
        '*'(cascadeValidate: 'dirty')
        //cache usage: System.getProperty("cacheStrategy", "read-write").toString()
    }
    gorm.default.constraints = {
        '*'(nullable:true)
    }
}

//grails.plugin.fields.disableLookupCache = true
//grails.converters.domain.include.version = true

// IF ITS TEST THEN EXIT FAST, as of rest-security v3.0.1 rest tests blows up if anyything is setup
if(Environment.getCurrent() == Environment.TEST){
    grails.plugin.springsecurity.rest.active = false
    grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
    grails.plugin.springsecurity.interceptUrlMap = [
        // all accesible anoymously by default
        [pattern: '/**', access: ['IS_AUTHENTICATED_ANONYMOUSLY']]
    ]
} else {
    // Added by the Spring Security Core plugin:
    grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
    grails.plugin.springsecurity.interceptUrlMap = [
        [pattern: '/',               access: ['permitAll']],
        [pattern: '/h2-console', 		 access: ['permitAll']],
        [pattern: '/api/login', 		 access: ['permitAll']],
        [pattern: '/api/register', 	 access: ['permitAll']],
        [pattern: '/api/logout', 	   access: ['isFullyAuthenticated()']],
        [pattern: '/**',             access: ['isFullyAuthenticated()']]
    ]

    grails.plugin.springsecurity.filterChain.chainMap = [
    [pattern: '/api/**', filters:'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter'],
    [pattern: '/**', filters:'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter']
    ]

    grails.plugin.springsecurity.rest.logout.endpointUrl = '/api/logout'
    // grails.plugin.springsecurity.rest.token.storage.grailsCacheName = 'authTokens'
    // grails.plugin.springsecurity.rest.token.generation.useSecureRandom = true
    grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt = false

    grails.plugin.springsecurity.rest.token.storage.useGorm = true
    grails.plugin.springsecurity.rest.token.storage.gorm.tokenDomainClassName = 'gorm.tools.security.domain.AppUserToken'

    // grails.plugin.springsecurity.rest.token.validation.useBearerToken = false
    // grails.plugin.springsecurity.rest.token.validation.headerName = 'X-Auth-Token'
    // grails.plugin.springsecurity.rest.token.storage.jwt.secret = ''
}
