/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm.model

import java.time.LocalDate
import java.time.LocalDateTime

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepository
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.events.AfterBindEvent
import gorm.tools.repository.events.BeforeBulkSaveEntityEvent
import gorm.tools.repository.events.BeforePersistEvent
import gorm.tools.repository.events.RepoListener
import gorm.tools.repository.model.LongIdGormRepo
import gorm.tools.validation.Rejector
import grails.gorm.transactions.Transactional
import net.datafaker.Faker
import yakworks.commons.lang.IsoDateUtil

@SuppressWarnings(['InsecureRandom', 'PropertyName'])
@GormRepository
@CompileStatic
class KitchenSinkRepo extends LongIdGormRepo<KitchenSink> {
    //for unit tests,
    static boolean doTestAuditStamp = true

    //used for testing and cleanupSpec since gorm spock nulls out appCtx and cant get to it in there.
    static KitchenSinkRepo INSTANCE
    static KitchenSinkRepo getINSTANCE(){
        if(!INSTANCE) INSTANCE = RepoLookup.findRepo(KitchenSink) as KitchenSinkRepo
        return INSTANCE
    }

    // for unit tests, truncates data and close
    static void cleanup(){
        INSTANCE?.deleteAll()
        INSTANCE = null
    }

    Faker _faker

    Faker faker(){
        if(!_faker) _faker = new Faker(new Random(0))
        return _faker
    }

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

    @RepoListener
    void beforeBulkSaveEntity(BeforeBulkSaveEntityEvent e) {
        if (e.syncJobArgs.isCreate()) {
            e.data['createdByJobId'] = e.syncJobArgs.jobId
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
     * Called after persist
     */
    @Override
    void doAfterPersist(KitchenSink kitchenSink, PersistArgs args) {
        if (args.bindAction && args.data) {
            Map data = args.data
            if (data.sinkItems) super.persistToManyData(kitchenSink, SinkItem.repo, data.sinkItems as List<Map>, "kitchenSink")
        }
    }

    //USED FOR UNIT TESTS
    static void auditStamp(KitchenSink ent){
        if(!doTestAuditStamp) return
        if( ent.isNew()) {
            ent['createdBy'] = 1
            ent['createdDate'] = LocalDateTime.now()
        }
        if(ent.hasChanged()) {
            ent['editedBy'] = 1
            ent['editedDate'] = LocalDateTime.now()
        }
    }

    KitchenSink build(Long id, boolean flushIt = true){
        new Thing(id: id, name: "Thing$id").persist()
        Map data = generateData(id) as Map<String, Object>
        data.putAll([
            id: id,
            name2:"KitchenSink-$id",
            thing: [id: id],
            sinkItems: [[name: "red"], [name: "blue"]]
        ])

        def ks = create(data, [bindId: true])
        if(flushIt) flush()
        return ks
    }

    Map generateData(Long id, Map extraData = [:]) {

        String name = faker().food().ingredient()
        String name2 = (id % 2) ? "$name-$id" + id : null

        Map data = [
            num: "$id",
            name: name,
            name2: name2,
            kind: ((id % 2) ? KitchenSink.Kind.VENDOR : KitchenSink.Kind.CLIENT) as String,
            status: ( (id % 2) ? SinkStatus.Inactive : SinkStatus.Active ) as String,
            inactive: (id % 2 == 0),
            amount: (id - 1) * 1.25,
            // actDate: LocalDateTime.now().plusDays(id).toDate(),
            localDate: IsoDateUtil.format(LocalDate.now().plusDays(id)),
            localDateTime: IsoDateUtil.format(LocalDateTime.now().plusDays(id)),
            ext:[ name: "SinkExt$id", totalDue: id * 10.25, thing: [id: id]],
            bazMap: [foo: 'bar']
            // thing: [id: id]
        ]
        data.putAll(extraData)
        return data
    }

    List<Map> generateDataList(int numRecords, Map extraData = [:]) {
        _faker = new Faker(new Random(0))
        List<Map> list = []
        (1..numRecords).each { int index ->
            list << generateData(index, extraData)
        }
        return list
    }

    void createKitchenSinks(int count){
        _faker = new Faker(new Random(0))
        KitchenSink.withTransaction {
            (1..2).each { id ->
                def ks = build(id)
                ks.kind = KitchenSink.Kind.PARENT
                ks.persist()
            }
        }

        List<List<Integer>> idSlices = (3..count).collate(100)

        for(List<Integer> ids: idSlices){

            KitchenSink.withTransaction {
                for(Integer oid: ids){
                    build(oid, false)
                }
                KitchenSink.repo.flushAndClear()
            }
        }
    }

    @Transactional
    void deleteAll(){
        KitchenSink.deleteAll()
    }
}
