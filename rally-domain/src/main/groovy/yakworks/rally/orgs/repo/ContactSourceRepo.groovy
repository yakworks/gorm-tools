/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic

import gorm.tools.model.SourceType
import gorm.tools.repository.GormRepository
import gorm.tools.repository.model.LongIdGormRepo
import grails.gorm.transactions.Transactional
import yakworks.commons.lang.EnumUtils
import yakworks.rally.orgs.model.Contact
import yakworks.rally.orgs.model.ContactSource

@GormRepository
@CompileStatic
class ContactSourceRepo extends LongIdGormRepo<ContactSource> {

    /**
     * lookup by sourceId
     * This is called from findWithData and is used to locate contact for updates and associtaions
     */
    @Override
    ContactSource lookup(Map data) {
        if(data.sourceId) return ContactSource.findWhere(sourceId: data.sourceId)
        return null
    }

    Long findContactIdBySourceId(String sid) {
        List<Long> results = ContactSource.executeQuery('select contactId from ContactSource where sourceId = :sourceId',
            [sourceId: sid]) as List<Long>

        return results ? results[0] : null
    }

    /**
     * Creates the source record from the data
     * @param c the contact
     * @param data with the source key or the sourceId information
     * @return the created source or null if no source info was passed in
     */
    ContactSource createSource(Contact c, Map data) {
        Map sourceData = [:]
        if(data.source && data.source instanceof Map){
            sourceData.putAll(data.source as Map)
        } else {
            ['sourceId', 'sourceType', 'source'].each {
                if(data[it]) sourceData[it] = data.remove(it)
            }
        }
        //if its empty dont do it.
        //FIXME should we not make this required like OrgSource, when we do reexamine the defaults in createSource below
        if(sourceData.isEmpty()) return null

        String sourceId = (String)sourceData['sourceId']
        String source = (String)sourceData['source']
        SourceType st = sourceData['sourceType'] ? EnumUtils.getEnumIgnoreCase(SourceType, (String)sourceData['sourceType']) : null
        //update sourceId with num if its not set
        if(!sourceId) {
            sourceId = c.num ?: c.id
        }

        return createSource(c, sourceId, st, source)
    }

    /** creates the ContactSource */
    @Transactional
    ContactSource createSource(Contact contact, String sourceId, SourceType sourceType, String source) {
        ContactSource cs = new ContactSource()
        cs.contactId = contact.id
        cs.sourceId = sourceId
        //FIXME we should not default these
        cs.source = source ?: 'External'
        cs.sourceType = sourceType ?: SourceType.ERP
        cs.persist()
        return cs
    }
}
