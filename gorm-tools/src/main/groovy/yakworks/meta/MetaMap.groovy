/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.meta

import groovy.transform.CompileStatic

import yakworks.commons.lang.Validate
import yakworks.commons.map.Maps
import yakworks.commons.model.IdEnum

/**
 * A map implementation that wraps an object tree and
 * reads properties from a gorm entity based on list of includes/excludes
 * Its used primarily for specifying a sql like select list and the feeding this into a json generator
 *
 * Setting properties on the wrapped object is not supported but a put will write to shadow map and look there first
 * on a get so properties can be overriden
 *
 * Ideas taken from BeanMap in http://commons.apache.org/proper/commons-beanutils/index.html
 * and grails LazyMetaPropertyMap
 *
 * @author Joshua Burnett (@basejump)
 * @since 6.1.12
 */
@SuppressWarnings(["CompileStatic", "FieldName", "ExplicitCallToEqualsMethod"])
@CompileStatic
class MetaMap extends AbstractMap<String, Object> implements Cloneable {

    private MetaClass entityMetaClass;
    private Object entity
    // if the wrapped entity is a map then this will be the cast intance
    private Map entityAsMap

    //GormProperties.IDENTITY, GormProperties.VERSION,
    private static List<String> EXCLUDES = [
        'class', 'constraints', 'hasMany', 'mapping', 'properties',
        'domainClass', 'dirty', 'errors', 'dirtyPropertyNames']

    private Set<String> _includes = []
    // private Map _includeProps = [:] as Map<String, MetaMapIncludes>
    private MetaMapIncludes metaMapIncludes

    private Map<String, Object> shadowMap = [:]

    Set<Converter> converters = [] as Set<Converter>

    /**
     * Constructs a new {@code EntityMap} that operates on the specified bean. The given entity
     * cant be null
     * @param entity The object to inspect
     */
    MetaMap(Object entity) {
        Validate.notNull(entity)
        this.entity = entity
        entityMetaClass = GroovySystem.getMetaClassRegistry().getMetaClass(entity.getClass())
        if(Map.isAssignableFrom(entity.class)) {
            entityAsMap = (Map)entity
        }
    }

    /**
     * Constructs a new {@code EntityMap} that operates on the specified bean. The given entity
     * cant be null
     * @param entity The object to inspect
     * @param entity The object to inspect
     */
    MetaMap(Object entity, MetaMapIncludes metaMapIncludes) {
        this(entity)
        initialise(metaMapIncludes)
    }

