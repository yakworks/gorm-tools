package gpbench.traits

import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Transactional
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity

@CompileStatic
class BenchDataInsert implements BenchConfig {

    Closure defaultInsertClosure = { Class<?> domainClass, Map row ->
        GormEntity instance = (GormEntity)domainClass.newInstance()
        insertRow(instance, row)
    }

    @CompileStatic
    void insertData(Class domainClass, List<Map> data) {
        setRepo(domainClass)
        runAndRecord(domainClass,data){
            //cleanup and remove the inserted data
            if(createAction.startsWith('save')){
                if(createAction == 'save batch') {
                    binderType == 'gorm-tools' ? insertDataRepository(domainClass, data) : insertDataBatch(domainClass, data)
                } else if(createAction == 'save async') {
                    binderType == 'gorm-tools' ? insertDataAsyncRepository(domainClass, data) : insertDataAsync(domainClass, data)
                } else {
                    insertDataBasicTrx(domainClass, data)
                }

            } else {
                insertDataBasic(domainClass, data)
            }
        }

        assertAndCleanup(domainClass, data.size())

    }

    void assertAndCleanup(Class domainClass, Integer dataSize){
        if(createAction.startsWith('save')){
            Integer rowCount = GormEnhancer.findStaticApi(domainClass).count()
            assert dataSize == rowCount
            cleanup(domainClass)
        }
    }

    void insertDataBasic(Class<?> domainClass, List<Map> data, Closure rowClosure = defaultInsertClosure) {
        //logMessage "insertDataBasic"
        for (Map row : data) {
            rowClosure.call(domainClass, row)
        }
    }

    @Transactional
    void insertDataBasicTrx(Class<?> domainClass, List<Map> data, Closure rowClosure = defaultInsertClosure) {
        //logMessage "insertDataBasic"
        for (Map row : data) {
            rowClosure.call(domainClass, row)
        }
        RepoUtil.flushAndClear()
    }

    void insertDataRepository(Class<?> domainClass, List<Map> data) {
        setRepo(domainClass)
        List<List<Map>> collatedList = dataList.collate(batchSize)
        for (List<Map> batchList : collatedList) {
            repository.batchCreate(batchList)
        }
    }

    void insertDataBatch(Class<?> domainClass, List<Map> data, Closure rowClosure = defaultInsertClosure) {
        //slice or chunk the list into a list of lists
        List<List<Map>> collatedList = dataList.collate(batchSize)
        for (List<Map> batchList : collatedList) {
            insertDataBatchChunk(domainClass, batchList, rowClosure)
        }
    }

    @Transactional
    void insertDataBatchChunk(Class<?> domainClass, List<Map> data, Closure rowClosure = defaultInsertClosure){
        //println "insertDataBatchChunk $domainClass with ${data.size()} data items "
        data.eachWithIndex { Map row, int index ->
            rowClosure.call(domainClass, row)
        }
        RepoUtil.flushAndClear()
    }

    void insertDataAsync(Class<?> domainClass, List<Map> data, Closure rowClosure = defaultInsertClosure) {
        //slice or chunk the list into a list of lists
        List<List> collatedList = asyncBatchSupport.collate(data)
        asyncBatchSupport.parallelBatch(collatedList) { Map row, Map zargs ->
            GormEntity instance = (GormEntity)domainClass.newInstance()
            insertRow(instance, row)
        }
    }

    void insertDataAsyncRepository(Class<?> domainClass, List<Map> data) {
        setRepo(domainClass)
        List<List> collatedList = asyncBatchSupport.collate(data)
        asyncBatchSupport.parallel(collatedList) { List<Map> list, Map args ->
            repository.batchCreate(list)
        }
    }

    void insertRow(GormEntity instance, Map row) {
        //rowClosure.call(instance, row)
        if(binderType == 'grails') {
            //logMessage "insertRow setProperties"
            ((WebDataBinding)instance).setProperties(row)
        } else if(binderType == 'gorm-tools') {
            entityMapBinder.bind(instance, row)
        } else if(binderType == 'settersStatic') {
            instance.invokeMethod('setProps', row)
        } else if(binderType == 'settersDynamic') {
            setterDynamic(instance, row)
        }

        if(createAction == 'validate') {
            assert instance.validate()
        } else if(createAction.startsWith('save')) {
            if(binderType == 'gorm-tools') {
                ((GormRepoEntity)instance).doPersist()
                //instance.save([failOnError:true])
            } else{
                instance.save([failOnError:true])
            }
        } else if(createAction == 'persist') {
            ((GormRepoEntity)instance).persist()
        }
    }

    @CompileDynamic
    void flushAndClear(){
        RepoUtil.flushAndClear()
    }

    @Transactional
    @CompileDynamic
    @Override
    void cleanup(Class domainClass) {
        domainClass.executeUpdate("delete from ${domainClass.getSimpleName()}".toString())
        RepoUtil.flushAndClear()
    }

    @CompileDynamic
    void setterDynamic(instance, row) { }
}
