package grails.plugin.dao

import gorm.tools.dao.DaoEntity
import grails.compiler.traits.TraitInjector
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler

@CompileStatic
class DaoDomainClassTraitInjector implements TraitInjector {

    @Override
    Class getTrait() {
        DaoEntity
    }

    @Override
    String[] getArtefactTypes() {
        [DomainClassArtefactHandler.TYPE] as String[]
    }
}
