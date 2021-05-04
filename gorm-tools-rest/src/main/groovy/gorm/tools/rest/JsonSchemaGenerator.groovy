/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest

import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.core.io.support.GrailsFactoriesLoader
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.grails.orm.hibernate.cfg.Mapping
import org.grails.validation.discovery.ConstrainedDiscovery
import org.springframework.beans.factory.annotation.Autowired
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

import gorm.tools.utils.GormMetaUtils
import grails.core.DefaultGrailsApplication
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import grails.util.GrailsNameUtils

import static grails.util.GrailsClassUtils.getStaticPropertyValue

/**
 * Generates the domain part
 * should be merged with either Swaggydocs or Springfox as outlined
 * https://github.com/OAI/OpenAPI-Specification is the new openAPI that
 * Swagger moved to.
 * We are chasing this part https://github.com/OAI/OpenAPI-Specification/blob/master/versions/3.0.0.md#schemaObject
 * Created by JBurnett on 6/19/17.
 */
//@CompileStatic
@SuppressWarnings(['UnnecessaryGetter', 'AbcMetric'])
@CompileStatic
class JsonSchemaGenerator {

    @Autowired
    HibernateMappingContext persistentEntityMappingContext

    @Autowired
    DefaultGrailsApplication grailsApplication

    //good overview here
    //https://spacetelescope.github.io/understanding-json-schema/index.html
    //https://docs.spring.io/spring-data/rest/docs/current/reference/html/#metadata.json-schema
    //https://github.com/OAI/OpenAPI-Specification
    Map generate(Class clazz) {
        return generate(clazz.name)
    }

    void generateYmlModels() {
        def mapctx = GormMetaUtils.getMappingContext()
        for( PersistentEntity entity : mapctx.persistentEntities){
            generateYmlFile(entity.javaClass)
        }
    }

    Path generateYmlFile(Class clazz) {
        def map = generate(clazz.name)
        String projectDir = System.getProperty("gradle.projectDir", '')
        Files.createDirectories(Paths.get(projectDir, "build/schema"))
        def path = Paths.get(projectDir, "build/schema/${clazz.simpleName}.yaml")
        saveYaml(path, map)
        return path
    }

