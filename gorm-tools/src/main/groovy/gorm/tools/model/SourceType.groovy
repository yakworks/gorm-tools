/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

import groovy.transform.CompileStatic

/**
 * for syncing with app or external systems
 */
@CompileStatic
enum SourceType {
    App, // indicates that we creatd and are system of record for this
    ERP, //external system is system of record, rename from ERP.

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
