/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.ast

import groovy.transform.CompileStatic

import org.grails.core.artefact.DomainClassArtefactHandler

import gorm.tools.repository.GormRepoEntity
import grails.compiler.traits.TraitInjector

/**
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class GormRepoEntityTraitInjector implements TraitInjector {

    @Override
    Class getTrait() {
        GormRepoEntity
    }

    @Override
    String[] getArtefactTypes() {
        [DomainClassArtefactHandler.TYPE] as String[]
    }
}
