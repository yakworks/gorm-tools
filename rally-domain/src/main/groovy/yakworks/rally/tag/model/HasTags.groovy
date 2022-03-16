/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.model

import groovy.transform.CompileStatic

/**
 * Marks a domain as having Tags with a getTags() getter and constraintsMap
 * Used when a custom XRef table is used for Tags as as for Orgs.
 * Otherwise use Taggable
 */
@CompileStatic
trait HasTags {

    abstract List<Tag> getTags()

    static Map constraintsMap = [
        tags: [ d: 'the tags for this item', validate: false, required: false] //validate false so it does not initialize
    ]
}
