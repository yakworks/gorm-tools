/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.testing.gorm.model.Enummy
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.unit.DataRepoTest

class IncludesConfigSpec extends Specification implements DataRepoTest  {
    static List entityClasses = [KitchenSink, SinkExt, SinkItem, Thing, Enummy]

    @Autowired IncludesConfig includesConfig

    void 'getByKey for key'() {

        when:
        List<String> incs = includesConfig.getByKey(KitchenSink, 'stamp')
        List<String> incsNotFound = includesConfig.getByKey(KitchenSink, 'a-bad-key')
        then:
        3 == incs.size()
        incs.containsAll(KitchenSink.includes.stamp)
        !incsNotFound
    }

    void 'getIncludes by entity name'() {

        when:
        Class clazz = IncludesConfig.lookupClass('yakworks.testing.gorm.model.KitchenSink')
        List<String> incs = includesConfig.findByKeys(clazz, ['stamp'])
        List<String> incsNotFound = includesConfig.findByKeys(clazz, ['a-bad-key'])

        then:
        3 == incs.size()
        incs.containsAll(KitchenSink.includes.stamp)
        incsNotFound == ['*']

    }
}
