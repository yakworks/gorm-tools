/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.validation


import groovy.transform.CompileStatic

import org.grails.datastore.gorm.validation.javax.JavaxValidatorRegistry
import org.grails.datastore.mapping.core.connections.ConnectionSourceSettings
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.springframework.context.MessageSource
import org.springframework.context.support.StaticMessageSource
import org.springframework.validation.Validator
import org.springframework.validation.annotation.Validated

/**
 * A validator registry that creates validators
 *
 * @author Graeme Rocher
 * @since 6.1
 */
@CompileStatic
class RepoValidatorRegistry extends JavaxValidatorRegistry {

    RepoValidatorRegistry(MappingContext mappingContext, ConnectionSourceSettings settings, MessageSource messageSource = new StaticMessageSource()) {
        super(mappingContext, settings, messageSource)
    }

    @Override
    Validator getValidator(PersistentEntity entity) {
        def ann = entity.javaClass.getAnnotation(Validated)
        if(ann != null && isAvailable()) {
            return super.getValidator(entity)
        }
        else {
            //HERE BE DRAGONS
            return getRepoValidator(entity)
        }
    }

    Validator getRepoValidator(PersistentEntity entity) {
        Validator validator = validatorMap.get(entity)
        if(validator != null) {
            return validator
        }
        else {
            validator = new RepoEntityValidator(entity, messageSource, constraintsEvaluator)
            validatorMap.put(entity, validator)
        }
        return validator
    }

}
