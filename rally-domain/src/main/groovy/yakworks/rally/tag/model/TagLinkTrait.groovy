/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import javax.persistence.Transient

import groovy.transform.CompileStatic

import org.codehaus.groovy.util.HashCodeHelper

import gorm.tools.model.Persistable
import gorm.tools.repository.RepoUtil
import yakworks.rally.common.LinkXRefRepo
import yakworks.rally.common.LinkXRefTrait

/**
 * common trait that a concrete composite entity can implement if the stock TagLink will not suffice
 * for example, Org has its own OrgTag lining table
 *
 * @param <X> the LinkXRef entity
 */
@CompileStatic
trait TagLinkTrait<X> implements LinkXRefTrait {

    abstract Tag getTag()
    abstract void setTag(Tag t)

    abstract Serializable getAssociationId(String param1)

    @Transient
    Long getTagId() { (Long)this.getAssociationId("tag") }

    static constraintsMap = [
        tag:[ description: 'The tag', nullable: false]
    ]

    static List<Tag> listTags(Persistable entity) {
        getTagLinkRepo().listItems(entity)
    }

    static List<X> list(Tag tag) {
        getTagLinkRepo().queryFor(tag).list()
    }

    static List<X> list(Persistable linkedEntity) {
        getTagLinkRepo().queryFor(linkedEntity).list()
    }

    static boolean exists(Tag tag) {
        getTagLinkRepo().exists(tag)
    }

    static LinkXRefRepo<X,Tag> getTagLinkRepo() {
        (LinkXRefRepo<X,Tag>) RepoUtil.findRepo(this)
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
