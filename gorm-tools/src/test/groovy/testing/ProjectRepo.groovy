package testing

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.bulk.BulkableRepo
import groovy.transform.CompileStatic

@GormRepository
@CompileStatic
class ProjectRepo implements GormRepo<Project>, BulkableRepo<Project, JobImpl> {

}
