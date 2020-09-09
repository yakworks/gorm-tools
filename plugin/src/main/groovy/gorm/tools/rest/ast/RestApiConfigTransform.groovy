/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.ast


import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.config.CodeGenConfig
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.rest.controller.RestApiRepoController

//import grails.rest.Resource
//import grails.rest.RestfulController
/**
 * Reads from the restapi yml config and creates rest controllers based on that
 *
 * @author Joshua Burnett
 */
@SuppressWarnings(['ThrowRuntimeException'])
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RestApiConfigTransform implements ASTTransformation, CompilationUnitAware {
    // private static final ClassNode MY_TYPE = new ClassNode(RestApi)
    public static final String ATTR_READY_ONLY = "readOnly"
    public static final String ATTR_SUPER_CLASS = "superClass"
    public static final String ATTR_NAMESPACE = "namespace"
    public static final ClassNode AUTOWIRED_CLASS_NODE = new ClassNode(Autowired).getPlainNodeReference()

    private CompilationUnit unit

    @Override
    void visit(ASTNode[] astNodes, SourceUnit source) {
        if (!(astNodes[0] instanceof AnnotationNode) || !(astNodes[1] instanceof ClassNode)) {
            throw new RuntimeException('Internal error: wrong types: $node.class / $parent.class')
        }

        // this should be set for multi project builds
        String projectDir = System.getProperty("gradle.projectDir", '')
        // add slash
        if (projectDir) projectDir = "${projectDir}/"
        // println "projectDir ${projectDir}"
        def config = new CodeGenConfig()
        config.loadYml(new File("${projectDir}grails-app/conf/restapi-config.yml"))

        Map restApi = config.getProperty('restApi', Map) as Map<String, Map>
        restApi.each { String key, Map val ->
            // Map entry = val as Map
            if (val?.entityClass) {
                generateController(source, key, val)
            }
        }
    }

    void generateController(SourceUnit source, String resourceName, Map ctrlConfig) {
        // Class entityClass = ClassHelper.make(ctrlConfig['entityClass'])
        ClassNode entityClassNode = ClassHelper.make((String)ctrlConfig['entityClass'])

        ClassNode<?> superClassNode
        String superClassName = (String)ctrlConfig['controllerClass']
        if (superClassName) {
            superClassNode = ClassHelper.make(getClass().classLoader.loadClass(superClassName))
        } else {
            superClassNode = ClassHelper.make(RestApiRepoController)
        }
        String namespace = (String)ctrlConfig['controllerClass']
        RestApiTransform.makeController(unit, source, resourceName, superClassNode, entityClassNode, namespace, false)

    }

    // implements CompilationUnitAware
    void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit
    }

}
