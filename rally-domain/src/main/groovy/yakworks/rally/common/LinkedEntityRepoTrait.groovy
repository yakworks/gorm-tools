/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common


import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.transaction.TransactionStatus

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo

/**
 *
 * @param <D> the Link entity
 * @param <I> the linked item entity
 */
@Slf4j
@CompileStatic
trait LinkedEntityRepoTrait<D, I> implements GormRepo<D> {

    abstract I loadItem(Long id)

    abstract String getItemPropName()

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    @SuppressWarnings(['EmptyMethod'])
    void validateCreate(Persistable entity, I item){ }

    Map getKeyMap(long linkedId, String linkedEntity, I item){
        [linkedId: linkedId, linkedEntity: linkedEntity, (getItemPropName()): item]
    }

    D create(Persistable entity, I item, Map args = [:]) {
        validateCreate(entity, item)
        //checkCreditTag(entity.creditInfo, tag)
        create(entity.id, entity.class.simpleName, item, args)
    }

    D create(long linkedId, String linkedEntity, I item, Map args = [:]) {
        def params = getKeyMap(linkedId, linkedEntity, item)
        D leInstance = (D) getEntityClass().newInstance(params)
        Map mArgs = [flush: false, insert: true, failOnError: true]
        mArgs.putAll(args) //overrides
        doPersist leInstance, args
        leInstance
    }

    String getEntityName(Persistable entity){
        entity.class.simpleName
    }

    D get(Persistable entity, I item) {
        queryFor(entity, item).get()
    }

    D get(long linkedId, String linkedEntity, I item) {
        def keyMap = getKeyMap(linkedId, linkedEntity, item)
        Serializable entityKey = (Serializable) getEntityClass().newInstance(keyMap)
        gormStaticApi().get(entityKey)
    }

    MangoDetachedCriteria<D> queryFor(Persistable entity){
        query(linkedId: entity.id, linkedEntity: entity.class.simpleName)
    }

    MangoDetachedCriteria<D> queryFor(Persistable entity, I item){
        queryFor(entity).eq(getItemPropName(), item)
    }

    List<I> listItems(Persistable entity) {
        list(entity).collect {it[getItemPropName()] } as List<I>
    }

    List<Long> listItemIds(Persistable entity) {
        list(entity).collect {it["${getItemPropName()}Id"] as Long }
    }

    List<D> list(Persistable entity) {
        queryFor(entity).list()
    }

    boolean exists(Persistable entity, I item) {
        queryFor(entity, item).count()
    }

    boolean remove(Persistable entity, I item) {
        queryFor(entity, item).deleteAll()
    }

    Integer removeAll(Persistable entity) {
        queryFor(entity).deleteAll() as Integer
    }

    void add(Persistable entity, List<Long> idList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long itemId : idList) {
                create(entity, loadItem(itemId), [:])
            }
        }
    }

    void remove(Persistable entity, List<Long> idList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long itemId : idList) {
                remove(entity, loadItem(itemId))
            }
        }
    }

    void bind(Persistable entity, Object itemParams){
        if(!itemParams) return
        Long linkedId = entity.id

        //default is to replace the tags with whats in tagParams
        if (itemParams instanceof List) {
            def itemList = itemParams as List<Map>
            List<Long> itemParamIds = itemList.collect { it.id as Long }
            List<Long> currentItemIds = listItemIds(entity)

            List<Long> itemsToAdd = itemParamIds - currentItemIds
            add(entity, itemsToAdd)

            List<Long> itemsToRemove = currentItemIds - itemParamIds
            remove(entity, itemsToRemove)

        } else if (itemParams instanceof Map &&  itemParams['op'] == 'remove') {
            removeAll(entity)
        }
    }

}
