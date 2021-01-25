/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rally.tag.repo

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import gorm.tools.repository.GormRepository
import yakworks.rally.tag.model.TagLink

@Slf4j
@GormRepository
@CompileStatic
class TagLinkRepo implements TagLinkRepoTrait<TagLink> {


}
