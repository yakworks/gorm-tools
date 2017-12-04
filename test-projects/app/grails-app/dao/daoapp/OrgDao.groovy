package daoapp

import gorm.tools.dao.DefaultGormDao
import grails.compiler.GrailsCompileStatic
import grails.gorm.transactions.Transactional

@Transactional
@GrailsCompileStatic
class OrgDao extends DefaultGormDao<Org> {
    //Class domainClass = Org

//	@CompileDynamic
//	@Override
//	Org create(Map params){
//		if(!params.name){
//			params.name = "default"
//		}
//		if (params.name){
//			params.name += " from Dao"
//		}
//		super.doInsert(params)
//	}
}

