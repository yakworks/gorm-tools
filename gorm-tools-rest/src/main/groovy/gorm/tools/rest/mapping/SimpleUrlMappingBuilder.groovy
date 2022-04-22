/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.mapping

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import grails.core.GrailsApplication
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import grails.web.mapping.UrlMapping
import grails.web.mapping.UrlMappingData
import grails.web.mapping.UrlMappingParser
import org.grails.datastore.gorm.validation.constraints.registry.ConstraintRegistry
import org.grails.web.mapping.DefaultUrlMappingParser
import org.grails.web.mapping.RegexUrlMapping
import org.springframework.context.ApplicationContext

import static grails.web.mapping.UrlMapping.CAPTURED_WILDCARD
import static grails.web.mapping.UrlMapping.OPTIONAL_EXTENSION_WILDCARD

@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class SimpleUrlMappingBuilder {
    String contextPath
    UrlMappingParser urlParser = new DefaultUrlMappingParser()

    Object urlMappingBuilder
    ConstraintRegistry constraintRegistry
    GrailsApplication grailsApplication

    boolean appendFormat = true
    String urlPattern
    String controller
    String action
    String namespace
    String httpMethod = 'GET'
    List<String> matchParams = [] as List<String>
    UrlMappingData mappingData
    List<ConstrainedProperty> constrainedProperties
    Map parameters = [:]

    SimpleUrlMappingBuilder(){}

    /**
     * initializes fields from the delegate which is a UrlMappingBuilder.
     * many protected and a private so this is CompileDynamic
     */
    @CompileDynamic //DefaultUrlMappingEvaluator.UrlMappingBuilder is protected
    SimpleUrlMappingBuilder urlMappingBuilder(Object bldr){
        urlMappingBuilder = bldr
        grailsApplication = bldr.getGrailsApplication()
        constraintRegistry = grailsApplication.mainContext.getBean(ConstraintRegistry)
        return this
    }

    static SimpleUrlMappingBuilder of(String contextPath, String namespace, String ctrl){
        def inst = new SimpleUrlMappingBuilder()
        inst.contextPath = contextPath
        inst.namespace = namespace
        inst.controller = ctrl
        return inst
    }

    /**
     * appends to base pattern
     */
    SimpleUrlMappingBuilder pattern(String suffix) {
        urlPattern = namespace ? "${contextPath}/${namespace}/${controller}" : "${contextPath}/${controller}"
        urlPattern = "${urlPattern}${suffix}"
        return this
    }
    /**
     * appends to base pattern
     */
    SimpleUrlMappingBuilder withIdPattern() {
        matchParams.add('id')
        return pattern("/${CAPTURED_WILDCARD}")
    }

    SimpleUrlMappingBuilder _appendFormat() {
        if(!urlPattern.endsWith(OPTIONAL_EXTENSION_WILDCARD)){
            urlPattern = "${urlPattern}${OPTIONAL_EXTENSION_WILDCARD}?"
            matchParams.add('format?')
        }
        return this
    }

    // SimpleUrlMappingBuilder pattern(String uriPattern, List<String> matchProps){
    //     mappingData = urlParser.parse(uriPattern)
    //     matchParams = matchProps
    //     constrainedProperties = matchParams.collect { getConstrainedProperty(it) }
    //     return this
    // }

    ConstrainedProperty getConstrainedProperty(String name, boolean isNullable = false) {
        if(name.endsWith('?')){
            isNullable = true
            name = name - '?'
        }
        ConstrainedProperty newConstrained = new DefaultConstrainedProperty(UrlMapping, name, String, constraintRegistry)
        if(isNullable) newConstrained.applyConstraint(ConstrainedProperty.NULLABLE_CONSTRAINT, true)
        return newConstrained
    }

    void setNullable(ConstrainedProperty constraint) {
        ConstrainedProperty constrainedProperty = constraint
        if(!constrainedProperty.isNullable()) {
            constrainedProperty.applyConstraint(ConstrainedProperty.NULLABLE_CONSTRAINT, true)
        }
    }

    RegexUrlMapping build(Object delegateMappingBuilder){
        urlMappingBuilder(delegateMappingBuilder)
        return build()
    }

    RegexUrlMapping build(){
        //if no string pattern then build it
        if(!urlPattern) pattern('')
        // if appendFormat true(default) then do it
        if(appendFormat) _appendFormat()
        //convert the matchParams to contrained props
        constrainedProperties = matchParams.collect { getConstrainedProperty(it) }
        //now parse the pattern
        mappingData = urlParser.parse(urlPattern)

        def m = new RegexUrlMapping(mappingData, controller, action, namespace, null, null, httpMethod,
            UrlMapping.ANY_VERSION, constrainedProperties as ConstrainedProperty[], grailsApplication)

        parameters['controller'] = controller
        parameters['action'] = action

        m.setParameterValues(parameters)
        addToMappings(m)
        return m
    }

    @CompileDynamic
    void addToMappings(UrlMapping mapping){
        urlMappingBuilder.getUrlMappings().add(mapping)
    }

}
