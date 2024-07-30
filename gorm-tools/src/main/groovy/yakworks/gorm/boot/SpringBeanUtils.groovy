/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.boot

import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.model.UuidGormRepo
import gorm.tools.repository.model.UuidRepoEntity

@CompileStatic
class SpringBeanUtils {

    /**
     * Uses the beanDefMap to setup beans. Similiar to the BeanBuilder but doesnt require closures.
     * This is used in Unit tests when the springBeans property is set.
     * The key of of the beanDefMap is the bean name and the value is the Class or a List.
     * If its a List then the first item is the Class and the remianing items are what to pass to the constructor args.
     * If its start with an `@` then its set as a bean reference, liek you would do with `ref("foo")`.
     *
     */
    static void registerBeans(BeanDefinitionRegistry beanDefinitionRegistry, Map<String, Object> beanMap) {
        for (String beanName : beanMap.keySet()) {
            Object val = beanMap.get(beanName)
            BeanDefinition bdef
            if(val instanceof Class){
                bdef = BeanDefinitionBuilder.rootBeanDefinition(val).setLazyInit(true).getBeanDefinition()
            } else if (val instanceof List){
                Class beanClass = val.pop()
                def bdb = BeanDefinitionBuilder.rootBeanDefinition(beanClass)
                val.each{ arg ->
                    if(arg instanceof String && arg.startsWith("@")){
                        bdb.addConstructorArgReference(arg.substring(1))
                    } else {
                        bdb.addConstructorArgValue(arg)
                    }
                }
                bdef = bdb.setLazyInit(true).getBeanDefinition()
            } else {
                throw new IllegalArgumentException("bean map value must either be a class or a list where arg[0] is class and the rest are const args")
            }
            beanDefinitionRegistry.registerBeanDefinition(beanName, bdef)
        }
    }

    /**
     * creates the GormRepo bean definitions in the registry for the entityClasses.
     * Checks to see if a bean name for the repo already exists and does nothing if so.
     * Otherwise registers a DefaultGormRepo or UuidGormRepo depending interfaces assigned to entityClass
     */
    static void registerRepos(BeanDefinitionRegistry registry, List<Class<?>> entityClasses) {
        for(Class entityClass: entityClasses){
            String repoName = RepoLookup.getRepoBeanName(entityClass)
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
