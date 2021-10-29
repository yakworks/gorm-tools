/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.testing.model


import java.time.LocalDateTime

import groovy.transform.CompileStatic

@CompileStatic
class KitchenSeedData {

    static void buildKitchen(int count){
        (1..2).each{
            def porg = createKitchen(it)
            porg.kind = KitchenSink.Kind.PARENT
            porg.persist()
        }
        // createOrg(1, type, Org.Kind.PARENT).persist()
        // createOrg(2, type, Org.Kind.PARENT).persist()
        (3..count).each { id ->
            def org = createKitchen(id)
            org.ext = new KitchenSinkExt(
                text1: "Ext$id",
                kitchenParent: id % 2 == 0 ? KitchenSink.get(1) : KitchenSink.get(2)
            )
            //org.ext.orgParent = id % 2 == 0 ? Org.get(1) : Org.get(2)
            org.persist()
        }
    }

    static KitchenSink createKitchen(Long id){
        def loc = new Thing(city: "City$id")
        loc.id = id
        loc.persist()

        String value = "Kitchen" + id
        def org = new KitchenSink(
            num: "$id",
            name: value,
            name2: (id % 2) ? "OrgName2" + id : null ,
            kind: (id % 2) ? KitchenSink.Kind.VENDOR : KitchenSink.Kind.CLIENT ,
            status: (id % 2) ? KitchenSinkStatus.Inactive : KitchenSinkStatus.Active,
            inactive: (id % 2 == 0),
            revenue: (id - 1) * 1.25,
            creditLimit: (id - 1) * 1.5,
            actDate: LocalDateTime.now().plusDays(id).toDate(),
            thing: loc
        )
        org.id = id
        return org
    }

}
