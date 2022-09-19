/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.utils

import groovy.transform.CompileStatic

import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry

@CompileStatic
class SpringBeanUtils {

    /**
     * Uses the beanDefMap to setup beans. similiar to the BeanBuilder but doesnt require closures.
     * The key of of the beanDefMap is the bean name and the value is the Class or a List.
     * If its a List then the first item is the Class and the remianing items are what to pass to the constructor args.
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
}
