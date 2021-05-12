/*
* Copyright 2021 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.commons.lang

import java.lang.reflect.Modifier

import groovy.transform.CompileStatic

import org.codehaus.groovy.reflection.CachedClass
import org.codehaus.groovy.reflection.ClassInfo
import org.codehaus.groovy.transform.trait.Traits

@CompileStatic
class ClassUtils {

    /**
     * gets the static properties from implemented traits on a class
     * @param mainClass the class to look for traits on.
     * @param name the name of the property
     * @param requiredTyped the type of the property
     * @return
     */
    public static <T> List<T> getStaticValuesFromTraits(Class mainClass, String name, Class<T> requiredTyped) {
        CachedClass cachedClass = ClassInfo.getClassInfo(mainClass).getCachedClass() //classInfo.getCachedClass()
        Collection<ClassInfo> hierarchy = cachedClass.getHierarchy()
        Class javaClass = cachedClass.getTheClass()
        List<T> values = []
        for (ClassInfo current : hierarchy) {
            def traitClass = current.getTheClass()
            def isTrait = Traits.isTrait(traitClass)
            if(!isTrait) continue
            def traitFieldName = getTraitFieldName(traitClass, name)
            T theval = getStaticPropertyValue(mainClass, traitFieldName, requiredTyped)
            if(theval){
                //println "$traitFieldName found with $theval"
                values.add(theval)
            }
        }
        Collections.reverse(values)
        return values
    }

    /**
     * trait fields get added in the form package_class__field name. this returns that
     */
    static String getTraitFieldName(Class traitClass, String fieldName) {
        return traitClass.getName().replace('.', '_') + "__" + fieldName;
    }


    public static <T> T getStaticPropertyValue(Class clazz, String name, Class<T> requiredType) {
        return returnOnlyIfInstanceOf(getStaticPropertyValue(GroovySystem.getMetaClassRegistry().getMetaClass(clazz), name), requiredType);
    }

    private static <T> T returnOnlyIfInstanceOf(Object value, Class<T> type) {
        if (value != null && (type == Object || ReflectionUtils.isAssignableFrom(type, value.getClass()))) {
            return (T)value;
        }
        return null;
    }

    static Object getStaticPropertyValue(MetaClass theMetaClass, String name) {
        MetaProperty metaProperty = theMetaClass.getMetaProperty(name);
        if(metaProperty != null && Modifier.isStatic(metaProperty.getModifiers())) {
            return metaProperty.getProperty(theMetaClass.getTheClass());
        }
        return null;
    }

    static Class<?> getPropertyType(Class<?> cls, String propertyName) {
        MetaProperty metaProperty = GroovySystem.getMetaClassRegistry().getMetaClass(cls).getMetaProperty(propertyName);
        if(metaProperty != null) {
            return metaProperty.getType();
        }
        return null;
    }

}
