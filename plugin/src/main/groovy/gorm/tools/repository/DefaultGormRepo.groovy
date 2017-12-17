package gorm.tools.repository

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.springframework.core.GenericTypeResolver

@CompileStatic
@Transactional
class DefaultGormRepo<D extends GormEntity> implements GormRepo<D> {

    DefaultGormRepo() {
        this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormRepo.class)
    }

    DefaultGormRepo(Class<D> clazz) {
        setDomainClass(clazz)
    }

//    @Override
//    D persist(D entity, Map args) {
//        return doPersist(this, entity, args)
//    }
//
//    @Override
//    D persist(D entity) {
//        return doPersist(entity, [:])
//    }

//    @Override
//    D create(Map params) {
//        gormDaoApi.doCreate(this, params)
//    }
}
