/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.rest.ast.RestApiAstUtils
import gorm.tools.support.ConfigAware
import gorm.tools.utils.GormMetaUtils
import yakworks.commons.build.BuildUtils
import yakworks.commons.io.FileSystemUtils
import yakworks.commons.io.FileUtil
import yakworks.commons.lang.NameUtils

import static gorm.tools.openapi.ApiSchemaEntity.CruType

/**
 * Generates the domain part
 * should be merged with either Swaggydocs or Springfox as outlined
 * https://github.com/OAI/OpenAPI-Specification is the new openAPI that
 * Swagger moved to.
 * We are chasing this part https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schemaObject
 * Created by JBurnett on 6/19/17.
 */
//@CompileStatic
@SuppressWarnings(['UnnecessaryGetter', 'AbcMetric', 'Println'])
@CompileStatic
class OpenApiGenerator implements ConfigAware {
    static final String API_SRC = 'src/api-docs'
    static final String API_BUILD = 'build/api-docs'

    @Autowired GormToSchema gormToSchema

    void generate() {
        def buildDest = makeBuildDirs()
        def srcPath = getApiSrcPath()

        FileSystemUtils.copyRecursively(srcPath, buildDest)

        generateModels()
        //do all but autocash
        genOpenapiYaml(['rally', 'security', 'ar'])
        //genOpenapiYaml(['autocash'])
    }

    /**
     * gets a path using the gradle.projectDir as the root
     */
    Path getApiSrcPath(String sub = null){
        def path =  Paths.get(BuildUtils.gradleProjectDir, API_SRC)
        return sub ? path.resolve(sub) : path
    }

    /**
     * gets a path using the gradle.projectDir as the root
     */
    Path getApiBuildPath(String sub = null){
        def path =  Paths.get(BuildUtils.gradleProjectDir, API_BUILD)
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
        def openapiYaml = getApiSrcPath('openapi/api.yaml')
        Map api = (Map) YamlUtils.loadYaml(openapiYaml)
        assert api['openapi'] == '3.0.3'
        spinThroughRestApi(api, namespaceList)
    }

    //iterate over the restapi keys and add setup the yaml
    void spinThroughRestApi(Map api, List namespaceList){
        Map restApiPaths = config.getProperty('restApi.paths', Map)

        List tags = (List)api.tags
        Map<String, List> xTagGroups = [:]

        Map namespaces = config.getProperty('restApi.namespaces', Map, [:])

        for(entry in restApiPaths){
            if(namespaces.containsKey(entry.key)){
                String namespace = entry.key
                for(epoint in (Map)entry.value){
                    String endpoint = (String)epoint.key
                    processEndpoint(api, endpoint, namespace, (Map)epoint.value, xTagGroups, tags)
                }
            }
            else { //normal not namespaced or may have slash like 'foo/bar' as key
                String pathName = entry.key
                Map pathCfg = (Map)entry.value
                Map pathParts = RestApiAstUtils.splitPath(pathName, pathCfg)
                String endpoint = pathParts.name
                String namespace = pathParts.namespace
                processEndpoint(api, endpoint, namespace, pathCfg, xTagGroups, tags)
            }

        }
        api.tags = tags
        //api.paths = paths
        def xTagGroupsList = []
        xTagGroups.each{k, v ->
            xTagGroupsList << [name: namespaces[k], tags: v as List]
        }
        api['x-tagGroups'] = xTagGroupsList
        def buildOpenapiYaml = getApiBuildPath().resolve('openapi/api.yaml')
        YamlUtils.saveYaml(buildOpenapiYaml, api)
    }

    void processEndpoint(Map api, String endpoint, String namespace, Map pathMap, Map xTagGroups, List tags){
        Map tagEntry = [name: endpoint]
        if(pathMap.description) tagEntry.description = pathMap.description
        tags << tagEntry

        if(!xTagGroups[namespace]) xTagGroups[namespace] = []
        ((List)xTagGroups[namespace]).add(endpoint)

        try{
            createPaths(api, endpoint, namespace, pathMap)
        } catch(e){
            String msg = "Error on $endpoint"
            throw new IllegalArgumentException(msg, e)
        }
    }

