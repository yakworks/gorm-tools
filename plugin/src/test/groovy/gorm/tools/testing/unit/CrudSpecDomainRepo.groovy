package gorm.tools.testing.unit

import gorm.tools.compiler.GormRepository
import gorm.tools.repository.GormRepo

@GormRepository
class CrudSpecDomainRepo implements GormRepo<CrudSpecDomain> {
    void beforePersist(CrudSpecDomain entity, Map args) {
        if(entity.firstName || entity.lastName) {
            entity.name = (entity.firstName + ' ' + (entity.lastName?:'')).trim()
        }
    }
}
