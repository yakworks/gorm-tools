/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.validation

import java.lang.reflect.Field
import java.lang.reflect.Modifier

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.codehaus.groovy.runtime.InvokerHelper
import org.grails.datastore.gorm.validation.constraints.eval.ConstraintsEvaluator
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.reflect.EntityReflector
import org.springframework.context.MessageSource
import org.springframework.util.ReflectionUtils
import org.springframework.validation.Errors

import gorm.tools.repository.GormRepo
import gorm.tools.repository.model.PersistableRepoEntity
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.PersistentEntityValidator

/**
 * A Validator that validates a {@link org.grails.datastore.mapping.model.PersistentEntity} against known constraints
 *
 * @author Graeme Rocher
 * @since 6.0
 */
@SuppressWarnings(['Println', 'FieldName'])
@CompileStatic
class RepoEntityValidator extends PersistentEntityValidator {

    public static final String API_CONSTRAINTS = 'constraintsMap'
    //private static final List<String> EMBEDDED_EXCLUDES = Arrays.asList(GormProperties.IDENTITY,GormProperties.VERSION)

    GormRepo gormRepo
    ConstraintsEvaluator constraintsEvaluator
    // Map<String, ConstrainedProperty> constrainedProperties = [:]

    RepoEntityValidator(PersistentEntity entity, MessageSource messageSource, ConstraintsEvaluator constraintsEvaluator) {
        super(entity, messageSource, constraintsEvaluator)
        this.constraintsEvaluator = constraintsEvaluator
        //turn it back into modifiable map so we can mess with it
        // Map<String, ConstrainedProperty> constrainedProps = [:]
        // constrainedProps.putAll(getConstrainedProperties())
        // setConstrainedProperties(PersistentEntityValidator, super, constrainedProps)

        //do the tweaks for external constraints
        //addConstraintsFromMap()
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

    //for future use, trickery to set the final field
    void setConstrainedProperties(Class clazz, Object obj, Map<String, ConstrainedProperty> map){
        //make the constrainedProperties accessible, remove private
        Field field = clazz.getDeclaredField("constrainedProperties")
        field.setAccessible(true)
        //remove final modifier
        Field modifiersField = Field.getDeclaredField("modifiers")
        modifiersField.setAccessible(true)
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL)
        //set the value now
        field.set(obj, map)
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

        //setup the path as super does
        String origNestedPath = errors.getNestedPath()
        errors.setNestedPath(buildNestedPath(origNestedPath, propertyName, indexOrKey))

        //fire event
        if(associatedObject instanceof PersistableRepoEntity){
            def repo = (GormRepo) InvokerHelper.invokeStaticMethod(associatedObject.class, 'getRepo', null)
            repo.publishBeforeValidate(associatedObject, errors)
        }

        // reset the nested path back to original as cascadeValidationToOne does it too.
        errors.setNestedPath(origNestedPath)

        super.cascadeValidationToOne(parentObject, propertyName, association, errors, reflector, associatedObject, indexOrKey, validatedObjects)

    }

    @CompileDynamic //so it can access the private super
    private String buildNestedPath(String nestedPath, String componentName, Object indexOrKey) {
        super.buildNestedPath(nestedPath, componentName, indexOrKey)
    }

}
