package gpbench.traits

import gorm.tools.repository.GormRepoEntity
import gorm.tools.repository.RepoUtil
import grails.gorm.transactions.Transactional
import grails.web.databinding.WebDataBinding
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import org.grails.datastore.gorm.GormEnhancer
import org.grails.datastore.gorm.GormEntity
import org.springframework.util.StopWatch

@CompileStatic
class BenchDataInsert implements BenchConfig {

    void warmUpAndInsert(Class<?> domainClass) {
        //logMessage "warmUpAndInsert with ${dataList.size()} records. ${domainClass.simpleName}-$saveAction - binderType: $binderType"
        muteConsole = true
        (1..warmupCycles).each {
            insertData(domainClass, warmupDataList)
        }
        muteConsole = false
        //run the real deal
        insertData(domainClass, dataList)
    }

    @CompileStatic
    void insertData(Class domainClass, List<Map> data) {
        //logMessage "insertData with ${dataList.size()} records. ${domainClass.simpleName}-$saveAction - binderType: $binderType"

        StopWatch watch = new StopWatch()
        watch.start()

        //cleanup and remove the inserted data
        if(createAction.startsWith('save')){

            if(createAction == 'save batch') {
                insertDataBatch(domainClass, data)
            } else if(createAction == 'save async') {
                insertDataAsync(domainClass, data)
            } else {
                insertDataBasicTrx(domainClass, data)
            }

        } else {
            insertDataBasic(domainClass, data)
        }

        watch.stop()

        Integer dataSize = data.size()

        //cleanup and remove the inserted data
        if(createAction.startsWith('save')){
            Integer rowCount = GormEnhancer.findStaticApi(domainClass).count()
            assert dataSize == rowCount
            cleanup(domainClass)
        }

        logMessage "${watch.totalTimeSeconds}s $benchKey - binderType: $binderType, " +
            "${createAction ? 'createAction: ' + createAction : ''} " +
            "- $domainClass.simpleName | $dataSize rows"

        recordStat(domainClass, dataSize, watch.totalTimeSeconds)

    }

    void insertDataBasic(Class<?> domainClass, List<Map> data) {
        //logMessage "insertDataBasic"
        for (Map row : data) {
            GormEntity instance = (GormEntity)domainClass.newInstance()
            insertRow(instance, row)
        }
    }

    @Transactional
    void insertDataBasicTrx(Class<?> domainClass, List<Map> data) {
        //logMessage "insertDataBasic"
        for (Map row : data) {
            GormEntity instance = (GormEntity)domainClass.newInstance()
            insertRow(instance, row)
        }
    }

    void insertDataBatch(Class<?> domainClass, List<Map> data) {
        //println "insertDataBatch $domainClass"
        //slice or chunk the list into a list of lists
        List<List<Map>> collatedList = dataList.collate(batchSize)
        for (List<Map> batchList : collatedList) {
            insertDataBatchChunk(domainClass, batchList)
        }

    }

    @Transactional
    void insertDataBatchChunk(Class<?> domainClass, List<Map> data){
        //println "insertDataBatchChunk $domainClass with ${data.size()} data items "
        data.eachWithIndex { Map row, int index ->
            GormEntity instance = (GormEntity)domainClass.newInstance()
            insertRow(instance, row)
            //if (index % batchSize == 0) RepoUtil.flushAndClear()
        }
        RepoUtil.flushAndClear()
    }

    void insertDataAsync(Class<?> domainClass, List<Map> data) {
        //slice or chunk the list into a list of lists
        List<List> collatedList = asyncBatchSupport.collate(data)
        asyncBatchSupport.parallelBatch(collatedList) { Map row, Map zargs ->
            GormEntity instance = (GormEntity)domainClass.newInstance()
            insertRow(instance, row)
        }
    }

    void insertRow(GormEntity instance, Map row) {
        //rowClosure.call(instance, row)
        if(binderType == 'grails') {
            //logMessage "insertRow setProperties"
            ((WebDataBinding)instance).setProperties(row)
        } else if(binderType == 'fast') {
            entityMapBinder.bind(instance, row)
        } else if(binderType == 'settersStatic') {
            instance.invokeMethod('setProps', row)
        } else if(binderType == 'settersDynamic') {
            setterDynamic(instance, row)
        }

        if(createAction == 'validate') {
            assert instance.validate()
        } else if(createAction.startsWith('save')) {
            if(binderType == 'fast') {
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
