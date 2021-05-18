/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.openapi

import java.lang.reflect.ParameterizedType
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.codehaus.groovy.reflection.CachedMethod
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.OneToMany
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.grails.orm.hibernate.cfg.Mapping
import org.springframework.beans.factory.annotation.Autowired
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import gorm.tools.beans.EntityMapService
import gorm.tools.utils.GormMetaUtils
import grails.core.DefaultGrailsApplication
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils

/**
 * Helper to dump yml to file
 */
@CompileStatic
class YamlUtils {

    static void saveYaml(Path path, Object yml){
        DumperOptions dops = new DumperOptions()
        dops.indent = 2
        dops.prettyFlow = true
        dops.defaultFlowStyle = DumperOptions.FlowStyle.BLOCK
        //dops.width = 120
        Yaml yaml = new Yaml(dops)
        yaml.dump(yml, new FileWriter(path.toString()))
        // path.toFile().withWriter {Writer writer ->
        //     yaml.dump(yml, writer)
        // }

    }

    static Object loadYaml(Path path){
        Yaml yaml = new Yaml()
        yaml.load(Files.newInputStream(path))
    }

}
