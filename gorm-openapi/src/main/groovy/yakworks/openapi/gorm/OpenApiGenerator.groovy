/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.openapi.gorm

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired
import org.yaml.snakeyaml.Yaml

import gorm.tools.utils.GormMetaUtils
import yakworks.commons.io.PathTools
import yakworks.commons.lang.NameUtils
import yakworks.commons.map.Maps
import yakworks.commons.util.BuildSupport
import yakworks.commons.util.StringUtils
import yakworks.gorm.api.ApiConfig
import yakworks.gorm.api.PathItem
import yakworks.yaml.YamlUtils

import static ApiSchemaEntity.CruType

/**
 * Generates domains to schema
 * We are chasing this part https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schemaObject
 * @author Joshua Burnett (@basejump)
 * @since 7.0.8
 */
@SuppressWarnings(['UnnecessaryGetter', 'AbcMetric', 'Println'])
@CompileStatic
class OpenApiGenerator {
    @Autowired ApiConfig apiConfig
    //inject src and build dirs when setting up bean
    String apiSrc
    String apiBuild
    List namespaceList

    GormToSchema gormToSchema

    GormToSchema getGormToSchema(){
        if(!this.gormToSchema) this.gormToSchema = new GormToSchema()
        return this.gormToSchema
    }

    void generate(List nsList = []) {
        def buildDest = makeBuildDirs()
        def srcPath = getApiSrcPath()

        PathTools.copyRecursively(srcPath, buildDest)

        generateModels()
        //do all but autocash
        genOpenapiYaml(nsList ?: namespaceList)
    }

    /**
     * gets a path using the project.projectDir as the root
     */
    Path getApiSrcPath(String sub = null){
        def path =  Paths.get(BuildSupport.rootProjectDir?:'', apiSrc)
        return sub ? path.resolve(sub) : path
    }

    /**
     * gets a path using the project.projectDir as the root
     */
    Path getApiBuildPath(String sub = null){
        def path =  Paths.get(BuildSupport.rootProjectDir?:'', apiBuild)
        return sub ? path.resolve(sub) : path
    }

    //creates the build/openapi dir
    Path makeBuildDirs(){
        def path = getApiBuildPath()
        Files.createDirectories(path)
        return path
    }

    //generates the openapi.yaml with paths. starts by reading the src/openapi/openapi.yaml
    void genOpenapiYaml(List namespaceList){
        def openapiYaml = getApiSrcPath('api.yaml')
        Map api = (Map) YamlUtils.loadYaml(openapiYaml)
        assert api['openapi'] == '3.0.3'
        spinThroughRestApi(api, namespaceList)
    }


    //iterate over the restapi keys and add setup the yaml
    void spinThroughRestApi(Map api, List namespaceList){
        List tags = (List)api.tags
        Map<String, List> newTagGroups = [:]

        Map namespaces = apiConfig.namespaces ?: [:]
        Map groupedPaths = apiConfig.paths.groupBy { it.value.namespace }

        for(ns in namespaces){
            //it is possible that there are no entities under a space, eg "reports"
            Map spacePaths = groupedPaths[ns.key] ?: [:]
            Map sortedPathz = spacePaths.sort{ it.key }
            for(entry in sortedPathz){
                PathItem pathItem = (PathItem)entry.value
                String endpoint = pathItem.name
                String namespace = pathItem.namespace
                if(namespace && namespaceList && !namespaceList.contains(namespace)) continue
                processEndpoint(api, endpoint, namespace, pathItem, newTagGroups, tags)
            }
        }

        api.tags = tags

        mergeTagGroups(api, namespaces, newTagGroups)

        def buildOpenapiYaml = getApiBuildPath().resolve('api.yaml')
        YamlUtils.saveYaml(buildOpenapiYaml, api)
    }

