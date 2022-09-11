/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.ast

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

import yakworks.commons.util.BuildSupport
import yakworks.rest.gorm.controller.RestRepoApiController

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
    public static final String ATTR_SUPER_CLASS = "controllerTrait"

    private CompilationUnit compilationUnit

    @Override
    void visit(ASTNode[] astNodes, SourceUnit source) {
        if (!(astNodes[0] instanceof AnnotationNode) || !(astNodes[1] instanceof ClassNode)) {
            throw new RuntimeException('Internal error: wrong types: $node.class / $parent.class')
        }

        // this should be set for multi project builds
        String projectDir = BuildSupport.gradleProjectDir
        // add slash
        if (projectDir) projectDir = "${projectDir}/"
        // println "projectDir ${projectDir}"
        def config = new CodeGenConfig()
        config.loadYml(new File("${projectDir}grails-app/conf/restapi-config.yml"))

        Map restApi = config.getProperty('api', Map) as Map<String, Map>
        String defaultPackage = restApi.defaultPackage as String
        Map namespaces = (Map)restApi.namespaces

        Map paths = restApi.paths as Map<String, Map>
        paths.each { String key, Map val ->
            //if its a namespace iterate on it
            if(namespaces.containsKey(key)){
                String namespace = key
                for(entry in val){
                    String resourceName = "${namespace}/${entry.key}"
                    generateController(source, defaultPackage, resourceName, (Map)entry.value)
                }
            } else { //normal not namespaced
                generateController(source, defaultPackage, key, val)
            }
        }
    }

    void generateController(SourceUnit source, String defaultPackage, String resourceName, Map ctrlConfig) {
        String entityClassName = (String)ctrlConfig['entityClass']
        //exit fast if not entityClassName
        if(!entityClassName) return

        ClassNode entityClassNode
        try {
            //first checks for already compiled classes from libs
            Class entityClass = getClass().classLoader.loadClass((String)ctrlConfig['entityClass'])
            entityClassNode = ClassHelper.make(entityClass)
        } catch(e){
            //looks for source files in the current project
            entityClassNode = compilationUnit.getClassNode(entityClassName)
        }

        ///ClassNode entityClassNode = compilationUnit.getClassNode(entityClassName)
        assert entityClassNode, "entityClass not found with name: ${entityClassName}"

        ClassNode traitNode
        String superClassName = (String)ctrlConfig['controllerTrait']
        if (superClassName) {
            traitNode = ClassHelper.make(getClass().classLoader.loadClass(superClassName))
        } else {
            traitNode = ClassHelper.make(RestRepoApiController)
            //traitNode = ClassHelper.make(RepoController)
        }

        Map pathParts = RestApiAstUtils.splitPath(resourceName, ctrlConfig)
        String endpoint = pathParts.name
        String namespace = pathParts.namespace
        //println "endpoint: $endpoint namespace: $namespace"
        RestApiAstUtils.makeController(compilationUnit, source, defaultPackage, endpoint, traitNode, entityClassNode, namespace, false)

    }

    // implements CompilationUnitAware
    void setCompilationUnit(CompilationUnit unit) {
        this.compilationUnit = unit
    }

}
