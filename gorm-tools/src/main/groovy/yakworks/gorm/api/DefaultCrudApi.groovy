/*
* Copyright 2019 Yak.Works - Licensed under the Apache License, Version 2.0 (the "License")
* You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
*/
package yakworks.gorm.api

import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic

import org.grails.orm.hibernate.HibernateDatastore
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier

import gorm.tools.beans.Pager
import gorm.tools.job.SyncJobEntity
import gorm.tools.mango.api.QueryArgs
import gorm.tools.metamap.services.MetaMapService
import gorm.tools.repository.PersistArgs
import gorm.tools.repository.RepoLookup
import gorm.tools.repository.RepoUtil
import gorm.tools.repository.model.ApiCrudRepo
import gorm.tools.repository.model.DataOp
import gorm.tools.repository.model.EntityResult
import gorm.tools.transaction.TrxUtils
import grails.gorm.transactions.Transactional
import yakworks.api.problem.data.DataProblemException
import yakworks.gorm.api.bulk.BulkApiSupport
import yakworks.gorm.api.bulk.BulkExportSupport
import yakworks.gorm.api.support.QueryArgsValidator
import yakworks.gorm.config.QueryConfig
import yakworks.meta.MetaMap
import yakworks.meta.MetaMapList
import yakworks.spring.AppCtx

/**
 * CRUD api for rest repo
 */
@CompileStatic
class DefaultCrudApi<D> implements CrudApi<D> {

    private static final String FORMAT_XLSX = "xlsx"

    Class<D> entityClass

    @Autowired HibernateDatastore hibernateDatastore
    @Autowired IncludesConfig includesConfig
    @Autowired ApiConfig apiConfig
    @Autowired MetaMapService metaMapService

    @Qualifier("queryArgsValidator")
    @Autowired QueryArgsValidator queryArgsValidator

    @Autowired QueryConfig queryConfig

    /** Not required but if an BulkSupport bean is setup then it will get get used */
    @Autowired(required = false)
    BulkExportSupport<D> bulkExportSupport

    /** Not required but if an BulkSupport bean is setup then it will get get used */
    @Autowired(required = false)
    BulkApiSupport<D> bulkApiSupport

    DefaultCrudApi(Class<D> entityClass){
        this.entityClass = entityClass
    }

    public static <D> DefaultCrudApi<D> of(Class<D> entityClass){
        def capiInstance = new DefaultCrudApi(entityClass)
        AppCtx.autowire(capiInstance) //wire up the @Autowired
        //erInstance.pathItem = erInstance.apiConfig.pathsByEntity[entityClass.name]
        return capiInstance
    }

    class DefaultApiResult<D> implements CrudApiResult<D> {
        List<String> includes
        D entity
        int status

        DefaultApiResult(D entity, List<String> includes){
            this.entity = entity
            this.includes = includes
        }

        @CompileDynamic //getting goofy error in intellij about casting entity to D
        Map asMap(){
            entityToMap(this.entity, includes)
        }
    }

    ApiCrudRepo<D> getApiCrudRepo() {
        RepoLookup.findRepo(getEntityClass())
    }

    BulkApiSupport<D> getBulkApiSupport(){
        if (!bulkApiSupport) this.bulkApiSupport = BulkApiSupport.of(getEntityClass())
        return bulkApiSupport
    }

    BulkExportSupport<D> getBulkExportSupport(){
        if (!bulkExportSupport) this.bulkExportSupport = new BulkExportSupport(getEntityClass())
        return bulkExportSupport
    }

    /**
     * calls the IncludesConfig's getIncludes passing in any controller overrides
     */
    Map getIncludesMap(){
        return includesConfig.getIncludesMap(entityClass)
    }

    @Transactional(readOnly = true)
    @Override
    CrudApiResult<D> get(Serializable id, Map qParams){
        D instance = (D) getApiCrudRepo().read(id)
        RepoUtil.checkFound(instance, id, getEntityClass().simpleName)
        return createApiResult(instance, qParams)
    }

    /**
     * Create entity from data and return the MetaMap of what was created
     */
    @Transactional
    @Override
    CrudApiResult<D> create(Map data, Map qParams){
        Boolean bindId = qParams.getBoolean('bindId', false)
        var args = PersistArgs.of(bindId: bindId)
        D instance = (D) getApiCrudRepo().create(data, args)
        return createApiResult(instance, qParams)
    }

    /**
     * Update entity from data
     */
    @Transactional
    @Override
    CrudApiResult<D> update(Map data, Map qParams){
        Map dataMap = [id: qParams.id]
        // json dataMap will normally not contain id because it passed in url params,
        // but if it does it copies it in and overrides, so the id in the dataMap will win
        // FIXME I dont think the above is the right default, the url id I think should always win
        dataMap.putAll(data)
        D instance = (D) getApiCrudRepo().update(dataMap, PersistArgs.of())
        return createApiResult(instance, qParams)
    }

