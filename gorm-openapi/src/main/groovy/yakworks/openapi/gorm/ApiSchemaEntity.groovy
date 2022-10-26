/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.openapi.gorm


import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.PersistentProperty
import org.grails.datastore.mapping.model.types.Association
import org.grails.datastore.mapping.model.types.OneToMany
import org.grails.orm.hibernate.cfg.HibernateMappingContext
import org.grails.orm.hibernate.cfg.Mapping

import gorm.tools.utils.GormMetaUtils
import grails.gorm.validation.ConstrainedProperty
import grails.gorm.validation.DefaultConstrainedProperty
import yakworks.commons.lang.ClassUtils
import yakworks.commons.lang.NameUtils
import yakworks.commons.lang.PropertyTools
import yakworks.commons.map.Maps
import yakworks.openapi.OapiUtils

/**
 * Entity holder for schema based on CRUType
 */
@SuppressWarnings(['UnnecessaryGetter', 'AbcMetric', 'Println'])
@CompileStatic
class ApiSchemaEntity {
    public static final String REF = '$ref'
    public static final String OBJECT = 'object'
    //static final Map<String, Map> SCHEMA_CACHE = new ConcurrentHashMap<String, Map>()
    static enum CruType {
        Create, Read, Update
        String getPropertyName(){ NameUtils.getPropertyName(name()) }
        boolean isEdit(){ (this == Create || this == Update)}
        String getSuffix(){ this == Read ? '' : "_$this" }
    }

    HibernateMappingContext persistentEntityMappingContext
    PersistentEntity entity
    Mapping mapping
    Class entityClass
    String entityName
    String simpleEntityName
    Map<String, ConstrainedProperty> constrainedProperties
    List<PersistentProperty> persistentProperties

    Map allSchemaProps = [:]
    Map createSchemaProps = [:]
    Map updateSchemaProps = [:]

    ApiSchemaEntity(PersistentEntity entity) {
        this.entity = entity
        this.entityClass = entity.javaClass
        this.persistentEntityMappingContext = (HibernateMappingContext) entity.getMappingContext()
        this.constrainedProperties = GormMetaUtils.findAllConstrainedProperties(entity)
        this.entityName = entity.name
        this.simpleEntityName = entityClass.simpleName
        this.persistentProperties = resolvePersistentProperties()
        //TODO figure out a more performant way to do these if
        this.mapping = getMapping(entity)

    }

    Map generate(CruType kind = CruType.Read) {

        Map schema = [:]//SCHEMA_CACHE.getOrDefault(entityName, [:])

        //Map cols = mapping.columns
        schema.title = simpleEntityName
        schema['x-entity'] = simpleEntityName

        if (mapping?.comment) schema.description = mapping.comment
        schema.type = OBJECT
        //schema.required = []

        Map propsMap = getEntityProperties(kind)

        //do the other misc fields
        def auditStamp = [:]
        //Audit Stamp put in its own so we can append to end of list
        ['createdBy', 'editedBy', 'createdDate', 'editedDate'].each {
            def item = propsMap.remove(it)
            if(item) auditStamp[it] = item
        }
        //ID Version
        Map idVerMap = createIdVersionProps(entity)
        //def sortedProps = propsMap.sort()
        def p = [:]
        p.putAll(propsMap.sort())
        //FIXME be smarter about this
        if(kind == CruType.Read){
            p.putAll(idVerMap)
        }

        // dont put audit stamp in for now as it just creates more noise
        // p.putAll(auditStamp)

        if(p.required) schema.required = p.remove('required') as List

        schema['properties'] = p

        return schema
    }

