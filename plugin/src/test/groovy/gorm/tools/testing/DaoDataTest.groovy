package gorm.tools.testing

import gorm.tools.dao.DaoEventInvoker
import gorm.tools.dao.DaoUtil
import gorm.tools.databinding.FastBinder
import grails.plugin.dao.DaoArtefactHandler
import grails.testing.gorm.DataTest


trait DaoDataTest extends DataTest {
	//TODO work in progress
	void mockDao(Class daoClass) {
		registerBeanIfRequired("fastBinder", FastBinder, false)
		final daoArtefact = grailsApplication.addArtefact(DaoArtefactHandler.TYPE, daoClass)
		registerBeanIfRequired(daoArtefact.propertyName, daoClass)
		registerBeanIfRequired("daoEventInvoker", DaoEventInvoker)
		registerBeanIfRequired('daoUtilBean', DaoUtil)
	}

	void registerBeanIfRequired(String name, Class clazz, autowire = true) {
		if(!applicationContext.containsBean(name)) {
			defineBeans({
				"$name"(clazz) {bean ->
					bean.autowire = autowire
				}
			})
		}
	}



}
