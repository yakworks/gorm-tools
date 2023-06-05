/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

/**
 * Source information, where the data came from, where was it called from
 */
@CompileStatic
trait SourceTrait  {

    // Open field for descriptive name of the system or job
    String source

    // External or App for most sources, indicates what the system of record is
    SourceType sourceType = SourceType.App

    // Unique id from the external source or the id if its internal
    String sourceId

    static constraintsMap = [
        source:[ d: 'A description of where this came from', example: 'Oracle, BankOfAmerica, Lockbox', maxSize:50],
        sourceType:[ d: 'Enum, defaults to SourceType.App', nullable: false, example: 'App', default: 'App'],
        sourceId:[ d: 'the unique id from the outside source or name of the scheduled job', nullable: false, example: 'AR-123-A64', maxSize:50]
    ]
}
