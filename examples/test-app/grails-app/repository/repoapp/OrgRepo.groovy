package repoapp

import gorm.tools.repository.DefaultGormRepo
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional

@Transactional
@GrailsCompileStatic
class OrgRepo extends DefaultGormRepo<Org> {
    //Class entityClass = Org


}
