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
