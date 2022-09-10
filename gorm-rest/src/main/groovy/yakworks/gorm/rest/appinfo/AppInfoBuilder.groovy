/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.rest.appinfo

import java.lang.management.ManagementFactory
import java.lang.management.MemoryPoolMXBean
import java.lang.management.MemoryType
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import groovy.transform.CompileDynamic

import org.springframework.beans.factory.annotation.Autowired

import grails.core.DefaultGrailsApplication
import grails.web.mapping.UrlMappingsHolder

/**
 * Misc Application Info. TODO probably move to its own plugin
 */
@CompileDynamic
class AppInfoBuilder {

    @Autowired DefaultGrailsApplication grailsApplication

    @Autowired UrlMappingsHolder grailsUrlMappingsHolder

    List urlMappings() {

        List urlMappings = grailsUrlMappingsHolder.urlMappings.collect {
            [
                    name      : it.mappingName ?: '',
                    url       : it.urlData.logicalUrls.first(),
                    methods   : it.parameterValues,
                    parameters: it.constraints.propertyName

            ]
        }
        return urlMappings
    }

    Map beanInfo() {
        SpringInfoHelper springInfo = new SpringInfoHelper()
        springInfo.grailsApplication = grailsApplication

        return springInfo.splitBeans()
    }

    /**
     * Collect information about memory usage. The returned map has the following info:
     * heapPoolNames: a list of the names of the heap memory pools
     * heapSectionNames: a list of the names of the heap section names
     * heapNumbers: a Map with section names as keys, and values are a list of memory values (in MB) for each heap pool section
     * nonheapPoolNames: a list of the names of the non-heap memory pools
     * nonheapSectionNames: a list of the names of the non-heap section names
     * nonheapNumbers: a Map with section names as keys, and values are a list of memory values (in MB) for each non-heap pool section
     * memoryNames: hard-coded to ['Heap'] for consistency
     * memorySectionNames: hard-coded to ['Free', 'Used'] for consistency
     * memoryNumbers: a Map with section names as keys, and values are a list of memory values (in MB) for each memory section
     * @return the info
     */
    Map<String, Object> memoryInfo() {

        List heapPoolNames = []
        Map heapNumbers = [:]
        generatePoolGraphData MemoryType.HEAP, heapPoolNames, heapNumbers

        List nonheapPoolNames = []
        Map nonheapNumbers = [:]
        generatePoolGraphData MemoryType.NON_HEAP, nonheapPoolNames, nonheapNumbers

        long memoryTotal = Runtime.runtime.totalMemory()
        long memoryFree = Runtime.runtime.freeMemory()
        long memoryUsed = memoryTotal - memoryFree

        List memoryNames = ['Heap']
        Map memoryNumbers = ['Free': [formatMB(memoryFree)],
                             'Used': [formatMB(memoryUsed)]]

        [heapPoolNames      : heapPoolNames,
         heapSectionNames   : heapNumbers.keySet(),
         heapNumbers        : heapNumbers,
         nonheapPoolNames   : nonheapPoolNames,
         nonheapSectionNames: nonheapNumbers.keySet(),
         nonheapNumbers     : nonheapNumbers,
         memoryNames        : memoryNames,
         memorySectionNames : memoryNumbers.keySet(),
         memoryNumbers      : memoryNumbers]
    }

    void generatePoolGraphData(MemoryType type, List poolNames, Map numbers) {

        numbers.Init = []
        numbers.Used = []
        numbers.Committed = []
        numbers.Max = []

        for (MemoryPoolMXBean bean : ManagementFactory.memoryPoolMXBeans) {
            if (bean.type == type) {
                numbers.Init << formatMB(bean.usage.init)
                numbers.Used << formatMB(bean.usage.used)
                numbers.Committed << formatMB(bean.usage.committed)
                numbers.Max << formatMB(bean.usage.max)
                poolNames << bean.name
            }
        }
    }

    BigDecimal formatMB(long value) {
        String formatted = new DecimalFormat('.000', new DecimalFormatSymbols(Locale.ENGLISH)).format(value / 1024.0 / 1024.0)
        formatted.toBigDecimal()
    }

}
