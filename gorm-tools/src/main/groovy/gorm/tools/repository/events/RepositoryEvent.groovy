/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.events

import groovy.transform.CompileStatic

import org.springframework.context.ApplicationEvent
import org.springframework.core.ResolvableType
import org.springframework.core.ResolvableTypeProvider

import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepo
import gorm.tools.repository.PersistArgs

//import org.springframework.core.GenericTypeResolver

/**
 * Base event class for Repository event firing
 * @param D the domain class
 */
@CompileStatic
class RepositoryEvent<D> extends ApplicationEvent implements ResolvableTypeProvider {//extends ApplicationEvent {//

    /** the domain instance this event is for */
    D entity
    /** if this event fired during binding action or a persist that is caused from it then this is the data used */
    Map data
    /** during a binding action, if event trickles down from a bindEvent this will be the name of the BindAction it came from*/
    //String bindActionName
    /** during a binding action or if event trickles down from a bindEvent this will be the BindAction that it came from*/
    BindAction bindAction

    /** the args passed into whatever method fired this. such as flush, failOnError etc */
    PersistArgs args

    /** RepositoryEventType.eventKey. set in constructor. ex: a BeforePersistEvent this will be 'beforePersist' */
    String eventKey //= "repoEvent"

    RepositoryEvent(GormRepo<D> repo, final D entity, String eventKey) {
        super(repo)
        this.entity = entity
        this.eventKey = eventKey
        //this.entity = mappingContext.getPersistentEntity(entityObject.getClass().getName());
    }

    RepositoryEvent(GormRepo<D> repo, final D entity, String eventKey, PersistArgs args) {
        super(repo)
        this.entity = entity
        this.eventKey = eventKey
        this.args = args
        setDataFromArgMap(args)
        //this.entity = mappingContext.getPersistentEntity(entityObject.getClass().getName());
    }

    RepositoryEvent(GormRepo<D> repo, final D entity, String eventKey, Map data, BindAction bindAction, PersistArgs args) {
        super(repo)
        this.entity = entity
        this.eventKey = eventKey
        this.data = data
        this.bindAction = bindAction
        this.args = args
    }

    /**
     * done per the spring docs so that listeners can bind to the generic of the event.
     * ex: implements ApplicationListener<BeforeBindEvent<City>>
     * or @EventListener
     *    void beforeBind(BeforeBindEvent<City> event)
     */
    @Override
    ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(getEntity()))
    }

    /**
     * @return the routing key in the form of "DomainClass.eventMethod", for example "City.afterPersist"
     */
    String getRoutingKey() { "${entity.class.simpleName}.${eventKey}" }

    void setDataFromArgMap(PersistArgs args){
        this.data = args.data ?: [:]
        this.bindAction = args.bindAction ?: null
    }

    boolean isBindCreate(){
        BindAction.Create == bindAction
    }

    boolean isBindUpdate(){
        BindAction.Update == bindAction
    }
}
