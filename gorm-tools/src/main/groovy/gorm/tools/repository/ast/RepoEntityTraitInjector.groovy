/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.ast

import groovy.transform.CompileStatic

import org.codehaus.groovy.ast.ClassNode
import org.grails.compiler.injection.GrailsASTUtils
import org.grails.core.artefact.DomainClassArtefactHandler

import gorm.tools.repository.model.RepoEntity
import grails.compiler.ast.SupportsClassNode
import grails.compiler.traits.TraitInjector

/**
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class RepoEntityTraitInjector { //implements TraitInjector, SupportsClassNode {

    // @Override
    // Class getTrait() {
    //     RepoEntity
    // }
    //
    // @Override
    // String[] getArtefactTypes() {
    //     [DomainClassArtefactHandler.TYPE] as String[]
    // }
    //
    // @Override
    // boolean supports(ClassNode classNode) {
    //     return GrailsASTUtils.hasAnnotation(classNode, gorm.tools.repository.RepoEntity)
    // }
}
