/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.module.tag

import groovy.transform.CompileStatic

import org.grails.datastore.mapping.reflect.ClassPropertyFetcher
import org.springframework.core.GenericTypeResolver

import yakworks.module.tag.entity.Tag

@CompileStatic
trait Taggable<D> {

    abstract Long getId()

    List<Tag> getTags() {
        getEntityTagRepo().listTags(getId())
    }

    boolean hasTag(Tag tag) {
        return getEntityTagRepo().exists(getId(), tag)
    }

    Class<D> getEntityTagClass() {
        (Class<D>) GenericTypeResolver.resolveTypeArgument(getClass(), Taggable)
    }

    @SuppressWarnings(['FieldName'])
    private static EntityTagRepoTrait _entityTagRepo

    EntityTagRepoTrait getEntityTagRepo() {
        if (!_entityTagRepo) this._entityTagRepo = ClassPropertyFetcher.getStaticPropertyValue(getEntityTagClass(), 'repo', EntityTagRepoTrait)
        return _entityTagRepo
    }
}
