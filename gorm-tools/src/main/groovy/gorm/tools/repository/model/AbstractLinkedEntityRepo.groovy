/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.utils.GormMetaUtils
import yakworks.commons.lang.Validate

/**
 * CrossRefRepo for a linked entity table that is a composite key, many to many.
 * The main class is the LinkedEntity Persitable and the Linked item (such as Tag) is the related
 *
 * @param <X> the cross ref (LinkXRef) links domain
 * @param <R> the Related linked Item entity, Tag for example
 */
@Slf4j
@CompileStatic
abstract class AbstractLinkedEntityRepo<X, R extends Persistable> extends AbstractCrossRefRepo<X, Persistable, R> {

    protected AbstractLinkedEntityRepo(Class<R> relatedClazz, String propName){
        super(Persistable, relatedClazz, ['dummy', propName])
    }

    @Override
    void validateCreate(Persistable linkEntity, R related){
        Validate.notNull(linkEntity.getId(), "[linkEntity.id]")
    }

    /**
     * this is the map that makes the composite key across the 3 fields.
     */
    @Override
    Map getKeyMap(Persistable<Long> main, R related){
        getKeyMap(main.getId(), getLinkedEntityName(main), related)
    }

    /**
     * this is the map that makes the composite key across the 3 fields.
     */
    Map getKeyMap(long linkedId, String linkedEntityName, R related){
        [linkedId: linkedId, linkedEntity: linkedEntityName, (relatedPropName): related]
    }

    /**
     * used in testing and for copying. bypasses the validation so use with caution.
     * Also event will not have the mainEntity which is the linkedEntity
     */
    X create(long linkedId, String linkedEntityName, R related) {
        def params = getKeyMap(linkedId, linkedEntityName, related)
        return create(params, [:])
    }

    String getLinkedEntityName(Persistable linkedEntity){
        getLinkedEntityName(linkedEntity.class)
    }

    String getLinkedEntityName(Class linkedEntityClass){
        GormMetaUtils.unwrapIfProxy(linkedEntityClass.simpleName)
    }

    @Override
    MangoDetachedCriteria<X> queryByMain(Persistable entity){
        query(linkedId: entity.getId(), linkedEntity: getLinkedEntityName(entity))
    }


    void copyLinked(R fromRelated, R toRelated){
        for (X link  : list(fromRelated)) {
            create(link['linkedId'] as Long, link['linkedEntity'] as String, toRelated)
        }
    }

}
