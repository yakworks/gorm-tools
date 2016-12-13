package daoapp.api

import daoapp.Org
import grails.converters.JSON
import grails.plugin.dao.RestDaoController

class OrgController extends RestDaoController{
	static responseFormats = ['json']
	static namespace = "api"

	OrgController() {
		super(Org)
	}
}
