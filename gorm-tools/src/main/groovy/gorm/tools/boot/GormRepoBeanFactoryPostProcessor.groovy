/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.boot

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.AbstractMappingContext
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity

/**
 * Sets up the spring beans for the GormRepos.
 * The GormRepo annotation should have already put the @Component annotation on it to make it eligible for scanning
 * this will spin through the entities and look for ones that dont have a concerete one setup and
 * make a DefaultGormRepo or UuidGormRepo depending on what setup.
 */
@CompileStatic
class GormRepoBeanFactoryPostProcessor implements BeanFactoryPostProcessor {

    List<Class> entityClasses

    AbstractMappingContext grailsDomainClassMappingContext

    GormRepoBeanFactoryPostProcessor(List<Class> entityClasses){
        this.entityClasses = entityClasses
    }

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

        for(Class entityClass: entityClasses){
            String repoName = RepoUtil.getRepoBeanName(entityClass)
            // def hasRepo = repoClasses.find { NameUtils.getPropertyName(it.simpleName) == repoName }
            // look for Entities that dont have a Repo registered.
            if (!registry.containsBeanDefinition(repoName)) {
                //if its not found then set a default one up.
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
