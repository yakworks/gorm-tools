/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.boot

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.AbstractMappingContext
import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.MessageSource

import gorm.tools.validation.RepoValidatorRegistry

/**
 * Sets up the spring beans for the GormRepos.
 * The GormRepo annotation should have already put the @Component annotation on it to make it eligible for scanning
 * this will spin through the entities and look for ones that dont have a concerete repo setup and
 * make a DefaultGormRepo or UuidGormRepo depending on what setup.
 */
@CompileStatic
class GormRepoBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    List<Class> entityClasses

    AbstractMappingContext grailsDomainClassMappingContext

    // @Autowired HibernateDatastore hibernateDatastore
    // @Autowired MessageSource messageSource

    // GormRepoBeanFactoryPostProcessor(List<Class> entityClasses){
    //     this.entityClasses = entityClasses
    // }

    GormRepoBeanFactoryPostProcessor(AbstractMappingContext grailsDomainClassMappingContext){
        this.grailsDomainClassMappingContext = grailsDomainClassMappingContext
        this.entityClasses = grailsDomainClassMappingContext.persistentEntities*.javaClass
    }

    @Override
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory){
        // def entClasses = beanFactory.getBean(AbstractMappingContext).persistentEntities
        // entityClasses = grailsDomainClassMappingContext.persistentEntities*.javaClass
        var registry = (BeanDefinitionRegistry) beanFactory

        // for(Class repoClass: repoClasses){
        //     BeanDefinition bdef = BeanDefinitionBuilder.rootBeanDefinition(repoClass).getBeanDefinition()
        //     String beanName = NameUtils.getPropertyName(repoClass.simpleName)
        //     registry.registerBeanDefinition(beanName, bdef)
        // }

        // Map<String, Object> newRepoBeanMap = [:]

        SpringBeanUtils.registerRepos(registry, entityClasses)

        //RepoValidatorRegistry.init(hibernateDatastore, messageSource)
    }

}
