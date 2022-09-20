/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.boot

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.core.AbstractDatastore
import org.grails.datastore.mapping.model.AbstractMappingContext
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity
import yakworks.commons.lang.NameUtils

/**
 * Sets up the spring beans for the GormRepos.
 * The GormRepo annotation should have already put the @Component annotation on it to make it eligible for scanning
 * this will spin through the entities and look for ones that dont have a concerete one setup and
 * make a DefaultGormRepo or UuidGormRepo depending on what setup.
 */
@CompileStatic
class GormRepoBeanFactoryPostProcessor implements BeanFactoryPostProcessor, ApplicationContextAware {

    List<Class> repoClasses
    List<Class> entityClasses

    ApplicationContext applicationContext
    AbstractMappingContext grailsDomainClassMappingContext

    GormRepoBeanFactoryPostProcessor(List<Class> repoClasses, List<Class> entityClasses){
        this.repoClasses = repoClasses
        this.entityClasses = entityClasses
    }

    GormRepoBeanFactoryPostProcessor(AbstractMappingContext grailsDomainClassMappingContext){
        this.grailsDomainClassMappingContext = grailsDomainClassMappingContext
    }

    @Override
    void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory){
        // def entClasses = beanFactory.getBean(AbstractMappingContext).persistentEntities
        entityClasses = grailsDomainClassMappingContext.persistentEntities*.javaClass
        var registry = (BeanDefinitionRegistry) beanFactory

        // for(Class repoClass: repoClasses){
        //     BeanDefinition bdef = BeanDefinitionBuilder.rootBeanDefinition(repoClass).getBeanDefinition()
        //     String beanName = NameUtils.getPropertyName(repoClass.simpleName)
        //     registry.registerBeanDefinition(beanName, bdef)
        // }

        // Map<String, Object> newRepoBeanMap = [:]

        for(Class entityClass: entityClasses){
            String repoName = RepoUtil.getRepoBeanName(entityClass)
            // def hasRepo = repoClasses.find { NameUtils.getPropertyName(it.simpleName) == repoName }
            if (!registry.containsBeanDefinition(repoName)) {
                Class repoClass = DefaultGormRepo
                if(UuidRepoEntity.isAssignableFrom(entityClass)) {
                    repoClass = UuidGormRepo
                }
                // newRepoBeanMap[repoName] = [repoClass, entityClass]
                var bdef = BeanDefinitionBuilder.rootBeanDefinition(repoClass)
                    .addConstructorArgValue(entityClass)
                    .setLazyInit(true)
                    .getBeanDefinition()
                registry.registerBeanDefinition(repoName, bdef)
            }
        }

    }

}
