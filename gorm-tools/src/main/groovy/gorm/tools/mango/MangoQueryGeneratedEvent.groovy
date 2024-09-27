/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

//import org.springframework.core.GenericTypeResolver

/**
 * Event fired after the MangoDetachedCriteria has been built.
 * Meaning criteriaMap has been run through tidymap
 *
 */
//WIP
@CompileStatic
class MangoQueryGeneratedEvent<D> extends ApplicationEvent implements ResolvableTypeProvider {

    Class<D> entityClass // the domain class this is for

    MangoDetachedCriteria<D> criteria

    MangoQueryGeneratedEvent(Class<D> entityClass, MangoDetachedCriteria<D> criteria) {
        super(criteria)
        this.entityClass = entityClass
        this.criteria = criteria
    }

    /**
     * done per the spring docs so that listeners can bind to the generic of the event.
     * ex: implements ApplicationListener<BeforeBindEvent<City>>
     * or @EventListener
     *    void beforeBind(BeforeBindEvent<City> event)
     */
    @Override
    ResolvableType getResolvableType() {
        // return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getEntity()))
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forClass(entityClass))
    }

}
