/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.validation


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.validation.constraints.NullableConstraint
import org.grails.datastore.gorm.validation.constraints.eval.ConstraintsEvaluator
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.config.GormProperties
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.reflect.EntityReflector
import org.springframework.context.MessageSource
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.beans.AppCtx
import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.PersistableRepoEntity
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.Constraint
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
    private static final List<String> EMBEDDED_EXCLUDES = Arrays.asList(
        GormProperties.IDENTITY,
        GormProperties.VERSION)

    public static final String API_CONSTRAINTS = 'constraintsMap'
    //private static final List<String> EMBEDDED_EXCLUDES = Arrays.asList(GormProperties.IDENTITY,GormProperties.VERSION)

    RepoEventPublisher repoEventPublisher
    Map<String, ConstrainedProperty> slimConstrainedProperties = [:]
    // ConstraintsEvaluator constraintsEvaluator
    // Map<String, ConstrainedProperty> constrainedProperties = [:]

    RepoEntityValidator(PersistentEntity entity, MessageSource messageSource, ConstraintsEvaluator constraintsEvaluator) {
        super(entity, messageSource, constraintsEvaluator)
        //this.constraintsEvaluator = constraintsEvaluator
        repoEventPublisher = AppCtx.get('repoEventPublisher', RepoEventPublisher)
        setupSlimConstraints()
        //turn it back into modifiable map so we can mess with it
        // setConstrainedProperties(PersistentEntityValidator, super, constrainedProps)
        //do the tweaks for external constraints
        //addConstraintsFromMap()
    }

    /**
     * performance tweak to setup a skinny slimmed down map of properties that should be validated.
     * make a millisecond difference on one single items but its a "game of inches" with large batch processing.
     * adds up when its has to spin through and validate nullable across 50 properties a 100,000 times
     * in an entity and 100's of 100's of rows.
     */
    void setupSlimConstraints(){
        //Map<String, ConstrainedProperty> slimConstrainedProps = [:]
        //newConstrainedProps.putAll(getConstrainedProperties())
        ApiConstraints apiConstraints = ApiConstraints.findApiConstraints(targetClass)
        //remove contraints that only have nullable so we dont validate them
        for (entry in constrainedProperties) {
            def constrainedProp = (ConstrainedProperty) entry.value
            String prop = (String) entry.key
            Collection<Constraint> appliedConst = constrainedProp.getAppliedConstraints()
            //if the only applied constraint is nullable:true, which is default, then skip it for performance reasons
            if(appliedConst?.size() == 1 && appliedConst[0] instanceof NullableConstraint){
                def nullableConst = (NullableConstraint) appliedConst[0]
                if(!nullableConst.isNullable()){ //only add it if nullable is false
                    slimConstrainedProperties[prop] = constrainedProp
                }
                // else {
                //     //add it to the nonValidate onese
                //     apiConstraints.addNonValidatedProperty(prop, constrainedProp)
                // }
            } else if(appliedConst) { //it has more so let it flow
                slimConstrainedProperties[prop] = constrainedProp
            }
        }
        // ClassUtils.setPrivateFinal(PersistentEntityValidator, super, 'constrainedProperties', slimConstrainedProperties)
    }

    //
    // @Override
    // void validate(Object obj, Errors errors, boolean cascade = true) {
    //     if (obj == null || !targetClass.isInstance(obj)) {
    //         throw new IllegalArgumentException("Argument [$obj] is not an instance of [$targetClass] which this validator is configured for")
    //     }
    //     fireValidateEvent(obj, errors)
    //     super.validate(obj, errors, cascade)
    // }

    @Override
    void validate(Object obj, Errors errors, boolean cascade = true) {
        if (obj == null || !targetClass.isInstance(obj)) {
            throw new IllegalArgumentException("Argument [$obj] is not an instance of [$targetClass] which this validator is configured for")
        }

        fireValidateEvent(obj, errors)

        Map<String, ConstrainedProperty> constrainedProperties = this.slimConstrainedProperties
        Set<String> constrainedPropertyNames = new HashSet<>(constrainedProperties.keySet())

        def validatedObjects = [] as Set
        validatedObjects.add(obj)

        for(PersistentProperty pp in entity.persistentProperties) {
            def propertyName = pp.name

            ConstrainedProperty constrainedProperty = constrainedProperties.get(propertyName)

            if(constrainedProperty != null) {
                validatePropertyWithConstraint(obj, propertyName, entityReflector, errors, constrainedProperty, pp)
            }

            if(pp instanceof Association) {
                Association association = (Association)pp
                if(cascade) {
                    cascadeToAssociativeProperty(obj, errors, entityReflector, association, validatedObjects)
                }
            }

            constrainedPropertyNames.remove(propertyName)
        }

        for(String remainingProperty in constrainedPropertyNames) {
            ConstrainedProperty constrainedProperty = constrainedProperties.get(remainingProperty)
            if(remainingProperty != null) {
                validatePropertyWithConstraint(obj, remainingProperty, entityReflector, errors, constrainedProperty, null)
            }
        }

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
        if (associatedObject == null) {
            return
        }

        if(validatedObjects.contains(associatedObject)) {
            return
        }

        validatedObjects.add(associatedObject)

        PersistentEntity associatedEntity = association.getAssociatedEntity()
        if (associatedEntity == null) {
            return
        }

        // Make sure this object is eligible to cascade validation at all.
        if (!association.doesCascadeValidate(associatedObject)) {
            return
        }

        MappingContext mappingContext = associatedEntity.getMappingContext()
        EntityReflector associatedReflector = mappingContext.getEntityReflector(associatedEntity)

        Association otherSide = null
        if (association.isBidirectional()) {
            otherSide = association.getInverseSide()
        }

        Map associatedConstrainedProperties

        def validator = mappingContext.getEntityValidator(associatedEntity)
        if(validator instanceof RepoEntityValidator) {
            associatedConstrainedProperties = ((RepoEntityValidator)validator).slimConstrainedProperties
        }
        else if(validator instanceof PersistentEntityValidator) {
            associatedConstrainedProperties = ((PersistentEntityValidator)validator).getConstrainedProperties()
        }
        else {
            associatedConstrainedProperties = Collections.<String, ConstrainedProperty>emptyMap()
        }

        // Invoke any beforeValidate callbacks on the associated object before validating
        validateHelper.invokeBeforeValidate(associatedObject, associatedConstrainedProperties.keySet() as List<String>)

        List<PersistentProperty> associatedPersistentProperties = associatedEntity.getPersistentProperties()
        String origNestedPath = errors.getNestedPath()
        try {
            errors.setNestedPath(buildNestedPath(origNestedPath, propertyName, indexOrKey))
            //fire event
            fireValidateEvent(associatedObject, errors)

            for (PersistentProperty associatedPersistentProperty : associatedPersistentProperties) {
                if (association.isEmbedded() && EMBEDDED_EXCLUDES.contains(associatedPersistentProperty.getName())) {
                    continue
                }

                String associatedPropertyName = associatedPersistentProperty.getName()
                if (associatedConstrainedProperties.containsKey(associatedPropertyName)) {
                    ConstrainedProperty associatedConstrainedProperty = associatedConstrainedProperties.get(associatedPropertyName)
                    validatePropertyWithConstraint(associatedObject, errors.getNestedPath() + associatedPropertyName, associatedReflector,
                        errors, associatedConstrainedProperty, associatedPersistentProperty)
                }

                // Don't continue cascade if the the other side is equal to avoid stack overflow
                if (associatedPersistentProperty == otherSide) {
                    continue
                }

                if (associatedPersistentProperty instanceof Association) {
                    if(association.isBidirectional() && associatedPersistentProperty == association.inverseSide) {
                        // If this property is the inverse side of the currently processed association then
                        // we don't want to process it
                        continue
                    }

                    cascadeToAssociativeProperty(
                        associatedObject,
                        errors,
                        associatedReflector,
                        (Association)associatedPersistentProperty,
                        validatedObjects)
                }
            }
        }
        finally {
            errors.setNestedPath(origNestedPath)
        }
    }

    void fireValidateEvent(Object obj, Errors errors){
        if(obj instanceof PersistableRepoEntity){
            repoEventPublisher.doBeforeValidate((GormRepo)obj.findRepo(), obj, errors, [:])
        }
    }

    @SuppressWarnings('UnnecessaryOverridingMethod')
    @CompileDynamic //so it can access the private super
    private String buildNestedPath(String nestedPath, String componentName, Object indexOrKey) {
        super.buildNestedPath(nestedPath, componentName, indexOrKey)
    }

    private void validatePropertyWithConstraint(Object obj, String propertyName, EntityReflector reflector, Errors errors,
                                                ConstrainedProperty constrainedProperty, PersistentProperty persistentProperty) {

        String constrainedPropertyName = propertyName
        int i = propertyName.lastIndexOf(".")
        if (i > -1) constrainedPropertyName = propertyName.substring(i + 1, propertyName.length())

        FieldError fieldError = errors.getFieldError(constrainedPropertyName)
        if (fieldError == null) {
            if(persistentProperty != null) {
                constrainedProperty.validate(obj, reflector.getProperty(obj, constrainedPropertyName), errors)
            }
            else {
                if(obj instanceof GroovyObject) {
                    constrainedProperty.validate(obj, ((GroovyObject)obj).getProperty(constrainedPropertyName), errors)
                }
            }
        }
    }

}
