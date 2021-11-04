/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.model

import groovy.transform.CompileStatic

import yakworks.commons.lang.EnumUtils
import yakworks.commons.lang.NameUtils
import yakworks.commons.model.IdEnum

/**
 * OrgType enum for Orgs. Some are implemented as concrete entity classes.
 * Any concrete class should match the name, for example CustAccount also has a CustAccount class.
 */
@CompileStatic
enum OrgType implements IdEnum<OrgType, Long> {
    Customer(1),
    CustAccount(2),
    Branch(3),
    Division(4),
    Business(5),
    Company(6),
    Prospect(7),
    Sales(8),
    Client(9),
    Factory(10),
    Region(11)

    final Long id

    OrgType(Long id) {
        this.id = id
    }

    OrgTypeSetup getTypeSetup() {
        return OrgTypeSetup.get(id)
    }

    /**
     * get the property name for this, will follow normal java bean prop names
     * so CustAccount will become custAccount
     */
    String getPropertyName(){
        NameUtils.getPropertyName(name())
    }

    /**
     * get the property name with Id appended for the field name
     * ex: Customer -> customerId, CustAccount -> custAccountId, etc...
     */
    String getIdFieldName(){
        getPropertyName() + "Id"
    }

    //get the user customizable description from typeSetup
    String getDescription(){
        getTypeSetup().description
    }

    /**
     * getters based on Class
     */
    static OrgType get(Class clazz){
        EnumUtils.getEnum(OrgType, clazz.simpleName)
    }

    /**
     * case insensitive getter, will convert object to string
     */
    static OrgType get(Object name){
        EnumUtils.getEnumIgnoreCase(OrgType, name.toString())
    }
}
