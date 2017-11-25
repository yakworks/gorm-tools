package gorm.tools.dao

import grails.gorm.transactions.Transactional
import org.grails.datastore.gorm.GormEntity

@Transactional
class DefaultGormDao<D extends GormEntity> implements GormDao<D> {

    DefaultGormDao(Class<D> clazz){
        setDomainClass(clazz)
    }
}
