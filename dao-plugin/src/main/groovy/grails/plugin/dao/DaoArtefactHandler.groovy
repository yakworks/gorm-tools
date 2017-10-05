/*
 * Copyright (c) 2011 Joshua Burnett or other authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package grails.plugin.dao

import grails.core.ArtefactHandlerAdapter
import groovy.transform.CompileStatic

/**
 * Grails artefact handler for dao classes
 *
 * @author Joshua Burnett
 * based on the grails source ServiceArtefactHandler
 */
@CompileStatic
public class DaoArtefactHandler extends ArtefactHandlerAdapter {

	public static final String TYPE = "Dao"
	public static final String PLUGIN_NAME = "dao"

	DaoArtefactHandler() {
		super(TYPE, GrailsDaoClass.class, DefaultGrailsDaoClass.class, DefaultGrailsDaoClass.DAO, false)
	}

	String getPluginName() {
		return PLUGIN_NAME
	}

}
