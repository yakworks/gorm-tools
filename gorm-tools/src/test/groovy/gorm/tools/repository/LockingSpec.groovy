/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository

import org.springframework.core.task.SimpleAsyncTaskExecutor

import spock.lang.Specification
import yakworks.testing.gorm.model.KitchenSink
import yakworks.testing.gorm.model.SinkItem
import yakworks.testing.gorm.unit.GormHibernateTest

class LockingSpec extends Specification implements GormHibernateTest  {
    static List entityClasses = [KitchenSink, SinkItem]

    void setupSpec() {
        KitchenSink.createKitchenSinks(10)
    }

    void cleanupSpec() {
        KitchenSink.cleanup()
    }

    void "lock test"() {
        when:
        KitchenSink jobLocked2 //= TestSyncJob.lock(job.id)

        KitchenSink jobLocked
        var te = new SimpleAsyncTaskExecutor()
        te.execute(() -> {
            KitchenSink.withTransaction {
                jobLocked = KitchenSink.lock(1)
                assert jobLocked
                sleep(2000) //hold it for 2 seconds
            }
        });
        sleep(500)

        //this waits until it can get the lock
        jobLocked2 = KitchenSink.lock(1)

        then:
        jobLocked
        jobLocked2
    }

}
