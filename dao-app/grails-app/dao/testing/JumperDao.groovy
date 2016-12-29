package testing

import grails.compiler.GrailsCompileStatic
import grails.plugin.dao.GormDaoSupport


@GrailsCompileStatic
class JumperDao extends GormDaoSupport {
	Class domainClass = Jumper
}

