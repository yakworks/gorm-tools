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

import gorm.tools.beans.EntityMapService
import gorm.tools.utils.GormMetaUtils
import grails.core.DefaultGrailsApplication
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils

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
class GormToSchema {

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
        YamlUtils.saveYaml(path, map)
        return path
    }

    Map generate(String domainName) {
        PersistentEntity domClass = GormMetaUtils.getPersistentEntity(domainName)
        Map schema = [:]
        // schema['$schema'] = 'http://json-schema.org/schema#'
        // schema['$id'] = "http://localhost:8080/schema/${domClass.getDecapitalizedName().capitalize()}.json".toString()
        // schema["definitions"] = [:]
        generate(domClass, schema)
        return schema
    }

    Map generate(PersistentEntity domainClass, Map schema) {
        //Map<String, ?> map = [:]
        //TODO figure out a more performant way to do these if
        Mapping mapping = getMapping(domainClass.name)

        //Map cols = mapping.columns
        schema.title = domainClass.javaClass.simpleName

        if (mapping?.comment) schema.description = mapping.comment

        // if (AnnotationUtils.findAnnotation(domainClass.class, RestApi)) {
        //     map.description = AnnotationUtils.getAnnotationAttributes(AnnotationUtils.findAnnotation(domainClass.class, RestApi)).description
        // }

        schema.type = 'object'
        schema.required = []

        Map propMap = getDomainProperties(domainClass, schema)

        schema['properties'] = propMap.props
        schema.required = propMap.required

        return schema
    }

    @SuppressWarnings(['MethodSize'])
    // @CompileDynamic
    private Map getDomainProperties(PersistentEntity perEntity, Map schema) {
        println "----- ${perEntity.name} getDomainProperties ----"
        String domainName = NameUtils.getPropertyNameRepresentation(perEntity.name)
        Map<String, ?> propsMap = [:]
        List required = []

        Map idVerMap = [:]

        //id
        PersistentProperty idProp = perEntity.getIdentity()
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
        if (perEntity.version) {
            idVerMap[perEntity.version.name] = [
                type: 'integer',
                description: 'version of the edit, incremented on each change',
                example: 0,
                readOnly: true
            ] as Map
        }

        Mapping mapping = getMapping(domainName)
        List<PersistentProperty> props = resolvePersistentProperties(perEntity)

        Map<String, ConstrainedProperty> constrainedProperties = GormMetaUtils.findConstrainedProperties(perEntity)
        Map<String, ConstrainedProperty> nonValidatedProperties = GormMetaUtils.findNonValidatedProperties(perEntity)
        def constrainedProps = constrainedProperties + nonValidatedProperties

        println "-- All constrainted props --"
        List constrainedPropsNames = []
        constrainedProps.each { String k, ConstrainedProperty val ->
            println "  $k"
            if(k != 'version' && val.display != false) {
                constrainedPropsNames.add(k)
            }
        }
        println "-- end constrainted props --"
        println "-- PersistentProperties --"
        for (PersistentProperty prop : props) {
            def constraints = (DefaultConstrainedProperty) constrainedProps[prop.name]
            //remove from constraint name list so we can spin through the remaining later that are not persistentProperties
            //and set them up to for transients and set them up too.
            constrainedPropsNames.remove(prop.name)

            //if its version should have been taken care of or is set to version false
            if(prop.name == 'version') continue
            //skip if display is false
            if (!constraints.display) continue

            Map jprop = [:]

            if (prop instanceof Association) {
                println "  ${prop.name} association, ${prop} ${prop.type}"
                if(prop instanceof OneToMany){
                    def assocEntity = prop.associatedEntity
                    collectionType(jprop, assocEntity.javaClass)
                } else {
                    PersistentEntity referencedDomainClass = GormMetaUtils.getPersistentEntity(prop.type)
                    Map schemaDefs = (Map) schema.definitions
                    /*(prop.isManyToOne() || prop.isOneToOne()) && */
                    if (referencedDomainClass && !schemaDefs?.containsKey(referencedDomainClass.name)) {
                        //treat as a seperate file
                        jprop['$ref'] = "${referencedDomainClass.javaClass.simpleName}.yaml".toString()
                    }
                }
            } else { //setup type
                println "  ${prop.name} basic, ${prop} ${prop.type}"
                basicType(jprop, constraints)
            }

            title(jprop, constraints)

            description(jprop, constraints)

            example(jprop, constraints)

            defaults(jprop, mapping, prop.name)

            if(isRequired(jprop, constraints)) required.add(prop.name)

            readOnly(jprop, constraints)

            //minLength
            if (constraints.getMaxSize()) jprop.maxLength = constraints.getMaxSize()
            //maxLength
            if (constraints.getMinSize()) jprop.minLength = constraints.getMinSize()

            if (constraints.getMin() != null) jprop.minimum = constraints.getMin()
            if (constraints.getMax() != null) jprop.maximum = constraints.getMax()
            if (constraints.getScale() != null) jprop.multipleOf = 1 / Math.pow(10, constraints.getScale())

            propsMap[prop.name] = jprop

        }

        //now do the remaining constraints
        println "-- Contrained Non-PersistentProperties --"
        for(String propName : constrainedPropsNames){
            // MetaBeanProperty mbp = EntityMapService.getMetaBeanProp(perEntity.javaClass, propName)
            // Class returnType = mbp.getter.returnType
            def constraints = (DefaultConstrainedProperty) constrainedProps[propName]
            Class returnType = constraints.propertyType
            Map propMap = [:]

            description(propMap, constraints)

            if(Collection.isAssignableFrom(returnType)){
                Class genClass = findGenericClassForCollection(perEntity.javaClass, propName)
                println "  ${propName} collection of type ${genClass.simpleName}"
                collectionType(propMap, genClass)
            } else {
                println "  ${propName} basic ${constraints.propertyType}"
                basicType(propMap, constraints)
            }
            propsMap[propName] = propMap
        }

        def auditStamp = [:]
        //Audit Stamp put in its own so we can append to end of list
        ['createdBy', 'editedBy', 'createdDate', 'editedDate'].each {
            def item = propsMap.remove(it)
            if(item) auditStamp[it] = item
        }
        //def sortedProps = propsMap.sort()
        def p = [:]
        p.putAll(propsMap.sort())
        p.putAll(idVerMap)
        // p.putAll(auditStamp)

        return [props: p, required: required.unique()]
    }

    void description(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        String description = constrainedProp.getMetaConstraintValue("description")
        description = description ?: constrainedProp.getMetaConstraintValue("d")
        if (description) propMap.description = description.stripIndent()
    }

    void defaults(Map propMap, Mapping mapping, String propName){
        String defVal = getDefaultValue(mapping, propName)
        if (defVal != null) propMap.default = defVal //TODO convert to string?
    }

    void readOnly(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        Boolean readOnly = constrainedProp.getMetaConstraintValue("readOnly")
        if (readOnly || constrainedProp.editable == false) propMap.readOnly = true
    }

    boolean isRequired(Map propMap, DefaultConstrainedProperty constrainedProp){
        //required
        Boolean req = constrainedProp.getMetaConstraintValue("required")

        if (!constrainedProp.isNullable() && constrainedProp.editable && req != false) {
            //if it doesn't have a default value and its not a boolean
            if (propMap.default == null && !Boolean.isAssignableFrom(constrainedProp.propertyType)) {
                return true
            }
        }
    }

    void title(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        String title = constrainedProp.getMetaConstraintValue("title")
        if (title) propMap.title = title
    }

    void example(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        String example = constrainedProp.getMetaConstraintValue("example")
        if (example) propMap.example = example
    }

    void basicType(Map propMap, DefaultConstrainedProperty constrainedProp){
        //type
        Map typeFormat = getJsonType(constrainedProp.propertyType)
        propMap.type = typeFormat.type

        //format
        if (typeFormat.format) propMap.format = typeFormat.format
        if (typeFormat.enum) propMap.enum = typeFormat.enum
        //format override from constraints
        if (constrainedProp.format) propMap.format = constrainedProp.format
    }

    void collectionType(Map propMap, Class clazz){
        propMap['type'] = 'array'
        propMap['items'] = [
            '$ref' : "${clazz.simpleName}.yaml".toString()
        ]
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

    @CompileDynamic
    static Class findGenericClassForCollection(Class entityClass, String prop){
        MetaBeanProperty metaProp = EntityMapService.getMetaBeanProp(entityClass, prop)
        CachedMethod gen = metaProp.getter as CachedMethod
        def genericReturnType = gen.cachedMethod.genericReturnType as ParameterizedType
        def actualTypeArguments = genericReturnType.actualTypeArguments
        actualTypeArguments ? actualTypeArguments[0] : null
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
            Map scaffoldProp = ClassUtils.getStaticPropertyValue(domainClass.class, 'scaffold', Map)
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
