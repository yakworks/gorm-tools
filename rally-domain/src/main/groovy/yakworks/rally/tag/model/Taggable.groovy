/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.springframework.core.GenericTypeResolver

import gorm.tools.model.Persistable
import yakworks.commons.lang.ClassUtils
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
        if (!_entityTagRepo) this._entityTagRepo = ClassUtils.getStaticPropertyValue(getTagLinkClass(), 'repo', LinkedEntityRepoTrait)
        return _entityTagRepo
    }

    // @CompileDynamic
    // static TaggableConstraints(Object delegate) {
    //     def c = {
    //         tags description: "the tags for this item", nullable: true
    //     }
    //     c.delegate = delegate
    //     c()
    // }
    //
    static Map constraintsMap = [
        tags: [ description: 'the tags for this item', validate: false] //validate false so it does not retrieve the value
    ]
}
