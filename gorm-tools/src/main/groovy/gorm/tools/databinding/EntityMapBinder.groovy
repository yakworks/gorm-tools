/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.databinding

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

import groovy.transform.CompileStatic

import org.apache.commons.lang3.StringUtils
import org.grails.core.artefact.AnnotationDomainClassArtefactHandler
import org.grails.core.artefact.DomainClassArtefactHandler
import org.grails.core.exceptions.GrailsConfigurationException
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormStaticApi
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.springframework.context.MessageSource
import org.springframework.core.convert.ConversionService
import org.springframework.core.convert.support.DefaultConversionService
import org.springframework.validation.AbstractBindingResult
import org.springframework.validation.BeanPropertyBindingResult
import org.springframework.validation.BindingResult
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError

import gorm.tools.utils.GormMetaUtils
import grails.databinding.DataBindingSource
import grails.databinding.SimpleDataBinder
import grails.databinding.SimpleMapDataBindingSource
import grails.databinding.converters.ValueConverter
import grails.databinding.events.DataBindingListener
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import grails.util.Environment
import grails.validation.ValidationErrors
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.IsoDateUtil

import static gorm.tools.utils.GormMetaUtils.getEntityClass

/**
 * Faster data binder for PersistentEntity.persistentProperties. Uses the persistentProperties to assign values from the Map
 * Explicitly checks and converts most common property types eg (numbers and dates). Otherwise fallbacks to value converters.
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1
 */
@SuppressWarnings("EmptyClass")
@CompileStatic
class EntityMapBinder extends SimpleDataBinder implements MapBinder {

    /**
     * A map that holds lists of properties which should be bound manually by a binder.
     * A key represents a domain class and the value is a list with properties.*/
    static final Map<Class, List> EXPLICIT_BINDING_LIST = new ConcurrentHashMap<Class, List>()
    protected static final Map<Class, List> CLASS_TO_BINDING_INCLUDE_LIST = new ConcurrentHashMap<Class, List>()

    protected MessageSource messageSource
    boolean trimStrings = true
    boolean convertEmptyStringsToNull = true
    protected List<DataBindingListener> listeners = []
    protected ConversionService springConversionService

    EntityMapBinder() {
        this.springConversionService = new DefaultConversionService()
        // registerConverter new ByteArrayMultipartFileValueConverter()
    }

    /**
     * Binds data from a map on target object.
     *
     * @param obj The target object to bind
     * @param source The data binding source
     */
    @Override
    void bind(Object obj, DataBindingSource source) {
        bind obj, source, null, getBindingIncludeList(obj), null, null
    }

    /**
     * Binds data from a map on target object.
     *
     * @param obj The target object to bind
     * @param source The data binding source
     * @param listener DataBindingListener
     */
    @Override
    void bind(Object obj, DataBindingSource source, DataBindingListener listener) {
        bind obj, source, null, getBindingIncludeList(obj), null, listener
    }

    /**
     * Binds data from a map on target object.
     *
     * @param obj The target object to bind
     * @param source The data binding source
     * @param filter Only properties beginning with filter will be included in the data binding.
     * @param whiteList A list of property names to be included during this
     * data binding.  All other properties represented in the binding source
     * will be ignored
     * @param blackList A list of properties names to be excluded during
     * this data binding.
     * @param listener DataBindingListener
     */
    @Override
    void bind(Object object, DataBindingSource source, String filter, List whiteList, List blackList, DataBindingListener listener) {

        String className = getEntityClass(object).name

        BindingResult bindingResult = new BeanPropertyBindingResult(object, className)
        doBind object, source, filter, whiteList, blackList, listener, bindingResult
    }

    @Override
    protected void doBind(Object object, DataBindingSource source, String filter, List whiteList, List blackList,
                          DataBindingListener listener, Object errors) {

        EntityMapBindingListener errorHandlingListener = new EntityMapBindingListener(messageSource)

        fastBind(object, source, whiteList, errorHandlingListener, errors)

        def bindingResult = (BeanPropertyBindingResult) errors
        if(bindingResult.hasErrors()) populateErrors(object, bindingResult)
    }

