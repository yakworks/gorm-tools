/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.ast

import java.lang.reflect.Modifier

import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.SourceUnit
import org.grails.compiler.injection.ArtefactTypeAstTransformation
import org.grails.compiler.injection.GrailsAwareInjectionOperation
import org.grails.compiler.injection.TraitInjectionUtils
import org.grails.compiler.web.ControllerActionTransformer
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.datastore.mapping.reflect.AstUtils
import org.grails.plugins.web.rest.transform.LinkableTransform

import grails.artefact.Artefact
import grails.compiler.ast.ClassInjector
import yakworks.commons.lang.NameUtils

import static java.lang.reflect.Modifier.FINAL
import static java.lang.reflect.Modifier.PUBLIC
import static java.lang.reflect.Modifier.STATIC
import static org.grails.compiler.injection.GrailsASTUtils.ZERO_PARAMETERS

@SuppressWarnings(['Println', 'ParameterCount'])
@CompileStatic
class RestApiAstUtils {

    static ConstructorNode addConstructor(ClassNode controllerClassNode, ClassNode domainClassNode, boolean readOnly) {
        BlockStatement constructorBody = new BlockStatement()
        constructorBody.addStatement(new ExpressionStatement(new ConstructorCallExpression(ClassNode.SUPER, new
                TupleExpression(new ClassExpression(domainClassNode), new ConstantExpression(readOnly, true)))))
        controllerClassNode.addConstructor(Modifier.PUBLIC, ZERO_PARAMETERS, ClassNode.EMPTY_ARRAY, constructorBody)
    }

    static makeController(CompilationUnit unit, SourceUnit source, String defaultPackage, String resourceName,
                          ClassNode controllerTraitClassNode, ClassNode entityClassNode, String namespace, boolean readOnly = false){
        final ast = source.getAST()
        //String ctrlPrefixName = entityClassNode.name
        if(!defaultPackage) defaultPackage = NameUtils.getPackageName(entityClassNode.name)
        if(namespace) defaultPackage = "${defaultPackage}.${namespace}"
        String ctrlPrefixName = "${defaultPackage}.${NameUtils.getClassNameFromKebabCase(resourceName)}"
        // if resourceName then convert foo-bar hyphenated to FooBar
        // if(resourceName) {
        //     String entityPackage = NameUtils.getPackageName(entityClassNode.name)
        //     ctrlPrefixName = "${entityPackage}.${NameUtils.getClassNameFromKebabCase(resourceName)}"
        // }
        String className = "${ctrlPrefixName}Controller"
        //String controllerName = "${NameUtils.getClassNameFromKebabCase(resourceName)}Controller"
        // println "making className $className"
        final File resource = findSourceFile(className)
        //if the resource exists in source then dont generate
        if(resource) {
            println "Controller source $className already exists and will not be generated"
            return
        }

        LinkableTransform.addLinkingMethods(entityClassNode)

        final newControllerClassNode = new ClassNode(className, PUBLIC, ClassHelper.OBJECT_TYPE)
        //add the trait
        addTrait(newControllerClassNode, entityClassNode, controllerTraitClassNode)

        // Add the compileStatic
        newControllerClassNode.addAnnotation(new AnnotationNode(ClassHelper.make(CompileStatic)))

        addResponseFormats(newControllerClassNode)
        addNamespace(newControllerClassNode, namespace)
        //addConstructor(newControllerClassNode, entityClassNode, readOnly)

        List<ClassInjector> injectors = ArtefactTypeAstTransformation.findInjectors(ControllerArtefactHandler
            .TYPE, GrailsAwareInjectionOperation.getClassInjectors())

        ArtefactTypeAstTransformation.performInjection(source, newControllerClassNode, injectors.findAll {
            !(it instanceof ControllerActionTransformer)
        })

        if (unit) {
            TraitInjectionUtils.processTraitsForNode(source, newControllerClassNode, 'Controller', unit)
        }

        ArtefactTypeAstTransformation.performInjection(source, newControllerClassNode, injectors.findAll {
            it instanceof ControllerActionTransformer
        })
        // new TransactionalTransform().visit(source, transactionalAnn, newControllerClassNode)
        newControllerClassNode.setModule(ast)

        // add @Artifact(Controller) annotation that marks it as a controller
        final artefactAnnotation = new AnnotationNode(new ClassNode(Artefact))
        artefactAnnotation.addMember("value", new ConstantExpression(ControllerArtefactHandler.TYPE))
        newControllerClassNode.addAnnotation(artefactAnnotation)

        ast.classes.add(newControllerClassNode)

    }

