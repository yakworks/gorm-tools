/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.proxy

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.hibernate.boot.registry.StandardServiceInitiator
import org.hibernate.boot.registry.StandardServiceRegistryBuilder
import org.hibernate.bytecode.internal.bytebuddy.ByteBuddyState
import org.hibernate.bytecode.internal.bytebuddy.BytecodeProviderImpl
import org.hibernate.bytecode.spi.BytecodeProvider
import org.hibernate.bytecode.spi.ProxyFactoryFactory
import org.hibernate.proxy.pojo.bytebuddy.ByteBuddyProxyHelper
import org.hibernate.service.spi.ServiceContributor
import org.hibernate.service.spi.ServiceRegistryImplementor

/**
 * Most commonly the {@link ProxyFactoryFactory} will depend directly on the chosen {@link BytecodeProvider},
 * however by registering them as two separate services we can allow to override either one
 * or both of them.
 */
@CompileStatic
class GroovyProxyFactoryServiceContributor implements ServiceContributor {
    private static final long serialVersionUID = 1L

    @Override
    void contribute(StandardServiceRegistryBuilder serviceRegistryBuilder) {
        //only register if its not javassist (assumes its bytebuddy)
        if(serviceRegistryBuilder.settings['hibernate.bytecode.provider'] != 'javassist') {
            serviceRegistryBuilder.addInitiator(GroovyProxyFactoryFactoryInitiator.INSTANCE)
        }
    }

    @CompileDynamic
    public static class GroovyProxyFactoryFactoryInitiator implements StandardServiceInitiator<ProxyFactoryFactory> {

        /**
         * Singleton access
         */
        public static final StandardServiceInitiator<ProxyFactoryFactory> INSTANCE = new GroovyProxyFactoryFactoryInitiator();

        @Override
        public ProxyFactoryFactory initiateService(Map configurationValues, ServiceRegistryImplementor registry) {
            BytecodeProviderImpl bytecodeProvider = registry.getService(BytecodeProvider.class) as BytecodeProviderImpl
            ByteBuddyState byteBuddyState = bytecodeProvider.@byteBuddyState
            ByteBuddyProxyHelper byteBuddyProxyHelper = bytecodeProvider.@byteBuddyProxyHelper
            return new GroovyProxyFactoryFactory(byteBuddyState, byteBuddyProxyHelper);
        }

        @Override
        public Class<ProxyFactoryFactory> getServiceInitiated() {
            return ProxyFactoryFactory.class;
        }
    }
}
