import grails.util.Environment

if(Environment.getCurrent() == Environment.PRODUCTION){
//PRODUCTION

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
    grails.plugin.springsecurity.rest.token.storage.gorm.tokenDomainClassName = 'yakworks.security.gorm.model.AppUserToken'

    // grails.plugin.springsecurity.rest.token.validation.useBearerToken = false
    // grails.plugin.springsecurity.rest.token.validation.headerName = 'X-Auth-Token'
    // grails.plugin.springsecurity.rest.token.storage.jwt.secret = ''
}
