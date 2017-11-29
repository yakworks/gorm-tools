package grails.plugin.dao

import gorm.tools.Pager
import gorm.tools.dao.DaoUtil
import gorm.tools.dao.GormDao
import gorm.tools.dao.errors.DomainNotFoundException
import grails.artefact.Artefact
import grails.compiler.GrailsCompileStatic
import grails.converters.JSON
import grails.rest.RestfulController
import grails.web.http.HttpHeaders

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.OK

@SuppressWarnings('AbstractClassWithoutAbstractMethod')
@Artefact("Controller")
//@GrailsCompileStatic
abstract class RestDaoController<T> extends RestfulController<T> {
    //Responce formats, json - by default
    static responseFormats = ['json', 'xml']
    ErrorMessageService errorMessageService

    RestDaoController(Class<T> domainClass) {
        this(domainClass, false)
    }

    RestDaoController(Class<T> domainClass, boolean readOnly) {
        super(domainClass, readOnly)
    }

    Class<T> getDomainClass() {
        resource
    }

    protected GormDao getDao() {
        resource.dao
    }

    /**
     * Queries for a resource for the given id
     *
     * @param id The id
     * @return The resource or null if it doesn't exist
     */
    @Override
    protected T queryForResource(Serializable id) {
        T entity = resource.get(id)
        DaoUtil.checkFound(entity, [id: id], resource.name)
        entity

    }

    /**
     * Lists all resources with paging
     *
     * @param max The maximum
     * @return A list of resources
     */
    def index(Integer max) {
        Pager pager = new Pager(params)
        Map json = pager.setupData(listAllResources(params as Map)).jsonData
        respond json
    }

    /**
     * List all of resource based on parameters
     *
     * @return List of resources or empty if it doesn't exist
     */
    protected List<T> listAllResources() {
        List q = dao.list(params)
    }

    def saveOrUpdate() {
        if (handleReadOnly()) {
            return
        }
        Map result = params.id ? updateDomain() : insertDomain()
        request.withFormat {
            '*' {
                response.addHeader(HttpHeaders.LOCATION,
                        grailsLinkGenerator.link(resource: this.controllerName, action: 'show', id: result.entity.id, absolute: true,
                                namespace: hasProperty('namespace') ? this.namespace : null))
                respond result.entity, [status: (params.id ? OK : CREATED)]
            }
        }
    }

    Map insertDomain() {
        dao.create(request.JSON)
    }

    Map updateDomain() {
        dao.update(fullParams(params, request))
    }
    /**
     * Saves a resource
     */
    @Override
    def save() {
        saveOrUpdate()
    }

    /**
     * Updates a resource for the given id
     * @param id
     */
    @Override
    def update() {
        saveOrUpdate()
    }

    /**
     * Deletes a resource for the given id
     * @param id The id
     */
    @Override
    def delete() {
        if (handleReadOnly()) {
            return
        }
        T instance = queryForResource(params.id)
        deleteResource(params)
        respond instance, [status: OK]

    }

    /**
     * Deletes a resource
     *
     * @param resource The resource to be deleted
     */
    @Override
    protected void deleteResource(p) {
        dao.remove(p.id)
    }

    def fullParams(params, request) {
        Map p = new HashMap(JSON.parse(request))
        p.id = params.id
        p
    }

    def handleDomainNotFoundException(DomainNotFoundException e) {
        response.status = 404
        render([error: e.message] as JSON)
    }

//    def handleException(Exception e) {
//        Object ent
//        if(e.hasProperty("entity")) ent = e.entity
//        Map errResponse = errorMessageService.buildErrorResponse(e)
//        response.status = errResponse.code
//        request.withFormat {
//            '*' { respond ent, model: [errors: errResponse.errors], status: errResponse.code }
//        }
//    }
}
