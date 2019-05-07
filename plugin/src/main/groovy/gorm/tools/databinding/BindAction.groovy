/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import groovy.transform.CompileStatic

@CompileStatic
enum BindAction {
    Create, Update, Delete, Generic
}