    /**
     * Binds data from a map on target object.
     *
     * @param args An optional map of options. supports two boolean options
     * <ul>
     *     <li><b>include</b> The list of properties to include in data binding</li>
     *     <li><b>errorHandling</b> If type conversion error should be handled and Added to Errors</li>
     * </ul>
     *
     * @param target The target object to bind
     * @param source The data binding source
     */
    @Override
    void bind(Map args = [:], Object target, Map<String, Object> source) {
        //BindAction bindAction = (BindAction) args["bindAction"]

        List includeList = (List) args["include"] ?: getBindingIncludeList(target)
        Boolean errorHandling = args["errorHandling"] == null ? true : args["errorHandling"]

        if (errorHandling) {
            bind target, new SimpleMapDataBindingSource(source), null, includeList, null, null
        } else {
            fastBind target, new SimpleMapDataBindingSource(source), includeList
        }
    }

    /**
     * Binds properties which specified in a white list on the given entity.
     * In case the white list is empty method takes the list of persistent properties and iterates on them.
     *
     * @param target a target entity
     * @param source a data binding source which contains property values
     * @param whiteList a list which contains properties for binding
     * @param listener DataBindingListener
     * @param errors the errors object to add binding to
     */
    void fastBind(Object target, DataBindingSource source, List whiteList = null, DataBindingListener listener =
            null, Object errors = null) {
        Objects.requireNonNull(target, "Target is null")
        if (!source) return
        GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(target.getClass() as Class)
        PersistentEntity entity = gormStaticApi.gormPersistentEntity
        List<String> properties = whiteList ?: entity.persistentPropertyNames

        for (String prop : properties) {
            PersistentProperty perProp = entity.getPropertyByName(prop)
            try {
                setProp(target, source, perProp, listener, errors)
            } catch (Exception e) {
                if (errors) {
                    addBindingError(target, perProp.name, source.getPropertyValue(perProp.name), e, listener, errors)
                } else {
                    throw e
                }
            }
        }
    }

    /**
     * Quick way to convert a string to basic type such as Date, LocalDate, LocalDateTime and number
     * if its a string it trims it and returns a null.
     *
     * @param sval the string value to parse to typeToConvertTo
     * @param typeToConvertTo the Class to try and convert
     * @return the converted object, or a NotParsed object if not converted
     */
    static Object parseBasicType(String sval, Class typeToConvertTo){
        Object valueToAssign = Boolean.FALSE
        //do we have tests for this?
        if (String.isAssignableFrom(typeToConvertTo)) {
            valueToAssign = sval
        } else if (Date.isAssignableFrom(typeToConvertTo)) {
            valueToAssign = IsoDateUtil.parse(sval)
        } else if (LocalDate.isAssignableFrom(typeToConvertTo)) {
            valueToAssign = IsoDateUtil.parseLocalDate(sval)
            //LocalDate.parse(val, DateTimeFormatter.ISO_DATE_TIME)
        } else if (LocalDateTime.isAssignableFrom(typeToConvertTo)) {
            valueToAssign = IsoDateUtil.parseLocalDateTime(sval)
        } else if (Number.isAssignableFrom(typeToConvertTo)) {
            valueToAssign = sval.asType(typeToConvertTo as Class<Number>)
        } else {
            valueToAssign = new UnParsed()
        }
        return valueToAssign
    }

    static class UnParsed {
        // see good explanation of thread safe static instance stratgey https://stackoverflow.com/a/16106598/6500859
        @SuppressWarnings('UnusedPrivateField')
        private static class Holder {
            private static final UnParsed INSTANCE = new UnParsed();
        }

        static UnParsed getInstance() {
            return Holder.INSTANCE
        }
    }