    void mergeTagGroups(Map api, Map namespaces, Map<String, List> newTagGroups){
        List xTagGroups = api['x-tagGroups']
        //convert to Map so we can merge easier.
        Map mergedTagGroups = [:]
        xTagGroups.each{
            List tags = it['tags'] as List
            String name = it['name'] as String
            if(newTagGroups.containsKey(name)){
                //merge in the tags
                newTagGroups[name].addAll(tags)
            } else {
                //new list
                newTagGroups[name] = tags
            }
            //make sure they are sorted and unique
            newTagGroups[name] = newTagGroups[name].unique().sort()

        }

        def xTagGroupsList = []
        newTagGroups.each{k, v ->
            //if namespaces is not specified then put it in root.
            String name = namespaces[k]?: 'root'
            xTagGroupsList << [name: name, tags: v as List]
        }

        api['x-tagGroups'] = xTagGroupsList

    }

    void processEndpoint(Map api, String endpoint, String namespace, PathItem pathItem, Map xTagGroups, List tags){
        Map tagEntry = [name: endpoint]
        if(pathItem.description) tagEntry.description = pathItem.description
        tags = tags as List<Map>
        if(!tags.find { it.name == endpoint }) {
            tags << tagEntry
        }

        //default to root for tag groups if its empty
        def tagGroupKey = namespace ?: 'root'

        if(!xTagGroups[tagGroupKey]) xTagGroups[tagGroupKey] = []
        ((List)xTagGroups[tagGroupKey]).add(endpoint)

        try{
            createPaths(api, endpoint, namespace, pathItem)
        } catch(e){
            String msg = "Error on $endpoint"
            throw new IllegalArgumentException(msg, e)
        }
    }

    //create the files for the path
    Map createPaths(Map api, String endpoint, String namespace, PathItem pathItem){
        Map paths = (Map)api.paths ?: [:]
        String namespacePrefix = namespace ? "$namespace/" : ''
        String pathKey = "/${namespacePrefix}${endpoint}"//.toString()
        String pathKeyId = "${pathKey}/{id}"//.toString()
        String pathKeyPrefix = "./paths/${namespacePrefix}"
        String pathFileBase = "${pathKeyPrefix}${endpoint}"
        //make sure dirs exist
        Files.createDirectories(getApiBuildPath().resolve(pathKeyPrefix))

        String capitalName = NameUtils.getClassNameFromKebabCase(endpoint)
        String modelName = NameUtils.getShortName(pathItem.entityClass)
        String baseDir = namespace ? '../../' : '../'
        Map model = [
            endpoint: endpoint, name: endpoint, capitalName: capitalName,
            modelName: modelName, namespace: namespace, baseDir: baseDir
        ]

        //do no param path file
        String filePathRef = "${pathFileBase}.yaml"//.toString()
        processTplFile(pathItem, 'paths/tpl.yaml', filePathRef, model)
        //update the API key
        paths[pathKey] = ['$ref': filePathRef]

        //path Id file
        filePathRef = "${pathFileBase}@{id}.yaml"//.toString()
        processTplFile(pathItem, 'paths/tpl@{id}.yaml', filePathRef, model)
        paths[pathKeyId] = ['$ref': filePathRef]

        //upsert - do only if both create/update are allowed
        if(pathItem.upsertAllowed()) {
            filePathRef = "${pathFileBase}@upsert.yaml"//.toString()
            processTplFile(pathItem, 'paths/tpl@upsert.yaml', filePathRef, model)
            paths["${pathKey}/upsert"] = ['$ref': filePathRef]
        }

        //if bulk operations are enabled
        if(pathItem.bulkOps){
            paths["${pathKey}/bulk"] = ['$ref': "${pathFileBase}@bulk.yaml".toString()]
            filePathRef = "${pathFileBase}@bulk.yaml"//.toString()
            processTplFile(pathItem, 'paths/tpl@bulk.yaml', filePathRef, model)
        }

    }


