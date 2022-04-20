package yakworks.security.rest.token


import gorm.tools.jdbc.DbDialectService
import gorm.tools.security.AppUserDetailsService
import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.plugin.springsecurity.rest.token.storage.TokenStorageService
import grails.testing.mixin.integration.Integration
import org.junit.Assume
import spock.lang.Specification
import yakworks.security.rest.token.PostgresTokenStorageService

@Integration
@Rollback
class PostgresTokenStorageSpec extends Specification implements DataIntegrationTest {

    TokenStorageService tokenStorageService
    AppUserDetailsService userDetailsService
    DbDialectService dbDialectService

    // @IgnoreIf({ env['DBMS']=='mysql' })
    def "store and load token sanity check"() {
        Assume.assumeTrue(dbDialectService.isPostgres())

        when:
        def userdetails = userDetailsService.loadUserByUsername("admin")
        tokenStorageService.storeToken("1234", userdetails)

        then:
        tokenStorageService.loadUserByToken("1234")
    }

}
