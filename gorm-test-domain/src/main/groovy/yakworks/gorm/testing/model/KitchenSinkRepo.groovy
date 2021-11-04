/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepo
import gorm.tools.repository.GormRepository
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import grails.gorm.transactions.Transactional
import yakworks.commons.lang.IsoDateUtil

@GormRepository
@CompileStatic
class KitchenSinkRepo implements GormRepo<KitchenSink> {

    @RepoListener
    void beforeValidate(KitchenSink o) {
        o.beforeValidateCheck = "got it"
        //test rejectValue
        if(o.thing?.country == 'USSR'){
            // can put anything if for the code
            rejectValue(o, 'beatles', o.thing.country, 'no.backInThe.USSR.from.KitchenSinkRepo')
        }
        if(o.name == 'foos'){
            rejectValue(o, 'name', o.name, 'no.foos')
        }
        stamp(o)
    }

    @RepoListener
    void beforePersist(KitchenSink o, BeforePersistEvent e) {
        if(!o.kind) o.kind = KitchenSink.Kind.CLIENT
    }


    @Transactional
    KitchenSink inactivate(Long id) {
        KitchenSink o = KitchenSink.get(id)
        o.inactive = true
        o.persist()
        o
    }

    void stamp(Object ent){
        ent['createdBy'] = 1
        ent['createdDate'] = LocalDateTime.now()
        ent['editedBy'] = 1
        ent['editedDate'] = LocalDateTime.now()
    }

    KitchenSink build(Long id){
        def loc = new Thing(id: id, name: "Thing$id").persist()
        def data = generateData(id)
        data.putAll([id: id, thing: [id: id] ])
        def ks = KitchenSink.create(data, bindId: true)
        new SinkItem(kitchenSink: ks, name: "red").persist(flush:true)
        new SinkItem(kitchenSink: ks, name: "blue").persist(flush:true)
        return ks
    }

    Map generateData(Long id) {
        return [
            num: "$id",
            name: "Sink$id",
            name2: (id % 2) ? "SinkName2-$id" + id : null,
            kind: ((id % 2) ? KitchenSink.Kind.VENDOR : KitchenSink.Kind.CLIENT) as String,
            status: ( (id % 2) ? SinkStatus.Inactive : SinkStatus.Active ) as String,
            inactive: (id % 2 == 0),
            amount: (id - 1) * 1.25,
            // actDate: LocalDateTime.now().plusDays(id).toDate(),
            localDate: IsoDateUtil.format(LocalDate.now().plusDays(id)),
            localDateTime: IsoDateUtil.format(LocalDateTime.now().plusDays(id)),
            ext:[ name: "SinkExt$id"],
            bazMap: [foo: 'bar']
            // thing: [id: id]
        ]
    }

    List<Map> generateDataList(int numRecords) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            list << generateData(index)
        }
        return list
    }
}
