/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.validation


import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.gorm.validation.constraints.eval.ConstraintsEvaluator
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.config.GormProperties
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.reflect.EntityReflector
import org.springframework.context.MessageSource
import org.springframework.validation.Errors

import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import grails.gorm.validation.PersistentEntityValidator

/**
 * A Validator that validates a {@link org.grails.datastore.mapping.model.PersistentEntity} against known constraints
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@CompileStatic
class RepoEntityValidator extends PersistentEntityValidator {

    //private static final List<String> EMBEDDED_EXCLUDES = Arrays.asList(GormProperties.IDENTITY,GormProperties.VERSION)

    GormRepo gormRepo

    RepoEntityValidator(PersistentEntity entity, MessageSource messageSource, ConstraintsEvaluator constraintsEvaluator) {
        super(entity, messageSource, constraintsEvaluator)
        //gormRepo = (GormRepo) InvokerHelper.invokeStaticMethod(entity.javaClass, 'getRepo', null)
    }

    @Override
    void validate(Object obj, Errors errors, boolean cascade = true) {
        if (obj == null || !targetClass.isInstance(obj)) {
            throw new IllegalArgumentException("Argument [$obj] is not an instance of [$targetClass] which this validator is configured for")
        }
        //GormRepo.metaClass.getStaticMetaMethod('getRepo')
        if(obj instanceof PersistableRepoEntity){
            if(!gormRepo) gormRepo = (GormRepo) InvokerHelper.invokeStaticMethod(obj.class, 'getRepo', null)
            gormRepo.publishBeforeValidate(obj, errors)
        }
        super.validate(obj, errors, cascade)
    }

    /**
     * Cascades validation to a one-to-one or many-to-one property.
     *
     * @param errors The Errors instance
     * @param bean The original BeanWrapper
     * @param associatedObject The associated object's current value
     * @param association The GrailsDomainClassProperty instance
     * @param propertyName The name of the property
     */
    @SuppressWarnings("rawtypes")
    @Override
    protected void cascadeValidationToOne(Object parentObject, String propertyName, Association association, Errors errors, EntityReflector reflector,
                                          Object associatedObject, Object indexOrKey, Set validatedObjects) {
        if (!associatedObject || validatedObjects.contains(associatedObject)
            || !association.getAssociatedEntity() || !association.doesCascadeValidate(associatedObject)) {
            return
        }
        String origNestedPath = errors.getNestedPath()
        errors.setNestedPath(buildNestedPath(origNestedPath, propertyName, indexOrKey))

        if(associatedObject instanceof PersistableRepoEntity){
            def repo = (GormRepo) InvokerHelper.invokeStaticMethod(associatedObject.class, 'getRepo', null)
            repo.publishBeforeValidate(associatedObject, errors)
        }

        // reset the nested path back to original as cascadeValidationToOne does it
        errors.setNestedPath(origNestedPath)
        super.cascadeValidationToOne(parentObject, propertyName, association, errors, reflector, associatedObject, indexOrKey, validatedObjects)

    }

    private String buildNestedPath(String nestedPath, String componentName, Object indexOrKey) {
        if (indexOrKey == null) {
            // Component is neither part of a Collection nor Map.
            return nestedPath + componentName
        }

        if (indexOrKey instanceof Integer) {
            // Component is part of a Collection. Collection access string
            // e.g. path.object[1] will be appended to the nested path.
            return nestedPath + componentName + "[" + indexOrKey + "]"
        }

        // Component is part of a Map. Nested path should have a key surrounded
        // with apostrophes at the end.
        return nestedPath + componentName + "['" + indexOrKey + "']"
    }

}
