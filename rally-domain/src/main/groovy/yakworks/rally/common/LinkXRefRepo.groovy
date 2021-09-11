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
import yakworks.rally.tag.model.Tag
import yakworks.rally.tag.model.TagLink

/**
 * Repo for a linked entity table that is a composite key, many to many.
 * Normal GormRepo doesn't support such things so this is for those.
 * the LinkXref has linkedId, linkedEntity and a field for the "item" that is linked such a Tag.
 *
 * Terminology Example for Tags and Contacts.
 * Linked Entity - This would be the Contact
 * Item or Linked Item - this would the the Tag
 * LinkedXRef - this would the the TagLink that has the many to many.
 *
 * @param <X> the cross ref (LinkXRef) links domain
 * @param <I> the linked Item entity, Tag for example
 */
@Slf4j
@CompileStatic
trait LinkXRefRepo<X, I> extends GormRepo<X> {

    /**
     * the linked item, example: Tag
     */
    abstract I loadItem(Long id)

    /**
     * the property/field name for the linked Item. in TagLink this is "tag"
     */
    abstract String getItemPropName()

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    @SuppressWarnings(['EmptyMethod'])
    void validateCreate(Persistable linkEntity, I item){ }

    /**
     * this is the map that makes the composite key across the 3 fields.
     */
    Map getKeyMap(long linkedId, String linkedEntityName, I item){
        [linkedId: linkedId, linkedEntity: linkedEntityName, (getItemPropName()): item]
    }

    /**
     * Create the LinkedEntity
     *
     * @param linkEntity - the linked entity to grab the id and class.simple name from
     * @param item - the item that is linked to linkEntity
     * @param args - the extra args to pass on through to gorm and gorm repo
     * @return the created LinkXRef
     */
    X create(Persistable linkEntity, I item, Map args = [:]) {
        validateCreate(linkEntity, item)
        create(linkEntity.id, linkEntity.class.simpleName, item, args)
    }

    X create(long linkedId, String linkedEntityName, I item, Map args = [:]) {
        def params = getKeyMap(linkedId, linkedEntityName, item)
        X leInstance = (X) getEntityClass().newInstance(params)
        // default is to givre it insert as a hint
        Map mArgs = [flush: false, insert: true, failOnError: true]
        mArgs.putAll(args) //overrides
        doPersist leInstance, args
        leInstance
    }

    /**
     * return class.simpleName as default
     */
    String getEntityName(Persistable linkedEntity){
        linkedEntity.class.simpleName
    }

    X get(Persistable entity, I item) {
        queryFor(entity, item).get()
    }

    X get(long linkedId, String linkedEntityName, I item) {
        def keyMap = getKeyMap(linkedId, linkedEntityName, item)
        Serializable entityKey = (Serializable) getEntityClass().newInstance(keyMap)
        gormStaticApi().get(entityKey)
    }

    /**
     * a bit different than the GormRepo bind.
     * If itemParams is a list, then its considered the system of record
     *  - will compare items that attached to the linkedEntity, remove whats not in itemParams
     *  - link whats in itemParams thats not there.
     *
     * If itemParams is an object then look for the op
     *  - will compare items that attached to the linkedEntity, remove whats not in itemParams
     *  - link whats in itemParams thats not there.
     *
     * @param linkedEntity the entity that is the main one, such as Contacts or Orgs
     * @param itemParams the data map with id key for what should be synced.
     */
    void bind(Persistable linkedEntity, Object itemParams){
        if(!itemParams) return
        Long linkedId = linkedEntity.id

        //default is to replace the tags with whats in tagParams
        if (itemParams instanceof List) {
            def itemList = itemParams as List<Map>
            List<Long> itemParamIds = itemList.collect { it.id as Long }
            List<Long> currentItemIds = listItemIds(linkedEntity)

            List<Long> itemsToAdd = itemParamIds - currentItemIds
            add(linkedEntity, itemsToAdd)

            List<Long> itemsToRemove = currentItemIds - itemParamIds
            remove(linkedEntity, itemsToRemove)

        } else if (itemParams instanceof Map &&  itemParams['op'] == 'remove') {
            removeAll(linkedEntity)
        }
    }

    /**
     * add the list of items to the linkedEntity, wrapped in a TRX
     * for example, would link a list of tag ids to contact
     * @param linkedEntity - the entity to link to
     * @param itemIdList - the list of ids for the item
     */
    void add(Persistable linkedEntity, List<Long> itemIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long itemId : itemIdList) {
                create(linkedEntity, loadItem(itemId), [:])
            }
        }
    }

    /**
     * remove a list of item ids from the linkXref.
     *
     * @param linkedEntity - the entity to remove the links from
     * @param itemIdList - the list of ids for the item
     */
    void remove(Persistable linkedEntity, List<Long> itemIdList) {
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long itemId : itemIdList) {
                remove(linkedEntity, loadItem(itemId))
            }
        }
    }

    //*** query, find and list methods ***

    boolean exists(I item) {
        queryFor(item).count()
    }

    boolean exists(Persistable linkedEntity) {
        queryFor(linkedEntity).count()
    }

    boolean exists(Persistable linkedEntity, I item) {
        queryFor(linkedEntity, item).count()
    }

    /**
     * list the linked items for the linked entity.
     * for example calling this and passing in Contacts will return the list of tags
     */
    List<I> listItems(Persistable linkedEntity) {
        queryFor(linkedEntity).list().collect {it[getItemPropName()] } as List<I>
    }

    List<Long> listItemIds(Persistable linkedEntity) {
        queryFor(linkedEntity).list().collect {it["${getItemPropName()}Id"] as Long }
    }


    /**
     * query by the item
     */
    MangoDetachedCriteria<X> queryFor(I item){
        query((getItemPropName()): item)
    }

    /**
     * query for and entity
     */
    MangoDetachedCriteria<X> queryFor(Persistable linkedEntity){
        query(linkedId: linkedEntity.id, linkedEntity: linkedEntity.class.simpleName)
    }

    /**
     * query by the composite key
     */
    MangoDetachedCriteria<X> queryFor(Persistable linkedEntity, I item){
        queryFor(linkedEntity).eq(getItemPropName(), item)
    }

    /**
     * removes a specific entry
     */
    boolean remove(Persistable linkedEntity, I item) {
        queryFor(linkedEntity, item).deleteAll()
    }

    /**
     * removes all xref entries for a linked entity
     */
    Integer removeAll(Persistable linkedEntity) {
        queryFor(linkedEntity).deleteAll() as Integer
    }

    /**
     * removes all xref entries for an item
     */
    void removeAll(I item) {
        queryFor(item).deleteAll() as Integer
    }


}
