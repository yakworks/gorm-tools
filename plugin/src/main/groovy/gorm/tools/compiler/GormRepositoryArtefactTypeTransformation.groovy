/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.compiler

import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.compiler.injection.ArtefactTypeAstTransformation

import grails.artefact.Artefact
import grails.plugin.gormtools.RepositoryArtefactHandler

/**
 * A transformation that makes an Artefact a GormRepository
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class GormRepositoryArtefactTypeTransformation extends ArtefactTypeAstTransformation {

    // this gets called from visit. the one in ArtefactTypeAstTransformation super doesn't work as condition check is broken
    @Override
    protected void postProcess(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode, String artefactType) {
        AnnotationNode annotation=new AnnotationNode(new ClassNode(Artefact))
        annotation.addMember("value", new ConstantExpression(artefactType))
        classNode.addAnnotation(annotation)
    }

    @Override
    protected String resolveArtefactType(SourceUnit sourceUnit, AnnotationNode annotationNode, ClassNode classNode) {
        return RepositoryArtefactHandler.TYPE
    }

    @Override
    protected ClassNode getAnnotationType() {
        return new ClassNode(GormRepository)
    }
}
