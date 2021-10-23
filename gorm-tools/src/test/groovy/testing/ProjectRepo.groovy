package testing

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository

@GormRepository
@CompileStatic
class ProjectRepo implements GormRepo<Project> {

}
