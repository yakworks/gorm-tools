/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate

import javax.inject.Inject

import groovy.transform.CompileStatic

import org.grails.orm.hibernate.GrailsHibernateTransactionManager
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanPostProcessor

/**
 * Sets default transaction timeout for GrailsHibernateTransactionManager
 * timeout value can be configured using `spring.transaction.default-timeout`
 */
@CompileStatic
class GormToolsTrxManagerBeanPostProcessor implements BeanPostProcessor {

    @Inject QueryTimeoutConfig queryTimeoutConfig

    /**
     * Sets default transaction timeout for transactions.
     * Default value is -1 which is same as TransactionDefinition.TIMEOUT_DEFAULT
     */
    @Override
    def postProcessAfterInitialization(Object bean, String beanName) {
        if(bean instanceof GrailsHibernateTransactionManager) {
            bean.setDefaultTimeout(queryTimeoutConfig.transaction)
        }
        return bean
    }
}
