/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.async

import groovy.transform.CompileStatic
import groovy.transform.builder.Builder
import groovy.transform.builder.SimpleStrategy

import org.grails.datastore.mapping.core.Datastore

import gorm.tools.repository.RepoLookup

/**
 * common config settings for async operations, such as Futures and Parralel processing
 */
@Builder(builderStrategy= SimpleStrategy, prefix="")
@CompileStatic
class AsyncConfig {

    /**
     * the size of the lists when collated or sliced into chunks,
     * overrides defaults that get set from jdbc.batch_size
     */
    Integer sliceSize

    /**
     * override the default pool size that gets set from gorm.tools.async.poolSize
     * gets passed down into the GParsPool.withPool for example
     */
    Integer poolSize

    /**
     * override the default config from gorm.tools.async.enabled
     * useful for testing
     */
    Boolean enabled

    /**
     * wrap the closure for slice or future in a transaction, so its transaction for each parrallel run
     */
    Boolean transactional

    /**
     * wrap the closure for slice or entry with a session
     * this setting is ignored if transactional=true since a session already implied in a transaction
     */
    Boolean session

    /**
     * the datastore to use for the session or transaction
     * Optional and is only really needed if multiple datasources. if only on defualt datasource
     * then it will get that default from the trxService
     */
    Datastore datastore

    static AsyncConfig transactional(){
        new AsyncConfig(transactional: true)
    }

    static AsyncConfig withSession(){
        new AsyncConfig(session: true)
    }

    static AsyncConfig of(Datastore ds, boolean session = true){
        // if passing in a datastore then assume you want a session too. so set to false if not desired
        // if transactional is set later then it overrides session
        new AsyncConfig(datastore: ds, session: true)
    }

    static AsyncConfig of(Class entity){
        def repo = RepoLookup.findRepo(entity)
        AsyncConfig.of(repo.datastore)
    }
}
