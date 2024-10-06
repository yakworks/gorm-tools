package gorm.tools.mango

import javax.persistence.criteria.JoinType

import gorm.tools.mango.hibernate.PathKeyMapPagedList
import gorm.tools.model.SourceType
import spock.lang.Ignore
import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.unit.GormHibernateTest

import static gorm.tools.mango.jpql.JpqlCompareUtils.formatAndStrip

/**
 * Check in method missing is adding an extra count query. This smoke tests that
 */
class MangoExtraCountSpec extends Specification implements GormHibernateTest  {

    static List entityClasses = [KitchenSink, SinkItem, Thing]

    boolean compareQuery(String hql, String expected){
        assert formatAndStrip(hql) == formatAndStrip(expected)
        return true
    }

    void setupSpec(){
        KitchenSink.withTransaction {
            KitchenSink.repo.createKitchenSinks(10)
            def ks1 = KitchenSink.get(1)
            def list = KitchenSink.list()
            assert list
            assert KitchenSink.findWhere(num: '1')
            //set one to have null thing so that when we sort etc we can test the join
            ks1.thing = null
            ks1.persist()
            flushAndClear()
            assert KitchenSink.get(1).thing == null

            def ks = KitchenSink.get(2)
            ks.ext.thing.country = 'ME'
            ks.ext.thing.persist(flush: true)
        }

    }

    @Ignore //XXX turn SQL logging on, generates a messed up sql with 1=1 in it.
    void "gets it to fire an extra count query when its building"() {
        when:"🎯props are set on the criteria"

        def criteria = KitchenSink.query {
            //eq('ext.thing.name', 'Thing1')
            or {
                eq('ext.thing.name', 'Thing1')
                eq('ext.thing.country', "ME")
            }
        }

        List list = criteria.list()

        then:
        list.size() == 2

    }

    void "mango map gets it to fire an extra count query when its building"() {
        when:"🎯props are set on the criteria"
        def criteria = KitchenSink.query([
            // 'ext.thing':[
            //     $or: [
            //         'name': 'Thing1',
            //         'id': 3
            //     ]
            // ]
            $or: [
                ['ext.thing.name': 'Thing1'],
                ['ext.thing.country': "ME"]
            ]
        ])

        List list = criteria.list()

        then:
        list.size() == 2

    }

}
