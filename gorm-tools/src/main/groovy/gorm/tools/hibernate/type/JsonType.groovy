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

import com.vladmihalcea.hibernate.type.json.internal.JsonTypeDescriptor

/**
 * Overrides so we can access set using the gorm mapping
 */
@SuppressWarnings(["ClassNameSameAsSuperclass"])
@CompileStatic
class JsonType extends com.vladmihalcea.hibernate.type.json.JsonType {

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

    //dynamic so we can access the setPropertyClass method on the JsonTypeDescriptor
    @CompileDynamic
    void setJavaTypeDescriptorClass(Properties parameters) {
        //type prop can be either class or String
        def typeProp = parameters.getProperty("type")
        //if typeProp then it string ref, otherwise assume its the class ref itself
        Type type = (Class) ( typeProp ? loadClass(typeProp) : parameters.get("type") )

        ((JsonTypeDescriptor) getJavaTypeDescriptor()).setPropertyClass(type)
    }

    static Class loadClass(String clazz){
        Thread.currentThread().contextClassLoader.loadClass(clazz)
    }
}
