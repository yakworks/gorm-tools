package yakworks.security.rest.token


import gorm.tools.jdbc.DbDialectService
import gorm.tools.security.AppUserDetailsService
import yakworks.gorm.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.testing.mixin.integration.Integration
import org.junit.Assume
import spock.lang.Ignore
import spock.lang.Specification

@Integration
@Rollback
class PostgresTokenStorageSpec extends Specification implements DataIntegrationTest {

    TokenStorageService tokenStorageService
    AppUserDetailsService userDetailsService
    DbDialectService dbDialectService

    @Ignore
    def "store and load token sanity check"() {
        Assume.assumeTrue(dbDialectService.isPostgres())

        when:
        def userdetails = userDetailsService.loadUserByUsername("admin")
        tokenStorageService.storeToken("1234", userdetails)

        then:
        tokenStorageService.loadUserByToken("1234")
    }

}
