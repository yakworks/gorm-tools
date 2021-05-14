/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.testing

import gorm.tools.beans.BeanPathTools
import gorm.tools.testing.unit.DataRepoTest
import grails.buildtestdata.BuildDataTest
import spock.lang.Specification
import testing.Address
import testing.Cust
import testing.CustExt
import testing.CustType
import testing.Nested

class TestDataJsonBuildAllSpec extends Specification implements BuildDataTest, DataRepoTest{

    def setupSpec(){
        mockDomains(Cust, CustType, Nested, CustExt, Address)
    }

    void "test getJsonIncludes"(){
        when:
        def incs = TestDataJson.getFieldsToBuild(Cust, '*')
        def bpathIncs = BeanPathTools.getIncludes(Cust.name, incs)

        then:
        incs.containsAll(['location', 'ext', 'ext.*', 'location.id'])
        bpathIncs.containsAll(['amount', 'ext', 'ext.id', 'ext.text1', 'location.id'])
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
