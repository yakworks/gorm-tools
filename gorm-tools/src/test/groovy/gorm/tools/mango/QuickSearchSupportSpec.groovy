/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango

import org.springframework.beans.factory.annotation.Autowired

import spock.lang.Specification
import yakworks.gorm.api.IncludesConfig
import yakworks.gorm.api.IncludesKey
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.unit.DataRepoTest

class QuickSearchSupportSpec extends Specification implements DataRepoTest  {
    static List entityClasses = [KitchenSink]
    static springBeans = [QuickSearchSupport]

    @Autowired IncludesConfig includesConfig
    @Autowired QuickSearchSupport quickSearchSupport

    void 'sanity check qsearch includes'() {
        when:
        List<String> qsFields =  includesConfig.getIncludes(KitchenSink, IncludesKey.qSearch.name())
        List<String> stampFields =  includesConfig.getIncludes(KitchenSink, IncludesKey.stamp.name())
        then:
        qsFields == ['id', 'num', 'name']
        stampFields == ['id', 'num', 'name']
        quickSearchSupport
    }

    void 'test getQSearchFields'() {
        when:
        Map qsFields = quickSearchSupport.getQSearchFields(KitchenSink)
        then:
        qsFields == [id: Long, num: String, name: String]
    }

    void 'test buildSearchMap'() {
        when:
        Map smap = quickSearchSupport.buildSearchMap([id: Long, num: String], 'foo')
        then:
        smap == [
            $or:[
                [id: [$eq:'foo']],
                [num: [$ilike:'foo%']]
            ]
        ]
    }

    void 'test buildSearchMap with class'() {
        when:
        Map smap = quickSearchSupport.buildSearchMap(KitchenSink, 'foo')
        then:
        smap == [
            $or:[
                [id: [$eq:'foo']],
                [num: [$ilike:'foo%']],
                [name: [$ilike:'foo%']]
            ]
        ]
    }

}