    void processTplFile(PathItem pathItem, String srcPath, String outputPath, Map model){
        Path tplFile = getApiSrcPath().resolve(srcPath)
        String ymlTpl = tplFile.getText()
        ymlTpl = StringUtils.parseStringAsGString(ymlTpl, model)
        //check for existing override file

        //parse the yaml
        Map pathMap = parseAndLoadYaml(srcPath, model)
        //remove ops that are not allowed
        modifyForAllowedOps(pathItem, pathMap)
        //see if an existing tpl exists in paths to merge into the generated one
        Path existingTplFile = getApiSrcPath().resolve(outputPath)
        if(Files.exists(existingTplFile)){
            Map overridePathMap = parseAndLoadYaml(outputPath, model)
            pathMap = Maps.merge(pathMap, overridePathMap)
        }

        Path outPath = getApiBuildPath().resolve(outputPath)
        YamlUtils.saveYaml(outPath, pathMap)
        // Files.write(outPath, ymlTpl.getBytes())
    }

    /**
     * parse the file as SimpleTemplate binding the passed in model
     * then load load as yaml return the Map or List
     *
     * @param srcTplYamlPath - the source file name of the Tpl.yml
     * @param outputPath - the output yml file, will also look to see if exists and merge
     * @param model - the mode to merge into the srcTplYaml when parsed
     * @return the loaded yaml object
     */
    public <T> T parseAndLoadYaml(String srcTplYamlPath, Map model){
        Path tplFile = getApiSrcPath().resolve(srcTplYamlPath)
        String ymlTpl = tplFile.getText()
        ymlTpl = StringUtils.parseStringAsGString(ymlTpl, model)
        //parse the yaml
        return new Yaml().load(ymlTpl)
    }

    void modifyForAllowedOps(PathItem pathItem, Map pathMap){
        //if it doesn't have the allowedOps then return
        List allowedOps = pathItem.allowedOps
        if(!allowedOps) return

        if(!allowedOps.contains('create')){
            pathMap.remove('post')
        }
        if(!allowedOps.contains('update')){
            pathMap.remove('put')
        }
        if(!allowedOps.contains('delete')){
            pathMap.remove('delete')
        }
    }

    void generateModels() {
        def mapctx = GormMetaUtils.getMappingContext()
        for( PersistentEntity entity : mapctx.persistentEntities){
            def map = getGormToSchema().generate(entity, CruType.Read)
            def mapCreate = getGormToSchema().generate(entity, CruType.Create)
            def mapUpdate = getGormToSchema().generate(entity, CruType.Update)
            writeYmlModel(entity.javaClass, map, CruType.Read)
            writeYmlModel(entity.javaClass, mapCreate, CruType.Create)
            writeYmlModel(entity.javaClass, mapUpdate, CruType.Update)
        }
    }

    Path writeYmlModel(Class clazz, Map schemaMap, CruType type) {
        //if type is read then dont do suffix
        String suffix = type == CruType.Read ? '' : "_$type"
        Files.createDirectories(getApiBuildPath('models'))
        def path = getApiBuildPath("models/${clazz.simpleName}${suffix}.yaml")
        YamlUtils.saveYaml(path, schemaMap)
        return path
    }

    // static Map splitPath(String resourceName, Map ctrlConfig){
    //     Map pathParts = [name: resourceName, namespace: '']
    //     if (resourceName.contains("/")) {
    //         List parts = resourceName.split("[/]") as List
    //         String name = parts.last()
    //         pathParts['name'] = name
    //         final int nestedIndex = resourceName.lastIndexOf('/')
    //         String namespace = resourceName.substring(0, nestedIndex)
    //         pathParts['namespace'] = namespace
    //         return pathParts
    //     } else {
    //         pathParts['name'] = resourceName
    //         if(ctrlConfig['namespace']) pathParts['namespace'] = ctrlConfig['namespace'] as String
    //     }
    //     return pathParts
    // }

}
