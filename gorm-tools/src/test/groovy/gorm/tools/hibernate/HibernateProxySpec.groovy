package gorm.tools.hibernate

import org.hibernate.proxy.HibernateProxy

import spock.lang.Specification
import testing.CustType
import yakworks.testing.gorm.unit.GormHibernateTest


class HibernateProxySpec extends Specification implements GormHibernateTest{
    static entityClasses = [CustType]

    void "simple constructed"() {
        expect:
        def custType = new CustType()
        custType
        custType.metaClass
    }

    void "proxy null and metaClass checks"() {
        setup:
        def custType = build(CustType)
        flushAndClear()

        when:
        def proxy = CustType.load(1)

        then:
        HibernateProxyAsserts.asserNullCheck(proxy)
    }

    void "proxy null and metaClass checks in CompileDynamic"() {
        setup:
        def custType = build(CustType)
        flushAndClear()

        when:
        def proxy = CustType.load(custType.id)

        then:
        HibernateProxyAsserts.asserChecksDynamic(proxy)
    }

    void "load Cust"() {
        when:
        def custType = build(CustType)
        flushAndClear()

        then:
        custType

        when:
        def p = CustType.load(1)
        assert p instanceof HibernateProxy

        then:
        HibernateProxyAsserts.assertIdCheckStaysProxy(p)
        HibernateProxyAsserts.asserNullCheck(p)
        //
        // then:
        // custType2
    }


}
