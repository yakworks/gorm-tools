/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.boot

import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.RootBeanDefinition
import org.springframework.core.ResolvableType

import gorm.tools.repository.DefaultGormRepo
import gorm.tools.repository.GormRepo
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
     * NOTE: lazy=false by default as its assumed you want them ready in tests.
     */
    static void registerBeans(BeanDefinitionRegistry beanDefinitionRegistry, Map<String, Object> beanMap) {
        for (String beanName : beanMap.keySet()) {
            Object val = beanMap.get(beanName)
            BeanDefinition bdef
            if(val instanceof Class){
                bdef = BeanDefinitionBuilder.rootBeanDefinition(val).setLazyInit(false).getBeanDefinition()
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
                bdef = bdb.setLazyInit(false).getBeanDefinition()
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
    static <D> void registerRepos(BeanDefinitionRegistry registry, List<Class<D>> entityClasses) {
        for(Class<D> entityClass: entityClasses){
            String repoName = RepoLookup.getRepoBeanName(entityClass)
            // def hasRepo = repoClasses.find { NameUtils.getPropertyName(it.simpleName) == repoName }
            // look for Entities that dont have a Repo registered.
            if (!registry.containsBeanDefinition(repoName)) {
                //if its not found then set a default one up.
                RootBeanDefinition bdef

                //var repoClass = DefaultGormRepo as Class<DefaultGormRepo<D>>
                if(UuidRepoEntity.isAssignableFrom(entityClass)) {
                    bdef = BeanDefinitionBuilder.rootBeanDefinition(UuidGormRepo<D>)
                        .addConstructorArgValue(entityClass)
                        .setLazyInit(true)
                        .getBeanDefinition() as RootBeanDefinition
                    bdef.setTargetType(ResolvableType.forClassWithGenerics(GormRepo, entityClass))
                } else {
                    bdef = BeanDefinitionBuilder.rootBeanDefinition(DefaultGormRepo<D>)
                        .addConstructorArgValue(entityClass)
                        .setLazyInit(true)
                        .getBeanDefinition() as RootBeanDefinition
                    bdef.setTargetType(ResolvableType.forClassWithGenerics(GormRepo, entityClass))
                }

                //FIXME seems not matter what we do above, we can get the generic on the DefaultGormRepo

                // newRepoBeanMap[repoName] = [repoClass, entityClass]
                // var bdef = BeanDefinitionBuilder.rootBeanDefinition(repoClass)
                //     .addConstructorArgValue(entityClass)
                //     .setLazyInit(true)
                //     .getBeanDefinition()

                // var rt = ResolvableType.forClassWithGenerics(DefaultGormRepo, entityClass)
                // var bdefBuilder = BeanDefinitionBuilder.rootBeanDefinition(rt, () -> DefaultGormRepo.of(entityClass))
                //     //.addConstructorArgValue(entityClass)
                // bdefBuilder.beanDefinition.setBeanClass(DefaultGormRepo)
                //
                // var bdef = bdefBuilder.setLazyInit(true)
                //     .getBeanDefinition()
                // var bdef = BeanDefinitionBuilder.rootBeanDefinition(repoClass)
                //     .addConstructorArgValue(entityClass)
                //     .setLazyInit(true)
                //     .getBeanDefinition()

                registry.registerBeanDefinition(repoName, bdef)
            }
        }

    }

}
