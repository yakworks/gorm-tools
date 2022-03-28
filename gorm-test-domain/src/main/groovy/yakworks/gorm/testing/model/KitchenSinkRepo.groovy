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
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.IdGeneratorRepo
import gorm.tools.validation.Rejector
import grails.gorm.transactions.Transactional
import yakworks.commons.lang.IsoDateUtil

@GormRepository
@CompileStatic
class KitchenSinkRepo implements GormRepo<KitchenSink>, IdGeneratorRepo<KitchenSink>  {

    List<String> toOneAssociations = [ 'ext' ]

    @RepoListener
    void beforeValidate(KitchenSink o) {
        o.beforeValidateCheck = "got it"
        //test rejectValue
        def rejector = Rejector.of(o)
        if(o.thing?.country == 'USSR'){
            // not recomended but shows you can put anything if for the propname and code
            rejector.withError('beatles', o.thing.country, 'no.backInThe.USSR.from.KitchenSinkRepo')
        }
        if(o.name == 'foos'){
            rejector.withError('name', 'no.foos')
        }
        //if(o.id == null) o.id = generateId(o)
        //if(o.ext && !o.ext.kitchenSink) o.ext.kitchenSink = o
        auditStamp(o)
    }

    @RepoListener
    void beforePersist(KitchenSink o, BeforePersistEvent e) {
        if(!o.kind) o.kind = KitchenSink.Kind.CLIENT
    }

    @RepoListener
    void afterBind(KitchenSink o, Map data, AfterBindEvent e) {
        if (e.isBindCreate()) {
            //mess with the data so that we can verify that places such as bulk are sending a clone
            //and not relying on the repo to keep the data pristine
            data.remove('name2')
        }
    }


    @Transactional
    KitchenSink inactivate(Long id) {
        KitchenSink o = KitchenSink.get(id)
        o.inactive = true
        o.persist()
        o
    }

    /**
     * Called after persist if its had a bind action (create or update) and it has data
     * creates or updates One-to-Many associations for this entity.
     */
    @Override
    void doAfterPersistWithData(KitchenSink kitchenSink, PersistArgs args) {
        Map data = args.data
        if(data.sinkItems) persistToManyData(kitchenSink, SinkItem.repo, data.sinkItems as List<Map>, "kitchenSink")
    }

    void auditStamp(Object ent){
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

    Map generateData(Long id, Map extraData = [:]) {
        Map data = [
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
        data.putAll(extraData)
        return data
    }

    List<Map> generateDataList(int numRecords, Map extraData = [:]) {
        List<Map> list = []
        (1..numRecords).each { int index ->
            list << generateData(index, extraData)
        }
        return list
    }
}
