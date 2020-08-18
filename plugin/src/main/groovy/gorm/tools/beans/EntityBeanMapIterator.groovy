package gorm.tools.beans

import groovy.transform.CompileStatic

@CompileStatic
class EntityBeanMapIterator implements Iterator<EntityBeanMap>{

    private Iterator<Object> iter
    private Map<String, Object> includeMap

    EntityBeanMapIterator(Iterator iter, Map includeMap) {
        this.iter = iter
        this.includeMap = includeMap
    }

    @Override
    boolean hasNext() {
        return iter.hasNext()
    }

    @Override
    EntityBeanMap next() {
        def origObj = iter.next()
        def eb = new EntityBeanMap(origObj, includeMap)
        return eb
    }

    @Override
    void remove() {
        throw new UnsupportedOperationException("remove() not supported for EntityBeanIterable")
    }
}
