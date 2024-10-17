/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.testing.gorm

/**
 * @author Joshua Burnett (@basejump)
 */
class GormTestSupportGrailsPlugin extends grails.plugins.Plugin {

    def loadAfter = ['spring-security-core', 'spring-security-ldap', 'spring-security-rest', 'gorm-tools', 'datasource']


    // @Override
    // void doWithApplicationContext() {
    //     Class[] domainClasses = grailsApplication.domainClasses*.clazz
    //     addBuildMetaMethods(domainClasses)
    // }
    //
    // @CompileDynamic
    // void addBuildMetaMethods(Class<?>... entityClasses){
    //     entityClasses.each { ec ->
    //         def mc = ec.metaClass
    //         //println("adding gradmeta for $ec")
    //         // mc.static.build = {
    //         //     return TestData.build(ec)
    //         // }
    //         mc.static.update = { Map data ->
    //             return RepoLookup.findRepo(ec).update(data) as Map
    //         }
    //
    //     }
    // }
}
