package gorm.tools.beans

import gorm.tools.dao.DaoApi
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.grails.datastore.gorm.GormEntity

/**
 * Delegates missing properties as method calls to the dao for the domain class.
 */
@CompileStatic
class DaoDelegatingBean extends DelegatingBean {
    DaoApi dao

    @CompileStatic(TypeCheckingMode.SKIP)
    DaoDelegatingBean(GormEntity target) {
        super(target)
        dao = target.getDao()
    }

    //first try if target bean has property, if not, check if dao has the method
    Object propertyMissing(String name) {
        try {
            return super.propertyMissing(name)
        } catch (MissingPropertyException e) {
            String method
            if (name.startsWith("has") || name.startsWith("is")) {
                method = name
            } else {
                method = "get" + name.capitalize()
            }

            try {
                return dao.invokeMethod(method, target)
            } catch (MissingMethodException me) {
                //dao does not have that method either, so throw back original MissingPropertyException exception
                throw e
            }
        }
    }

    Object methodMissing(String name, args) {
        try {
            return target.invokeMethod(name, args)
        } catch (MissingMethodException e) {
            try {
                dao.invokeMethod(name, args)
            } catch (MissingMethodException me) {
                //if dao does not have the method either, throw back original exception
                throw e
            }
        }
    }

}
