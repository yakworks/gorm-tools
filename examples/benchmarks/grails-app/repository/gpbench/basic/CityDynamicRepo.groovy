package gpbench.basic

import gorm.tools.utils.GormUtils
import gorm.tools.repository.DefaultGormRepo
import gpbench.Country
import gpbench.Region
import grails.gorm.transactions.Transactional

/**
 * vFuly dynamic compile with liberal use of defs and no typing
 */
@Transactional
class CityDynamicRepo extends DefaultGormRepo<CityDynamic> {

    def bindWithCopyDomain(Map row) {
        def r = Region.load(row['region']['id'] as Long)
        def country = Country.load(row['country']['id'] as Long)
        def c = entityClass.newInstance()
        GormUtils.copyDomain(c, row)
        c.region = r
        c.country = country
        return c
    }

    //See at the bottom why we need this doXX methods
    def insert(row, args) {
        def entity
        if (args.dataBinder == 'grails') {
            entity = entityClass.newInstance()
            entity.properties = row
        } else if (args.dataBinder == 'copy') {
            entity = bindWithCopyDomain(row)
        } else {
            entity = "${args.dataBinder}"(row)
        }
        if (fireEvents) super.beforeInsertSave(entity, row)
        super.save(entity, [validate: args.validate ?: false])
        //RepoMessage.created(entity) slows it down by about 15-20%
        return entity //[ok: true, entity: entity, message: RepoMessage.created(entity)]
    }

}
