package gorm.tools.repository

import gorm.tools.beans.BeanPathTools
import grails.core.GrailsApplication
import org.springframework.beans.factory.annotation.Autowired

trait EntityFieldsHandler<D> {

    @Autowired
    GrailsApplication grailsApplication

    abstract Class<D> getEntityClass()
    List<String> getSelectFieldsConfig(){getFieldsConfig('select')}
    List<String> getListFieldsConfig(){getFieldsConfig('list') ?: getSelectFieldsConfig()}
    List<String> getShowFieldsConfig(){getFieldsConfig('show') ?: getSelectFieldsConfig()}


    List<String> getFieldsConfig(String type) {
        grailsApplication.config.domainFields?.get(getEntityClass().simpleName)?.get(type) ?: null
    }

    Map getFields(D entity, List<String> fieldList = getShowFieldsConfig()){
        BeanPathTools.buildMapFromPaths(entity, fieldList)
    }

    List getListFields(List<D> entities,  List<String> fieldList = getListFieldsConfig()){
        entities.collect{D entity->
            getFields(entity, fieldList)
        }
    }

}
