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
import org.springframework.stereotype.Service
import org.yaml.snakeyaml.Yaml

import gorm.tools.rest.ast.RestApiAstUtils
import gorm.tools.support.ConfigAware
import gorm.tools.utils.GormMetaUtils
import yakworks.commons.io.FileSystemUtils
import yakworks.commons.io.FileUtil
import yakworks.commons.lang.NameUtils
import yakworks.commons.map.Maps
import yakworks.commons.util.BuildSupport

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
@Service
@SuppressWarnings(['UnnecessaryGetter', 'AbcMetric', 'Println'])
@CompileStatic
class OapiGen implements ConfigAware {
    public static final String API_SRC = 'api-docs/openapi'
    public static final String API_BUILD = 'api-docs/dist/openapi'

    @Autowired GormToSchema gormToSchema

    void generate() {
        def buildDest = makeBuildDirs()
        def srcPath = getApiSrcPath()

        FileSystemUtils.copyRecursively(srcPath, buildDest)

        generateModels()
        //do all but autocash
        genOpenapiYaml(['rally', 'ar', 'autocash'])
    }

    /**
     * gets a path using the gradle.projectDir as the root
     */
    Path getApiSrcPath(String sub = null){
        def path =  Paths.get(BuildSupport.gradleRootProjectDir?:'', API_SRC)
        return sub ? path.resolve(sub) : path
    }

    /**
     * gets a path using the gradle.projectDir as the root
     */
    Path getApiBuildPath(String sub = null){
        def path =  Paths.get(BuildSupport.gradleRootProjectDir?:'', API_BUILD)
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
        Map restApiPaths = config.getProperty('api.paths', Map)

        List tags = (List)api.tags
        Map<String, List> newTagGroups = [:]

        Map namespaces = config.getProperty('api.namespaces', Map, [:])

        for(entry in restApiPaths){
            if(namespaces.containsKey(entry.key)){
                String namespace = entry.key
                if(namespaceList && !namespaceList.contains(namespace)) continue
                for(epoint in (Map)entry.value){
                    String endpoint = (String)epoint.key
                    processEndpoint(api, endpoint, namespace, (Map)epoint.value, newTagGroups, tags)
                }
            }
            else { //normal not namespaced or may have slash like 'foo/bar' as key
                String pathName = entry.key
                Map pathCfg = (Map)entry.value
                Map pathParts = RestApiAstUtils.splitPath(pathName, pathCfg)
                String endpoint = pathParts.name
                String namespace = pathParts.namespace
                if(namespace && namespaceList && !namespaceList.contains(namespace)) continue
                processEndpoint(api, endpoint, namespace, pathCfg, newTagGroups, tags)
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
            xTagGroupsList << [name: namespaces[k], tags: v as List]
        }

        api['x-tagGroups'] = xTagGroupsList

    }

    // FIXME for 388 - Merge tag and description in yml
    void processEndpoint(Map api, String endpoint, String namespace, Map pathMap, Map xTagGroups, List tags){
        Map tagEntry = [name: endpoint]
        if(pathMap.description) tagEntry.description = pathMap.description
        tags = tags as List<Map>
        if(!tags.find { it.name == endpoint }) {
            tags << tagEntry
        }

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
        Files.createDirectories(getApiBuildPath().resolve(pathKeyPrefix))

        String capitalName = NameUtils.getClassNameFromKebabCase(endpoint)
        String modelName = NameUtils.getShortName((String)restConfig.entityClass)
        String baseDir = namespace ? '../../' : '../'
        Map model = [
            endpoint: endpoint, name: endpoint, capitalName: capitalName,
            modelName: modelName, namespace: namespace, baseDir: baseDir
        ]

        //do no param path file
        String filePathRef = "${pathFileBase}.yaml"//.toString()
        processTplFile(restConfig, 'paths/tpl.yaml', filePathRef, model)
        //update the API key
        paths[pathKey] = ['$ref': filePathRef]

        //path Id file
        filePathRef = "${pathFileBase}@{id}.yaml"//.toString()
        processTplFile(restConfig, 'paths/tpl@{id}.yaml', filePathRef, model)
        paths[pathKeyId] = ['$ref': filePathRef]

        //if bulk operations are enabled
        if(restConfig.bulkOps){
            paths["${pathKey}/bulk"] = ['$ref': "${pathFileBase}@bulk.yaml".toString()]
            filePathRef = "${pathFileBase}@bulk.yaml"//.toString()
            processTplFile(restConfig, 'paths/tpl@bulk.yaml', filePathRef, model)
        }

    }


    void processTplFile(Map restConfig, String srcPath, String outputPath, Map model){
        Path tplFile = getApiSrcPath().resolve(srcPath)
        String ymlTpl = FileUtil.readFileToString(tplFile.toFile())
        ymlTpl = FileUtil.parseStringAsGString(ymlTpl, model)
        //check for existing override file

        //parse the yaml
        Map pathMap = parseAndLoadYaml(srcPath, model)
        //remove ops that are not allowed
        modifyForAllowedOps(restConfig, pathMap)
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
        String ymlTpl = FileUtil.readFileToString(tplFile.toFile())
        ymlTpl = FileUtil.parseStringAsGString(ymlTpl, model)
        //parse the yaml
        return new Yaml().load(ymlTpl)
    }

    void modifyForAllowedOps(Map restConfig, Map pathMap){
        //if it doesn't have the allowedOps then return
        List allowedOps = restConfig.allowedOps as List
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
        Files.createDirectories(getApiBuildPath('models'))
        def path = getApiBuildPath("models/${clazz.simpleName}${suffix}.yaml")
        YamlUtils.saveYaml(path, schemaMap)
        return path
    }


}