    /**
     * Create or Update entity from data. Checks if key exists and updates, otherwise inserts
     */
    @Transactional
    @Override
    CrudApiResult<D> upsert(Map data, Map qParams){
        EntityResult<D> entityResult = getApiCrudRepo().upsert(data, PersistArgs.of())
        var apiRes = createApiResult(entityResult.entity, qParams)
        apiRes.status = entityResult.status.code
        return apiRes
    }

    /**
     * Remove by ID
     * @param id - the id to delete
     * @param args - the PersistArgs to pass to delete. flush being the most common
     */
    @Transactional
    @Override
    void removeById(Serializable id, Map qParams){
        getApiCrudRepo().removeById(id)
    }

    /**
     * Checks if the id exists
     */
    @Transactional(readOnly = true)
    @Override
    boolean exists(Serializable id){
        getApiCrudRepo().exists(id)
    }

    /**
     * Mango Query that returns a paged list
     */
    @Transactional(readOnly = true)
    @Override
    Pager list(Map qParams, URI uri){
        Pager pager = Pager.of(qParams)
        QueryArgs qargs = createQueryArgs(pager, qParams, uri)

        List dlist = queryList(qargs)
        List<String> incs = getIncludes(qParams, [IncludesKey.list, IncludesKey.get])
        return createPagerResult(pager, qParams, dlist, incs)
    }

    @Transactional(readOnly = true)
    @Override
    Pager pickList(Map qParams, URI uri){
        Pager pager = Pager.of(qParams)
        QueryArgs qargs = createQueryArgs(pager, qParams, uri)
        List dlist = queryList(qargs)
        List<String> incs = getIncludes(qParams, [IncludesKey.picklist, IncludesKey.stamp])
        return createPagerResult(pager, qParams, dlist, incs)
    }

    @Override
    SyncJobEntity bulkImport(DataOp dataOp, List<Map> dataList, Map qParams, String sourceId){
        getBulkApiSupport().bulkImport(dataOp, dataList, qParams, sourceId)
    }

    SyncJobEntity bulkExport(Map params, String sourceId) {
        getBulkExportSupport().queueExportJob(params, sourceId)
    }

    protected List<D> queryList(QueryArgs qargs) {
        return getApiCrudRepo().query(qargs, null).pagedList(qargs.pager)
    }

    protected Pager createPagerResult(Pager pager, Map qParams, List dlist, List<String> incs) {
        MetaMapList entityMapList = metaMapService.createMetaMapList(dlist, incs)
        var elist = entityMapList as List<Map>
        pager.dataList(elist)
        return pager
    }


    @Override
    CrudApiResult<D> createApiResult(D instance, Map params){
        List<String> incs = getIncludes(params, [IncludesKey.get])
        new DefaultApiResult(instance, incs)
    }

    @Override
    Map entityToMap(D instance, List<String> includes) {
        flushIfSession() //in testing need to flush before generating MetaMap
        MetaMap emap = metaMapService.createMetaMap(instance, includes)
        return emap
    }

    /**
     * Gets the includes/includesKey from the qParams or from the fallbackKeys
     * @return the list of fields in our mango format.
     */
    protected List<String> getIncludes(Map qParams, List fallbackIncludesKeys) {
        //parse the params into the IncludesProps
        var incProps = IncludesProps.of(qParams).fallbackKeys(fallbackIncludesKeys)

        //if includes was passed in, then it wins
        if(incProps.includes) return incProps.includes

        //otherwise search based on includesKey
        List<String> incs = includesConfig.findIncludes(getEntityClass(), incProps)
        return incs
    }

    protected QueryArgs createQueryArgs(Pager pager, Map qParams, URI uri) {
        QueryArgs qargs = QueryArgs.withPager(pager)
            .strict(true) //only use criteria if its under the q query param
            .uri(uri)
            .build(qParams)
            .defaultSortById() //add default id sort if none exists
            .validateQ(qRequired()) //if q is required then blows error if nothing was parsed out

        validateQueryArgs(qargs, qParams)

        return qargs
    }

    protected void validateQueryArgs(QueryArgs args, Map params) {
        //FIXME, export to xlsx passes large number for max eg 10K at RNDC, below hack is to allow tht max for export
        boolean isExcelExport = params && params['format'] == FORMAT_XLSX

        try {
            getQueryArgsValidator().validate(args)
        } catch(DataProblemException ex) {
            //For excel export, ui can send max=10,000 : if thts the case dont fail, catch and move on to override max to exportMax
            if(isExcelExport && ex.code == "error.query.max") {
                args.pager.max = queryConfig.exportMax
            } else {
                throw ex
            }
        }
    }

    /**
     * checks the apiconfig to see if we set it as reuiqred for the endpoint
     */
    protected boolean qRequired(){
        PathItem pathItem = apiConfig.pathsByEntity[entityClass.name]
        return pathItem?.qRequired
    }

    /**
     * In certain rare cases controller action will be inside a hibernate session
     * primarily needed for testing but there are some edge cases where this is needed
     * checks if repo datastore has a session and flushes if so
     */
    protected void flushIfSession(){
        if(hibernateDatastore.hasCurrentSession()){
            TrxUtils.flush(hibernateDatastore)
        }
    }
}
