/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.transform.CompileStatic

@CompileStatic
class PathItem {
    /**
     * A resource refers to one or more nouns being served, represented in namespaced fashion, because it is easy for humans to comprehend:
     */
    String name
    /**
     * Description to flow through to open api docs
     */
    String description
    /**
     * The full key in form "/namespace/resource"
     */
    String key
    /**
     * The namespace this falls under
     */
    String namespace
    /**
     * The fully qulaified class name, ex: yakworks.rally.orgs.Org
     */
    String entityClass
    /**
     * The list of includes with key
     */
    Map<String, List<String>> includes
    /**
     * The list of allowed operations. create, read, update, delete
     */
    List<String> allowedOps
    /**
     * Is bulk endpoint enabled for this resource.
     */
    boolean bulkOps
    /**
     * when true, q is required when doing a GET list.
     */
    boolean qRequired

    /**
     * Check if upsert is allowed for the given path item / domain
     * Upsert is allowed if both create/update are allowed.
     * Note: when allowedOps are not explicitely specified, its considered as all ops are allowed.
     */
    boolean upsertAllowed() {
        return (!allowedOps || allowedOps.containsAll("create", "update"))
    }
}
