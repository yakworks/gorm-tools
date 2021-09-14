/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.common

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.transaction.TransactionStatus

import gorm.tools.databinding.BindAction
import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.DataOp
import yakworks.commons.lang.Validate

import static gorm.tools.utils.GormUtils.collectLongIds

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
trait LinkXRefRepoOld<X, I> extends GormRepo<X> {

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
        doPersist leInstance, mArgs
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
     * a bit different than the GormRepo addOrRemove, this works well for tags where they already exist,
     * but not so good for attachments.
     *
     * If itemParams is a list, then it will spin through and look for the op field in each object
     *
     * If itemParams is an object and has op=replace the uses data to replace
     *  - will compare items that attached to the linkedEntity, remove whats not in itemParams
     *  - link whats in itemParams thats not there.
     *
     * @param linkedEntity the entity that is the main one, such as Contacts or Orgs
     * @param itemParams the data map or list with id key for what should be synced and possible op key for instructions
     */
    List<X> addOrRemove(Persistable linkedEntity, Object itemParams){
        if(!itemParams) return []
        Long linkedId = linkedEntity.id

        Validate.isTrue(itemParams instanceof List || itemParams instanceof Map, "bind data must be map or list: %s", itemParams.class)

        List xlist = [] as List<X>

        //default is to replace the tags with whats in tagParams
        if (itemParams instanceof Map) {
            DataOp op = DataOp.get(itemParams.op)
            List dataList = (List)itemParams.data

            Validate.isTrue(itemParams.data instanceof List)
            if(op == DataOp.replace) {
                xlist = replaceList(linkedEntity, dataList)
            } else {
                throw new UnsupportedOperationException("op=replace is the only supported operation when passing a map for associations")
            }
        } else { //its a list
            xlist = addOrRemoveList(linkedEntity, itemParams as List)
        }
        return xlist
    }

    List<X> replaceList(Persistable linkedEntity, List dataList){
        def itemList = dataList as List<Map>
        // if its empty, then remove all
        if(dataList.isEmpty()) {
            removeAll(linkedEntity)
            return []
        } else {
            List<Long> itemParamIds = collectLongIds(itemList)
            List<Long> currentItemIds = listItemIds(linkedEntity)

            List<Long> itemsToAdd = itemParamIds - currentItemIds
            List xlist = add(linkedEntity, itemsToAdd)

            List<Long> itemsToRemove = currentItemIds - itemParamIds
            remove(linkedEntity, itemsToRemove)
            return xlist
        }

    }

    /**
     * iterates over list and calls the createOrRemove
     * SEE addOrRemove,  This should NOT normally be called directly
     */
    List<X> addOrRemoveList(Persistable linkedEntity, List dataList){
        List xlist = [] as List<X>
        for (Map item : dataList as List<Map>) {
            X xref = createOrRemove(linkedEntity, item)
            if(xref) xlist.add(xref)
        }
        return xlist
    }

    /**
     * Creates link xref from data map or removes xref if data.op = remove
     */
    X createOrRemove(Persistable linkedEntity, Map data){
        Validate.notNull(data.id, "This context requires data map to have an id key")
        X entity
        DataOp op = DataOp.get(data.op) //add, update, delete really only needed for delete
        if(data.id && op == DataOp.remove){
            remove(linkedEntity, loadItem(data.id as Long))
        } else {
            entity = create(linkedEntity, loadItem(data.id as Long), [:])
        }
        return entity
    }

    /**
     * add the list of items to the linkedEntity, wrapped in a TRX
     * for example, would link a list of tag ids to contact
     * @param linkedEntity - the entity to link to
     * @param itemIdList - the list of ids for the item
     */
    List<X> add(Persistable linkedEntity, List<Long> itemIdList) {
        List resultList = [] as List<X>
        gormStaticApi().withTransaction { TransactionStatus status ->
            for (Long itemId : itemIdList) {
                def entity = create(linkedEntity, loadItem(itemId), [:])
                resultList.add(entity)
            }
        }
        return resultList
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
    void removeAllByItem(I item) {
        queryByItem(item).deleteAll() as Integer
    }

    //*** query, find and list methods ***

    boolean exists(I item) {
        queryByItem(item).count()
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
        list(linkedEntity).collect {it[getItemPropName()] } as List<I>
    }

    List<Long> listItemIds(Persistable linkedEntity) {
        list(linkedEntity).collect {it["${getItemPropName()}Id"] as Long }
    }

    List<X> list(I item) {
        queryByItem(item).list()
    }

    List<X> list(Persistable linkedEntity) {
        queryFor(linkedEntity).list()
    }

    /**
     * query by the item
     */
    MangoDetachedCriteria<X> queryByItem(I item){
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


    // ***** some gormRepo methods should not be called with an XRef tables so blow errors for these
    @Override
    X createOrUpdate(Map data){
        throw new UnsupportedOperationException("Method createOrUpdate is not supported by this implementation")
    }

    // ***** some gormRepo methods should not be called with an XRef tables so blow errors for these
    @Override
    void removeById(Serializable id, Map args) {
        throw new UnsupportedOperationException("Method removeById is not supported by this implementation")
    }

    @Override
    void bind(X entity, Map data, BindAction bindAction, Map args) {
        throw new UnsupportedOperationException(
            "Standard Method bind(entity,data,bindAction ) is not supported by this implementation")
    }

    X doUpdate(Map data, Map args) {
        throw new UnsupportedOperationException(
            "Method doUpdate(entity,data,bindAction ) is not supported, these ar immutable and should only ever get inserted or removed")
    }

}
