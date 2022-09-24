/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.ast

import javax.inject.Named

import groovy.transform.CompileStatic

import org.apache.groovy.ast.tools.AnnotatedNodeUtils
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.compiler.injection.ArtefactTypeAstTransformation
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

import gorm.tools.repository.GormRepository
import gorm.tools.repository.artefact.RepositoryArtefactHandler
import grails.artefact.Artefact

/**
 * A transformation that makes the GormRepository class into an @Artefact and adds the @Component annotation for scan
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class GormRepositoryArtefactTypeTransformation extends ArtefactTypeAstTransformation {
    public static final ClassNode NAMED_CLASS_NODE = ClassHelper.make(Named)
    public static final ClassNode COMPONENT_CLASS_NODE = ClassHelper.make(Component)
    public static final ClassNode SERVICE_CLASS_NODE = ClassHelper.make(Service)
    public static final AnnotationNode COMPONENT_ANN_NODE = new AnnotationNode(COMPONENT_CLASS_NODE)
    // this gets called from visit. the one in ArtefactTypeAstTransformation super doesn't work as condition check is broken
    @Override
    protected void postProcess(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode, String artefactType) {
        if(!hasAnnotation(classNode, ClassHelper.make(Artefact))) {
            AnnotationNode annotation = new AnnotationNode(new ClassNode(Artefact))
            annotation.addMember("value", new ConstantExpression(artefactType))
            classNode.addAnnotation(annotation)
        }
        //now add the Component annotation
        if(!hasAnnotation(classNode, COMPONENT_CLASS_NODE) && !hasAnnotation(classNode, SERVICE_CLASS_NODE) && !hasAnnotation(classNode, NAMED_CLASS_NODE)){
            classNode.addAnnotation(COMPONENT_ANN_NODE)
        }

    }

    @Override
    protected String resolveArtefactType(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode) {
        return RepositoryArtefactHandler.TYPE
    }

    @Override
    protected ClassNode getAnnotationType() {
        return new ClassNode(GormRepository)
    }

    static boolean hasAnnotation(ClassNode node, ClassNode annotation) {
        return AnnotatedNodeUtils.hasAnnotation(node, annotation)
    }
}
