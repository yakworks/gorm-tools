/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing.integration

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.datastore.mapping.core.Session
import org.grails.orm.hibernate.HibernateDatastore
import org.junit.After
import org.springframework.jdbc.core.JdbcTemplate

import gorm.tools.beans.AppCtx
import gorm.tools.jdbc.DbDialectService
import gorm.tools.transaction.TrxService
import grails.build.support.MetaClassRegistryCleaner
import grails.buildtestdata.TestDataBuilder
import grails.config.Config

/**
 * Contains helpers for integration tests. Can be chained with some custom helper traits with the application-specific
 * initialization logic.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
trait DataIntegrationTest implements TestDataBuilder {

    JdbcTemplate jdbcTemplate
    DbDialectService dbDialectService
    HibernateDatastore hibernateDatastore

    Config getConfig(){
        AppCtx.config
    }

    /**
     * A metaclass registry cleaner to track and clean all changes, that were made to the metaclass during the test.
     * It is automatically cleaned up after each test case.
     */
    private MetaClassRegistryCleaner registryCleaner

    @After
    void cleanupRegistry() {
        //clear meta class changes after each test, if they were tracked and are not already cleared.
        if(registryCleaner) clearMetaClassChanges()
    }

    /** consistency with other areas of grails and other unit tests */
    AbstractDatastore getDatastore() {
        hibernateDatastore
    }

    Session getCurrentSession(){
        getDatastore().currentSession
    }

    void flushAndClear(){
        flush()
        getDatastore().currentSession.clear()
    }

    /**
     * Flushes the current datastore session.
     */
    void flush() {
        TrxService.flush(getDatastore())
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