    //create the files for the path
    Map createPaths(Map api, String endpoint, String namespace, Map restConfig){
        Map paths = (Map)api.paths
        String namespacePrefix = namespace ? "$namespace/" : ''
        String pathKey = "/${namespacePrefix}${endpoint}"//.toString()
        String pathKeyId = "${pathKey}/{id}"//.toString()
        String pathKeyPrefix = "./paths/${namespacePrefix}"
        String pathFileBase = "${pathKeyPrefix}${endpoint}"
        //make sure dirs exist
        Files.createDirectories(getApiBuildPath('openapi').resolve(pathKeyPrefix))

        String capitalName = NameUtils.getClassNameFromKebabCase(endpoint)
        String modelName = NameUtils.getShortName((String)restConfig.entityClass)
        String baseDir = namespace ? '../../' : '../'
        Map model = [
            endpoint: endpoint, name: endpoint, capitalName: capitalName,
            modelName: modelName, namespace: namespace, baseDir: baseDir
        ]

        //do no param path file
        String filePathRef = "${pathFileBase}.yaml"//.toString()
        processTplFile('paths/tpl.yaml', filePathRef, model)
        //update the API key
        paths[pathKey] = ['$ref': filePathRef]

        //path Id file
        filePathRef = "${pathFileBase}@{id}.yaml"//.toString()
        processTplFile('paths/tpl@{id}.yaml', filePathRef, model)
        paths[pathKeyId] = ['$ref': filePathRef]

        //if bulk operations are enabled
        if(restConfig.bulkOps){
            paths["${pathKey}/bulk"] = ['$ref': "${pathFileBase}@bulk.yaml".toString()]
            filePathRef = "${pathFileBase}@bulk.yaml"//.toString()
            processTplFile('paths/tpl@bulk.yaml', filePathRef, model)
        }

    }

    void processTplFile(String srcPath, String outputPath, Map model){
        Path tplFile = getApiSrcPath('openapi').resolve(srcPath)
        String ymlTpl = FileUtil.readFileToString(tplFile.toFile())
        ymlTpl = FileUtil.parseStringAsGString(ymlTpl, model)
        Path outPath = getApiBuildPath('openapi').resolve(outputPath)
        Files.write(outPath, ymlTpl.getBytes())
    }

    void modifyForAllowedOps(String filePathRef, Map restConfig){
        //if it doesn't have the allowedOps then return
        List allowedOps = restConfig['restConfig']
        if(!allowedOps) return

        Path yamlPath = getApiBuildPath('openapi').resolve(filePathRef)

        Map tplYaml = (Map)YamlUtils.loadYaml(yamlPath)

        if(!allowedOps.contains('create')){
            tplYaml.remove('post')
        }
        if(!allowedOps.contains('update')){
            tplYaml.remove('put')
        }
        if(!allowedOps.contains('delete')){
            tplYaml.remove('delete')
        }

        // Map tplGet = (Map)tplYaml['get']
        // tplGet.tags = [model.name]
        // tplGet.summary = "${model.capitalName} List".toString()
        // tplGet.description = "Query and retrieve a ${model.capitalName} list".toString()
        // tplGet.operationId = "get${model.capitalName}List".toString()
        // tplGet['responses']['200']['$ref'] = "${model.name}_pager.yaml".toString()
        //
        // Map tplPost = (Map)tplYaml['post']
        // tplPost.tags = [model.name]
        // tplPost.summary = "Create a ${model.capitalName}".toString()
        // tplPost.description = "Create a new ${model.capitalName}".toString()
        // tplPost.operationId = "create${model.capitalName}".toString()
        // tplPost.requestBody['$ref'] = "${model.name}_request_create.yaml".toString()
        // tplPost['responses']['201']['$ref'] = "${model.name}_response.yaml".toString()
        //
        // def pathYaml = getApiBuildPath('openapi').resolve(pathRef)
        // YamlUtils.saveYaml(pathYaml, tplYaml)
    }

    void generateModels() {
        def mapctx = GormMetaUtils.getMappingContext()
        for( PersistentEntity entity : mapctx.persistentEntities){
            def map = gormToSchema.generate(entity, CruType.Read)
            def mapCreate = gormToSchema.generate(entity, CruType.Create)
            def mapUpdate = gormToSchema.generate(entity, CruType.Update)
            writeYmlModel(entity.javaClass, map, CruType.Read)
            writeYmlModel(entity.javaClass, mapCreate, CruType.Create)
            writeYmlModel(entity.javaClass, mapUpdate, CruType.Update)
        }
    }

    Path writeYmlModel(Class clazz, Map schemaMap, CruType type) {
        //if type is read then dont do suffix
        String suffix = type == CruType.Read ? '' : "_$type"
        Files.createDirectories(getApiBuildPath('openapi/models'))
        def path = getApiBuildPath("openapi/models/${clazz.simpleName}${suffix}.yaml")
        YamlUtils.saveYaml(path, schemaMap)
        return path
    }

}
