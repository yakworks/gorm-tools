/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package restify

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import grails.gorm.transactions.Transactional

@CompileStatic
@GormRepository
class BookRepo implements GormRepo<Book> {

    @Transactional(rollbackForClassName = "TestTransactionException")
    def foo(){
        assert '1'=="1"
    }
}
