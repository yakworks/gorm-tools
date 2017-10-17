package daoapp.api

import daoapp.Org
import grails.plugin.dao.DomainNotFoundException
import grails.plugin.dao.RestDaoController

class OrgController extends RestDaoController<Org> {
	static responseFormats = ['json']
	static namespace = "api"

	OrgController() {
		super(Org)
	}
}
