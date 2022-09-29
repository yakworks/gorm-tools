package yakworks.rally

import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.hibernate.proxy.HibernateProxy

import gorm.tools.model.Persistable
import gorm.tools.utils.GormMetaUtils
import grails.core.support.proxy.ProxyHandler
import yakworks.rally.orgs.model.Org

@CompileStatic
class HibernateProxyAsserts  {

    // static ProxyHandler proxyHandler
    static boolean load() {
        Org.load(2)
    }

    static boolean assertIdCheckStaysProxy(Persistable proxy, ProxyHandler proxyHandler) {

        assert proxy
        if (proxy instanceof HibernateProxy) {
            def hli = ((HibernateProxy) proxy).getHibernateLazyInitializer()
            assert hli.isUninitialized()
        }
        assert !proxyHandler.isInitialized(proxy)

        assert proxy.getId() == GormMetaUtils.getId(proxy as GormEntity)
        // getId should also not unwrap the proxy
        assert !proxyHandler.isInitialized(proxy)

        //this triggers it, seems any method call does it
        // proxy.isAttached()
        // GormMetaUtils.getId(proxy)
        // getId should also not unwrap the proxy
        // !proxyHandler.isInitialized(proxy)
        //def proxyHandler = Org.getGormPersistentEntity().mappingContext.proxyHandler
        // !proxyHandler.isInitialized(proxy)
        // id should also not unwrap the proxy
        // proxy.id
        // !proxyHandler.isInitialized(proxy)
        // !Hibernate.isInitialized(proxy)

        return true
    }

}
