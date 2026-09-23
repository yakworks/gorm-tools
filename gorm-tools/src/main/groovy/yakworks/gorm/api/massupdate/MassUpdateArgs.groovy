/*
* Copyright 2026 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api.massupdate

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import gorm.tools.repository.PersistArgs

/**
 * Args for applying the same field changes to many records by id.
 */
@Builder(builderStrategy = SimpleStrategy, prefix = "")
@CompileStatic
class MassUpdateArgs {

    /**
     * Ids of records to update.
     */
    List ids

    /**
     * Shared field values applied to each id. Should not need an id; it is injected per item.
     * Optional {@code activity} map is stripped and handled by MassUpdateService.
     */
    Map data

    /**
     * Persist options passed through to repo update.
     */
    PersistArgs persistArgs

    /**
     * Extra pass-through for hooks / events / repo PersistArgs.
     */
    Map params = [:]

    /**
     * When creating mass-update activities, true creates ActivityLinks (ArTran/Payment);
     * false for Customer/CustAccount where org on the activity is enough.
     */
    boolean linkTargets = false

    static MassUpdateArgs of(List ids, Map data) {
        new MassUpdateArgs(ids: ids, data: data)
    }
}
