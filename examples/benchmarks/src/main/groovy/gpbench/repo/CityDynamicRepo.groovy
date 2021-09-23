package gpbench.repo

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.utils.GormUtils
import gpbench.model.Country
import gpbench.model.Region
import gpbench.model.basic.CityDynamic
import grails.gorm.transactions.Transactional

/**
 * vFuly dynamic compile with liberal use of defs and no typing
 */
@GormRepository
class CityDynamicRepo implements GormRepo<CityDynamic> {

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
