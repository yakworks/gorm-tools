/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.massupdate

import groovy.transform.CompileStatic

import yakworks.api.ApiResults

/**
 * Applies the same field changes to many records by id.
 * Default implementation, MassUpdateService, stays in rally-domain, so it can access Activity etc.
 * @param <D> the entity class
 */
@CompileStatic
interface MassUpdater<D> {

    /**
     * Applies args.data to every id in args.ids.
     *
     * @return ApiResults with an entry per id
     */
    ApiResults massUpdate(MassUpdateArgs args)

}
