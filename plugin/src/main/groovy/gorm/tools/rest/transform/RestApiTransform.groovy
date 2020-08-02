/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.transform

import java.lang.reflect.Modifier

import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.ConstructorNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.TupleExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.compiler.injection.ArtefactTypeAstTransformation
import org.grails.compiler.injection.GrailsASTUtils
import org.grails.compiler.injection.GrailsAwareInjectionOperation
import org.grails.compiler.injection.TraitInjectionUtils
import org.grails.compiler.web.ControllerActionTransformer
import org.grails.core.artefact.ControllerArtefactHandler
import org.grails.plugins.web.rest.transform.LinkableTransform
import org.springframework.beans.factory.annotation.Autowired

import gorm.tools.rest.RestApi
import gorm.tools.rest.controller.RestApiRepoController
import grails.artefact.Artefact
import grails.compiler.ast.ClassInjector
import grails.io.IOUtils
import grails.util.GrailsNameUtils

import static java.lang.reflect.Modifier.FINAL
import static java.lang.reflect.Modifier.PUBLIC
import static java.lang.reflect.Modifier.STATIC
import static org.grails.compiler.injection.GrailsASTUtils.ZERO_PARAMETERS

//import grails.rest.Resource
//import grails.rest.RestfulController
/**
 * The  transform automatically exposes a domain class as a RESTful resource. In effect the transform adds a
 * controller to a Grails application
 * that performs CRUD operations on the domain. See the grails Resource annotation for more details
 *
 *
 * This is modified from {@link org.grails.plugins.web.rest.transform.ResourceTransform}
 * to use the RestApiController and get rid of the bits that mess with the URL mapping
 *
 * @author Joshua Burnett
 * @author Graeme Rocher
 *
 */
// @SuppressWarnings(['VariableName', 'AbcMetric', 'ThrowRuntimeException', 'MethodSize'])
@SuppressWarnings(['ThrowRuntimeException'])
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class RestApiTransform implements ASTTransformation, CompilationUnitAware {
    private static final ClassNode MY_TYPE = new ClassNode(RestApi)
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

        ClassNode entityClassNode = (ClassNode) astNodes[1]
        // String entityClassName = entityClassNode.name
        // final entityClassPropName = GrailsNameUtils.getPropertyName(entityClassName)

        // println "RestApiTransform ${parent.name}"
        AnnotationNode annotationNode = (AnnotationNode) astNodes[0]
        if (MY_TYPE != annotationNode.getClassNode()) {
            return
        }
        final ast = source.getAST()

        ClassNode<?> superClassNode
        Expression superClassAttribute = annotationNode.getMember(ATTR_SUPER_CLASS)
        if (superClassAttribute instanceof ClassExpression) {
            superClassNode = ((ClassExpression) superClassAttribute).getType()
        } else {
            superClassNode = ClassHelper.make(RestApiRepoController)
        }
        // get annotation vals
        final readOnlyAttr = annotationNode.getMember(ATTR_READY_ONLY)
        boolean isReadOnly = readOnlyAttr != null && ((ConstantExpression) readOnlyAttr).trueExpression
        // final responseFormatsAttr = annotationNode.getMember(ATTR_RESPONSE_FORMATS)
        // final uriAttr = annotationNode.getMember(ATTR_URI)
        final namespaceAttr = annotationNode.getMember(ATTR_NAMESPACE)
        String namespace = namespaceAttr?.getText()
        // final domainPropertyName = GrailsNameUtils.getPropertyName(entityClassNode.getName())

        makeController(unit, source, null, superClassNode, entityClassNode, namespace, isReadOnly)

    }

    static ConstructorNode addConstructor(ClassNode controllerClassNode, ClassNode domainClassNode, boolean readOnly) {
        BlockStatement constructorBody = new BlockStatement()
        constructorBody.addStatement(new ExpressionStatement(new ConstructorCallExpression(ClassNode.SUPER, new
                TupleExpression(new ClassExpression(domainClassNode), new ConstantExpression(readOnly, true)))))
        controllerClassNode.addConstructor(Modifier.PUBLIC, ZERO_PARAMETERS, ClassNode.EMPTY_ARRAY, constructorBody)
    }

    // implements CompilationUnitAware
    void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit
    }

    static makeController(CompilationUnit unit, SourceUnit source, String resourceName, ClassNode superClassNode, ClassNode entityClassNode,
                        String namespace, boolean readOnly = false){
        final ast = source.getAST()
        String ctrlPrefixName = entityClassNode.name
        // if resourceName then convert foo-bar hyphenated to FooBar
        if(resourceName) {
            String entityPackage = GrailsNameUtils.getPackageName(entityClassNode.name)
            ctrlPrefixName = "${entityPackage}.${GrailsNameUtils.getNameFromScript(resourceName)}"
        }
        String className = "${ctrlPrefixName}Controller"
        println "making className $className"
        final File resource = IOUtils.findSourceFile(className)
        LinkableTransform.addLinkingMethods(entityClassNode)

        if (resource == null) {

            def superClassNodeGeneric = GrailsASTUtils.replaceGenericsPlaceholders(superClassNode, [D: entityClassNode])
            final newControllerClassNode = new ClassNode(className, PUBLIC, superClassNodeGeneric)
            // Add the compileStatic
            newControllerClassNode.addAnnotation(new AnnotationNode(ClassHelper.make(CompileStatic)))

            addConstructor(newControllerClassNode, entityClassNode, readOnly)

            List<ClassInjector> injectors = ArtefactTypeAstTransformation.findInjectors(ControllerArtefactHandler
                .TYPE, GrailsAwareInjectionOperation.getClassInjectors())

            ArtefactTypeAstTransformation.performInjection(source, newControllerClassNode, injectors.findAll {
                !(it instanceof ControllerActionTransformer)
            })

            if (unit) {
                TraitInjectionUtils.processTraitsForNode(source, newControllerClassNode, 'Controller', unit)
            }

            // ListExpression responseFormatsExpression = new ListExpression()
            // boolean hasHtml = false
            // if (responseFormatsAttr != null) {
            //     if (responseFormatsAttr instanceof ConstantExpression) {
            //         if (responseFormatsExpression.text.equalsIgnoreCase('html')) {
            //             hasHtml = true
            //         }
            //         responseFormatsExpression.addExpression(responseFormatsAttr)
            //     } else if (responseFormatsAttr instanceof ListExpression) {
            //         responseFormatsExpression = (ListExpression) responseFormatsAttr
            //         for (Expression expr in responseFormatsExpression.expressions) {
            //             if (expr.text.equalsIgnoreCase('html')) hasHtml = true; break
            //         }
            //     }
            // } else {
            //     responseFormatsExpression.addExpression(new ConstantExpression("json"))
            //     //responseFormatsExpression.addExpression(new ConstantExpression("xml"))
            // }

            if (namespace != null) {
                final namespaceField = new FieldNode('namespace', STATIC, ClassHelper.STRING_TYPE,
                    newControllerClassNode, new ConstantExpression(namespace))
                newControllerClassNode.addField(namespaceField)
            }

            final publicStaticFinal = PUBLIC | STATIC | FINAL

            // newControllerClassNode.addProperty("scope", publicStaticFinal, ClassHelper.STRING_TYPE, new
            //         ConstantExpression("singleton"), null, null)
            // newControllerClassNode.addProperty("responseFormats", publicStaticFinal, new ClassNode(List)
            //         .getPlainNodeReference(), responseFormatsExpression, null, null)

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
    }
}