    @SuppressWarnings(['MethodSize'])
    // @CompileDynamic
    private Map getEntityProperties(CruType type) {
        //println "----- ${perEntity.name} getDomainProperties ----"
        //String domainName = NameUtils.getPropertyNameRepresentation(perEntity.name)
        Map<String, ?> propsMap = [:]
        def required  = []

        List<String> constrainedPropsNames = getConstraintedNames(constrainedProperties)

        //println "-- PersistentProperties --"
        for (PersistentProperty prop : persistentProperties) {
            String propName = prop.name
            def constrainedProperty = (DefaultConstrainedProperty) constrainedProperties[propName]
            //remove from constraint name list so we can spin through the remaining later that are not persistentProperties
            //and set them up to for transients and set them up too.
            constrainedPropsNames.remove(propName)

            //if its version should have been taken care of or is set to version false
            //skip if display is false
            if (propName == 'version' || !constrainedProperty || !constrainedProperty.display) continue

            Map apiProp = getOapiProps(propName, constrainedProperty)

            if(!isAllowed(type, apiProp)) continue

            if (prop instanceof Association && prop.associatedEntity) {
                associationProp(type, apiProp, prop, constrainedProperty)
            } else { //setup type
                //println "  ${prop.name} basic, ${prop} ${prop.type}"
                basicType(apiProp, constrainedProperty)
            }

            apiProp.remove('allowed')
            if(apiProp.remove('required')) required.add(prop.name)
            propsMap[prop.name] = apiProp
        }

        //now do the remaining constraints
        //println "-- Contrained Non-PersistentProperties --"
        for(String propName : constrainedPropsNames){
            def constrainedProp = (DefaultConstrainedProperty) constrainedProperties[propName]

            Map apiProp = getOapiProps(propName, constrainedProp)
            if(!isAllowed(type, apiProp)) continue

            Class returnType = constrainedProp.propertyType
            if(Collection.isAssignableFrom(returnType)){
                Class genClass =(Class) PropertyTools.findGenericTypeForCollection(entityClass, propName)
                //if its primitive or Object collection then just do the the typ
                Map propsToAdd
                if(ClassUtils.isBasicType(genClass)){
                    propsToAdd = OapiUtils.getJsonType(genClass)
                }
                else if(genClass == Object){
                    propsToAdd = [type: 'object']
                }
                else {
                    propsToAdd = setupAssociationObject(type, apiProp, genClass.simpleName, constrainedProp, null)
                }
                apiProp['type'] = 'array'
                apiProp['items'] = propsToAdd
            } else {
                //println "  ${propName} basic ${constrainedProp.propertyType}"
                basicType(apiProp, constrainedProp)
            }
            apiProp.remove('allowed') //remove allowed so it doesn't get added to the json output
            if(apiProp.remove('required')) required.add(propName)
            propsMap[propName] = apiProp
        }
        if(required) propsMap.required = required
        return propsMap
    }

    Map createIdVersionProps(PersistentEntity perEntity){
        Map idVerMap = [:]
        PersistentProperty idProp = perEntity.getIdentity()
        if(idProp){
            Map idJsonType = OapiUtils.getJsonType(idProp.type)
            idJsonType.putAll([
                description: 'unique id',
                example: 954,
                readOnly: true
            ])
            idVerMap[idProp.name] = idJsonType
        }

        if (perEntity.version) {
            idVerMap[perEntity.version.name] = [
                type: 'integer',
                description: 'version of the edit, incremented on each change',
                example: 0,
                readOnly: true
            ] as Map
        }
        return idVerMap
    }


    boolean isAllowed(CruType type, Map oapiProps){
        Map allowed = (Map)oapiProps.allowed
        return (type == CruType.Create && allowed.create) ||
            (type == CruType.Update && allowed.update) ||
            (type == CruType.Read && allowed.read)
    }

    Map getOapiProps(String propName, DefaultConstrainedProperty constrainedProp){
        Map newOapi = createApiProperty(propName, constrainedProp)
        Map allowed = (Map)newOapi.allowed

        def oapi = constrainedProp.getMetaConstraintValue('oapi')
        // if string then its in form oapi:'CRU' and overrides
        if(oapi && oapi instanceof String){
            if(!oapi.contains('C')) allowed.create = false
            if(!oapi.contains('U')) allowed.update = false
            if(!oapi.contains('R')) allowed.read = false
        }
        else if(oapi instanceof Map){ //its a map
            def oapiMap = Maps.clone(oapi)
            ['create', 'update', 'read'].each {
                if(oapiMap.containsKey(it)) allowed[it] = oapiMap.remove(it)
            }
            if(oapiMap.containsKey('edit')) {
                allowed['create'] = oapiMap.remove('edit')
                allowed['update'] = allowed['create']
            }
            //if anything is left then they are overrides so add them
            newOapi.putAll(oapiMap)
        }
        newOapi.allowed = allowed
        return newOapi
    }

