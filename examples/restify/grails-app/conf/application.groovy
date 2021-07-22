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
// CAREFUL NOT TO PUSH THIS TO PRODUCTION
grails.dbconsole.enabled = true

//grails.plugin.fields.disableLookupCache = true
//grails.converters.domain.include.version = true

// Added by the Spring Security Core plugin:
grails.plugin.springsecurity.userLookup.userDomainClassName = 'com.mysecurerest.User'
grails.plugin.springsecurity.userLookup.authorityJoinClassName = 'com.mysecurerest.UserAuthority'
grails.plugin.springsecurity.authority.className = 'com.mysecurerest.Authority'
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
grails.plugin.springsecurity.interceptUrlMap = [
	[pattern: '/',               access: ['permitAll']],
	[pattern: '/error',          access: ['permitAll']],
	[pattern: '/index',          access: ['permitAll']],
	[pattern: '/index.gsp',      access: ['permitAll']],
	[pattern: '/shutdown',       access: ['permitAll']],
	[pattern: '/assets/**',      access: ['permitAll']],
	[pattern: '/**/js/**',       access: ['permitAll']],
	[pattern: '/**/css/**',      access: ['permitAll']],
	[pattern: '/**/images/**',   access: ['permitAll']],
	[pattern: '/**/favicon.ico', access: ['permitAll']],
	[pattern: '/api/login', 		 access: ['permitAll']],
	[pattern: '/api/register', 	 access: ['permitAll']],
    [pattern: '/dbconsole', 		 access: ['permitAll']],
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

// grails.plugin.springsecurity.rest.token.storage.memcached.hosts = 'localhost:11211'
// grails.plugin.springsecurity.rest.token.storage.memcached.username = ''
// grails.plugin.springsecurity.rest.token.storage.memcached.password = ''
// grails.plugin.springsecurity.rest.token.storage.memcached.expiration = 86400
