/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.springframework.core.convert.converter.Converter
import org.springframework.core.convert.support.ConfigurableConversionService
import org.springframework.core.env.ConfigurableEnvironment

/**
 * Misc utils for testing, asserting and "helpers" in spock tests
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
@SuppressWarnings(['ClassForName', 'MethodParameterTypeRequired'])
class TestTools {

    static void addEnvConverters(ConfigurableEnvironment env){
        ConfigurableConversionService conversionService = env.getConversionService()
        conversionService.addConverter(new Converter<String, Class>() {
            @Override
            Class convert(String source) {
                Class.forName(source)
            }
        })
    }

    @CompileDynamic
    static void addConfigConverters(cfg){
        // ConfigurableConversionService conversionService = cfg.conversionService
        cfg.@conversionService.addConverter(new Converter<String, Class>() {
            @Override
            Class convert(String source) {
                Class.forName(source)
            }
        })
    }

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
     * asserts more or less that subset:[a: 1, b: 2] == full:[a: 1, b: 2, c: 3]
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
