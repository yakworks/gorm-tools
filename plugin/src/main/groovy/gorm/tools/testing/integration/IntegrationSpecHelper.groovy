package gorm.tools.testing.integration

import gorm.tools.DbDialectService
import gorm.tools.beans.MapFlattener
import gorm.tools.repository.RepoUtil
import gorm.tools.testing.utils.SpecHookCaller
import grails.build.support.MetaClassRegistryCleaner
import grails.web.servlet.mvc.GrailsParameterMap
import org.junit.After
import org.junit.AfterClass
import org.junit.Before
import org.junit.BeforeClass
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.mock.web.MockHttpServletRequest

/**
 * Contains helpers for integration tests. Can be chained with some custom helper traits with the application-specific
 * initialization logic.
 *
 * The logic may be injected, using the standard Spock approach with @Before/@After, etc. JUnit annotations (Spock
 * conventional 'setup/cleanup/setupSpec/cleanupSpec' names do not work for traits).
 *
 * As an alternative, the gorm-tools provides the approach with auxiliary conventional methods
 * 'specificSetup/specificCleanup/specificSetupSpec/specificCleanupSpec' that should not be marked with any annotations.
 * That's useful for the case, when you don't want your code to depend on test dependencies.
 */
trait IntegrationSpecHelper implements SpecHookCaller {

    /**
     * An util for working with JDBC.
     */
    JdbcTemplate jdbcTemplate

    /**
     * A service that provides the info about the current dialect of the DBMS used.
     */
    DbDialectService dbDialectService

    /**
     * A metaclass registry cleaner to track and clean all changes, that were made to the metaclass during the test.
     * It is automatically cleaned up after each test case.
     */
    private MetaClassRegistryCleaner registryCleaner

    /**
     * Contains common 'setupSpec' logic to be executed before the one from the Spock Specification.
     */
    @BeforeClass
    void integrationSetupSpec() {
        doSpecificSetupSpec()
    }

    /**
     * Contains common 'setup' logic to be executed before the one from the Spock Specification.
     */
    @Before
    void integrationSetup() {
        dbDialectService.updateOrDateFormat()
        doSpecificSetup()
    }

    /**
     * Contains common 'cleanup' logic to be executed before the one from the Spock Specification.
     */
    @After
    void integrationCleanup() {
        doSpecificCleanup()

        //clear meta class changes after each test, if they were tracked and are not already cleared.
        if(registryCleaner) clearMetaClassChanges()
    }

    /**
     * Contains common 'cleanupSpec' logic to be executed before the one from the Spock Specification.
     */
    @AfterClass
    void integrationCleanupSpec() {
        doSpecificCleanupSpec()
    }

    /**
     * Flushes the session, clears the session cache and the DomainClassGrailsPlugin.PROPERTY_INSTANCE_MAP.
     */
    void flushAndClear() {
        RepoUtil.flushAndClear()
    }

    /**
     * Flushes the current datastore session.
     */
    void flush() {
        RepoUtil.flush()
    }

    /**
     * Build GrailsParameter map from a given map.
     */
    GrailsParameterMap buildParams(Map params) {
        flattenMap(params)
    }

    /**
     * Returns a flattened version of a given map with request params.
     */
    GrailsParameterMap flattenMap(Map map) {
        MockHttpServletRequest request = new MockHttpServletRequest()
        Map<String, String> params = new MapFlattener(convertEmptyStringsToNull: false).flatten(map)
        request.addParameters(params)

        new GrailsParameterMap(request)
    }

    /**
     * Start tracking all metaclass changes made after this call, so it can all be undone later.
     */
    void trackMetaClassChanges() {
        registryCleaner = MetaClassRegistryCleaner.createAndRegister()
        GroovySystem.metaClassRegistry.addMetaClassRegistryChangeEventListener(registryCleaner)
    }

    /**
     * Reverts all metaclass changes done since last call to trackMetaClassChanges()
     */
    void clearMetaClassChanges() {
        if(registryCleaner) {
            registryCleaner.clean()
            GroovySystem.metaClassRegistry.removeMetaClassRegistryChangeEventListener(registryCleaner)
            registryCleaner = null
        }
    }
}
