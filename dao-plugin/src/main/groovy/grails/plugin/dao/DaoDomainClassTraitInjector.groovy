package grails.plugin.dao

import grails.compiler.traits.TraitInjector
import groovy.transform.CompileStatic
import org.grails.core.artefact.DomainClassArtefactHandler

@CompileStatic
class DaoDomainClassTraitInjector implements TraitInjector {

	@Override
	Class getTrait() {
		DaoDomainTrait
	}

	@Override
	String[] getArtefactTypes() {
		[DomainClassArtefactHandler.TYPE] as String[]
	}
}