/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

import groovy.json.JsonSlurper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.EmbeddedPersistentEntity
import org.grails.datastore.mapping.model.types.Association

import gorm.tools.metamap.services.MetaEntityService
import gorm.tools.metamap.services.MetaMapService
import grails.buildtestdata.builders.DataBuilderContext
import grails.buildtestdata.builders.PersistentEntityDataBuilder
import yakworks.json.groovy.JsonEngine

/**
 * static build methods to wrap {@link RepoTestData}
 * These helpers enable the easy generation of test data in Map form to test methods like the
 * repo's create and update.
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
     * @return the JsonifyResult
     */
    static String buildJson(Map args = [:], Class entityClass) {
        //default for save should be false and find true, we don't want to save the dom as we are ust using it to build the json map
        Map<String, Map> parsedArgs = parseArgs(args)
        Object obj = RepoTestData.build(parsedArgs.args, entityClass, parsedArgs.data)
        def incs = getFieldsToBuild(entityClass, parsedArgs.args['includes'], parsedArgs.data)
        //println res.jsonArgs['includes']
        MetaMapService metaMapService = new MetaMapService(
            metaEntityService: new MetaEntityService()
        )
        def emap = metaMapService.createMetaMap(obj, incs)
        return JsonEngine.toJson(emap)
    }

    static List<String> getFieldsToBuild(Class entityClass, Object buildDataIncludes = null, Map data = [:]) {
        PersistentEntityDataBuilder builder = (PersistentEntityDataBuilder)RepoTestData.findBuilder(entityClass)
        //build an empty DataBuilderContext to set includes
        DataBuilderContext ctx = new DataBuilderContext()
        ctx.includes = buildDataIncludes //as Set<String>

        Set<String> fieldsToBuild = builder.getFieldsToBuild(ctx)

        // if(fieldsToBuild.contains('id') && !getIncludeList(builder, ctx).contains('id') ) fieldsToBuild.remove('id')

        fieldsToBuild.addAll(data.keySet())

        builder.persistentEntity.associations.each{ Association assc ->
            if(fieldsToBuild.contains(assc.name) && !(assc.associatedEntity instanceof EmbeddedPersistentEntity)){
                fieldsToBuild.remove(assc.name)
                if(assc.isOwningSide()){
                    fieldsToBuild.add(assc.name + ".*")
                } else{
                    fieldsToBuild.add(assc.name + ".id")
                }
            }
        }

        return fieldsToBuild as List<String>
    }

    // hack to get include list
    static Set<String> getIncludeList(PersistentEntityDataBuilder builder, DataBuilderContext ctx) {
        Set<String> includeList = [] as Set
        if (ctx.includes && ctx.includes instanceof String && ctx.includes == '*') {
            includeList = builder.getConstraintsPropertyNames()
        }
        else if (ctx.includes instanceof List) {
            includeList = ctx.includes as Set<String>
        }
        return includeList
    }

    static Object parseJsonText(String text){
        return new JsonSlurper().parseText(text)
    }

    /**
     * Just a convienience method to return buildJson().json as Map
     * @return Map
     */
    static Map buildMap(Map args = [:], Class entityClass) {
        parseJsonText(buildJson(args, entityClass)) as Map
    }

    /**
     * buildMap test data and passes to the create() method from the domain repo
     *
     * @param args overrides
     * @param clazz the gorm class
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
