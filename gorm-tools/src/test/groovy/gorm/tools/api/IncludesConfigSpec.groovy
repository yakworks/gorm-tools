/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.api

import yakworks.gorm.testing.unit.DataRepoTest
import spock.lang.Shared
import spock.lang.Specification
import yakworks.gorm.testing.model.Enummy
import yakworks.gorm.testing.model.KitchenSink
import yakworks.gorm.testing.model.SinkExt
import yakworks.gorm.testing.model.SinkItem
import yakworks.gorm.testing.model.Thing

class IncludesConfigSpec extends Specification  implements DataRepoTest  {

    @Shared
    def includesConfig = new IncludesConfig()

    void setupSpec() {
        //mockDomain Person
        mockDomains KitchenSink, SinkExt, SinkItem, Thing, Enummy
    }

    void 'getIncludes for key'() {

        when:
        List<String> incs = includesConfig.getIncludes(KitchenSink, 'stamp')
        List<String> incsNotFound = includesConfig.getIncludes(KitchenSink, 'a-bad-key')
        then:
        3 == incs.size()
        incs.containsAll(KitchenSink.includes.stamp)
        !incsNotFound
    }

    void 'getIncludes by entity name'() {

        when:
        List<String> incs = includesConfig.getIncludesByKey('yakworks.gorm.testing.model.KitchenSink', 'stamp')
        List<String> incsNotFound = includesConfig.getIncludesByKey('yakworks.gorm.testing.model.KitchenSink', 'a-bad-key')

        then:
        3 == incs.size()
        incs.containsAll(KitchenSink.includes.stamp)
        incsNotFound == ['*']

    }
}