    static void addNamespace(ClassNode controllerNode, String namespace) {
        if (namespace != null) {
            final namespaceField = new FieldNode('namespace', STATIC, ClassHelper.STRING_TYPE,
                controllerNode, new ConstantExpression(namespace))
            controllerNode.addField(namespaceField)
        }
    }

    static void addResponseFormats(ClassNode controllerNode) {
        //right now just hard code json
        ListExpression responseFormatsExpression = new ListExpression()
        responseFormatsExpression.addExpression(new ConstantExpression("json"))
        final publicStaticFinal = PUBLIC | STATIC | FINAL
        controllerNode.addProperty("responseFormats", publicStaticFinal, new ClassNode(List).getPlainNodeReference(), responseFormatsExpression, null, null)
    }

    static void addTrait(ClassNode classNode, ClassNode entityClassNode, ClassNode traitClassNode) {
        //ClassNode traitClassNode = ClassHelper.make(traitClass)
        boolean implementsTrait = false
        boolean traitNotLoaded = false
        try {
            implementsTrait = classNode.declaresInterface(traitClassNode)
        } catch (e) {
            // if we reach this point, the trait injector could not be loaded due to missing dependencies
            // (for example missing servlet-api). This is ok, as we want to be able to compile against non-servlet environments.
            traitNotLoaded = true
        }
        if (!implementsTrait && !traitNotLoaded) {
            final GenericsType[] genericsTypes = traitClassNode.getGenericsTypes()
            final Map parameterNameToParameterValue = [:] as Map<String, ClassNode>
            if(genericsTypes != null) {
                for(GenericsType gt : genericsTypes) {
                    parameterNameToParameterValue.put(gt.getName(), entityClassNode)
                }
            }
            ClassNode traitWithGenerics = AstUtils.replaceGenericsPlaceholders(traitClassNode, parameterNameToParameterValue, entityClassNode)
            classNode.addInterface(traitWithGenerics)
            classNode.setUsingGenerics(true)
        }
    }

    static Map splitPath(String resourceName, Map ctrlConfig){
        Map pathParts = [name: resourceName, namespace: '']
        if (resourceName.contains("/")) {
            List parts = resourceName.split("[/]") as List
            String name = parts.last()
            pathParts['name'] = name
            final int nestedIndex = resourceName.lastIndexOf('/')
            String namespace = resourceName.substring(0, nestedIndex)
            pathParts['namespace'] = namespace
            return pathParts
        } else {
            pathParts['name'] = resourceName
            if(ctrlConfig['namespace']) pathParts['namespace'] = ctrlConfig['namespace'] as String
        }
        return pathParts
    }

    /**
     * Finds a source file for the given class name
     *
     * @param className The class name
     * @return The source file
     */
    static File findSourceFile(String className) {
        //File applicationDir = BuildSettings.BASE_DIR
        String projectDir = System.getProperty("gradle.projectDir", '')
        File applicationDir = new File(projectDir)
        //println "applicationDir $applicationDir"
        File file = null
        if(applicationDir != null) {
            String fileName = className.replace('.' as char, File.separatorChar) + '.groovy'
            List<File> allFiles = [ new File(applicationDir, "src/main/groovy") ]
            File[] files = new File(applicationDir, "grails-app").listFiles(new FileFilter() {
                @Override
                boolean accept(File f) {
                    return f.isDirectory() && !f.isHidden() && !f.name.startsWith('.')
                }
            })
            if(files != null) {
                allFiles.addAll( Arrays.asList(files) )
            }
            for(File dir in allFiles) {
                File possibleFile = new File(dir, fileName)
                //println "looking for $fileName in $dir"
                if(possibleFile.exists()) {
                    file = possibleFile
                    break
                }
            }

        }
        return file
    }
}