    def saveYaml(Path path, yml){
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

    Map generate(String domainName) {
        PersistentEntity domClass = GormMetaUtils.getPersistentEntity(domainName)
        Map schema = [:]
        schema['$schema'] = 'http://json-schema.org/schema#'
        schema['$id'] = "http://localhost:8080/schema/${domClass.getDecapitalizedName().capitalize()}.json".toString()
        schema["definitions"] = [:]
        schema.putAll generate(domClass, schema)
        return schema
    }

    Map generate(PersistentEntity domainClass, Map schema) {
        Map<String, ?> map = [:]
        //TODO figure out a more performant way to do these if
        Mapping mapping = getMapping(domainClass.name)

        //Map cols = mapping.columns
        map.title = domainClass.name //TODO Should come from application.yml !?

        if (mapping?.comment) map.description = mapping.comment

        // if (AnnotationUtils.findAnnotation(domainClass.class, RestApi)) {
        //     map.description = AnnotationUtils.getAnnotationAttributes(AnnotationUtils.findAnnotation(domainClass.class, RestApi)).description
        // }

        map.type = 'Object'
        map.required = []

        Map propMap = getDomainProperties(domainClass, schema)

        map.props = propMap.props
        map.required = propMap.required

        return map
    }

    @SuppressWarnings(['MethodSize'])
    // @CompileDynamic
    private Map getDomainProperties(PersistentEntity domClass, Map schema) {
        println "----- ${domClass.name} getDomainProperties ----"
        String domainName = GrailsNameUtils.getPropertyNameRepresentation(domClass.name)
        Map<String, ?> propsMap = [:]
        List required = []

        Map idVerMap = [:]

        //id
        PersistentProperty idProp = domClass.getIdentity()
        if(idProp){
            Map idJsonType = getJsonType(idProp.type)
            idJsonType.putAll([
                description: 'unique id',
                example: 954,
                readOnly: true
            ])
            idVerMap[idProp.name] = idJsonType
        }

        //version
        if (domClass.version) {
            idVerMap[domClass.version.name] = [
                type: 'integer',
                description: 'version of the edit, incremented on each change',
                example: 0,
                readOnly: true
            ] as Map
        }

        Mapping mapping = getMapping(domainName)
        List<PersistentProperty> props = resolvePersistentProperties(domClass)

        Map<String, ConstrainedProperty> constrainedProps = GormMetaUtils.findConstrainedProperties(domClass)

        for (PersistentProperty prop : props) {
            def constraints = (DefaultConstrainedProperty) constrainedProps[prop.name]

            //Map mappedBy = domClass.mappedBy
            if (!constraints.display) continue //skip if display is false

            Map jprop = [:]

            if (prop instanceof Association) {
                PersistentEntity referencedDomainClass = GormMetaUtils.getPersistentEntity(prop.type)
                Map schemaDefs = (Map) schema.definitions
                /*(prop.isManyToOne() || prop.isOneToOne()) && */
                if (referencedDomainClass && !schemaDefs?.containsKey(referencedDomainClass.name)) {
                    //treat as a seperate file
                    jprop['$ref'] = "${referencedDomainClass.javaClass.simpleName}.yaml".toString()
                    //propsMap[prop.name] = refMap

                    // if (referencedDomainClass.javaClass.isAnnotationPresent(RestApi)) {
                    //     //treat as a seperate file
                    //     Map refMap = ['$ref': "${referencedDomainClass.name}.json"]
                    //     map[prop.name] = refMap
                    // } else {
                    //     //treat as definition in same schema
                    //     String refName = referencedDomainClass.getDecapitalizedName().capitalize()
                    //     schemaDefs[refName] = [:]
                    //     //schemaDefs[refName] = generate(referencedDomainClass, schema)
                    //     schema.definitions = schemaDefs
                    //     Map refMap = ['$ref': "#/definitions/$referencedDomainClass.name"]
                    //     map[prop.name] = refMap
                    // }
                }
            } else { //setup type
                //type
                Map typeFormat = getJsonType(constraints.propertyType)
                jprop.type = typeFormat.type

                //format
                if (typeFormat.format) jprop.format = typeFormat.format
                if (typeFormat.enum) jprop.enum = typeFormat.enum
                //format override from constraints
                // assert constraints.class == PersistentEntity
                if (constraints.format) jprop.format = constraints.format
                //if (constraints.property?.appliedConstraints?.email) jprop.format = 'email'
                //pattern TODO
            }

            //title
            def title = constraints.getMetaConstraintValue("title")
            if(title) jprop.title = title

            //description
            String description = constraints.getMetaConstraintValue("description")
            if (description) jprop.description = description

            //example
            def example = constraints.getMetaConstraintValue("example")
            if (example) jprop.example = example

            //defaults
            String defVal = getDefaultValue(mapping, prop.name)
            if (defVal != null) jprop.default = defVal //TODO convert to string?

            //required
            Boolean req = constraints.getMetaConstraintValue("required")
            println "  ${prop.name} : required: $req"
            if (!constraints.isNullable() && constraints.editable && req != false) {
                //if it doesn't have a default value and its not a boolean
                if (jprop.default == null && !Boolean.isAssignableFrom(constraints.propertyType)) {
                    //jprop.required = true
                    required.add(prop.name)
                }
            }
            //readOnly
            if (!constraints.editable) jprop.readOnly = true
            //default TODO
            //minLength
            if (constraints.getMaxSize()) jprop.maxLength = constraints.getMaxSize()
            //maxLength
            if (constraints.getMinSize()) jprop.minLength = constraints.getMinSize()

            if (constraints.getMin() != null) jprop.minimum = constraints.getMin()
            if (constraints.getMax() != null) jprop.maximum = constraints.getMax()
            if (constraints.getScale() != null) jprop.multipleOf = 1 / Math.pow(10, constraints.getScale())

            propsMap[prop.name] = jprop


            //def typeFormat = getJsonType(constraints)
            //map.properties[prop.pathFromRoot] = typeFormat

        }
        def auditStamp = [:]
        //Audit Stamp put in its own so we can append to end of list
        ['createdBy', 'editedBy', 'createdDate', 'editedDate'].each {
            auditStamp[it] = propsMap.remove(it)
        }
        //def sortedProps = propsMap.sort()
        def p = [:]
        p.putAll(propsMap.sort())
        p.putAll(idVerMap)
        p.putAll(auditStamp)
        //sortedProps.putAll(idVerMap)
        //sortedProps.putAll(auditStamp)

        return [props: p, required: required.unique()]
    }

    @CompileDynamic
    String getDefaultValue(Mapping mapping, String propName) {
        mapping?.columns?."$propName"?.columns?.getAt(0)?.defaultValue
        //cols[prop.name]?.columns?.getAt(0)?.defaultValue
    }

    @CompileDynamic
    Mapping getMapping(String domainName) {
        PersistentEntity pe = GormMetaUtils.findPersistentEntity(domainName)
        return GormMetaUtils.getMappingContext().mappingFactory?.entityToMapping?.get(pe)
    }

    /* see http://epoberezkin.github.io/ajv/#formats */
    /* We are adding 'money' and 'date' as formats too
     * big decimal defaults to money
     */

    @CompileDynamic
    protected Map<String,Object> getJsonType(Class propertyType) {
        Map typeFormat = [type: 'string'] as Map<String,Object>
        switch (propertyType) {
            case [Boolean, Byte]:
                typeFormat.type = 'boolean'
                break
            case [Integer, Short]:
                typeFormat.type = 'integer'
                break
            case [Long]:
                typeFormat.type = 'integer'
                typeFormat.format = 'int64'
                break
            case [Double, Float, BigDecimal]:
                typeFormat.type = 'number'
                break
            case [BigDecimal]:
                typeFormat.type = 'number'
                typeFormat.format = 'money'
                break
            case [LocalDate]:
                typeFormat.type = 'string'
                //date. verified to be a date of the format YYYY-MM-DD
                typeFormat.format = 'date'
                break
            case [Date, LocalDateTime]:
                //date-time. verified to be a valid date and time in the format YYYY-MM-DDThh:mm:ssZ
                typeFormat.type = 'string'
                typeFormat.format = 'date-time'
                break
            case [String]:
                typeFormat.type = 'string'
                break
            case { it.isEnum() }:
                typeFormat.type = 'string'
                typeFormat.enum = propertyType.values()*.name() as String[]

        }
        //TODO what about types like Byte etc..?
        return typeFormat
    }

    //copied from FormFieldsTagLib in the Fields plugin
    @CompileDynamic
    private List<PersistentProperty> resolvePersistentProperties(PersistentEntity domainClass, Map attrs = [:]) {
        List properties
        if (attrs.order) {
            def orderBy = attrs.order?.tokenize(',')*.trim() ?: []
            properties = orderBy.collect { propertyName -> domainClass.getPersistentProperty(propertyName) }
        } else {
            properties = domainClass.persistentProperties
            def blacklist = attrs.except?.tokenize(',')*.trim() ?: []
            //blacklist << 'dateCreated' << 'lastUpdated'
            Map scaffoldProp = getStaticPropertyValue(domainClass.class, 'scaffold')
            if (scaffoldProp) {
                blacklist.addAll(scaffoldProp.exclude)
            }
            def constProps = getConstrainedProperties(domainClass)

            properties.removeAll { it.name in blacklist }
            properties.removeAll { !constProps[it.name]?.display }
            properties.removeAll { it.propertyMapping.mappedForm.derived }

            //Collections.sort(properties, new org.grails.validation.DomainClassPropertyComparator(domainClass))
        }

        return properties
    }

    Map<String, ConstrainedProperty> getConstrainedProperties(PersistentEntity persistentEntity) {
        return GormMetaUtils.findConstrainedProperties(persistentEntity)
    }

}
