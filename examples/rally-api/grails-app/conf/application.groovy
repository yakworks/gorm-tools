import grails.util.Environment
import yakworks.commons.util.BuildSupport

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

List restConfigs = ["classpath:restapi-config.yml"]

// rally rest configs
[
   "org.yml", "orgTypeSetup.yml", "tag.yml", "user.yml", "contact.yml",
    "role.yml", "roleUser.yml", "syncJob.yml", "activity.yml"
].each { fname ->
    restConfigs.add "classpath:restapi/rally/$fname"
}

// securityConfig << "classpath:security/security-config.groovy"

grails.config.locations = restConfigs

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

    // Added by the Spring Security Core plugin:
    grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
    grails.plugin.springsecurity.interceptUrlMap = [
        [pattern: '/api/login',       access: ['permitAll']],
        [pattern: '/api/oauth/**',    access: ['permitAll']],
        [pattern: '/api/validate',    access: ['permitAll']],
        [pattern: '/api/logout',      access: ['permitAll']],
        // [pattern: '/h2-console', 		 access: ['permitAll']],
        // [pattern: '/api/register', 	 access: ['permitAll']],
        [pattern: '/**',             access: ['isFullyAuthenticated()']]
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


    // grails.plugin.springsecurity.rest.logout.endpointUrl = '/api/logout'
    // grails.plugin.springsecurity.rest.login.endpointUrl = '/api/login'
    // grails.plugin.springsecurity.rest.token.storage.grailsCacheName = 'authTokens'
    // grails.plugin.springsecurity.rest.token.generation.useSecureRandom = true
    grails.plugin.springsecurity.rest.token.storage.useJwt=false
    grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt=false

    grails.plugin.springsecurity.rest.token.storage.useGorm = true
    grails.plugin.springsecurity.rest.token.storage.gorm.tokenDomainClassName = 'gorm.tools.security.domain.AppUserToken'

}


String projectRoot = BuildSupport.gradleRootProjectDir
app {
    resources {
        currentTenant = {
            return [num: 'virgin', id: 2]
        }
        rootLocation = { args ->
            File root = new File("${projectRoot}/examples/resources")
            return root.canonicalPath
        }
        tempDir = {
            File file = new File("./build/rootLocation/tempDir")
            if (!file.exists()) file.mkdirs()
            return file.canonicalPath
        }
        attachments.location = 'attachments'
    }
}
