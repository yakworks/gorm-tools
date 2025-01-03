/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.validation

import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.Synchronized

import org.grails.datastore.gorm.validation.constraints.builder.ConstrainedPropertyBuilder
import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry
import org.grails.datastore.mapping.config.groovy.MappingConfigurationBuilder
import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.core.io.ClassPathResource
import org.yaml.snakeyaml.Yaml

import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import yakworks.commons.lang.ClassUtils
import yakworks.commons.map.Maps

/**
 * A helper to find constraints that can find from yaml and a constraintsMap static block
 * that will also search the trait heirarchy.
 * PersistableRepoEntity overrides getConstraints and calls ApiConstraints.processConstraints to set this up
 * Adds defaults so that
 * - when nullable:false then blank:false, set blank to true in cases where that is needed
 */
@SuppressWarnings(['Println', 'FieldName'])
@CompileStatic
class ApiConstraints {

    public static final String API_CONSTRAINTS = 'constraintsMap'
    public static Map<Class, ApiConstraints> apiConstraintsMap = new ConcurrentHashMap<>()
    //PersistentEntity entity
    Class targetClass
    // these are informational props such as for openapi etc that should not be validated, ie validate: false was set
    // we track these so that docs can be built but these dont get processed for validation
    Map<String, ConstrainedProperty> nonValidatedProperties = [:] as Map<String, ConstrainedProperty>
    //the builder passed to constraints closure. will be either MappingConfigurationBuilder or ConstrainedPropertyBuilder
    Object delegateBuilder
    //if bulder is MappingConfigurationBuilder
    boolean isMappingBuilder = false
    //if its a ConstrainedPropertyBuilder this will get set from its private
    ConstraintRegistry constraintRegistry

    ApiConstraints(Class targetClass) {
        this.targetClass = targetClass
    }

    ApiConstraints(Class targetClass, Object delegateBuilder) {
        this.targetClass = targetClass
        this.delegateBuilder = delegateBuilder
        setBuilderInfo(delegateBuilder)
    }

    @CompileDynamic
    static ApiConstraints findApiConstraints(Class entityClass){
        ApiConstraints theApiCons = apiConstraintsMap.get(entityClass)
        if(!theApiCons){
            theApiCons = new ApiConstraints(entityClass)
            apiConstraintsMap.put(entityClass, theApiCons)
        }
        return theApiCons
    }

    @Synchronized //only one needs to be here as its done once
    @CompileDynamic
    static void processConstraints(Class entityClass, Object builder){
        def ac = new ApiConstraints(entityClass, builder)
        ac.processConstraints()
        apiConstraintsMap.put(entityClass, ac)
    }

    @CompileDynamic //so it can access privates on ConstrainedPropertyBuilder
    void setBuilderInfo(Object delegateBuilder){
        if(delegateBuilder instanceof MappingConfigurationBuilder){
            isMappingBuilder = true //else assume its a ConstrainedPropertyBuilder
        } else if(delegateBuilder instanceof ConstrainedPropertyBuilder){
            //groovy 3.0.11 hack, the `this` is not working in traits and passing wrong class so grab it from the builder
            // targetClass = delegateBuilder.@targetClass
            constraintRegistry = delegateBuilder.@constraintRegistry
            assert constraintRegistry
        }
    }

    void processConstraints(){
        Map consMap = collectContraints()
        processProps(consMap)
    }

    void processProps(Map consMap){
        for (entry in consMap) {
            def attrs = (Map) entry.value
            String prop = (String) entry.key
            if(isMappingBuilder){
                addMappingConstraint(prop, attrs)
            } else {
                addConstraint(prop, attrs)
            }
        }
    }

    void addMappingConstraint(String prop, Map attrs){
        addNullableIfMissing(attrs)
        invokeOnBuilder(prop, attrs)
    }

    void addConstraint(String prop, Map attrs){
        //if validate is false then add it to the nonContrainedProps so it doesn't use it during validation
        if(attrs && attrs['validate'] == false){
            createNonValidated(prop, attrs)
        } else {
            descriptionShortcut(attrs)
            addNullableIfMissing(attrs)
            addStringDefaults(prop, attrs)
            //addBlankFalseIfNullableFalse(prop, attrs)
            invokeOnBuilder(prop, attrs)
        }

    }

    /**
     * calls the default builder to register the constraint
     */
    @CompileDynamic
    void invokeOnBuilder(String prop, Map attrs){
        // builder.createNode(prop, attr)
        // delegate."$prop"(attrs)
        if(isMappingBuilder){
            delegateBuilder.invokeMethod(prop, *[attrs])
        } else {
            delegateBuilder.createNode(prop, attrs)
        }
    }