    Map createApiProperty(String propName, DefaultConstrainedProperty constrainedProp){
        Map jprop = [:]

        title(jprop, constrainedProp)

        description(jprop, constrainedProp)

        example(jprop, constrainedProp)

        defaultFromConstraint(jprop, constrainedProp)

        if(isRequired(jprop, constrainedProp)) jprop.required = true //required.add(prop.name)
        //alowed methods default to all true
        Map allowed = [read: true, create: true, update: true]
        if (constrainedProp.editable == false){
            allowed.putAll([create: false, update: false])
            jprop.readOnly = true
        }
        jprop.allowed = allowed

        //minLength
        if (constrainedProp.maxSize) jprop.maxLength = constrainedProp.getMaxSize()
        //maxLength
        if (constrainedProp.minSize) jprop.minLength = constrainedProp.getMinSize()

        if (constrainedProp.min != null) jprop.minimum = constrainedProp.getMin()
        if (constrainedProp.max != null) jprop.maximum = constrainedProp.getMax()
        if (constrainedProp.scale != null) jprop.multipleOf = 1 / Math.pow(10, constrainedProp.getScale())
        if (constrainedProp.hasAppliedConstraint('email')) jprop.format = 'email'

        return jprop
    }

    void associationProp(CruType type, Map apiProp, Association association, DefaultConstrainedProperty constrainedProp){
        PersistentEntity assocEntity = association.associatedEntity
        String assocSimpleName = assocEntity.javaClass.simpleName
        Map propsToAdd = setupAssociationObject(type, apiProp, assocSimpleName, constrainedProp, association)

        if(association instanceof OneToMany){
            apiProp['type'] = 'array'
            apiProp['items'] = propsToAdd
        } else {
            apiProp.putAll(propsToAdd)
        }
    }

    Map setupAssociationObject(CruType type, Map apiProp, String assocSimpleName,
                             DefaultConstrainedProperty constrainedProp, Association association){
        Map allowedProp = (Map)apiProp.allowed
        def allowedInfo =  allowedProp[type.getPropertyName()]
        //it need to either be true or its a list of fields to make obj
        if(!allowedInfo) return [:]
        List fields = []
        if(allowedInfo instanceof List){
            fields = (List) allowedInfo
        }

        //if its create or update and its the owner or bindable
        if(!fields && type.isEdit() && !isOwnerOrBindable(association, constrainedProp)){
            //if no fields then default to just the id
            fields = ['id']
        }

        Map objProps = [:]
        for(String field: fields){
            if(field == 'id'){
                objProps['id'] = idMap(assocSimpleName)
            }
            else if(field == '$ref'){
                //if its has $ref then break, we wont use objProps
                break
            }
            else {
                objProps[field] = [
                    type: 'string'
                ] as Map
            }
        }

        Map propsToAdd = [:]
        //at this point if it has objProps then set it up, otherwise its a $ref
        if(objProps){
            propsToAdd.title = assocSimpleName
            propsToAdd.type = OBJECT
            propsToAdd['properties'] = objProps
        } else {
            propsToAdd[REF] = "${assocSimpleName}${type.suffix}.yaml".toString()
        }

        return propsToAdd
    }

    Map idMap(String modelName){
        return [
            description: "$modelName id".toString(),
            example: 954,
            type: 'integer',
            format: 'int64'
        ]
    }

    /**
     * Check if association is bindable.
     * An association is bindable, if it is owning side, or if explicit bindable:true
     */
    boolean isOwnerOrBindable(Association prop, DefaultConstrainedProperty cp) {
        return (prop?.isOwningSide() || isExplicitBind(cp))
    }

    boolean isExplicitBind(DefaultConstrainedProperty cp) {
        return cp.getMetaConstraintValue("bindable") as Boolean
    }