    /**
     * Sets a value to a specified target's property.
     *
     * @param target a target entity
     * @param source a data binding source which contains property values
     * @param prop a persistent property which should be filled with the value
     * @param listener DataBindingListener
     * @param errors the erros object to add the binding errors to
     */
    void setProp(Object target, DataBindingSource source, PersistentProperty prop,
                 DataBindingListener listener = null, Object errors = null) {

        String propName = getPropertyNameToUse(source, prop)
        if (!propName) return

        Object propValue = source.getPropertyValue(propName)

        Object valueToAssign = propValue
        // if its string this wil get populated

        Class<?> typeToConvertTo = prop.getType() as Class<?>

        //try association first
        if (prop instanceof Association) {
            bindAssociation(target, valueToAssign, (Association) prop, listener, errors)
        }
        else if (propValue instanceof String) {
            //trim and make null if empty
            String sval = processStringValue(propValue)
            Object parsedVal = sval

            if(sval != null) {
                //attempt all other basic conversions, only if value is not null/empty
                parsedVal = parseBasicType(sval, typeToConvertTo)

                // if its not a basic type then try the conversionHelpers
                if (parsedVal instanceof UnParsed) {
                    if (conversionHelpers.containsKey(typeToConvertTo)) {
                        List<ValueConverter> convertersList = conversionHelpers.get(typeToConvertTo)
                        ValueConverter converter = convertersList?.find { ValueConverter c -> c.canConvert(propValue) }
                        if (converter) {
                            parsedVal = converter.convert(propValue)
                        }
                    } else if (springConversionService?.canConvert(propValue.getClass(), typeToConvertTo)) {
                        parsedVal = springConversionService.convert(propValue, typeToConvertTo as Class<Object>)
                    }
                }
            }
            // if it has a parsed value then set it and return as we are good
            if(!(parsedVal instanceof UnParsed)){
                target[prop.name] = parsedVal
                //we are good so return
            }
        }
        // if its an enum and value if number or map
        else if (typeToConvertTo.isEnum() && (valueToAssign instanceof Number || valueToAssign instanceof Map)){
            //if its a map then it should be in form [id:1, ...] and it will grab id
            def idVal = valueToAssign //assume its a number
            if(valueToAssign instanceof Map) idVal = valueToAssign['id']
            // assume its an enum with a get(id)
            target[prop.name] = getEnumWithGet(typeToConvertTo, idVal as Number)
        }
        else {
            //its a something other than string, or enum and its not an association.
            // Try value converters again
            ValueConverter converter
            if (conversionHelpers.containsKey(typeToConvertTo)) {
                List<ValueConverter> convertersList = conversionHelpers.get(typeToConvertTo)
                converter = convertersList?.find { ValueConverter c -> c.canConvert(propValue) }
            }
            if (converter) {
                target[prop.name] = converter.convert(propValue)
            } else {
                target[prop.name] = valueToAssign
            }
        }

    }

    protected processStringValue(String sval) {
        return sval.trim() ?: null
    }

    def getEnumWithGet(Class<?> enumClass, Number id){
        return ClassUtils.callStaticMethod(enumClass, 'get', id)
    }

    /**
     * checks is DataBindingSource containsProperty
     * and if PersistentProperty instanceof Association appends Id to see if it exists and uses that
     */
    String getPropertyNameToUse(DataBindingSource source, PersistentProperty prop) {
        boolean hasIt = source.containsProperty(prop.name)
        if (!hasIt && prop instanceof Association && source.containsProperty("${prop.name}Id")) {
            return "${prop.name}Id"
        }
        return hasIt ? prop.name : null
    }

