/*
* Copyright 2024 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.spring

import java.nio.charset.StandardCharsets

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.util.MultiValueMap
import org.springframework.web.util.UriComponentsBuilder

@CompileStatic
@Slf4j
class QueryParamsUtil {

    /**
     * Parses name=xyz&size=123 query string into a map
     * Uses springs UriComponentsBuilder
     * @param queryString the query params string to parse
     * @return Map<String,String> the map version of the parsed string
     */
    static Map<String, String[]> parseQueryString(String queryString) {
        if(!queryString) return [:]
        MultiValueMap<String, String> queryParams = UriComponentsBuilder.fromUriString('?'+queryString).build().getQueryParams()
        return toArrayMap(queryParams)
    }

    private static Map<String, String[]> toArrayMap(MultiValueMap<String, String> mvMap) {
        Map decodedMap = [:] as Map<String, String[]>
        //decode each one now
        mvMap.each { String k, List<String> v ->
            List<String> decodedVals = v ? v.collect{ decode(it) }  : []
            decodedMap[decode(k)] = decodedVals as String[]
        }
        return decodedMap
    }

    private static String decode(String s) {
        if(!s) return ""
        URLDecoder.decode(s, StandardCharsets.UTF_8)
    }
}
