/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.core.GenericTypeResolver

import gorm.tools.model.Persistable
import yakworks.rally.common.LinkedEntityRepoTrait

@CompileStatic
trait Taggable<D> {

    List<Tag> getTags() {
        getTagLinkRepo().listItems(this as Persistable)
    }

    boolean hasTag(Tag tag) {
        return getTagLinkRepo().exists(this as Persistable, tag)
    }

    Class<D> getTagLinkClass() {
        (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), Taggable)
    }

    @SuppressWarnings(['FieldName'])
    private static LinkedEntityRepoTrait _entityTagRepo

    LinkedEntityRepoTrait getTagLinkRepo() {
        if (!_entityTagRepo) this._entityTagRepo = ClassPropertyFetcher.getStaticPropertyValue(getTagLinkClass(), 'repo', LinkedEntityRepoTrait)
        return _entityTagRepo
    }
}