    /**
     * Binds a given association to the target entity.
     * It checks if the given value contains an id and loads the associated entity.
     * In case the id is not specified, this method checks if the given association belongs to the target entity.
     * In case it does, or the it has the explicitly specified 'bindable:true' constraint, then a new instance is
     * created for the association.
     *
     * @param target a target entity to bind an association to
     * @param value an association's value
     * @param association an association property
     * @param listener DataBindingListener
     */
    void bindAssociation(Object target, Object value, Association association, DataBindingListener listener = null,
                         Object errors = null) {
        String aprop = association.name

        //return early if its a collection type - eg hasMany
        if(Collection.isAssignableFrom(association.getType())) return

        //if value is null or they are the same instance type then just set and exit fast
        if (value == null || association.getType().isAssignableFrom(value.getClass())) {
            target[aprop] = value
            return
        }

        if (value instanceof String) {
            String sval = value as String
            //if its an empty string then its nothing so return
            if(!sval.trim()) return
            //convert to long if its numeric
            if(StringUtils.isNumeric(sval.trim())) value = sval as Long
        }

        // if its a number then its the identifier so set it
        if (value instanceof Number) {
            bindNewAssociationIfNeeded(target, aprop, value)
            return
        }

        //at this point if its assumed the value is mostlikely a Map or some object with an id property or maybe a UUID
        Object idValue = isDomainClass(value.getClass()) ? value['id'] : getIdentifierValueFrom(value)
        idValue = idValue == 'null' ? null : idValue

        //on CSV and form binds id might be a string value
        // check for empty string id and return if so
        if(value instanceof String && (idValue as String).trim() == ''){
            return
        }

        if (idValue) {
            // check if the target[aprop].id is the same and we don't need to do anything or
            // the target's property is null and we should bind it
            bindNewAssociationIfNeeded(target, aprop, idValue as Serializable)

            //bind if not null, map has values other then id, and the association is owning side or bindable
            if(target[aprop] && value instanceof Map && value.size() > 1 && shouldBindAssociation(target, association)) {
                fastBind(target[aprop], new SimpleMapDataBindingSource((Map) value), getBindingIncludeList(target[aprop]))
            }

        }
        //else no id the setup new association use the value's map
        else if (shouldBindAssociation(target, association)) {

            if (!(value instanceof Map)) {
                String msg = "Unable to create an association instance for the entity=${target}, the value=$value is not a Map"
                throw new IllegalArgumentException(msg)
            }
            //if its null then set it up
            if (target[aprop] == null) target[aprop] = association.type.newInstance()
            //recursive call to set the association up and assume its a map
            fastBind(target[aprop], new SimpleMapDataBindingSource((Map) value), getBindingIncludeList(target[aprop]))
        }
    }

    /**
     * if target prop is null or id is different then load and assign it
     */
    void bindNewAssociationIfNeeded(Object target, String assocName, Serializable ident){
        def assocProp = target[assocName]
        if (!assocProp || (assocProp && (target["${assocName}Id"] != ident))) {
            //we are setting it to a new id so load it and assign
            target[assocName] = getPersistentInstance(getDomainClassType(target, assocName), ident)
        }
    }

    /**
     * Check if association is bindable.
     * An association is bindable, if it is owning side, or if explicit bindable:true
     */
    private boolean shouldBindAssociation(Object target, Association association) {
        return (association.isOwningSide() || isExplicitBind(target, association.name))
    }

    /**
     * Checks if a given association is explicitly marked as bindable and should be binded in any case.
     *
     * @param target an entity which contains an association
     * @param name a name of an association to check if it should be binded
     * @return true if the association name with prefix is present in the white list
     */
    static boolean isExplicitBind(Object target, String name) {
        List explicitBindingForClass = EXPLICIT_BINDING_LIST.get(target.getClass())
        return explicitBindingForClass && explicitBindingForClass.contains(name)
    }

    /**
     * Sets up a list of properties which can be binded to a given domain entity.
     *
     * Puts the created list to ${@code CLASS_TO_BINDING_INCLUDE_LIST} map,
     * which caches such lists for domain classes.
     *
     * The method also checks if a property has an explicitly defined 'bindable:true' constraint.
     * In case the constraint is present, the property name is added to {@code EXPLICIT_BINDING_LIST}.
     *
     * @param object an entity for which the list should be created
     * @return a list of properties which can be bound to the given entity
     */
    static List getBindingIncludeList(final Object object) {
        List<String> whiteList = []
        final Class<? extends Object> objectClass = object.getClass()
        if (CLASS_TO_BINDING_INCLUDE_LIST.containsKey(objectClass)) {
            whiteList = CLASS_TO_BINDING_INCLUDE_LIST.get objectClass
        } else {
            GormStaticApi gormStaticApi = GormEnhancer.findStaticApi(object.getClass() as Class)
            PersistentEntity entity = gormStaticApi.gormPersistentEntity
            List<PersistentProperty> properties = entity.persistentProperties
            Map<String, ConstrainedProperty> constraints = GormMetaUtils.findAllConstrainedProperties(entity)
            List explicitBindingList = EXPLICIT_BINDING_LIST.get(objectClass) ?: []
            for (PersistentProperty prop : properties) {
                DefaultConstrainedProperty cp = (DefaultConstrainedProperty) constraints[prop.name]
                Boolean bindable = cp?.getMetaConstraintValue("bindable") as Boolean
                if (bindable == null || bindable == true) {
                    whiteList.add(prop.name)
                }
                if (bindable == true) {
                    explicitBindingList.add(prop.name)
                }
            }
            if (!Environment.getCurrent().isReloadEnabled()) {
                CLASS_TO_BINDING_INCLUDE_LIST.put objectClass, whiteList
            }
            if (explicitBindingList) EXPLICIT_BINDING_LIST.put(objectClass, explicitBindingList)
        }

        return whiteList
    }

