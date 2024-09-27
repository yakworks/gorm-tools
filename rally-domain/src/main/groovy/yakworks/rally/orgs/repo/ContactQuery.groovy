/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.orgs.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.springframework.context.annotation.Lazy
import org.springframework.stereotype.Service

import yakworks.rally.orgs.model.Contact
import yakworks.rally.tag.TaggableQueryService

@Service @Lazy
@CompileStatic
@Slf4j
class ContactQuery extends TaggableQueryService<Contact> {

    ContactQuery() {
        super(Contact)
    }
}
