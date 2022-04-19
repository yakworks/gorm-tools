/**
 * common config for testing and dev. can be referenced in dependent plugins and apps using config location
 */

//make sure security is on for testing
grails.plugin.springsecurity.active = true
grails.plugin.springsecurity.rest.active = true
grails.plugin.springsecurity.rest.token.storage.useJwt=false
grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt=false
// grails.plugin.springsecurity.rest.token.storage.useJwt=false
// grails.plugin.springsecurity.rest.token.storage.jwt.useSignedJwt=false
grails.plugin.springsecurity.securityConfigType = "InterceptUrlMap"
// all accesible anoymously by default
grails.plugin.springsecurity.interceptUrlMap = [[pattern: '/**', access: ['IS_AUTHENTICATED_ANONYMOUSLY']]]
