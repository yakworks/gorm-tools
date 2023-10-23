/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkExt
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.model.Thing
import yakworks.testing.gorm.unit.GormHibernateTest

//FIXME Not clear what would happen if hasChanged is used inside of the beforeValidate in a Repo, need some smoke tests for that too.
// can trace when trackChanges or activateDirtyChecking is called so its clear.
class HasChangedTesting {

    static KitchenSink saveSink() {
        KitchenSink sink = new KitchenSink(num: "123", name: "name").persist()
        //XXX this makes its fail
        //sink.persist()
        assert !sink.hasChanged()
        return sink
    }

}
