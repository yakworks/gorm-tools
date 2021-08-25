/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.source

import groovy.transform.CompileStatic

/** Source information, where the data came from, where was it called from */
@CompileStatic
trait SourceTrait  {

    // 9ci or Erp system name. Open field for descriptive name of the system or job
    String source = '9ci'

    // Erp or App for most sources
    SourceType sourceType = SourceType.App

    //Unique id from the outside source or specific name of the scheduled job so it's easy to find
    String sourceId

    static constraintsMap = [
        source:[ description: 'A description of where this came from',
                 nullable: true, example: 'Oracle, BankOfAmericaLockbox'],
        sourceType:[ description: 'Enum, defaults to SourceType.App',
                     nullable: false, example: 'App', required: false],
        sourceId:[ description: 'the unique id from the outside source or name of the scheduled job',
                   nullable: false, example: 'AR-123-A64']
    ]
}
