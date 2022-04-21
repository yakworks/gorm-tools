package security

// The core security config comes from gorm-tools-security in its plugin.groovy

println "------------ rest security config ----------------"

grails.plugin.springsecurity.useSecurityEventListener = true

grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.interceptUrlMap = [
    [pattern: '/api/login',       access: ['permitAll']],
    [pattern: '/api/oauth/**',    access: ['permitAll']],
    [pattern: '/api/validate',    access: ['permitAll']],
    [pattern: '/api/logout',      access: ['permitAll']],
    [pattern: '/api/actuator/health',      access: ['permitAll']],
    [pattern: '/api/actuator/liveness',      access: ['permitAll']],
    [pattern: '/**',              access: ['isFullyAuthenticated()']]
]
String restSecFilter = 'JOINED_FILTERS,-anonymousAuthenticationFilter,-exceptionTranslationFilter,-authenticationProcessingFilter,-securityContextPersistenceFilter,-rememberMeAuthenticationFilter'
String anonSecFilter = 'anonymousAuthenticationFilter,restTokenValidationFilter,restExceptionTranslationFilter,filterInvocationInterceptor'
String baseSecFilter = 'JOINED_FILTERS,-restTokenValidationFilter,-restExceptionTranslationFilter'

grails.plugin.springsecurity.filterChain.chainMap = [
    //We need anonymousAuthenticationFilter filter for /api/oauth/** urls for oauth login and callbacks to work - permitAll expects Anonymously authenticated user.
    [pattern: '/api/oauth/**', filters: anonSecFilter],
    [pattern: '/api/actuator/health', filters: anonSecFilter],
    [pattern: '/api/actuator/liveness', filters: anonSecFilter],
    [pattern: '/api/**', filters: restSecFilter],
    // [pattern: '/**', filters: anonSecFilter],
]

// JWT
grails.plugin.springsecurity.rest.token.storage.useJwt=false
grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt=false
// grails.plugin.springsecurity.rest.token.generation.jwt.algorithm='RS256'
//for dev ONLY, do not deploy if using JWT with this key.
// grails.plugin.springsecurity.rest.token.storage.jwt.secret='qrD6h8K6S9503Q06Y6Rfk21TErImPYqa'
// grails.plugin.springsecurity.rest.token.storage.jwt.expiration=3600
