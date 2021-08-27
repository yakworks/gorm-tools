/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.source

import groovy.transform.CompileStatic

@CompileStatic
enum SourceType {
    App,
    Scheduler, //for Job only, more specific App
    ERP,
    RestApi, // more specific ERP
    PayGateway

    public String getName() {
        return name()
    }

    static List<String> stringValues() {
        return SourceType.values()*.name()
    }

    static SourceType findByName(String name) {
        return SourceType.values().find { it.name == name }
    }

}
