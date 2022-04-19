package nine.security

import javax.annotation.Resource

import gorm.tools.idgen.IdGenerator
import gorm.tools.jdbc.DbDialectService
import gorm.tools.security.AppUserDetailsService
import gorm.tools.testing.integration.DataIntegrationTest
import grails.gorm.transactions.Rollback
import grails.testing.mixin.integration.Integration
import org.junit.Assume
import org.springframework.jdbc.core.JdbcTemplate
import spock.lang.Specification

@Integration
@Rollback
class AppUserTokenStorageSpec extends Specification implements DataIntegrationTest {

    AppUserTokenStorageService tokenStorageService
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
