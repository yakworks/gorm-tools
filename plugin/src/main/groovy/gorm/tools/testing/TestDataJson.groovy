/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.EmbeddedPersistentEntity
import org.grails.datastore.mapping.model.types.Association

import gorm.tools.json.Jsonify
import grails.buildtestdata.TestData
import grails.buildtestdata.builders.DataBuilderContext
import grails.buildtestdata.builders.PersistentEntityDataBuilder

/**
 * static build methods to wrap {@link TestData} and Jsonify's statics for the json-views.
 * These helpers enable the easy generation of test data in Map form to test methods like the
 * repo's create and update.
 * Note: when using this in unit test the {@link gorm.tools.testing.unit.JsonViewSpecSetup} should
 * be used to make sure the proper view-tools beans are setup. This is alrady taken care of if using
 * one of the main DataRepoTest or DomainRepoTest traits
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@CompileStatic
class TestDataJson {

    /**
     * Uses the build-test-data plugin to first build the entity with data and then converts
     * to with json-views
     *
     * @param args see {@link #parseArgs}
     * @param entityClass
     * @return the JsonifyResult
     */
    static Jsonify.JsonifyResult buildJson(Map args = [:], Class entityClass) {
        //default for save should be false and find true, we don't want to save the dom as we are ust using it to build the json map
        Map<String, Map> parsedArgs = parseArgs(args)
        Object obj = TestData.build(parsedArgs.args, entityClass, parsedArgs.data)
        parsedArgs.jsonArgs['includes'] = getFieldsToBuild(entityClass, parsedArgs.args['includes'], parsedArgs.data)
        //println res.jsonArgs['includes']
        return Jsonify.render(obj, parsedArgs.jsonArgs)
    }

    static List<String> getFieldsToBuild(Class entityClass, Object buildDataIncludes = null, Map data = [:]) {
        PersistentEntityDataBuilder builder = (PersistentEntityDataBuilder)TestData.findBuilder(entityClass)
        //build an empty DataBuilderContext to set includes
        DataBuilderContext ctx = new DataBuilderContext()
        ctx.includes = buildDataIncludes //as Set<String>

        Set<String> fieldsToBuild = builder.getFieldsToBuild(ctx)
        fieldsToBuild.addAll(data.keySet())

        builder.persistentEntity.associations.each{ Association assc ->
            if(fieldsToBuild.contains(assc.name) && !(assc.associatedEntity instanceof EmbeddedPersistentEntity)){
                if(assc.isOwningSide()){
                    fieldsToBuild.add(assc.name + ".*")
                } else{
                    fieldsToBuild.add(assc.name + ".id")
                }
            }
        }
//        builder.findRequiredAssociations().each {
//            fieldsToBuild.add(it.name + ".id")
//        }

        return fieldsToBuild as List<String>
    }

    /**
     * Just a convienience method to return buildJson().json as Map
     * @return Map
     */
    static Map buildMap(Map args = [:], Class entityClass) {
        buildJson(args, entityClass).json as Map
    }

    /**
     * buildMap test data and passes to the create() method from the domain repo
     *
     * @param args
     * @param clazz
     * @return the new entity from the create
     */
    @CompileDynamic
    static <T> T buildCreate(Map args = [:], Class<T> clazz) {
        Map p = buildMap(args, clazz)
        return clazz.create(p)
    }

    /**
     * Parse and split the args map into its components for both the TestData and json-views
     *
     * @param args <br>
     *     - 'includes', 'excludes', 'expand', 'associations', 'deep', 'renderNulls' for jsonArgs to pass to the json-views
     *     - 'save', 'find', 'includes', 'flush', 'failOnError' that gets passed to the TestData
     *
     * @return a map with key args: for the TestData, data: data for the TestData , jsonArgs: to pass into the json-views jsonify
     */
    @SuppressWarnings(['UnnecessaryCast'])
    static Map<String, Map> parseArgs(Map args){
        Map<String, Map> resMap = [args:[:], data:[:], jsonArgs:[:]] as Map<String, Map>
        if (args){
            //the renderArgs for
            ['excludes', 'expand', 'associations', 'deep', 'renderNulls'].each { key ->
                if (args.containsKey(key)) resMap['jsonArgs'][key] = args.remove(key)
            }
            //save should default to false and find to true
            //args['save'] = args.containsKey('save') ? args['save'] : false
            //args['find'] = args.containsKey('find') ? args['find'] : true
            //setup the args for TestData
            ['save', 'find', 'includes', 'flush', 'failOnError'].each { key ->
                if (args.containsKey(key)) resMap['args'][key] = args.remove(key)
                if(key == 'save') resMap['args'][key]
            }
            //make sure includes is in both
            if(resMap['jsonArgs']['includes']) resMap['args']['includes'] = resMap['jsonArgs']['includes']
            //if specifically setting jsonArgs then use those to override
            if(args['jsonArgs']) resMap['jsonArgs'].putAll(args['jsonArgs'] as Map)
            //args becomes the data unless its specifically set.
            resMap['data'] = (args['data'] ? args.remove('data') : args) as Map
        }
        return resMap
    }// $required

}
