package gorm.tools.dao

import grails.gorm.transactions.Transactional

@Transactional
class DefaultGormDao<D> implements GormDao<D> {

    DefaultGormDao(Class<D> clazz){
        setDomainClass(clazz)
    }
}
