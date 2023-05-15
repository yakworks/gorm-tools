package gorm.tools.hibernate

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.hibernate.Hibernate
import org.hibernate.proxy.HibernateProxy
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyInterceptor

import gorm.tools.model.Persistable

@CompileStatic
class HibernateProxyAsserts {

    // static ProxyHandler proxyHandler
    // static boolean load() {
    //     Org.load(2)
    // }

    static boolean assertIdCheckStaysProxy(Persistable proxy) {
        // assert proxy
        assert !Hibernate.isInitialized(proxy)
        if (proxy instanceof HibernateProxy) {
            ByteBuddyInterceptor hli = ((HibernateProxy) proxy).getHibernateLazyInitializer() as ByteBuddyInterceptor
            assert hli.isUninitialized()
        }
        //the getMetaClass is causing the ByteBuddyInterceptor to try and init it.
        if(proxy.getId()){
            assert !Hibernate.isInitialized(proxy)
        }
        if(proxy.id){
            assert !Hibernate.isInitialized(proxy)
        }
        return true
    }

    //makes sure truthy check doesnt hydrate the proxy
    static boolean assertNullCheckNotInit(Persistable proxy) {
        assert !Hibernate.isInitialized(proxy)
        //this get compiled as DefaultTypeTransformation.castToBoolean
        // -> which then calls InvokerHelper.invokeMethod(object, "asBoolean", InvokerHelper.EMPTY_ARGS)
        // -> which gets to InvokerHelper.invokePogoMethod
        // -> then line 1029 calls groovy.getMetaClass().invokeMethod(object, methodName, asArray(arguments));
        // -> that getMetaClass is what ends up triggering the ByteBuddyInterceptor.intercept and it calls getImplementation
        // so this style check will intialize and cause a hit to db
        //if(proxy){

        // this should be all fixed up now that we have hibernate-groovy-proxy
        if(proxy){
            assert !Hibernate.isInitialized(proxy)
        }
        return true
    }

    //checks that the groovy dynamic calls dont initialize it
    @CompileDynamic
    static boolean assertChecksDynamic(Persistable proxy) {
        //first check
        assertNotInitialized(proxy)
        //second check
        assertNotInitialized(proxy)
        //check if its not null
        assert proxy
        //should not init
        assertNotInitialized(proxy)
        //check metaClass prop (no get)
        assert proxy.metaClass
        //should not init
        assertNotInitialized(proxy)

        assert proxy.id
        //should not init
        assertNotInitialized(proxy)

        return true
    }

    static boolean assertNotInitialized(Object proxy){
        assert !Hibernate.isInitialized(proxy)
        return true
    }
}