    protected getIdentifierValueFrom(Object source) {
        def idValue = null
        if(source instanceof DataBindingSource && ((DataBindingSource)source).hasIdentifier()) {
            idValue = source.getIdentifierValue()
        } else if(source instanceof CharSequence){
            idValue = source
        } else if(source instanceof Map && ((Map)source).containsKey('id')) {
            idValue = source['id']
        } else if(source instanceof Number) {
            idValue = source.toString()
        }
        if (idValue instanceof GString) {
            idValue = idValue.toString()
        }
        idValue
    }

    protected boolean isDomainClass(final Class<?> clazz) {
        return DomainClassArtefactHandler.isDomainClass(clazz) || AnnotationDomainClassArtefactHandler.isJPADomainClass(clazz)
    }

    /**
     * @param obj any object
     * @param propName the name of a property on obj
     * @return the Class of the domain class referenced by propName, null if propName does not reference a domain class
     */
    protected Class getDomainClassType(Object obj, String propName) {
        def domainClassType
        def objClass = obj.getClass()
        def propertyType = ClassUtils.getPropertyType(objClass, propName)
        if(propertyType && isDomainClass(propertyType)) {
            domainClassType = propertyType
        }
        domainClassType
    }

    @SuppressWarnings(['NestedBlockDepth']) //FIXME lets fix, not suppress
    protected populateErrors(Object obj, BindingResult bindingResult) {
        PersistentEntity domain = getPersistentEntity(obj.getClass())

        if (domain != null && bindingResult != null) {
            def newResult = new ValidationErrors(obj, getEntityClass(obj).name)
            for (Object error : bindingResult.getAllErrors()) {
                if (error instanceof FieldError) {
                    def fieldError = (FieldError)error
                    final boolean isBlank = '' == fieldError.getRejectedValue()
                    if (isBlank) {
                        PersistentProperty prop = domain.getPropertyByName(fieldError.getField())
                        if (prop != null) {
                            final boolean isOptional = prop.isNullable()
                            if (!isOptional) {
                                newResult.addError(fieldError)
                            }
                        }
                        else {
                            newResult.addError(fieldError)
                        }
                    }
                    else {
                        newResult.addError(fieldError)
                    }
                }
                else {
                    newResult.addError((ObjectError)error)
                }
            }
            bindingResult = newResult
        }
        def mc = obj.metaClass
        if (mc.hasProperty(obj, "errors") != null && bindingResult != null) {
            def errors = obj['errors'] as AbstractBindingResult //this has the addError method

            //Here the 'objectName' in bindingResult and 'objectName' in 'errors' must match.
            //Ensure its not a proxy name in bindingResult, or else it will give IllegalArgumentException below when doing addAllErrors
            errors.addAllErrors(bindingResult)
        }
    }

    @SuppressWarnings(['EmptyCatchBlock'])
    private PersistentEntity getPersistentEntity(Class clazz) {
        try {
            return GormMetaUtils.mappingContext.getPersistentEntity(clazz.name)
        } catch (GrailsConfigurationException e) {
            //no-op
        }
        null
    }

    //@Override
    @SuppressWarnings(["EmptyCatchBlock"])
    protected getPersistentInstance(Class type, Object id) {
        try {
            GormEnhancer.findStaticApi(type).load((Serializable)id)
        } catch (Exception exc) {
        }
    }

}
