/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.model.LinkedEntity
import gorm.tools.model.Persistable
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.events.RepositoryEvent
import gorm.tools.repository.model.AbstractLinkedEntityRepo

/**
 * common trait that a concrete composite entity can implement if the stock TagLink will not suffice
 * for example, Org has its own OrgTag linking table
 *
 * @param <X> the LinkXRef entity (OrgTag for example)
 */
@CompileStatic
trait TagLinkTrait<X> implements LinkedEntity {

    abstract Tag getTag()
    abstract void setTag(Tag t)

    abstract Serializable getAssociationId(String param1)

    @Transient
    Long getTagId() { (Long)this.getAssociationId("tag") }

    static constraintsMap = [
        tag:[ description: 'The tag', nullable: false]
    ]

    // will return the same repo as getRepo, just casts it to AbstractLinkedEntityRepo for compilestatic
    // AST gets confused if we try using getRepo as we can't use abstract
    static AbstractLinkedEntityRepo<X, Tag> getTagLinkRepo() {
        (AbstractLinkedEntityRepo<X, Tag>) RepoLookup.findRepo(this)
    }

    static Integer remove(Persistable entity) {
        getTagLinkRepo().remove(entity)
    }

    // helper to call in afterPersist
    static List<X> addOrRemoveTags(Persistable linkedEntity, Object itemParams) {
        getTagLinkRepo().addOrRemove(linkedEntity, itemParams)
    }

    // helpe to call in afterPersist
    static List<X> addOrRemoveTags(Persistable linkedEntity, RepositoryEvent e) {
        if (e.bindAction && e.data?.tags){
            getTagLinkRepo().addOrRemove(linkedEntity, e.data.tags)
        }
    }

    static List<X> list(Persistable entity) {
        getTagLinkRepo().list(entity)
    }

    static List<Tag> listTags(Persistable entity) {
        getTagLinkRepo().listRelated(entity)
    }

    static boolean hasTags(Persistable entity) {
        getTagLinkRepo().count(entity)
    }

    static boolean exists(Tag tag) {
        getTagLinkRepo().count(tag)
    }

    static X create(Persistable entity, Tag theTag, Map args = [:]) {
        getTagLinkRepo().create(entity, theTag, args)
    }

    static X get(Persistable entity, Tag theTag) {
        getTagLinkRepo().get(entity, theTag)
    }

    static boolean exists(Persistable entity, Tag theTag) {
        getTagLinkRepo().exists(entity, theTag)
    }

    @Override
    boolean equals(Object other) {
        if (other == null) return false
        if (this.is(other)) return true
        if (other instanceof TagLinkTrait<X>) {
            return other.getLinkedId() == getLinkedId() && other.getLinkedEntity() == getLinkedEntity() && other.getTagId() == getTagId()
        }
        return false
    }

    @Override
    int hashCode() {
        int hashCode = HashCodeHelper.initHash()
        if (getLinkedId()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedId()) }
        if (getLinkedEntity()) { hashCode = HashCodeHelper.updateHash(hashCode, getLinkedEntity()) }
        if (getTagId()) { hashCode = HashCodeHelper.updateHash(hashCode, getTagId()) }
        hashCode
    }


}
