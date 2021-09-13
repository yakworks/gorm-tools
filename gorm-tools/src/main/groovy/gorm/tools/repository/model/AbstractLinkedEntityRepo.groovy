/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.model

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import yakworks.commons.lang.Validate

import static gorm.tools.utils.GormUtils.collectLongIds

/**
 * CrossRefRepo for a linked entity table that is a composite key, many to many.
 * The main class is the LinkedEntity Persitable and the Linked item (such as Tag) is the related
 *
 * @param <X> the cross ref (LinkXRef) links domain
 * @param <I> the linked Item entity, Tag for example
 */
@Slf4j
@CompileStatic
abstract class AbstractLinkedEntityRepo<X, R extends Persistable> extends AbstractCrossRefRepo<X, Persistable, R> {

    protected AbstractLinkedEntityRepo(Class<R> relatedClazz){
        super(Persistable, relatedClazz)
    }

    @Override
    void validateCreate(Persistable linkEntity, R related){
        Validate.notNull(linkEntity.id, "[linkEntity.id]")
    }

    /**
     * this is the map that makes the composite key across the 3 fields.
     */
    @Override
    Map getKeyMap(Persistable main, R related){
        getKeyMap(main.id, getLinkedEntityName(main), related)
    }

    /**
     * this is the map that makes the composite key across the 3 fields.
     */
    Map getKeyMap(long linkedId, String linkedEntityName, R related){
        [linkedId: linkedId, linkedEntity: linkedEntityName, (relatedPropName): related]
    }

    X create(long linkedId, String linkedEntityName, R related) {
        def params = getKeyMap(linkedId, linkedEntityName, related)
        return create(params, [:])
    }

    String getLinkedEntityName(Persistable linkedEntity){
        linkedEntity.class.simpleName
    }

    @Override
    MangoDetachedCriteria<X> queryByMain(Persistable entity){
        query(linkedId: entity.id, linkedEntity: getLinkedEntityName(entity))
    }

    /**
     * Copies all related from given entity to target main entity
     */
    void copyRelated(Persistable fromEntity, Persistable toEntity) {
        List<Long> relatedIds = collectLongIds(list(fromEntity), "${relatedPropName}Id")
        if (relatedIds) add(toEntity, relatedIds)
    }

    void copyLinked(R fromRelated, R toRelated){
        for (X link  : list(fromRelated)) {
            create(link['linkedId'] as Long, link['linkedEntity'] as String, toRelated)
        }
    }

}
