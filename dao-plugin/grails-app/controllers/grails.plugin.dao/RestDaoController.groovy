package grails.plugin.dao

import grails.converters.JSON
import grails.plugin.dao.DaoUtil
import grails.plugin.dao.DomainException
import grails.rest.RestfulController
import grails.transaction.Transactional
import grails.util.GrailsNameUtils
import grails.web.http.HttpHeaders
import grails.web.servlet.mvc.GrailsParameterMap
import org.springframework.http.HttpStatus

import static org.springframework.http.HttpStatus.CREATED
import static org.springframework.http.HttpStatus.NOT_FOUND
import static org.springframework.http.HttpStatus.NO_CONTENT
import static org.springframework.http.HttpStatus.OK

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

    Class getDomainClass() {
        resource
    }

    protected def getDao() {
        resource.dao
    }

    /**
     * Lists all resources with paging
     *
     * @param max The maximum
     * @return A list of resources
     */
    def index(Integer max) {
        params.max = max
        Pager pager = new Pager(params)
        def json = pager.setupData(listAllResources(params)).jsonData
        respond json
    }


    /**
     * List all of resource based on parameters
     *
     * @return List of resources or empty if it doesn't exist
     */
    protected List<T> listAllResources(Map params) {
        def crit = domainClass.createCriteria()
        def pager = new Pager(params)
        def datalist = crit.list(max: pager.max, offset: pager.offset) {
            if (params.sort)
                order(params.sort, params.order)
        }
        return datalist
    }

	def saveOrUpdate() {
		if(handleReadOnly()) {
			return
		}
		Map result = params.id ? updateDomain() : insertDomain()
            request.withFormat {
                '*' {
                    response.addHeader(HttpHeaders.LOCATION,
                            grailsLinkGenerator.link( resource: this.controllerName, action: 'show',id: result.entity.id, absolute: true,
                                    namespace: hasProperty('namespace') ? this.namespace : null ))
                    respond result.entity, [status: (params.id ? OK: CREATED)]
                }
            }
	}

	def insertDomain(){
		dao.insert(request.JSON)
	}

	def updateDomain(){
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
        if(handleReadOnly()) {
            return
        }

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
        dao.remove(p)
    }


    def fullParams(params, request) {
        def p = new HashMap(JSON.parse(request))
        p.id = params.id
        p
    }


}
