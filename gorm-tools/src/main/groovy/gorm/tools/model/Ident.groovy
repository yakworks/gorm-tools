/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.model

/**
 * id property
 * @param <ID>
 */
interface Ident<ID> {

    ID getId()
    void setId(ID theid)

}