    /**
     * gets the list of constrained names that are not diplay false or version
     */
    List<String> getConstraintedNames(Map<String, ConstrainedProperty> constrainedProps){
        List<String> constrainedPropsNames = []
        constrainedProps.each { String k, ConstrainedProperty val ->
            //println "  $k"
            if(k != 'version' && val.display != false) {
                constrainedPropsNames.add(k)
            }
        }
        return constrainedPropsNames as List<String>
    }

    void description(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        String description = constrainedProp.getMetaConstraintValue("description")
        description = description ?: constrainedProp.getMetaConstraintValue("d")
        if (description) propMap.description = description.stripIndent()
    }

    Object defaultFromConstraint(Map propMap, DefaultConstrainedProperty constrainedProp ){
        def defVal = constrainedProp.getMetaConstraintValue("default")?.toString()
        if (defVal == null) return null
        if(!(defVal instanceof Number)) defVal = defVal.toString()
        propMap.default = defVal
    }

    void defaults(Map propMap, Mapping mapping, String propName){
        String defVal = getDefaultValue(mapping, propName)
        if (defVal != null) propMap.default = defVal //TODO convert to string?
    }

    boolean isRequired(Map propMap, DefaultConstrainedProperty constrainedProp){
        //required
        Boolean req = constrainedProp.getMetaConstraintValue("required")

        if (!constrainedProp.isNullable() && constrainedProp.editable && req != false) {
            //if it doesn't have a default value and its not a boolean
            if (propMap.default == null && !Boolean.isAssignableFrom(constrainedProp.propertyType)) {
                return true
            }
        }
    }

    void title(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        String title = constrainedProp.getMetaConstraintValue("title")
        if (title) propMap.title = title
    }

    void example(Map propMap, DefaultConstrainedProperty constrainedProp){
        //description
        String example = constrainedProp.getMetaConstraintValue("example")
        if (example) propMap.example = example
    }

    void basicType(Map propMap, DefaultConstrainedProperty constrainedProp){
        //type
        Map typeFormat = OapiUtils.getJsonType(constrainedProp.propertyType)
        propMap.type = typeFormat.type

        //format
        if (typeFormat.format) propMap.format = typeFormat.format
        if (typeFormat.enum) propMap.enum = typeFormat.enum
        //format override from constraints
        if (constrainedProp.format) propMap.format = constrainedProp.format
    }

    void collectionType(Map propMap, Class clazz, List fields = []){
        propMap['type'] = 'array'
        propMap['items'] = [
            '$ref' : "${clazz.simpleName}.yaml".toString()
        ]
    }


    @CompileDynamic
    String getDefaultValue(Mapping mapping, String propName) {
        mapping?.columns?."$propName"?.columns?.getAt(0)?.defaultValue
        //cols[prop.name]?.columns?.getAt(0)?.defaultValue
    }

    @CompileDynamic
    Mapping getMapping(PersistentEntity pe) {
        return GormMetaUtils.getMappingContext().mappingFactory?.entityToMapping?.get(pe)
    }

    //copied from FormFieldsTagLib in the Fields plugin
    @CompileDynamic
    List<PersistentProperty> resolvePersistentProperties(Map attrs = [:]) {
        List<PersistentProperty> perProps = entity.persistentProperties
        def blacklist = attrs.exclude?.tokenize(',')*.trim() ?: []
        //blacklist << 'dateCreated' << 'lastUpdated'
        Map scaffoldProp = ClassUtils.getStaticPropertyValue(entity.javaClass, 'scaffold', Map)
        if (scaffoldProp) {
            blacklist.addAll(scaffoldProp.exclude)
        }

        perProps.removeAll { it.name in blacklist }
        perProps.removeAll { !constrainedProperties[it.name]?.display }
        perProps.removeAll { it.propertyMapping.mappedForm.derived }

        //if its a composite id then add them to list as they are not added normally
        PersistentProperty[] compositeIds = entity.getCompositeIdentity()
        if(compositeIds) perProps.addAll(compositeIds)

        return perProps
    }

}