    private void initialise(MetaMapIncludes metaMapIncludes) {
        if(metaMapIncludes){
            this.metaMapIncludes = metaMapIncludes
            _includes = metaMapIncludes.propsMap.keySet()
            // _includeProps = includeMap.propsMap
            this.converters = MetaMapIncludes.CONVERTERS
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
        return size() == 0
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
            Map submap = [:]
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

        // check to see if the shadow override map has on and return it as is
        if(shadowMap.get(p)) return shadowMap.get(p)

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
        if(val == null) return null
        Map nestesIncludes = getNestedIncludes()
        MetaMapIncludes mapIncludes = nestesIncludes[prop]
        // if its an enum and doesnt have any include field specifed (which it normally should not)
        if( val.class.isEnum() && !(mapIncludes?.propsMap)) {
            if(val instanceof IdEnum){
                // convert Enums to string or id,name object if its IdEnum
                Map<String, Object> idEnumMap = [id: (val as IdEnum).id, name: (val as Enum).name()]
                val = idEnumMap
            } else {
                // then just get normal string name()
                val = (val as Enum).name()
            }
        }
        else if(mapIncludes){
            //its has its own includes so its either an object or an iterable
            if(val instanceof Iterable){
                val = new MetaMapList(val as List, mapIncludes)
            } else {
                //assume its an object then
                val = new MetaMap(val, mapIncludes)
            }
        }
        else if(val instanceof Map && !(val instanceof MetaMap)) {
            val = new MetaMap(val)
        }
        // if it has converters then use them. if its already a MetaMap or MetaMapList then it does not need converting.
        else if(converters && !(val instanceof MetaMap) && !(val instanceof MetaMapList)){
            Converter converter = findConverter(val)
            if (converter != null) {
                val = converter.convert(val, prop)
            }
        }
        return val
    }


    /**
     * Finds a converter that can handle the given type.  The first converter
     * that reports it can handle the type is returned, based on the order in
     * which the converters were specified.  A {@code null} value will be returned
     * if no suitable converter can be found for the given type.
     *
     * @param type that this converter can handle
     * @return first converter that can handle the given type; else {@code null}
     *         if no compatible converters are found for the given type.
     */
    protected Converter findConverter(Object val) {
        for (Converter c : converters) {
            if (c.handles(val)) {
                return c
            }
        }
        return null
    }


    /**
     * put will not set keys on the wrapped object but allows to add extra props and overrides
     * by using a shadow map to store the values
     */
    @Override
    Object put(final String name, final Object value) {
        //json-views want to set an object key thats a copy of this so allow it
        shadowMap.put(name, value)
        //make sure its in the includes now too
        _includes.add(name)
        return entity[name]
    }

    /**
     * throws UnsupportedOperationException
     */
    @Override
    Object remove(Object o) {
        throw new UnsupportedOperationException("Method remove(Object o) is not supported by this implementation")
    }

    /**
     * throws UnsupportedOperationException
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
        Collection<Object> values = []
        for (String p : getIncludes()) {
            values.add(entity[p])
        }
        return values
    }

    @Override
    Object clone() {
        return Maps.clone(this)
    }

    @Override
    int hashCode() {
        return entity.hashCode()
    }

    @Override
    boolean equals(Object o) {
        if (o instanceof MetaMap) {
            MetaMap other = (MetaMap)o
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
    Set<Map.Entry<String, Object>> entrySet() {
        def eset = new AbstractSet<Map.Entry<String, Object>>() {
            @Override
            Iterator<Map.Entry<String, Object>> iterator() {
                return entryIterator()
            }

            @Override
            int size() {
                return MetaMap.this.size()
            }
        }
        return Collections.unmodifiableSet(eset)
    }

    //------- Helper methods --------

    Map<String, MetaMapIncludes> getNestedIncludes(){
        return (metaMapIncludes?.nestedIncludes) ?: [:] as Map<String, MetaMapIncludes>
    }

    boolean isIncluded(String mp) {
        // NameUtils.isConfigurational(mp.getName())
        return getIncludes().contains(mp)
    }

    boolean isExcluded(String mp) {
        // NameUtils.isConfigurational(mp.getName())
        return EXCLUDES.contains(mp)
    }

    /**
     * gets the includes if specified or creates the meta properties
     */
    Set<String> getIncludes(){
        //if not includes then build default
        if(!_includes){
            if(entityAsMap != null) {
                _includes = entityAsMap.keySet().findAll{ key -> !isExcluded(key as String) }
            }
            else {
                //assume its an object
                for (MetaProperty mp : entityMetaClass.getProperties()) {
                    if (isExcluded(mp.name)) continue
                    _includes.add(mp.name)
                }
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
                final Object value = MetaMap.this.get(key)
                // This should not cause any problems; the key is actually a
                // string, but it does no harm to expose it as Object
                return new Entry(MetaMap.this, key, value)
            }

            @Override
            void remove() {
                throw new UnsupportedOperationException("remove() not supported for BeanMap")
            }
        };
    }

    /**
     * Map entry used by {@link MetaMap}.
     */
    protected static class Entry extends AbstractMap.SimpleEntry<String, Object> {

        private static final long serialVersionUID = 1L;
        private final MetaMap owner;

        /**
         * Constructs a new {@code Entry}.
         *
         * @param owner the EntityMap this entry belongs to
         * @param key the key for this entry
         * @param value the value for this entry
         */
        protected Entry(final MetaMap owner, final String key, final Object value) {
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

    /**
     * Handles converting a given type.
     *
     * @since 2.5.0
     */
    interface Converter {

        /**
         * Returns {@code true} if this converter can handle conversions
         * of the given type.
         *
         * @param value the object to convert
         * @return {@code true} if this converter can successfully convert values of
         *      the given type, else {@code false}
         */
        boolean handles(Object value);

        /**
         * Converts a given object.
         *
         * @param value the object to convert
         * @param key the key name for the value, may be {@code null}
         * @return the converted object
         */
        Object convert(Object value, String key);

    }
}
