/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.rest.appinfo

import groovy.transform.CompileDynamic

import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.BeanFactory
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.ApplicationContext

import grails.core.GrailsApplication

/**
 * @author <a href='mailto:burt@burtbeckwith.com'>Burt Beckwith</a>
 */
@SuppressWarnings(['FactoryMethodName', 'NoDef'])
@CompileDynamic
class SpringInfoHelper {

    static transactional = false

    GrailsApplication grailsApplication

    /**
     * Partition beans into Controller, Domain, Filter, Service, TagLib, and Other.
     *
     * @return the partitioned beans
     */
    Map splitBeans() {

        ApplicationContext ctx = grailsApplication.mainContext
        def beanFactory = ctx.beanFactory

        def split = [Controller: [],
                     Domain    : [],
                     Filter    : [],
                     Service   : [],
                     TagLib    : []]

        def names = ctx.beanDefinitionNames as List
        for (String name : names) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name)
            if (name.endsWith('ServiceClass')) {
                findServiceBeanName name, names, ctx, beanFactory, split.Service
            } else if (name.endsWith('DomainClass')) {
                findDomainClassBeanName name, names, beanFactory, split.Domain
            } else if (name.endsWith('TagLib')) {
                if (beanDefinition.singleton) {
                    split.TagLib << name
                }
            } else if (name.endsWith('Controller')) {
                if (beanDefinition.prototype) {
                    split.Controller << name
                }
            } else if (name.endsWith('Filters')) {
                if (beanDefinition.singleton) {
                    split.Filter << name
                }
            }
        }

        names.removeAll split.Controller
        names.removeAll split.TagLib
        names.removeAll split.Service
        names.removeAll split.Domain
        names.removeAll split.Filter
        split.Other = names

        split
    }

    /**
     * Service bean names end in 'Service' but not all Spring beans ending in 'Service' are Grails
     * services, so use the '*ServiceClass' beans to find the corresponding service bean.
     *
     * @param serviceClassName the '*ServiceClass' bean name
     * @param names all bean names
     * @param ctx the application context
     * @param beanFactory the BeanFactory
     * @param typeNames service bean names to add to (passed by reference)
     */
    void findServiceBeanName(String serviceClassName, List names, ApplicationContext ctx, BeanFactory beanFactory, List typeNames) {
        String beanName = ctx.getBean(serviceClassName).propertyName
        if (names.contains(beanName)) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName)
            if (beanDefinition.singleton) {
                typeNames << beanName
            }
        }
    }

    /**
     * There's no suffix for domain classes or their bean names, so use the
     * '*DomainClass' beans to find the corresponding domain class bean.
     *
     * @param domainClassName '*DomainClass' bean name
     * @param names all bean names
     * @param beanFactory the BeanFactory
     * @param typeNames domain class bean names to add to (passed by reference)
     */
    void findDomainClassBeanName(String domainClassName, List names, BeanFactory beanFactory, List typeNames) {
        String beanName = domainClassName - 'DomainClass'
        if (names.contains(beanName)) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(beanName)
            if (beanDefinition.prototype) {
                typeNames << beanName
            }
        }
    }

    /**
     * Builds a list of maps containing bean information: name, class name, scope, whether it's lazy,
     * whether it's abstract, its parent bean (may be null), and its BeanDefinition beanClassName.
     *
     * @param names all bean names
     * @param beanFactory the BeanFactory
     * @return the info
     */
    List<Map<String, Object>> getBeanInfo(List names, BeanFactory beanFactory) {
        names.sort()

        List beanDescriptions = []

        for (String name : names) {
            BeanDefinition beanDefinition = beanFactory.getBeanDefinition(name)
            String className = buildClassName(beanFactory, name, beanDefinition)
            beanDescriptions << [name         : name,
                                 className    : className,
                                 scope        : beanDefinition.scope ?: 'singleton',
                                 lazy         : beanDefinition.lazyInit,
                                 isAbstract   : beanDefinition.isAbstract(),
                                 parent       : beanDefinition.parentName,
                                 beanClassName: beanDefinition.beanClassName]
        }

        beanDescriptions
    }

    /**
     * Calculate the class name of a bean, taking into account if it's an abstract bean definition, a
     * proxy, factory, etc.
     *
     * @param beanFactory the BeanFactory
     * @param name the bean name
     * @param beanDefinition the BeanDefinition
     * @return the name
     */
    String buildClassName(BeanFactory beanFactory, String name, BeanDefinition beanDefinition) {

        if (beanDefinition.isAbstract()) {
            return '<i>abstract</i>'
        }

        if (beanDefinition.singleton) {
            def bean = beanFactory.getBean(name)
            if (AopUtils.isAopProxy(bean)) {
                return bean.getClass().name + " (" + AopUtils.getTargetClass(bean).name + ")"
            }
        }

        String beanClassName = beanDefinition.beanClassName
        if (!beanClassName && beanDefinition.factoryBeanName) {
            beanClassName = "Factory: $beanDefinition.factoryBeanName ($beanDefinition.factoryMethodName)"
        }

        beanClassName
    }
}
