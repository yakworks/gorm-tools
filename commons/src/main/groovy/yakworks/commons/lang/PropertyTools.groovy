/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.lang.reflect.ParameterizedType

import groovy.transform.CompileStatic
import groovy.util.logging.Slf4j

import org.codehaus.groovy.reflection.CachedMethod

/**
 * PropertyTools contains a set of static helpers, which provides a convenient way
 * for manipulating the object's properties.
 *
 * For example, it allows to retrieve object's properties using filters and place them in a map.
 */
@Slf4j
@CompileStatic
class PropertyTools {

    /**
     * Return the value of the (probably nested is your using this) property of the specified name, for the specified source object
     *
     * Example getPropertyValue(source, "x.y.z")
     *
     * @param source - The source object
     * @param property - the property
     * @return value of the specified property or null if any of the intermediate objects are null
     */
    static Object getProperty(Object source, String property) {
        Validate.notNull(source, '[source]')
        Validate.notEmpty(property, '[property]')

        Object result = property.tokenize('.').inject(source) { Object obj, String prop ->
            Object value
            if (obj != null){
                try {
                    value = obj[prop]
                }
                catch (MissingPropertyException e) {
                    // swallow the exceptin basically, if obj is a map then this never happens, but if prop doesn't exist
                    // then this get thrown for objects
                    value = null
                }
            }
            return value
        }

        return result
    }

    /**
     * Return the property value associated with the given key, or {@code null}
     * if the key cannot be resolved.
     * @param key the property name to resolve
     * @param targetType the expected type of the property value
     */
    static <T> T getProperty(Object source, String path, Class<T> targetType){
        getProperty(source, path) as T
    }

    /**
     * Return the property value associated with the given key, or defaultValue
     * if the key cannot be resolved.
     * @param key the property name to resolve
     * @param targetType the expected type of the property value
     */
    static <T> T getProperty(Object source, String path, Class<T> targetType, T defaultValue) {
        Object value = getProperty(source, path)
        return (value == null ? defaultValue : value ) as T
    }

    /**
     * Returns the deepest nested bean
     */
    static getNestedBean(Object bean, String path) {
        int i = path.lastIndexOf(".")
        if (i > -1) {
            path = path.substring(0, i)
            path.split('\\.').each { String it -> bean = bean[it] }
        }
        return bean
    }

    /**
     * finds the property in an entity class and returns is MetaBeanProperty which is useful for
     * things like getting the return type
     */
    static MetaBeanProperty getMetaBeanProp(Class entityClass, String prop) {
        return entityClass.metaClass.properties.find{ it.name == prop} as MetaBeanProperty
    }

    /**
     * see getMetaBeanProp, this calls that and returns the getter MetaMethod's returnType
     */
    static Class getPropertyReturnType(Class entityClass, String prop){
        return getMetaBeanProp(entityClass, prop).getter.returnType
    }

    /**
     * Trys to find the generic type for a collection property
     * For example if its a List<Foo> the this will return 'x.y.Foo' assuming its in the x.y package
     * @param entityClass the class to look on
     * @param prop the class property to check
     * @return
     */
    static String findGenericForCollection(Class entityClass, String prop){
        MetaBeanProperty metaProp = PropertyTools.getMetaBeanProp(entityClass, prop)
        CachedMethod gen = metaProp.getter as CachedMethod
        def genericReturnType = gen.cachedMethod.genericReturnType as ParameterizedType
        def actualTypeArguments = genericReturnType.actualTypeArguments
        actualTypeArguments ? actualTypeArguments[0].typeName : null
    }

}
