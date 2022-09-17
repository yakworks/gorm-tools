/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.support

import groovy.transform.CompileStatic

import org.junit.AfterClass
import org.junit.Before

import grails.buildtestdata.TestDataConfigurationHolder
import yakworks.testing.gorm.RepoTestData
import yakworks.testing.gorm.TestDataJson

/**
 * Integration tests, any class really, can implement this trait to add build-test-data functionality
 */
@CompileStatic
@SuppressWarnings(["GroovyUnusedDeclaration", "AssignmentToStaticFieldFromInstanceMethod"])
trait RepoTestDataBuilder {
    private static boolean hasCustomTestDataConfig = false

    /** calls {@link RepoTestData#build} */
    public <T> T build(Map args = [:], Class<T> clazz) {
        def o = RepoTestData.build(args, clazz)

    }

    /** calls {@link RepoTestData#build} */
    public <T> T build(Class<T> clazz, Map<String, Object> propValues) {
        RepoTestData.build([:], clazz, propValues)
    }

    /** calls {@link RepoTestData#build} */
    public <T> T build(Map args, Class<T> clazz, Map<String, Object> propValues) {
        RepoTestData.build(args, clazz, propValues)
    }

    Map buildMap(Class clazz, Map args = [:]) {
        TestDataJson.buildMap(args, clazz)
    }

    /** calls {@link RepoTestData#build} with [find: true] passed to args*/
    public <T> T findOrBuild(Class<T> clazz, Map<String, Object> propValues = [:]) {
        RepoTestData.build([find: true], clazz, propValues)
    }

    /**
     * Override this to override test data configuration for this test class
     */
    Closure doWithTestDataConfig() {
        null
    }

    @Before
    void setupCustomTestDataConfig() {
        Closure testDataConfig = doWithTestDataConfig()
        if (testDataConfig) {
            TestDataConfigurationHolder.mergeConfig(testDataConfig)
            hasCustomTestDataConfig = true
        }
    }

    @AfterClass
    static void cleanupTestDataBuilder() {
        RepoTestData.clear()
        if (hasCustomTestDataConfig) {
            hasCustomTestDataConfig = false
            TestDataConfigurationHolder.reset()
        }
    }
}