    /**
     * if its a string and doesn't have maxSize then set to 255 default
     */
    void addMaxSizeIfMissing(DefaultConstrainedProperty cp, Map attr){
        //default string maxSize
        if(String.isAssignableFrom(cp.propertyType) && !cp.maxSize){
            attr.maxSize = 255
        }
    }

    /**
     * if the attr map has a 'd' key shortcut change it to description
     */
    void descriptionShortcut(Map attr){
        String d = attr.remove('d') as String
        if(d) attr.description = d
    }

    /**
     * clean up description short cut
     * if the attr map has a 'd' key shortcut change it to description
     */
    void replaceDescriptionShortcut(List<Map> mergedList){
        //replace the shortcut d: with description before merge
        for (Map item : mergedList) {
            for (entry in item) {
                descriptionShortcut((Map) entry.value)
            }
        }
    }

    /**
     * if nullable is not set then add it to be true
     */
    void addNullableIfMissing(Map attr){
        if(!attr.containsKey('nullable')){
            //make sure we have a default of nullable:true
            attr.nullable = true
        }
    }

    /**
     * if nullable is not set then add it to be true
     * and add blank false if nullable is false
     */
    void addStringDefaults(String propName, Map attr) {
        //only if its a string type
        Class<?> propertyType = determinePropertyType(propName)
        if(propertyType && String.isAssignableFrom(propertyType)){
            //if no maxSize then set to 255
            if(!attr.containsKey('maxSize')){
                attr.maxSize = 255
            }
            //if nullable is false then by default make blank false too
            if(attr.containsKey('nullable') && attr.nullable == false){
                attr.blank = false
            }
        }
    }
    /**
     * if nullable is false then by default make blank false too
     */
    void addBlankFalseIfNullableFalse(String propName, Map attr){
        if(attr.containsKey('nullable') && attr.nullable == false){
            //only if its a string type
            Class<?> propertyType = determinePropertyType(propName)
            if(propertyType && String.isAssignableFrom(propertyType)){
                attr.blank = false
            }
        }
    }

    Map collectContraints(){

        List<Map> classMaps = ClassPropertyFetcher.getStaticPropertyValuesFromInheritanceHierarchy(targetClass, API_CONSTRAINTS, Map)
        List<Map> traitMaps = ClassUtils.getStaticValuesFromTraits(targetClass, API_CONSTRAINTS, Map)
        List<Map> mergedList = traitMaps + classMaps

        Map yamlMap = findYamlApiConfig(targetClass)
        if(yamlMap) mergedList.add(yamlMap)

        replaceDescriptionShortcut(mergedList)

        //with order above, classMaps override traitMaps and yamlMaps override all
        Map<String, Map> mergedMap = mergedList.size() == 1 ? mergedList[0] : Maps.merge([:], mergedList)

        return mergedMap //mergedList
    }

    Map findYamlApiConfig(Class mainClass) {
        def cname = mainClass.simpleName
        def cpr = new ClassPathResource("${cname}Api.yaml", mainClass)
        if(cpr.exists()){
            Yaml yaml = new Yaml()
            Map apiMap = yaml.load(cpr.inputStream)
            def entitySchema = (Map)apiMap[cname]
            def entityProps = entitySchema['properties']
            assert entityProps
            return (Map) entityProps
        }
        return [:]
    }

    DefaultConstrainedProperty createNonValidated(String propName, Map attributes) {
        DefaultConstrainedProperty cp
        if (nonValidatedProperties.containsKey(propName)) {
            cp = (DefaultConstrainedProperty)nonValidatedProperties.get(propName)
        }
        else {
            Class<?> propertyType = determinePropertyType(propName)
            // assume in dynamic use types are strings
            if (!propertyType) propertyType = CharSequence
            cp = new DefaultConstrainedProperty(targetClass, propName, propertyType, constraintRegistry)
            addNonValidatedProperty(propName, cp)
        }

        for (entry in attributes) {
            Object value = entry.value
            String constraintName = (String) entry.key
            if (cp.supportsContraint(constraintName)) {
                cp.applyConstraint(constraintName, value)
            }
            else {
                cp.addMetaConstraint(constraintName, value)
            }
        }
        return cp
    }

    void addNonValidatedProperty(String propName, ConstrainedProperty cprop){
        nonValidatedProperties.put(propName, cprop)
    }

    // DefaultConstrainedProperty initConstrainedProperty(String propertyName, Class propertyType){
    //     return new DefaultConstrainedProperty(targetClass, propertyName, propertyType, constraintRegistry)
    // }

    @CompileDynamic //dynamic so it can called builders determinePropertyType
    Class<?> determinePropertyType(String propertyName) {
        return delegateBuilder.determinePropertyType(propertyName)
    }

}
