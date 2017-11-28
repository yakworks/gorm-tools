package gorm.tools.dao

import grails.gorm.transactions.Transactional
import org.grails.datastore.gorm.GormEntity
import org.springframework.core.GenericTypeResolver

@Transactional
class DefaultGormDao<D extends GormEntity> implements GormDao<D> {

    DefaultGormDao() {
        this.domainClass = (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), DefaultGormDao.class)
    }

    DefaultGormDao(Class<D> clazz){
        setDomainClass(clazz)
    }
}
