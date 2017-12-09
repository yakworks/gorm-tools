package gpbench

import gorm.tools.GormUtils
import gorm.tools.dao.DefaultGormDao
import grails.gorm.transactions.Transactional

/**
 *vFuly dynamic compile with liberal use of defs and no typing
 */
@Transactional
class CityDynamicDao extends DefaultGormDao<CityDynamic> {

    def bindWithCopyDomain(Map row) {
        def r = Region.load(row['region']['id'] as Long)
        def country = Country.load(row['country']['id'] as Long)
        def c = domainClass.newInstance()
        GormUtils.copyDomain(c, row)
        c.region = r
        c.country = country
        return c
    }

    //See at the bottom why we need this doXX methods
    def insert( row, args) {
        def entity
        if(args.dataBinder == 'grails'){
            entity = domainClass.newInstance()
            entity.properties = row
        }
        else if(args.dataBinder == 'copy'){
            entity = bindWithCopyDomain(row)
        }
        else {
            entity = "${args.dataBinder}"(row)
        }
        if (fireEvents) super.beforeInsertSave(entity, row)
        super.save(entity, [validate: args.validate?:false ])
        //DaoMessage.created(entity) slows it down by about 15-20%
        return entity //[ok: true, entity: entity, message: DaoMessage.created(entity)]
    }

}
