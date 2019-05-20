/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools

import groovy.transform.CompileStatic

import gorm.tools.repository.GormRepoEntity
import grails.gorm.transactions.Transactional

@Transactional
@CompileStatic
class MassUpdateService {

    /**
     * Applies changes to a list of domains.
     * @params changes A map containing all the changes to be made.
     * @params targets A list of ACTUAL DOMAIN OBJECTS to be altered.
     * @params validationMap A map of valid changes
     * @params rootDomainName The name of the parent property
     * @param getDomain a Closure which accepts a key (which is a property name) and returns a new domain to match it.
     */
    Map massUpdate(Map changes, List targets, Map validationMap, String rootDomainName, Closure getDomain) {
        Map<String, Object> safeChanges = validate(changes, validationMap)
        Map result = [ok: true, data: targets, safeChanges: safeChanges]
        if (!(safeChanges.keySet()?.size())) return result
        targets.each { target ->
            //DataBindingUtils.bindObjectToInstance(target, safeChanges)
            GormRepoEntity targetEnt = target as GormRepoEntity
            setProps(targetEnt, safeChanges, null, rootDomainName, getDomain)
            targetEnt.persist()
        }
        return result
    }

    private void setProps(GormRepoEntity domain, Map<String, Object> changes, Object rootDomain, String rootDomainName, Closure getDomain) {
        changes.each { key, value ->
            if (key != 'SPECIAL') {
                // SPECIAL is for things that need special processing.
                if (value instanceof Map) {
                    if (!domain[key]) {
                        domain[key] = getDomain(key, domain)
                        if (domain[key].hasProperty(rootDomainName)) {
                            domain[key][rootDomainName] = domain
                        }
                    }
                    setProps(domain[key] as GormRepoEntity, value, rootDomain ?: domain, key, getDomain)
                    domain.persist()
                } else {
                    if (domain.hasProperty(key)) {
                        domain[key] = value
                    }
                }
            }
        }
    }

    Map<String, Object> validate(Map changes, Map validationMap) {
        Map r = valRecurse(changes, validationMap)
        return r
    }

    private Map<String, Object> valRecurse(Map changes, Map validationMap) {
        Map<String, Object> result = [:]
        if (!validationMap || !changes) return result
        changes.each { key, value ->
            String skey = key as String
            if (validationMap.containsKey(skey)) {
                if (value instanceof Map && validationMap[skey] instanceof Map) {
                    result[skey] = valRecurse(value, validationMap[skey] as Map)
                } else {
                    if (validationMap[skey]) {
                        if (value instanceof String && value.length() == 0) {
                            value = null
                        }
                        result[skey] = value
                    }
                }
            }
        }
        return result
    }

}
