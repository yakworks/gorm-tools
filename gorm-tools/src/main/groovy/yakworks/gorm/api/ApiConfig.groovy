/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import javax.annotation.PostConstruct

import groovy.transform.CompileStatic

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

import yakworks.commons.beans.BeanTools
import yakworks.spring.YamlSpringUtils

@Component
@CompileStatic
class ApiConfig {
    /**
     * Grab value from config to load and split in arrays
     * this allows for resources like the following
     */// classpath*:/api/**/\*.yml
    @Value('${api.config.import}')
    private List apiResources

    /** Full loaded yaml */
    Map<String, Object> api

    String defaultPackage
    Map<String, String> namespaces
    Map<String, PathItem> paths

    /** paths keyed by the entity class */
    Map<String, PathItem> pathsByEntity = [:] as Map<String, PathItem>
    /** temporary holder for raw map by entity */
    Map<String, Object> pathsMapByEntity = [:] as Map<String, Object>

    @PostConstruct
    void init() {
        Map yaml = YamlSpringUtils.loadYaml(apiResources)
        if(yaml && yaml.containsKey('api')) {
            api = (Map<String, Object>)yaml["api"]
            BeanTools.bind(this, api)
            updatePaths()
        }
    }

    /**
     * Update the paths with resrouce, namespace, key
     */
    void updatePaths(){
        paths.each { String key, PathItem pitem ->
            pitem.key = key
            Map keyParts = ApiUtils.splitPath(key)
            pitem.name = keyParts.name
            pitem.namespace = keyParts.namespace
            //should it error and stop if no entityClass
            if(pitem.entityClass){
                pathsByEntity[pitem.entityClass] = pitem
                pathsMapByEntity[pitem.entityClass] = api.paths[key]
            }
            //pathItems[key] = pathItem
        }
    }

    /**
     * Helper to get includes from pathsByEntity or return empty map if none
     */
    Map<String, List<String>> getIncludesForEntity(String className){
        pathsByEntity[className] ? pathsByEntity[className].includes : [:] as Map<String, List<String>>
    }

    /**
     * gets the raw Map config for the entityKey and namespace
     */
    Map<String, Object> getPathMap(String entityKey, String namespace){
        //String configPath = namespace ? "api.paths.${namespace}.${entityKey}" : "api.paths.${entityKey}"
        Map<String, Object> entityConfig
        String pathKey = namespace ? "/${namespace}/${entityKey}" : "/${entityKey}"
        entityConfig = api.paths[pathKey] as Map<String, Object>

        //if nothing and it has a dot than entity key might be full class name with package, so do search
        if(!entityConfig && entityKey.indexOf('.') != -1)
            entityConfig = pathsMapByEntity[entityKey] as Map<String, Object>

        return entityConfig
    }
}
