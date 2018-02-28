/* Copyright 2018. 9ci Inc. Licensed under the Apache License, Version 2.0 */
package grails.plugin.gormtools

import gorm.tools.repository.GormRepoEntity
import grails.compiler.traits.TraitInjector
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler

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
