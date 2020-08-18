/*
 * Copyright 2004-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gorm.tools.beans


import groovy.transform.CompileStatic

import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.model.PersistentEntity
import org.grails.datastore.mapping.model.config.GormProperties
import org.springframework.util.Assert

import gorm.tools.GormMetaUtils

/**
 * A map implementation that wraps and objects and
 * reads properties from a gorm entity based on list of includes/excludes
 * Its used primarily for specifying a sql like select list and the feeding this into a json generator
 *
 * Ideas taken from BeanMap in http://commons.apache.org/proper/commons-beanutils/index.html
 * and grails LazyMetaPropertyMap
 *
 * WORK IN PROGRESS - to replace BeanPathTools so we are not creating a map and then creating the json
 * @author Joshua Burnett (@basejump)
 * @since 6.12
 */
// @SuppressWarnings({"unchecked","rawtypes"})
@CompileStatic
class EntityBeanMap extends AbstractMap<String, Object> {

    private MetaClass entityMetaClass;
    private Object entity
    //GormProperties.IDENTITY, GormProperties.VERSION,
    private static List<String> EXCLUDES = [
        'class', 'constraints', 'hasMany', 'mapping', 'properties',
        'domainClass', 'dirty', GormProperties.ERRORS, 'dirtyPropertyNames']

    private Set<String> _includes = []
    private Map<String, Object> _includeMap
    private Map<String, Map> _includesNested = [:]

    /**
     * Constructs a new {@code EntityBeanMap} that operates on the specified bean. The given entity
     * cant be null
     * @param entity The object to inspect
     */
    EntityBeanMap(Object entity) {
        Assert.notNull(entity, "Object cannot be null")
        this.entity = entity
        entityMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(entity.getClass())
    }

    /**
     * Constructs a new {@code EntityBeanMap} that operates on the specified bean. The given entity
     * cant be null
     * @param entity The object to inspect
     * @param entity The object to inspect
     */
    EntityBeanMap(Object entity, Map includeMap) {
        this.entity = entity
        initialise(includeMap)
    }

    private void initialise(Map includeMap) {
        entityMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(entity.getClass())
        if(includeMap){
            _includeMap = includeMap
            _includes = includeMap['props'] as Set<String>
            _includesNested = includeMap['nested'] ? includeMap['nested'] as Map : [:]
        }
    }

    /**
     * {@inheritDoc}
     * @see Map#size()
     */
    @Override
    int size() {
        return keySet().size()
    }

    /**
     * {@inheritDoc}
     * @see Map#isEmpty()
     */
    @Override
    boolean isEmpty() {
        return false // will never be empty
    }

    /**
     * {@inheritDoc}
     * @see Map#containsKey(Object)
     */
    @Override
    boolean containsKey(Object key) {
        return getIncludes().contains(key as String)
    }

    /**
     * Checks whether the specified value is contained within the Map. Note that because this implementation
     * lazily initialises property values the behaviour may not be consistent with the actual values of the
     * contained object unless they have already been initialised by calling get(Object)
     *
     * @see Map#containsValue(Object)
     */
    boolean containsValue(Object o) {
        return values().contains(o)
    }

    /**
     * Obtains the value of an object's properties on demand using Groovy's MOP.
     *
     * @param name The name of the property or list of names
     * @return The property value or null
     */
    @Override
    Object get(Object name) {

        if (name instanceof List) {
            Map submap = new HashMap()
            List propertyNames = (List)name
            for (Object currentName : propertyNames) {
                if (currentName != null) {
                    currentName = currentName.toString()
                    if (containsKey(currentName)) {
                        submap.put(currentName, get(currentName))
                    }
                }
            }
            return submap
        }

        String p = name as String

        if (!getIncludes().contains(p)) {
            return null
        }

        //return val
        return convertValue(entity, p)
    }

    /**
     * Converts the value if need be for enums and GormEntity without associations props
     *
     * @param source the source object
     * @param p the property for the source object
     * @return the value to use
     */
    Object convertValue(Object source, String prop){
        Object val = source[prop]
        Map incNested = getIncludesNested()
        // convert Enums to string
        if( val && val.class.isEnum()) {
            val = (val as Enum).name()
        }
        else if(incNested[prop]){
            def incMap = incNested[prop] as Map<String, Object>
            //its has its own includes so its either an object or an iterable
            if(val instanceof Iterable){
                val = new EntityBeanMapList<EntityBeanMap>(val as List, incMap)
            } else {
                //assume its an object then
                val = new EntityBeanMap(val, incMap)
            }
        }
        else if(val instanceof GormEntity) {
            // if it reached here then just generate the default id
            PersistentEntity domainClass = GormMetaUtils.getPersistentEntity(val)
            String id = domainClass.identity.name
            Map idMap = [id: val[id]]
            val = idMap
        }
        return val
    }

