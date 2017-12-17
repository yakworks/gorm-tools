package repoapp

import gorm.tools.repository.DefaultGormRepo
import grails.compiler.GrailsCompileStatic
import groovy.transform.CompileDynamic

@GrailsCompileStatic
class OrgDelegateRepo extends DefaultGormRepo<Org> {

    @Override
    @CompileDynamic
    Org create(Map params) {
        if (!params.name) {
            params.name = "default"
        }
        super.create(params)
    }
}

