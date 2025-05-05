/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.api

import groovy.transform.CompileStatic

import org.hibernate.SessionFactory
import org.springframework.beans.factory.annotation.Autowired

import grails.core.GrailsApplication
import grails.plugins.GrailsPluginManager
import grails.plugins.PluginManagerAware

@CompileStatic
class ApplicationController implements PluginManagerAware {

    GrailsApplication grailsApplication
    GrailsPluginManager pluginManager
    @Autowired SessionFactory sessionFactory

    def index() {
        [grailsApplication: grailsApplication, pluginManager: pluginManager]
    }

    @SuppressWarnings(['EmptyCatchBlock'])
    def hazelHibernate() {
        Map reqionStats = [:]
        for(String s :sessionFactory.getStatistics().getSecondLevelCacheRegionNames()) {
            try {
                reqionStats[s] = sessionFactory.getStatistics().getDomainDataRegionStatistics(s)
            } catch(e){
                //swallow error
            }
            // println("[H-STATS] For region: \n"+ s + ":{"
            //     + "\n\tHit count: "+sessionFactory.getStatistics().getDomainDataRegionStatistics(s).getHitCount()
            //     + "\n\tMiss count: "+sessionFactory.getStatistics().getSecondLevelCacheStatistics(s).getMissCount()
            //     + "\n\tPut count: "+sessionFactory.getStatistics().getSecondLevelCacheStatistics(s).getPutCount()
            //     + "\n\tElement Count on disk: "+sessionFactory.getStatistics().getSecondLevelCacheStatistics(s).getElementCountOnDisk()
            //     + "\n\tElement Count in memory: "+sessionFactory.getStatistics().getSecondLevelCacheStatistics(s).getElementCountInMemory()
            //     + "\n\tSize in memory: "+sessionFactory.getStatistics().getSecondLevelCacheStatistics(s).getSizeInMemory()
            //     +"\n}");
        }
        respond reqionStats
    }
}
