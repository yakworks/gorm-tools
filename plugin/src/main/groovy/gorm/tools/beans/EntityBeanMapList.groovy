package gorm.tools.beans

import groovy.transform.CompileStatic

import org.springframework.util.Assert

import grails.gorm.PagedResultList

@SuppressWarnings(["rawtypes", "unchecked"])
@CompileStatic
class EntityBeanMapList<E> extends AbstractList<E> {

    private static final long serialVersionUID = -5820655628956173929L

    protected List resultList
    protected Map<String, Object> includeMap
    protected int totalCount = Integer.MIN_VALUE

    EntityBeanMapList(List resultList) {
        this.resultList = resultList
    }

    EntityBeanMapList(List resultList, Map includeMap) {
        this.resultList = resultList
        this.includeMap = includeMap
    }

    int getTotalCount() {
        return (resultList as PagedResultList).getTotalCount()
    }

    /**
     * wraps the item in a
     */
    @Override
    E get(int i) {
        def origObj = resultList.get(i)
        def eb = new EntityBeanMap(origObj, includeMap)
        return eb as E
    }

    @Override
    int size() {
        return resultList.size()
    }

    @Override
    boolean equals(Object o) {
        return resultList.equals(o)
    }

    @Override
    int hashCode() {
        return resultList.hashCode()
    }

    private void writeObject(ObjectOutputStream out) throws IOException {

        // find the total count if it hasn't been done yet so when this is deserialized
        // the null GrailsHibernateTemplate won't be an issue
        getTotalCount();

        out.defaultWriteObject();
    }
}
