/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.mango.MangoDetachedCriteria
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.AbstractLinkedEntityRepo
import grails.gorm.DetachedCriteria
import yakworks.commons.beans.Transform
import yakworks.commons.lang.Validate
import yakworks.rally.orgs.model.Org
import yakworks.rally.orgs.model.OrgTag
import yakworks.rally.tag.model.Tag

import static gorm.tools.utils.GormUtils.collectLongIds

@GormRepository
@CompileStatic
class OrgTagRepo extends AbstractLinkedEntityRepo<OrgTag, Tag> {

    OrgTagRepo(){
        super(Tag, 'tag')
    }

    @Override
    void validateCreate(Persistable entity, Tag tag) {
        Validate.notNull(entity.getId(), "[linkEntity.id]")
        Org org = (Org) entity
        if (!tag.isValidFor(org.type.name()))
            throw new IllegalArgumentException("Tags entityName: ${tag.entityName} not valid for org type ${org.type.name()}")
    }

    /**
     * Makes the composite key only be linkedId and item, since there is no linkedEntity field
     */
    @Override
    Map getKeyMap(long linkedId, String linkedEntityName, Tag tag){
        [linkedId: linkedId, 'tag': tag]
    }

    //override since there is no linkedEntity field
    @Override
    MangoDetachedCriteria<OrgTag> queryByMain(Persistable entity){
        query(linkedId: entity.getId())
    }

    List<Tag> listTags(Persistable org) {
        queryFor(org).list()*.tag
    }

    /**
     * shortcut to create by ids
     */
    OrgTag create(Long orgId, Long tagId) {
        super.create(Org.load(orgId), Tag.load(tagId))
    }

    /**
     * Copies all tags from given org to target org
     */
    void copyToOrg(Org fromOrg, Org toOrg) {
        List<Long> tagsIds = collectLongIds(list(fromOrg), "tagId")
        if (tagsIds) add(toOrg as Persistable, tagsIds)
    }

    /**
     * build exists criteria for the linkedId and tag list
     */
    DetachedCriteria buildExistsCriteria(List tagList, String linkedId = 'org_.id'){
        return OrgTag.query {
            eqProperty("linkedId", linkedId)
            inList('tag.id', Transform.toLongList(tagList))
        }.id()
    }

    /**
     * Add exists criteria to a DetachedCriteria if its has tags
     * in the criteriaMap
     */
    DetachedCriteria getExistsCriteria(Map criteriaMap, String linkedId = 'org_.id'){
        DetachedCriteria existsCrit
        if(criteriaMap.tags){
            //convert to id long list
            List<Long> tagIds = Transform.objectToLongList((List)criteriaMap.remove('tags'), 'id')
            existsCrit = buildExistsCriteria(tagIds, linkedId)
        } else if(criteriaMap.tagIds) {
            //should be list of id if this key is present
            existsCrit = buildExistsCriteria((List)criteriaMap.remove('tagIds'), linkedId)
        }
        return existsCrit
    }

}
