/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.spock

import groovy.transform.CompileStatic

import org.spockframework.runtime.extension.AbstractMethodInterceptor
import org.spockframework.runtime.extension.IMethodInvocation
import org.springframework.transaction.interceptor.DefaultTransactionAttribute

import yakworks.spring.AppCtx
import yakworks.testing.gorm.unit.GormHibernateTest

/**
 * AbstractMethodInterceptor is helper base clase so we can keep all the logic in one and it does the switch.
 * much easier and more reliable to do setup/cleaup here than trying to use annotations on a trait.
 */
@CompileStatic
class GormHibernateTestInterceptor extends AbstractMethodInterceptor {

    @Override
    void interceptSetupSpecMethod(IMethodInvocation invocation){
        def testInstance =  invocation.instance as GormHibernateTest
        testInstance.setupHibernate()
        invocation.proceed()
    }

    @Override
    void interceptSetupMethod(IMethodInvocation invocation) throws Throwable {
        def testInstance =  invocation.sharedInstance as GormHibernateTest
        testInstance.transactionStatus = testInstance.getTransactionManager().getTransaction(new DefaultTransactionAttribute())
        testInstance.autowire()
        invocation.proceed()
    }

    @Override
    void interceptCleanupMethod(IMethodInvocation invocation) throws Throwable {
        def testInstance =  invocation.sharedInstance as GormHibernateTest
        if (testInstance.isRollback()) {
            testInstance.transactionManager.rollback(testInstance.transactionStatus)
        } else {
            testInstance.transactionManager.commit(testInstance.transactionStatus)
        }
        invocation.proceed()
    }

    @Override
    void interceptCleanupSpecMethod(IMethodInvocation invocation) throws Throwable {
        AppCtx.setApplicationContext(null)
        invocation.proceed()
    }

}
