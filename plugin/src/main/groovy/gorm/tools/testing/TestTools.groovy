/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity

/**
 * Misc utils for testing, asserting and "helpers" in spock tests
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class TestTools {

    /**
     * Checks if the GormEntity's properties contains whats in expected
     * calls mapContains after is collects the entities properties into a map
     */
    static boolean entityContains(GormEntity entity, Map expected) {
        mapContains(entity.properties, expected)
    }

    /**
     * Loosely test 2 maps for equality
     * asserts more or less that every keySet in [a: 1, b: 2] exists in [a: 1, b: 2, c: 3] which is true in this example
     * mapContains([a: 1, b: 2], [a: 1, c: 3]) returns false
     * mapContains([a: 2, b: 2], [a: 1, b: 2]) also returns false
     * if subset is an empty map or null returns false
     *
     * @param full the map to look in
     * @param subset the subset of values to make sure are in the full
     * @param exclude optional list of keys to exclude from the subset
     * http://csierra.github.io/posts/2013/02/12/loosely-test-for-map-equality-using-groovy/
     */
    static boolean mapContains(Map full, Map subset, List<String> exclude=[]) {
        //println "subset: $subset"
        //println "full: $full"
        if(!subset) return false
        return subset.findAll{!exclude.contains(it.key)}.every {  it.value == full[it.key]}
    }

    /**
     * makes sure the passed in object is a list, if not then it wraps it in one
     * helpful when creating spocks data pipes
     */
    static List ensureList(Object obj){
        obj instanceof List ? obj : [obj]
    }

}
