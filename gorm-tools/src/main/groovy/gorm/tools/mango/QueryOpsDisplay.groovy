/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.mango


import groovy.transform.CompileStatic

/**
 * Used in epay/collectionStep page
 * Enum that contains list of comparison operators to provide more user friendly list
 * TODO WIP this is not finshed and is here as a placeholder
 */
@CompileStatic
enum QueryOpsDisplay {
    MoreThan('more than', '>', 'gt'),
    AtLeast('at least', '>=', 'ge'),
    Equals('equal to', '=', 'eq'),
    NoMoreThan('no more than', '<=', 'le'),
    LessThan('less than', '<', 'lt'),
    NotEquals('not equal to', '!=', 'ne')

    final String name
    final String operator
    final String criteria

    QueryOpsDisplay(String name, String operator, String criteria) {
        this.name = name
        this.operator = operator
        this.criteria = criteria
    }

    static List<String> listNames() {
        return QueryOpsDisplay.values().toList()*.name
    }

    static List<String> listOperators() {
        return QueryOpsDisplay.values().toList()*.operator
    }

    static List<String> listHibernate() {
        return QueryOpsDisplay.values().toList()*.criteria
    }

    //returns an ComparisonOperator enum for the passed in operator
    static QueryOpsDisplay getByOperator(String operator) {
        return QueryOpsDisplay.values().find { it.operator == operator }
    }

/*  static List toList(){
        return ComparisonOperator.values().toList()
    }*/
}
