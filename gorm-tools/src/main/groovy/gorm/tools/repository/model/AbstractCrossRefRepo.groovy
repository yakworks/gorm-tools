/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.runtime.InvokerHelper

import gorm.tools.databinding.BindAction
import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.RepoUtil
import yakworks.commons.lang.Validate

import static gorm.tools.utils.GormUtils.collectLongIds
import static gorm.tools.utils.GormUtils.listToIdMap

/**
 * XRef Repo for linking 2 entities. Also called a join table in hibernate.
 * One class is designated as the main and the other as related.
 * The 'main' is the first part the name, and related the second.
 * So for example: ActivityContact xref table, the Activity is the 'main' and the Contact is the 'related'
 * Its really arbitraty but need way to grok it
 *
 * @param <X> the cross ref domain
 * @param <P> the the main entity that will have the items as "children"
 * @param <L> the related entity
 */
@Slf4j
@CompileStatic
abstract class AbstractCrossRefRepo<X, P extends Persistable, R extends Persistable> implements GormRepo<X> {
    Class<P> mainClass
    Class<R> relatedClass
    List<String> propNames

    protected AbstractCrossRefRepo(Class<P> mainClazz, Class<R> relatedClazz, List<String> propKeys){
        mainClass = mainClazz
        relatedClass= relatedClazz
        propNames = propKeys
    }

    /**
     * in implementation will search for fields and call L.load if it has an id
     * but can do look ups such as for code or sourceId if no id is provided
     */
    Persistable lookup(Class clazz, Object data){
        //FIXME make a generic way to lookup id and code, for now only loads by id
        Long aid = data['id'] as Long
        return InvokerHelper.invokeStaticMethod(clazz, 'load', aid) as Persistable
    }

    String getMainPropName(){
        propNames[0]
    }

    String getRelatedPropName(){
        propNames[1]
    }

    /**
     * override in implementation to throw IllegalArgumentException if the tag.entityName does not match
     */
    void validateCreate(P main, R related){
        //by default check that ids are set when creating, no proxys as they just caue problems in this context
        Validate.notNull(main.id, "main entity.id can't be null: %s", mainClass )
        Validate.notNull(related.id, "related entity.id can't be null: %s", relatedClass)
    }

    /**
     * this is the map that makes the composite key across the 3 fields.
     */
    Map getKeyMap(P main, R related){
        [ (mainPropName): main, (relatedPropName): related ]
    }

    /**
     * return class.simpleName as default
     */
    String getSimpleName(Persistable linkedEntity){
        linkedEntity.class.simpleName
    }

    X create(P main, R related,  Map args = [:]) {
        def params = getKeyMap(main, related)
        validateCreate(main, related)
        //keep main entity in args so events can get to it
        args['mainEntity'] = main
        return create(params, args)
    }

    @Override
    X create(Map data,  Map args) {
        X leInstance = (X) getEntityClass().newInstance(data)
        // default is to give it insert as a hint
        Map mArgs = [flush: false, insert: true, failOnError: true]
        mArgs.putAll(args) //overrides
        doPersist leInstance, mArgs
        return leInstance
    }

    X get(P main, R related) {
        queryFor(main, related).get()
    }

    /**
     * removes a specific entry
     */
    boolean remove(P main, R related) {
        queryFor(main, related).deleteAll()
    }

    /**
     * removes all xref entries for an entity
     */
    Integer remove(Persistable entity) {
        queryFor(entity).deleteAll() as Integer
    }

    /**
     * remove related items
     *
     * @param main - the main entry to removed related ids from
     * @param relatedIdList - the list of ids to remove
     */
    void remove(P main, List<Long> relatedIdList) {
        for (Long relatedId : relatedIdList) {
            remove(main, (R)lookup(relatedClass, [id: relatedId]))
        }
    }


    //*** query, find and list methods ***

    Integer count(Persistable entity) {
        (Integer)queryFor(entity).count()
    }

    boolean exists(P main, R related) {
        queryFor(main, related).count()
    }

    List<X> list(Persistable entity) {
        queryFor(entity).list()
    }

    /**
     * list the related items for the related entity.
     * for example calling this and passing in Contacts will return the list of tags
     */
    List<R> listRelated(P main) {
        queryFor(main).list().collect {it[relatedPropName] } as List<R>
    }

