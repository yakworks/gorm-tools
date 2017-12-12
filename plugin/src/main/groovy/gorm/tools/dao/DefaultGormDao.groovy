package gorm.tools.dao

import grails.gorm.transactions.Transactional
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEntity
import org.springframework.core.GenericTypeResolver

@CompileStatic
@Transactional
class DefaultGormDao<D extends GormEntity> implements GormDao<D> {

    DefaultGormDao() {
        this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), GormDao.class)
    }

    DefaultGormDao(Class<D> clazz){
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
