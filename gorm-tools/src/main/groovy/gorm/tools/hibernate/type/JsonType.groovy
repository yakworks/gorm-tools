/*
* Copyright 2022 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.hibernate.type

import java.lang.reflect.Type

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.hibernate.type.descriptor.sql.SqlTypeDescriptor
import org.hibernate.usertype.ParameterizedType

import io.hypersistence.utils.hibernate.type.json.internal.JsonTypeDescriptor

/**
 * Overrides so we can access setParameterValues using the gorm mapping
 */
@SuppressWarnings(["ClassNameSameAsSuperclass"])
@CompileStatic
class JsonType extends io.hypersistence.utils.hibernate.type.json.JsonType {

    // public static final com.vladmihalcea.hibernate.type.json.JsonType INSTANCE = new JsonExtType();

    /**
     * The original assumes its being set using the @Type annotation which passes meta.
     * Cant get that working with Gorm without it assuming its an assoiation.
     * So we expect params to be passed with the class type
     */
    @Override
    public void setParameterValues(Properties parameters) {
        // ((JsonTypeDescriptor) getJavaTypeDescriptor()).setParameterValues(parameters);
        setJavaTypeDescriptorClass(parameters)
        //following is copied from super
        SqlTypeDescriptor sqlTypeDescriptor = getSqlTypeDescriptor()
        if (sqlTypeDescriptor instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) sqlTypeDescriptor
            parameterizedType.setParameterValues(parameters)
        }
    }

    /**
     * Uses config like
     * `json type: JsonType, params: [type: Map]`
     * @param parameters the params from the mapping
     */
    void setJavaTypeDescriptorClass(Properties parameters) {
        //type prop can be either class or String of with the class name
        def typeProp = parameters.getProperty("type")
        //if typeProp then it string ref, otherwise assume its the class ref itself
        Type type = (Class) ( typeProp ? loadClass(typeProp) : parameters.get("type") )
        setJavaTypeDescriptorPropertyClass(type)
    }

    //dynamic so we can access the private setPropertyClass method on the JsonTypeDescriptor
    @CompileDynamic
    void setJavaTypeDescriptorPropertyClass(Type type) {
        ((JsonTypeDescriptor) getJavaTypeDescriptor()).setPropertyClass(type)
        //might be able to do this and move off the private access
        // var jtd = new JsonTypeDescriptor(Configuration.INSTANCE.getObjectMapperWrapper(), type)
        // setJavaTypeDescriptor(jtd)
    }
    static Class loadClass(String clazz){
        Thread.currentThread().contextClassLoader.loadClass(clazz)
    }
}
