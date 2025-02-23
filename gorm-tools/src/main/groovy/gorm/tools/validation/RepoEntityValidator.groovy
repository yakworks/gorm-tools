/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.validation

import java.beans.Introspector

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.gorm.validation.constraints.AbstractConstraint
import org.grails.datastore.gorm.validation.constraints.NullableConstraint
import org.grails.datastore.gorm.validation.constraints.eval.ConstraintsEvaluator
import org.grails.datastore.mapping.model.MappingContext
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.config.GormProperties
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.reflect.EntityReflector
import org.springframework.context.MessageSource
import org.springframework.context.support.DefaultMessageSourceResolvable
import org.springframework.validation.BindingResult
import org.springframework.validation.Errors
import org.springframework.validation.FieldError

import gorm.tools.repository.GormRepo
import gorm.tools.repository.events.RepoEventPublisher
import gorm.tools.repository.model.PersistableRepoEntity
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.Constraint
import grails.gorm.validation.PersistentEntityValidator
import yakworks.commons.lang.ClassUtils
import yakworks.spring.AppCtx

/**
 * Overrides the PersistentEntityValidator to address a few things
 * - assocations are validated correctly
 * - beforeValidate event is fired for assocations properly too when its the default deep validate
 * - slims down the validations so that if it only has nullable:true then it skips those
 * - cleans up the message codes to be a sane number
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
        ApiConstraints apiConstraints = ApiConstraints.findApiConstraints(targetClass)
        //remove contraints that only have nullable so we dont validate them
        for (entry in constrainedProperties) {
            def constrainedProp = (ConstrainedProperty) entry.value
            String prop = (String) entry.key
            Collection<Constraint> appliedConstraints = constrainedProp.getAppliedConstraints()
            //if the only applied constraint is nullable:true, which is default, then skip it for performance reasons
            if(appliedConstraints?.size() == 1 && appliedConstraints[0] instanceof NullableConstraint){
                def nullableConst = (NullableConstraint) appliedConstraints[0]
                if(!nullableConst.isNullable()){ //only add it if nullable is false
                    slimConstrainedProperties[prop] = constrainedProp
                }
            } else if(appliedConstraints) { //it has more so let it flow
                slimConstrainedProperties[prop] = constrainedProp
            }
            // for(appliedConst in appliedConstraints){
            //     if(appliedConst instanceof AbstractConstraint){
            //         //TODO, not working
            //         replaceRejectValueWithDefaultMessage(appliedConst)
            //     }
            // }
        }
    }

    // //TODO not working, think we will need to replace the classes
    // @CompileDynamic
    // void replaceRejectValueWithDefaultMessage(AbstractConstraint appliedConst){
    //     appliedConst.metaClass.rejectValueWithDefaultMessage = { Object target, Errors errors, String defaultMessage, String[] codes, Object[] args ->
    //         def targetClass = target.class
    //         String classShortName = Introspector.decapitalize(targetClass.getSimpleName())
    //         String propName = (String)args[0]
    //         String code = (String)code[0]
    //
    //
    //         def newCodes = [] as Set<String>
    //         newCodes.add("${targetClass.getName()}.${propName}.${code}".toString())
    //         newCodes.add("${classShortName}.${propName}.${code}".toString())
    //         newCodes.add("${code}.${propName}".toString())
    //         newCodes.add(code)
    //
    //         FieldError error = new FieldError(
    //             errors.objectName,
    //             errors.nestedPath + propName,
    //             getPropertyValue(errors, target),
    //             false, //bind failure
    //             newCodes as String[],
    //             args,
    //             defaultMessage
    //         )
    //         ((BindingResult)errors).addError(error);
    //         // def abrErrors = errors as AbstractBindingResult //this has the addError method
    //         // abrErrors.addError(error)
    //     }
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
            def ferror = errors.getFieldError(constrainedPropertyName)
            if(ferror) {
                removeError(obj, constrainedPropertyName, errors, ferror)
            }
        }
    }

    /**
     * cleans up the insane number of message codes added for a validation error
     * only adds 3 for className.propname.code, simpleName.propname.code and propname.code.error and code
     */
    void removeError(Object target, String propName, Errors errors, FieldError fieldError){
        //base code
        String code = fieldError.code
        Map codeMap = JakartaValidationCodeAdaptor.codeMap
        Map messagesMap = JakartaValidationCodeAdaptor.messagesMap
        ValidationCode valCode = codeMap[code]
        // assert valCode, "failed finding $code"
        //just in case not found
        // String jakartaCode = messagesMap[valCode] ? messagesMap[valCode]['code'] : ''

        def newCodes = [] as Set<String>
        // String classShortName = Introspector.decapitalize(target.class.simpleName)

        // newCodes.add("${target.class.getName()}.${propName}.${code}".toString())
        // newCodes.add("${classShortName}.${propName}.${code}".toString())
        // newCodes.add("${propName}.${code}".toString())
        // newCodes.add("${propName}.${code}".toString())
        if(valCode?.jakartaCode) newCodes.add(valCode.jakartaCode)
        newCodes.add(valCode ? valCode.name() : code)
        //sets the private final for codes and args.
        ClassUtils.setFieldValue(DefaultMessageSourceResolvable, fieldError, 'codes', newCodes as String[])
        // icu4jArgs
        Object[] newArgs = icu4jArgs(valCode, fieldError.arguments)
        ClassUtils.setFieldValue(DefaultMessageSourceResolvable, fieldError, 'arguments', newArgs as Object[])
    }

    /**
     * See https://github.com/yakworks/spring-icu4j
     * To adapt spring messages we put the arg map as the first item in the array
     */
    Object[] icu4jArgs(ValidationCode valCode, Object[] arguments){
        //if only 3 args or none then return empty as it doesn' need them
        if(arguments == null || arguments.size() < 4) return [] as Object[]
        //most use arg 2 so get it.
        // value is the key used of the constraint is validating against, not the field value. Its the hibernate field validation defualt
        def value = arguments[3]
        Map argMap = [:]

        switch (valCode) {
            case ValidationCode.Range:
                //valid range from [{3}] to [{4}]
                //must be between {min} and {max}
                argMap.putAll([min: value, max: arguments[4]])
                break
            case ValidationCode.Min:
                //value [{3}]
                //equal to {value}'
                argMap.putAll([value: value])
                break
            case ValidationCode.Max:
                //value [{3}]
                //equal to {value}'
                argMap.putAll([value: value])
                break
            case ValidationCode.MinLength:
                //value [{3}]
                //'size must be between {min} and {max}'
                argMap.putAll([value: value])
                break
            case ValidationCode.MaxLength:
                //value [{3}]
                //'size must be between {min} and {max}'
                argMap.putAll([value: value])
                break
            case ValidationCode.Pattern:
                // match the required pattern [{3}]
                //'must match "{regexp}"'
                argMap.putAll([regexp: value])
                break
            case ValidationCode.InList:
                //list [{3}]
                //'must match "{value}"'
                argMap.putAll([value: value])
                break
            case ValidationCode.NotEqual:
                //  equal [{3}]
                //'must match "{value}"'
                argMap.putAll([value: value])
                break
            // default:
            //     argMap = [:]
        }

        return argMap ? [argMap] as Object[] : [] as Object[]
    }

}
