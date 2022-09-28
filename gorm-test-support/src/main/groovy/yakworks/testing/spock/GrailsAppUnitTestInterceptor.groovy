/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.spock

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.springframework.beans.factory.config.AutowireCapableBeanFactory

import yakworks.spring.AppCtx

/**
 * AbstractMethodInterceptor is helper base clase so we can keep all the logic in one and it does the switch.
 * much easier and more reliable to do setup/cleaup here than trying to use annotations on a trait.
 */
@CompileStatic
class GrailsAppUnitTestInterceptor extends AbstractMethodInterceptor {

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        autowire(invocation.instance)
        invocation.proceed()
    }

    @Override
    void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        //null out the AppCtx
        AppCtx.setApplicationContext(null)
        invocation.proceed()
    }

    @CompileDynamic
    void autowire(Object testInstance) {
        AutowireCapableBeanFactory beanFactory = testInstance.applicationContext.autowireCapableBeanFactory
        beanFactory.autowireBean testInstance
        // AutowireCapableBeanFactory beanFactory = testInstance.applicationContext.autowireCapableBeanFactory
        // beanFactory.autowireBeanProperties testInstance, AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE, false
    }

}
