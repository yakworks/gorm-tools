/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.tag

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.mango.api.QueryMangoEntity
import gorm.tools.model.Persistable
import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import yakworks.module.tag.entity.Tag

@CompileStatic
trait EntityTagTrait<D> implements PersistableRepoEntity<D, GormRepo<D>>, QueryMangoEntity<D> {

    Long linkedId

    abstract Tag getTag()
    abstract void setTag(Tag t)

    abstract Serializable getAssociationId(String param1)

    @Transient
    Long getTagId() { (Long)this.getAssociationId("tag") }

    static EntityTagRepoTrait<D> getEntityTagRepo() {
        getRepo() as EntityTagRepoTrait<D>
    }

    static D create(Persistable entity, Tag theTag, Map args = [:]) {
        getEntityTagRepo().create(entity, theTag, args)
    }

    static D create(long entityId, Tag theTag, Map args = [:]) {
        getEntityTagRepo().create(entityId, theTag, args)
    }

    static D get(long lid, Tag theTag) {
        getEntityTagRepo().get(lid, theTag)
    }

    static D get(long lid, long tagId) {
        get(lid, Tag.load(tagId))
    }

    static List<D> list(long lid) {
        getEntityTagRepo().list(lid)
    }

    static List<Tag> listTags(long lid) {
        getEntityTagRepo().listTags(lid)
    }

    static boolean exists(long linkedId, Tag theTag) {
        getEntityTagRepo().exists(linkedId, theTag)
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof EntityTagTrait<D>) {
            return other.getLinkedId() == getLinkedId() && other.getTagId() == getTagId()
        }
        return false
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (getLinkedId()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedId()) }
        if (getTagId()) { hashCode = HashCodeHelper.updateHash(hashCode, getTagId()) }
        hashCode
    }


}
