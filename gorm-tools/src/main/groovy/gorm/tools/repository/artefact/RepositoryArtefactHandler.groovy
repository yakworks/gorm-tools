/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package gorm.tools.repository.artefact

import java.util.regex.Pattern

import groovy.transform.CompileStatic

import grails.core.ArtefactHandlerAdapter
import yakworks.commons.lang.NameUtils

import static org.grails.io.support.GrailsResourceUtils.GRAILS_APP_DIR
import static org.grails.io.support.GrailsResourceUtils.REGEX_FILE_SEPARATOR

/**
 * Grails artefact handler for repository classes
 *
 * @author Joshua Burnett (@basejump)
 */
@CompileStatic
class RepositoryArtefactHandler extends ArtefactHandlerAdapter {

    static final String TYPE = "Repository"
    static final String SUFFIX = "Repo"
    static final String PLUGIN_NAME = "gorm-tools"
    static final Pattern REPO_PATH_PATTERN = Pattern.compile(".+" + REGEX_FILE_SEPARATOR + GRAILS_APP_DIR +
        REGEX_FILE_SEPARATOR + "repository" + REGEX_FILE_SEPARATOR + "(.+)\\.(groovy)")

    RepositoryArtefactHandler() {
        super(TYPE, GrailsRepositoryClass, DefaultGrailsRepositoryClass, SUFFIX, false)
    }

//    boolean isArtefact(ClassNode classNode) {
//        if(classNode == null ||
//            !isValidArtefactClassNode(classNode, classNode.getModifiers()) ||
//            !classNode.getName().endsWith(SUFFIX) ) {
//            return false
//        }
//
//        URL url = GrailsASTUtils.getSourceUrl(classNode)
//
//        url &&  REPO_PATH_PATTERN.matcher(url.getFile()).find()
//    }

    // boolean isArtefactClass(Class clazz) {
    //     // class shouldn't be null and should ends with Repo suffix
    //     (clazz != null && clazz.getName().endsWith(SUFFIX))
    // }

    @Override
    String getPluginName() {
        PLUGIN_NAME
    }

    /** Static helpers for other classes that need to look up the spring beans */
    static String getRepoClassName(Class domainClass) {
        return "${domainClass.name}$SUFFIX"
    }

    static String getRepoBeanName(Class domainClass) {
        return "${NameUtils.getPropertyName(domainClass.name)}$SUFFIX"
    }
}
