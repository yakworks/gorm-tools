package gorm.tools.hibernate.criteria

import grails.gorm.DetachedCriteria as GrDetachedCriteria
import org.hibernate.criterion.Criterion

public class DynamicCriteriaBuilder {
    private Class domain
    private List<Closure> criteriaClosures
    private GrDetachedCriteria gdc

    public DynamicCriteriaBuilder(Class domain) {
        this.domain = domain;
        criteriaClosures = new ArrayList<Closure>(10)
        this.gdc = new GrDetachedCriteria(domain)
    }

    GrDetachedCriteria addCriteria(key, val) {
        switch (val.getClass()) {
            case (String):
                gdc = gdc.build { like(key, val + "%") }
                break
            case [Boolean, boolean]:
                gdc = gdc.build { eq(key, val) }
                break
            default:
                gdc = gdc.build { eq(key, val) }
        }
    }

    public List list(Map params) {
        params.each { k, v ->
            addCriteria(k, v)
        }
        gdc.list()
    }
}
