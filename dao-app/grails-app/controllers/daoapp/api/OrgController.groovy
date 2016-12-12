package daoapp.api

import daoapp.Org
import grails.plugin.dao.RestDaoController

class OrgController extends RestDaoController {
	static namespace = "api"

	OrgController(Org){
		super(Org)
	}
}