    MangoDetachedCriteria<X> queryFor(Persistable entity){
        if(getRelatedClass().isAssignableFrom(entity.class)){
            query((relatedPropName): entity)
        } else {
            queryByMain(entity)
        }
    }

    MangoDetachedCriteria<X> queryByMain(Persistable entity){
        query((mainPropName): entity)
    }

    /**
     * query by the composite key
     */
    MangoDetachedCriteria<X> queryFor(Persistable linkedEntity, R related){
        queryByMain(linkedEntity).eq(relatedPropName, related)
    }


    /*** binding to add and remove ****/

    /**
     * This assumes that the entities already exist and we are just doing the linking.
     * id keys should exist in the data.
     *
     * If itemParams is a list then its assumed to be a replace.
     *  - will compare items , remove whats not on the itemParams and add whats not there
     *
     * If itemParams is an object and has op=update then it will spin through and look for the op field in each object
     *  - if no op field in data then its assumed to be an add and will add if not exists
     *  - if op=delete then removes
     *
     * @param main the primary entity, or linkedEntity if its a linkedEntityRepo
     * @param itemParams the List or Map data
     * @return
     */
    List<X> addOrRemove(P main, Object itemParams){
        if(!itemParams) return []
        Long mainId = main['id'] as Long

        Validate.isTrue(itemParams instanceof List || itemParams instanceof Map, "bind data must be map or list: %s", itemParams.class)

        List xlist = [] as List<X>

        //default is to replace the tags with whats in tagParams
        if (itemParams instanceof Map) {
            DataOp op = DataOp.get(itemParams.op)
            List dataList = (List)itemParams.data

            Validate.isTrue(itemParams.data instanceof List)
            if(op == DataOp.update) {
                xlist =  addOrRemoveList(main, dataList as List)
            }
            else {
                throw new UnsupportedOperationException("op=update is currently the only supported operation when passing a map for associations")
            }
        } else { //its a list
            xlist = replaceList(main, itemParams as List)
        }
        return xlist
    }

    /**
     * iterates over list and calls the createOrRemove
     * This should NOT normally be called directly, use addOrRemove
     */
    List<X> addOrRemoveList(P main, List<Map> dataList){
        //if its passing in an empty list on update then clear it out
        if(dataList.isEmpty()) {
            remove(main)
            return []
        }

        List xlist = [] as List<X>
        for (Map relatedItem : dataList) {
            X xref = createOrRemove(main, relatedItem)
            if(xref) xlist.add(xref)
        }
        return xlist
    }

    List<X> replaceList(P main, List<Map> dataList){
        def itemList = dataList as List<Map>
        // if its empty, then remove all
        if(dataList.isEmpty()) {
            return []
        } else {
            //list of existing related items
            List currentLinkList = queryFor(main).list()
            List<Long> currentLinkIds = collectLongIds(currentLinkList, "${relatedPropName}Id")
            List<Long> dataIds = collectLongIds(dataList)


            List<Long> itemsToAdd = dataIds - currentLinkIds
            List<Map> itemsToAddMap = listToIdMap(itemsToAdd)
            List xlist = addOrRemoveList(main, itemsToAddMap)

            List<Long> itemsToRemove = currentLinkIds - dataIds
            remove(main, itemsToRemove)
            return xlist
        }

    }

    /**
     * Creates link xref from data map or removes xref if data.op = remove
     */
    X createOrRemove(P main, Map data){
        Validate.notNull(data.id, "createOrRemove requires data map to have an id key")
        X xrefEntity
        DataOp op = DataOp.get(data.op) //add, update, delete really only needed for delete
        R related = (R)lookup(relatedClass, data)
        if(op == DataOp.remove){
            remove(main, related)
        } else {
            if(!exists(main, related)) { //if it already exists then move on
                xrefEntity = create(main, related)
            }
        }
        return xrefEntity
    }

    List<X> add(P main, List<Long> ids){
        List<Map> itemsToAddMap = listToIdMap(ids)
        return addOrRemoveList(main, itemsToAddMap)
    }

    /**
     * Copies all related from given entity to target main entity
     */
    void copyRelated(P fromEntity, P toEntity) {
        List<Long> relatedIds = collectLongIds(list(fromEntity), "${relatedPropName}Id")
        //FIXME we should bypass events for this so its faster
        if (relatedIds) add(toEntity, relatedIds)
    }

    // ***** Unsupported some gormRepo methods should not be called with an XRef tables so blow errors for these

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
