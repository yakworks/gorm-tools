package gpbench.traits

import gorm.tools.WithTrx
import gorm.tools.databinding.BindAction
import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Transactional
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.grails.datastore.mapping.core.Session

@CompileStatic
abstract class BenchProcessData implements BenchConfig, WithTrx  {

    //default for insert, override for updates
    Closure bindAndSaveClosure = { Class<?> domainClass, Map row ->
        bindAndSave((GormEntity)domainClass.newInstance(), row)
    }

    Closure repoBatchClosure = { List batchList, Map args ->
        getRepository().batchCreate(batchList)
    }

    abstract void run()

    void processData(Class domainClass, String binderType, String benchKey) {
        processData( domainClass, data(), binderType, benchKey)
    }

    @CompileStatic
    void processData(Class domainClass, List<Map> dataMap, String binderType, String benchKey) {
        this.binderType = binderType
        this.benchKey = benchKey
        setRepo(domainClass)
        runAndRecord(domainClass,dataMap){
            //cleanup and remove the inserted data
            if(createAction.startsWith('save')){
                if(createAction == 'save batch') {
                    saveBatch(domainClass,dataMap)
                } else if(createAction == 'save async') {
                    saveAsync(domainClass,dataMap)
                }
            } else {
                //it's a createAction of create, update or validate so just spin through.
                for (Map row : dataMap) {
                    getBindAndSaveClosure().call(domainClass, row)
                }
            }
        }
        cleanup(domainClass, dataMap.size())
    }

    void saveAsync(Class domainClass, List dataMap){
        //collates list into list of lists
        List<List> collatedList = asyncBatchSupport.collate(dataMap)
        if(binderType == 'gorm-tools-repo'){
            asyncBatchSupport.parallel(collatedList, getRepoBatchClosure())
        } else {
            asyncBatchSupport.parallelBatch(collatedList) { Map row, Map zargs ->
                getBindAndSaveClosure().call(domainClass, row)
            }
        }
    }

    void saveBatch(Class<?> domainClass, List<Map> dataMap, Closure rowClosure = getBindAndSaveClosure()){
        List<List<Map>> collatedList = dataMap.collate(batchSize)
        for (List<Map> batchList : collatedList) {
            binderType == 'gorm-tools-repo' ? getRepoBatchClosure().call(batchList, [:]) :
                saveBatchChunkTx(domainClass, batchList, rowClosure)
        }
    }

    @Transactional
    void saveBatchChunkTx(Class<?> domainClass, List<Map> dataMap, Closure rowClosure = getBindAndSaveClosure()){
        //println "saveBatchChunkTx $domainClass with ${data.size()} data items "
        for (Map row : dataMap) {
            rowClosure.call(domainClass, row)
        }
        flushAndClear(domainClass)
    }

    void flushAndClear(Class domainClass){
        Session session = GormEnhancer.findStaticApi(domainClass).datastore.currentSession
        session.flush() //probably redundant
        session.clear() //clear the cache
    }

    void bindAndSave(GormEntity instance, Map row) {
        //rowClosure.call(instance, row)
        if(binderType == 'grails') {
            //logMessage "insertRow setProperties"
            ((WebDataBinding)instance).setProperties(row)
        } else if(binderType.startsWith('gorm-tools')) {
            repository.bind([:], instance, row, BindAction.Create)
            //entityMapBinder.bind(instance, row)
        } else if(binderType == 'settersStatic') {
            instance.invokeMethod('setProps', row)
        } else if(binderType == 'settersDynamic') {
            setterDynamic(instance, row)
        }
        else if(binderType == 'settersDynamic-useGet') {
            setterDynamic(instance, row, true)
        }

        if(createAction == 'validate') {
            assert instance.validate()
        } else if(createAction.startsWith('save')) {
            if(binderType.startsWith('gorm-tools')) {
                ((GormRepoEntity)instance).persist()
                //instance.save([failOnError:true])
            } else {
                instance.save([failOnError:true])
            }
        }
    }

    void doSettersStatic(Class domainClass, String benchKey = 'setters static'){
        processData(domainClass, data(), 'settersStatic', benchKey)
    }

    void doSettersDynamic(Class domainClass, String benchKey = 'setters dynamic'){
        processData(domainClass, data(), 'settersDynamic', benchKey)
    }

    void doGormToolsRepo(Class domainClass, String benchKey = 'gorm-tools: repository batch methods'){
        processData(domainClass, data(), 'gorm-tools-repo', benchKey)
    }

    void doGormToolsRepoPersist(Class domainClass, String benchKey = 'gorm-tools: fast binder & persist'){
        processData(domainClass, data(), 'gorm-tools-persist', benchKey)
    }

    //defaults to insert clean up, override for updates
    @CompileDynamic
    void cleanup(Class domainClass, Integer dataSize) {
        if(createAction.startsWith('save')){
            Integer rowCount = GormEnhancer.findStaticApi(domainClass).count()
            assert dataSize == rowCount
            withTrx { status ->
                domainClass.executeUpdate("delete from ${domainClass.getSimpleName()}".toString())
                flushAndClear(status)
            }
        }
    }

    abstract void setterDynamic(instance, row, useGet = false)
}