    @Override
    Object put(final String name, final Object value) {
        // see BeanMap in http://commons.apache.org/proper/commons-beanutils/index.html
        // and grails LazyMetaPropertyMap to implement this if need be
        throw new UnsupportedOperationException("Wrapper is read-only")
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    Object remove(Object o) {
        throw new UnsupportedOperationException("Method remove(Object o) is not supported by this implementation")
    }

    /**
     * @throws UnsupportedOperationException
     */
    @Override
    void clear() {
        throw new UnsupportedOperationException("Method clear() is not supported by this implementation")
    }

    @Override
    Set<String> keySet() {
        return getIncludes() as Set<String>
    }

    @Override
    Collection<Object> values() {
        Collection<Object> values = new ArrayList<>()
        for (String p : getIncludes()) {
            values.add(entity[p])
        }
        return values
    }

    @Override
    int hashCode() {
        return entity.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof EntityBeanMap) {
            EntityBeanMap other = (EntityBeanMap)o
            return entity.equals(other.entity)
        }
        return false
    }

    /**
     * Gets a Set of MapEntry objects that are the mappings for this BeanMap.
     * <p>
     * Each MapEntry can be set but not removed.
     *
     * @return the unmodifiable set of mappings
     */
    @Override
    public Set<Map.Entry<String, Object>> entrySet() {
        return Collections.unmodifiableSet(new AbstractSet<Map.Entry<String, Object>>() {
            @Override
            Iterator<Map.Entry<String, Object>> iterator() {
                return entryIterator();
            }

            @Override
            int size() {
                return EntityBeanMap.this.size();
            }
        });
    }

    //------- Helper methods --------
    Map<String, Object> getIncludeMap(){
        return _includeMap
    }

    Map<String, Map> getIncludesNested(){
        return _includesNested
    }

    boolean isIncluded(String mp) {
        // NameUtils.isConfigurational(mp.getName())
        return getIncludes().contains(mp)
    }

    boolean isExcluded(String mp) {
        // NameUtils.isConfigurational(mp.getName())
        return EXCLUDES.contains(mp)
    }

    Set<String> getIncludes(){
        if(!_includes){
            // make the default includes
            for (MetaProperty mp : entityMetaClass.getProperties()) {
                if (isExcluded(mp.name)) continue
                _includes.add(mp.name)
            }
        }
        return _includes
    }

    /**
     * Convenience method for getting an iterator over the keys.
     * <p>
     * Write-only properties will not be returned in the iterator.
     *
     * @return an iterator over the keys
     */
    Iterator<String> keyIterator() {
        return getIncludes().iterator()
    }

    /**
     * Convenience method for getting an iterator over the entries.
     *
     * @return an iterator over the entries
     */
    Iterator<Map.Entry<String, Object>> entryIterator() {
        final Iterator<String> iter = keyIterator()
        return new Iterator<Map.Entry<String, Object>>() {
            @Override
            boolean hasNext() {
                return iter.hasNext()
            }

            @Override
            Map.Entry<String, Object> next() {
                final String key = iter.next()
                final Object value = EntityBeanMap.this.get(key)
                // This should not cause any problems; the key is actually a
                // string, but it does no harm to expose it as Object
                return new Entry(EntityBeanMap.this, key, value)
            }

            @Override
            void remove() {
                throw new UnsupportedOperationException("remove() not supported for BeanMap")
            }
        };
    }

    /**
     * Map entry used by {@link EntityBeanMap}.
     */
    protected static class Entry extends AbstractMap.SimpleEntry<String, Object> {

        private static final long serialVersionUID = 1L;
        private final EntityBeanMap owner;

        /**
         * Constructs a new {@code Entry}.
         *
         * @param owner the EntityBeanMap this entry belongs to
         * @param key the key for this entry
         * @param value the value for this entry
         */
        protected Entry(final EntityBeanMap owner, final String key, final Object value) {
            super(key, value);
            this.owner = owner;
        }

        /**
         * Sets the value.
         *
         * @param value the new value for the entry
         * @return the old value for the entry
         */
        @Override
        Object setValue(final Object value) {
            final String key = getKey();
            final Object oldValue = owner.get(key);

            owner.put(key, value);
            final Object newValue = owner.get(key);
            super.setValue(newValue);
            return oldValue;
        }
    }
}
