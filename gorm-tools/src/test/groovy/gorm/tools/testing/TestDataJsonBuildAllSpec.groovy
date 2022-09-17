/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing

import yakworks.testing.gorm.TestDataJson
import yakworks.testing.gorm.unit.DataRepoTest
import yakworks.testing.gorm.support.RepoBuildDataTest
import spock.lang.Specification
import testing.Address
import testing.Cust
import testing.CustExt
import testing.CustType
import testing.AddyNested

class TestDataJsonBuildAllSpec extends Specification implements RepoBuildDataTest, DataRepoTest{

    def setupSpec(){
        mockDomains(Cust, CustType, AddyNested, CustExt, Address)
    }

    void "test getJsonIncludes"(){
        when:
        def incs = TestDataJson.getFieldsToBuild(Cust, '*')

        then:
        incs.containsAll(['type.id', 'ext.*', 'location.id'])
    }

    void "test includes *"(){
        when:
        def map = TestDataJson.buildMap(Cust, includes:'*', deep:true)//, location: build(Location), ext: build(OrgExt), deep:true)
        then:
        map.ext.id
        map.ext.text1
        map.location.id
    }

}
