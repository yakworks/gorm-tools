/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.metamap.services

import org.springframework.util.SerializationUtils

import spock.lang.Specification
import yakworks.api.problem.data.DataProblemException
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import yakworks.testing.gorm.model.Enummy
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.TestEnum
import yakworks.testing.gorm.model.TestEnumIdent
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.unit.GormHibernateTest

class MetaMapTester {

    boolean testEquals(MetaMap m1, MetaMap m2){
        return m1 == m2
    }
}
