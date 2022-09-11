/*
* Copyright 2020 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.rest.gorm.controller.legacy

import javax.servlet.http.HttpServletResponse

import groovy.transform.CompileDynamic

import org.springframework.http.HttpStatus

import grails.artefact.Artefact
import grails.web.http.HttpHeaders

import static org.springframework.http.HttpStatus.NOT_FOUND

/**
 * Helper methods for looks ups using the standard gorm domain statics
 *
 * @author Joshua Burnett
 *
 * based on Grails' RestFullController
 */
@SuppressWarnings(['FactoryMethodName'])
@Artefact("Controller")
@CompileDynamic
trait CoreControllerActions<T> {

    /**
     * List all of resource based on parameters
     *
     * @return List of resources or empty if it doesn't exist
     */
    List<T> listAllResources(Map params) {
        resource.list(params)
    }

    /**
     * Counts all of resources
     *
     * @return List of resources or empty if it doesn't exist
     */
    Integer countResources() {
        resource.count()
    }

    /**
     * handles the request for write methods (create, edit, update, save, delete) when controller is in read only mode
     *
     * @return true if controller is read only
     */
    boolean handleReadOnly() {
        if (readOnly) {
            render status: HttpStatus.METHOD_NOT_ALLOWED.value()
            return true
        }
        return false
    }

    /**
     * The object that can be bound to a domain instance.  Defaults to the request.  Subclasses may override this
     * method to return anything that is a valid second argument to the bindData method in a controller.  This
     * could be the request, a {@link java.util.Map} or a {@link org.grails.databinding.DataBindingSource}.
     *
     * @return the object to bind to a domain instance
     */
    Object getObjectToBind() {
        request
    }

    /**
     * Queries for a resource for the given id
     *
     * @param id The id
     * @return The resource or null if it doesn't exist
     */
    T queryForResource(Serializable id) {
        resource.get(id)
    }

    /**
     * Creates a new instance of the resource for the given parameters
     *
     * @param params The parameters
     * @return The resource instance
     */
    T createResource(Map params) {
        resource.newInstance(params)
    }

    /**
     * Creates a new instance of the resource.  If the request
     * contains a body the body will be parsed and used to
     * initialize the new instance, otherwise request parameters
     * will be used to initialized the new instance.
     *
     * @return The resource instance
     */
    T createResource() {
        T instance = resource.newInstance()
        bindData instance, getObjectToBind()
        instance
    }

    void notFound() {
        render status: NOT_FOUND
    }

    /**
     * Saves a resource
     *
     * @param resource The resource to be saved
     * @return The saved resource or null if can't save it
     */
    T saveResource(T resource) {
        resource.save flush: true
    }

    /**
     * Updates a resource
     *
     * @param resource The resource to be updated
     * @return The updated resource or null if can't save it
     */
    T updateResource(T resource) {
        saveResource resource
    }

    /**
     * Deletes a resource
     *
     * @param resource The resource to be deleted
     */
    void deleteResource(T resource) {
        resource.delete flush: true
    }

    String addLocationHeader(HttpServletResponse response, Long id = null, String action = null) {
        String locLink = grailsLinkGenerator.link(
                resource: this.controllerName,
                action: action,
                id: id,
                absolute: true,
                namespace: hasProperty('namespace') ? this.namespace : null
        )

        response.addHeader(HttpHeaders.LOCATION, locLink)
    }

    String getClassMessageArg() {
        message(code: "${resourceName}.label".toString(), default: resourceClassName)
    }

}
