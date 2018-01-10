# GORM : batch importing large datasets and a performance benchmarking app


<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Intro](#intro)
- [How to run the benchmarks](#how-to-run-the-benchmarks)
- [Changing default pool size](#changing-default-pool-size)
- [The Benchmarks](#the-benchmarks)
- [Bench Mark Results and details](#bench-mark-results-and-details)
- [Pass 1 multi-thread - standard grails binding baseline](#pass-1-multi-thread-standard-grails-binding-baseline)
- [Pass 2 multi-thread](#pass-2-multi-thread)
- [Pass 3 multi-thread - copy props](#pass-3-multi-thread-copy-props)
	- [CPU Load during Gparse batch insert](#cpu-load-during-gparse-batch-insert)
- [System specs](#system-specs)
- [Conclusions](#conclusions)
	- [Optimum setting for Gpars pool size.](#optimum-setting-for-gpars-pool-size)
	- [Questions answered by above conclusion.](#questions-answered-by-above-conclusion)
- [Batching inserts and updates with hibernate](#batching-inserts-and-updates-with-hibernate)	
- [References and reading](#references-and-reading)

<!-- /TOC -->

## Intro

The application runs large batch inserts (115K records) in different ways. The goal is to decide the optimum way to run large batch inserts with Gorm.

The goal is to have good benchmarks to measure and answer following questions.

- Fastest way to persist/insert large batches using gorm.
- Show advantages of using multi-core.
- Does binding slow it down, why, can it be optimized, best alternative
- Does valiation slow it down, why, can it be optimized
- Does Auditstamp slow it down, can it be optimized
- Does Repostories slow it down
- Do custom Id generator slow it down, or improve speed
- does @compileStatic speed things up, where does it matter? does it help to have it on the domains?

## How to run the benchmarks

- There is a script ```run-benchmarks.sh``` which will run the benchmarks
- Run ```./run-benchmarks.sh``` or `gradle assemble; java -server -jar -DloadIterations=3 -Dgpars.poolsize=1 build/libs/benchmarks.war`
- **loadIterations** multiplies the base 36k City record set. so setting it to 3 will insert 118k rows.
- **gpars.poolsize** 1 for single thread, set to 5 if you have a 4 core processor (even if its 8 cores with hyperhreading)

## How to run the benchmarks with Second level cache

* By default Second level cache is disabled for benchmarks. To enable it we should use the 'secondLevelCache' system property.
  For example:
    ```java -server -jar -DloadIterations=3 -Dgpars.poolsize=1 -DsecondLevelCache=true build/libs/benchmarks.war```
    
*  ```./run-benchmarks.sh``` script by default starts benchmarks without Second Level Cache.
  It contains the "Running benchmarks with Second Level cache" section with commented out configuration,
  which can be used to run benchmarks with Second Level Cache.
  Uncomment the next line in the script to run benchmarks with Second Level cache:
  ```#gradle assemble; java -server -jar -DloadIterations=3 -Dgpars.poolsize=5 -DsecondLevelCache=true build/libs/benchmarks.war```
  
  **Note**: Changing caching strategy can affect benchmark results
    
* Caching strategies
    - read-only: Useful for data that is read frequently but never updated (e.g. referential data like Countries). It has the best performances of all.
    - read-write: Desirable if your data needs to be updated. But it doesn't provide a SERIALIZABLE isolation level, phantom reads can occur (you may see at the end of a transaction something that wasn't there at the start). It has more overhead than read-only.
    - nonstrict-read-write: Alternatively, if it's unlikely two separate transaction threads could update the same object, you may use the nonstrict–read–write strategy. It has less overhead than read-write. This one is useful for data that are rarely updated.
    - transactional: If you need a fully transactional cache. Only suitable in a JTA environment.
    See Grails [Caching strategy]{.new-tab} and [Hiberante Second Level cache]{.new-tab} docs for more information 

* Using 'cacheStrategy' system property we can specify the caching strategy for all domains.
  The default caching strategy is 'read-write'.
  For example:
  we can use default strategy (read-write)
  ```java -server -jar -DloadIterations=3 -Dgpars.poolsize=1 -DsecondLevelCache=true build/libs/benchmarks.war```
  or specify it as a string parameter like so
  ```java -server -jar -DloadIterations=3 -Dgpars.poolsize=1 -DsecondLevelCache=true -DcacheStrategy='read-write' build/libs/benchmarks.war ```
  
* EhCache configuration.
  We use [EhCache]{.new-tab} as the implementation for Second Level cache.
  Grails documentation has not much information about configuring EhCache.
  By default EhCache uses it's own configuration for cache (e.g. max elements in memory, etc).
  **Note** that there might be issues with benchmark results when the max number of records in
  default EhCache config is less that the number of records are used in a benchmark.
  Due to this fact it is highly critical to have appropriate cache size (it depends on data amount),
  we have defined custom configuration for EhCache in the ```benchmarks/grails-app/conf/ehcache.xml``` file.
  Here is the example of custom ```ehcache.xml`` file: 
  ```
  <ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../config/ehcache.xsd">
      <diskStore path="java.io.tmpdir"/>
      <defaultCache
              maxElementsInMemory="50000"
              overflowToDisk="false"
              maxElementsOnDisk="0"
      >
      </defaultCache>
  </ehcache>
  ```
  
  ```maxElementsInMemory``` defines maximum number of elements in memory;
  ```overflowToDisk``` defines if data should be saved to disk in case if maximum number of elements in memory is reached 
  For more information see docs for [EhCache Configuration]{.new-tabs}

* Using MySQL database.
  In case of using MySQL database instead of H2, the performance impact from using Second level cache
  can be better seen in benchmark results.
  To be able to use MySQL database please uncomment config for dataSource in 
  ```../benchmarks/conf/application.yml``` and dependency for the mysql connector in ```../benchmarks/build.gradle```
  and change default production environment config.
  Tested with MySQL 5.7.20.

## Changing default pool size

By default benchmarks uses default gpars pool size which is (availableProcessors + 1) which can be modified by passing system property ```gpars.poolsize=xx```
Example: ```java -Dgpars.poolsize=5 -jar grails-gpars-batch-load-benchmark-0.1.war```

## The Benchmarks

- GparsBatchInsertBenchmark - Runs the batches in parallel, each batch with the same size as jdbc batch size (50).
- GparsBatchInsertWithoutDaoBenchmark - Same as above, but without using dao for inserting records.
- GparsBatchInsertWithoutValidation - Runs the batches with gpars but with grails domain validation tunred off during save using ```domain.save(false)```
- GparsThreadPerTransactionBenchmark - Runs the inserts in parallel with one thread per transaction.
- BatchInsertWithDataFlawQueueBenchmark - Runs the batch inserts in parallel using Gprase dataflow operator [see](http://www.gpars.org/webapp/quickstart/index.html#__strong_dataflow_concurrency_strong)
- SimpleBatchInsertBenchmark - Batch inserts but without gpars parallelism.
- CommitEachSaveBenchmark - Insert each record in individual transactions.
- OneBigTransactionBenchmark - Inserts all records within a single big transaction.
- DataFlawQueueWithScrollableQueryBenchmark : same as BatchInsertWithDataFlawQueueBenchmark but the data is being loaded from another table simultanously using scrollable resultset.

Note: All of above benchmarks are run with and without data binding, and you will see the results for both.


## Bench Mark Results and details

* 115k CSV records on a MacBook pro 2.5 GHz Intel Core i7. 2048 mb ram was given to the VM and these were run using ```java -jar grails-gpars-batch-load-benchmark-0.1.war```
* All of these have jdbc.batch_size = 50 and use the principals from #2 above and flush/clear every 50 rows
* The winner seems to be gpars and batched (smaller chunks) transactions


**Results with Gparse pool size 8**

|                      | All records in single transaction | Commit each record | Batched Transaction - Without Gpars  | Batched Transactions - With Gpars  | Gpars single transaction per thread  |
|----------------------|-----------------------------------|--------------------|--------------------------------------|------------------------------------|--------------------------------------|
| With data binding    | 40.807                           | **81.521**          | 43.569                              |  12.32                              | 22.372                               |



**Results for Gparse batched with different pool sizes**

|            Pool size            | 2 threads | 3 threads | 4 threads | 5 threads | 6 threads | 7 threads | 8 threads | 9 threads | 10 threads | 11 threads | 12 threads |
| ------------------------------- | --------- | --------- | --------- | --------- | --------- | --------- | --------- | --------- | ---------- | ---------- | ---------- |
| With data binding               | 24.522    | 22.473    | 16.063    | 17.363    | 16.698    | 14.53     | 12.32     | 12.012    | 12.145     | 14.785     | 14.081     |
| Without data binding            | 12.302    | 12.593    | 9.52      | 8.586     | 8.509     | 7.46      | 6.842     | 6.27      | 6.696      | 6.896      | 7.074      |
| No validation                   | 15.335    | 17.588    | 9.906     | 10.3      | 10.724    | 9.489     | 7.993     | 8.112     | 8.203      | 9.069      | 9.032      |
| No validation & No data binding | 10.619    | 9.311     | 7.088     | 7.59      | 7.997     | 8.088     | 6.558     | 5.896     | 5.683      | 6.223      | 6.594      |


|               Gpars Benchs                |  time  |
| ----------------------------------------- | ------ |
| with databinding                          | 12.32  |
| no binding                                | 6.842  |
| No autowire                               | 12.969 |
| no validation                             | 7.993  |
| no binding, no autowire,  no validation   | 6.221  |
| grails date stamp fields                  | 14.726 |
| audit-trail stamp fields (user and dates) | 21.728 |
| no dao                                    | 10.603 |
| DataflowQueue (CED Way)                   | 12.356 |
| Custom IdGenerator                        | 12.532 |


## Benchmark results for case when using Second Level Cache

* Next benchmarks were launched on the PC - Intel® Core™ i5-6600 CPU @ 3.30GHz × 4; 16 GiB RAM; Ubuntu 16.04 64bit

* Benchmark results for insert operations with- and without- Second Level Cache.

**Note:** An attempt to update an entity with ```read-only``` caching strategy
        will produce the exception with message "Cannot make an immutable entity modifiable" 

**Using H2 database**

|            Pool size                                 | without cache | read-write | nonstrict-read-write | transactional |
| ---------------------------------------------------- | ------------- | ---------- | -------------------- | ------------- |
| GparsBaselineBenchmark<CityBaseline>                 | 2.394s        | 2.462s     | 2.26s                | 2.741s        |
| GparsBaselineBenchmark<City>                         | 2.237s        | 3.218s     | 2.641s               | 2.655s        |
| GparsRepoBenchmark<City> (Events disabled)           | 2.207s        | 2.335s     | 2.316s               | 2.6s          |
| GparsRepoBenchmark<City> (Events enabled)            | 2.309s        | 2.453s     | 2.463s               | 2.911s        |
| GparsRepoBenchmark<CityMethodEvents> (method events) | 2.619s        | 2.888s     | 3.812s               | 2.638s        |
| GparsRepoBenchmark<CitySpringEvents> (spring events) | 2.209s        | 3.572s     | 3.915s               | 4.955s        |
| GparsRepoBenchmark<CitySpringEventsRefreshable>      | 2.546s        | 3.223s     | 2.616s               | 2.573s        |
| GparsBaselineBenchmark<CityAuditTrail>               | 2.326s        | 2.371s     | 2.259s               | 2.429s        |


**Using MySQL database**

|            Pool size                                 | without cache | read-write | nonstrict-read-write | transactional |
| ---------------------------------------------------- | ------------- | ---------- | -------------------- | ------------- |
| GparsBaselineBenchmark<CityBaseline>                 | 6.569s        | 5.437s     | 5.486s               | 6.095s        |
| GparsBaselineBenchmark<City>                         | 6.092s        | 6.744s     | 6.744s               | 6.209s        |
| GparsRepoBenchmark<City> (Events disabled)           | 6.084s        | 6.786s     | 7.092s               | 6.024s        |
| GparsRepoBenchmark<City> (Events enabled)            | 6.359s        | 6.466s     | 7.176s               | 6.439s        |
| GparsRepoBenchmark<CityMethodEvents> (method events) | 5.04s         | 6.904s     | 5.634s               | 5.26s         |
| GparsRepoBenchmark<CitySpringEvents> (spring events) | 6.105s        | 6.495s     | 6.849s               | 6.207s        |
| GparsRepoBenchmark<CitySpringEventsRefreshable>      | 6.658s        | 6.521s     | 6.51s                | 6.083s        |
| GparsBaselineBenchmark<CityAuditTrail>               | 5.831s        | 5.877s     | 6.228s               | 6.952s        |


**Results of the benchmark for **read** operations for different databases and caching strategies**

  The benchmark reads all the records once at the beginning to save them to cache
  (records will be added to second level cache only after the first read).
  Then it reads the same records in multiple threads at the same time.
  **Each thread reads all records - 37230 records. As a result (37230 * numberOfThreads) will be read.**
  This is done to simulate work of multiple users, that interact with the same data.

**Using H2 database**

|         Cache strategy         | 2 threads | 5 threads | 9 threads |
| ------------------------------ | --------- | --------- | --------- |
| without second level cache     | 1.223s    | 1.55s     | 2.389s    |
| read-only                      | 0.556s    | 0.742s    | 0.839s    |
| read-write                     | 1.014s    | 1.046s    | 1.43s     |
| nonstrict-read-write           | 1.748s    | 1.204s    | 1.549s    |
| transactional                  | 0.584s    | 0.603s    | 0.848s    |

**Using MySQL database**

|         Cache strategy         | 2 threads | 5 threads | 9 threads |
| ------------------------------ | --------- | --------- | --------- |
| without second level cache     | 8.044s    | 9.533s    | 14.52s    |
| read-only                      | 0.476s    | 0.645s    | 1.113s    |
| read-write                     | 4.324s    | 4.442s    | 4.56s     |
| nonstrict-read-write           | 4.183s    | 5.036s    | 4.688s    |
| transactional                  | 0.488s    | 0.615s    | 1.124s    |

**Results of the benchmark for **update** operations for different databases and caching strategies.
  Firstly, this benchmark reads all the records (37k) at the beginning to save them to Second level cache.
  Then it updates records using pool of threads and splits 37k between threads** 

**Using H2 database**

|         Cache strategy         | 2 threads | 5 threads | 9 threads |
| ------------------------------ | --------- | --------- | --------- |
| without second level cache     | 3.101s    | 2.989s    | 2.389s    |
| read-write                     | 1.748s    | 1.204s    | 4.215s    |
| nonstrict-read-write           | 1.748s    | 1.204s    | 4.215s    |
| transactional                  | 2.565s    | 3.37s     | 2.827s    |

**Using MySQL database**

|         Cache strategy         | 2 threads | 5 threads | 9 threads |
| ------------------------------ | --------- | --------- | --------- |
| without second level cache     | 23.199s   | 16.534s   | 12.517s   |
| read-write                     | 14.99s    | 11.674s   | 10.447s   |
| nonstrict-read-write           | 22.197s   | 12.22s    | 11.631s   |
| transactional                  | 14.041s   | 8.849s    | 7.702s    |


**Conclusions**

  1. According to benchmarks there is no profit of using Second Level cache with insert operations.
     The results of the tests are ambiguous and in most cases show the worst performance when using Second level cache.
     There is an option to disable cache for the current session by adding ignore cache property.
     For example, ```session.setCacheMode(CacheMode.IGNORE)```. See [Disable cache for a session]{.new-tab} article.
  2. Second Level Cache makes sense for read operations.
     It significantly improves the performance of read operations when the data is not changing.
  3. Second level cache also makes sense for update operations, because updates use read to get required entities.
  

<!-- BENCHMARKS -->
```
- Running Benchmarks, loading 372300 records each run
Warm up cycle ********

# Gpars - standard grails binding with baseline
  - Grails Basic Baseline to measure against
26.866s for GparsBaselineBenchmark<CityBaseline> [ bindingMethod: grails ]

  - These should all run within 5% of baseline and each other
25.634s for GparsBaselineBenchmark<CityAuditStampManual> [ bindingMethod: grails ]
27.245s for GparsDaoBenchmark<City> [ bindingMethod: grails ]
27.778s for GparsScriptEngineBenchmark<City> [ bindingMethod: grails ]
25.511s for GparsDaoBenchmark<CityDynamic> [ bindingMethod: grails ]
Warm up cycle ****

  - Performance problems - standard grails binding with baseline
  - These show performance issues
29.554s for GparsBaselineBenchmark<CityAuditTrail> [ bindingMethod: grails ]
33.875s for GparsBaselineBenchmark<CityModelTrait> [ bindingMethod: grails ]
Warm up cycle ****

  - Faster Options - standard grails binding
  - These run faster
21.392s for GparsBaselineBenchmark<CityIdGen> [ bindingMethod: grails ]
24.261s for RxJavaBenchmark<City> [ bindingMethod: grails ]
Warm up cycle ********

# Gpars - copy to fields, no grails databinding
  - Grails Basic Baseline to measure against
17.926s for GparsBaselineBenchmark<CityBaseline> [ bindingMethod: copy ]

  - These should all run within 5% of baseline and each other
17.617s for GparsBaselineBenchmark<CityAuditStampManual> [ bindingMethod: copy ]
18.463s for GparsDaoBenchmark<City> [ bindingMethod: copy ]
17.982s for GparsScriptEngineBenchmark<City> [ bindingMethod: copy ]
18.311s for GparsDaoBenchmark<CityDynamic> [ bindingMethod: copy ]
Warm up cycle ****

  - Performance problems - standard grails binding with baseline
  - These show performance issues
20.566s for GparsBaselineBenchmark<CityAuditTrail> [ bindingMethod: copy ]
18.631s for GparsBaselineBenchmark<CityModelTrait> [ bindingMethod: copy ]
Warm up cycle ****

  - Faster Options - copy to fields, no grails databinding
  - These run faster
13.753s for GparsBaselineBenchmark<CityIdGen> [ bindingMethod: copy ]
17.131s for RxJavaBenchmark<City> [ bindingMethod: copy ]

## Misc sanity checks
17.97s for BatchInsertWithDataFlowQueueBenchmark<City> [ bindingMethod: copy ]
  - using copy instead of binding and no validation, <10% faster
19.377s for GparsBaselineBenchmark<CityBaseline> [ bindingMethod: copy , validation: false]

 - assign id inside domain with beforeValidate
25.183s for GparsBaselineBenchmark<CityIdGenAssigned> [ bindingMethod: grails ]

  - not much difference between static and dynamic method calls
17.17s for GparsDaoBenchmark<City> [ bindingMethod: setter ]
17.913s for GparsDaoBenchmark<City> [ bindingMethod: copy ]
17.71s for GparsDaoBenchmark<City> [ bindingMethod: bindWithSetters ]
19.076s for GparsDaoBenchmark<City> [ bindingMethod: bindWithCopyDomain ]
```
<!-- /BENCHMARKS -->

### CPU Load during Gparse batch insert

It can be seen that cpu load goes highest during Gparse batch insert

![Image of Yaktocat](./cupload.png)


**Note:**
- All Numbers are in Seconds.
- Domain autowiring, validation and databinding are enabled, unless explicitly specified.
- One service is injected in each of the domains.
- H2 In memory database is used.

## System specs

- Macbook Pro Intel(R) Core(TM) i7-4870HQ CPU @ 2.50GHz
- Gparse pool size of 9
- GRAILS_OPTS="-Xmx2048M -server -XX:+UseParallelGC"


## Conclusions

The key conclusions Are as below

1. Gparse with batch insert is the optimum way to do large batch inserts.
2. Grails Databinding has BIG (2-6x slower) performance penalty and an even bigger hit when using Traits for fields on the domain. 
   See [grails issue #10862](https://github.com/grails/grails-core/issues/10862) 
3. Inserting each record in seperate transaction has a BIG performance penalty and should be avoided when updating or inserting data.
5. use small transaction batches (50-100 items) and keep them the same size as the jdbc.batch_size. DO NOT (auto)commit on every insert
6. JDBC Batch size of 50-100 Gave the best results, as batch size goes higher, performance started to degrade.
7. Disabling validation improves performance only slightly and performs well eg. ```domain.save(false)```
8. Grails Date stamp fields has a negligible effect on performance.
9. AuditTrail stamp affects performance (see below for details)
10. Did not see any noticeable difference if Domain autowiring is enabled or disabled. (Domain with dependency on one service).
11. Dao does not have any major noticable effect on performance.
12. Disabling validation has slight performance benifits but not significant (see below for details)
13. Using custom [idgenerator](https://yakworks.github.io/gorm-tools/id-generation/) improves greatly over auto 
   as it allows hibernate to use the jdbc.batch_size. see links above
14. Using listeners and grails @Listener events slows it down a bit. With out specs about 3 seconds for synchronous single thread processing 100k+ records. 
   which drops to <1 second when doing the same thing asyncronously (gpars parralel batches)
15. From above table, it can be seen that
    * Going from 2 cores to 4 improves numbers significantly  
    * Going from 4 cores to 8 numbers either degrades or improves only slightly on an intel 4 core with hyper-threading simulating 8 cores
    * from pool size 9 onward, performance defintely starts degrading as expected with threads fighting for resources. 


### Optimum setting for Gpars pool size.

It is observed that 5-9 core gave the best results for [i7-4870HQ](https://ark.intel.com/products/83504) which has four(4) physical cores.
Intel hyper-threading simulates to the OS and Java a total of 8 cores and uses Hyper threading.

Gparse will utilize may benefit if it is given 8 cores even if there are just four physical cores and four virtual cores.

The default Gpars pool size is Runtime.getRuntime().availableProcessors() + 1 see ()here](https://github.com/vaclav/GPars/blob/master/src/main/groovy/groovyx/gpars/util/PoolUtils.java#L43)
And this indeed gives better performance.

As per Gpars performance tips [here](http://www.gpars.org/1.0.0/guide/guide/tips.html)
> In many scenarios changing the pool size from the default value may give you performance benefits. Especially if your tasks perform IO operations, like file or database access, networking and such, increasing the number of threads in the pool is likely to help performance.


### Questions answered by above conclusion.

**Fastest way to persist/insert large batches using gorm**
Gpars batch insert without data binding and validation.

**Does binding slow it down, why, can it be optimized, best alternative**
- Yes, databinding has huge overhead on performance
- The overhead is caused by iterating over each property of the domain for every instance that needs to be bind, calling type conversion system
  and other stuff done by GrailsWebDataBinder.
- There is nothing much can be done other then not using databinding  

**Does valiation slow it down, why, can it be optimized**
- Yes, it has slight performance impact
- That is because Grails validation (GrailsDomainClassValidator) has to iterate over each constrained property of domain class
  and invoke validators on it for every instance.

**Does Auditstamp slow it down, can it be optimized**
- Yes
- Thats because audit trail plugin hooks into validation and gets called every time when the domain is validated.
  It does some reflection to check for properties/value and checks in hibernate persistence context if the instance is being inserted or being updated. All this makes it little slower.
- Need to try what can be done in audit trail plugin to improve performance.   

**Do daos slow it down**
- No, very very little effect, some thing around 1 to 1.5 seconds for 115K records.

**Do custom Id generator slow it down, or improves speed**
- The batch id generator provided by Dao plugin actually improves the performance.

**Do using Dataflow queue/operator make it faster**
- No, it has no noticeable effect

**Does @compileStatic speed things up**
- Yes, compile static improves the performance as method calls are not intercepted and does not go through the metaclass.
- Putting CompileStatic on domain class improves the databinding speed.
- It is recommended to use compile static on services, domain classes and other code as far as possible, unless the code need to use dynamic dispatch.

### Batching inserts and updates with hibernate
JDBC API supports batching for DML operations, however by default Hibernate does not use JDBC batching support. Below are the settings which can be used to enable batching of insert/updates with hibernate.  

**hibernate.jdbc.batch_size**  
This is the most important setting which tells hibernate to enable batching of statements and configures the batch size used by hibernate.

**hibernate.order_inserts** and **hibernate.order_updates**  
By default hibernate executes each statement in the same order it appears. However it affects the batching. Batching can target one table at a time.
As soon as a DML statement for a different table is encountered, it ends the current batch. This can result in hibernate not being able to use batching effectively.
The above two settings tells hibernate to order inserts and update statements respectively based on entity type.
 
**hibernate.jdbc.batch_versioned_data**
This setting tells hibernate to enable batching for versioned entities and is required to enable batching of update statements.

**Note** from hibernate
>Some JDBC drivers return incorrect row counts when a batch is executed. If your JDBC driver falls into this category this setting should be set to false. Otherwise, it is safe to enable this which will allow Hibernate to still batch the DML for versioned entities and still use the returned row counts for optimistic lock checks. Since 5.0, it defaults to true. Previously (versions 3.x and 4.x), it used to be false.
 

**Caveats**
- Hibernate disables insert batching at the JDBC level transparently if you use an identity identifier generator.
- Hibernate disables batch insert when using hibernate.cache.use_second_level_cache = true
  
**Reference**
https://stackoverflow.com/questions/12011343/how-do-you-enable-batch-inserts-in-hibernate
https://stackoverflow.com/questions/35791383/spring-data-jpa-batch-insert-for-nested-entities#35794220
https://vladmihalcea.com/2015/03/18/how-to-batch-insert-and-update-statements-with-hibernate/
https://stackoverflow.com/questions/6687422/hibernate-batch-size-confusion
https://docs.jboss.org/hibernate/stable/orm/userguide/html_single/chapters/batch/Batching.html
https://docs.jboss.org/hibernate/orm/3.6/reference/en-US/html/batch.html 


## References and reading
Good Video from 2017 on High Performance Hibernate https://vimeo.com/190275665

Here are a 2 links you should read that will give you some background information on processing large bulk data batches.
read up through 13.2
<http://docs.jboss.org/hibernate/core/3.3/reference/en/html/batch.html>
and read this entire post
<http://naleid.com/blog/2009/10/01/batch-import-performance-with-grails-and-mysql/>

Thomas Lin setup a test for some processing for GPars
<http://fbflex.wordpress.com/2010/06/11/writing-batch-import-scripts-with-grails-gsql-and-gpars/>

and the gpars docs
<http://gpars.org/guide/index.html>

[GPars]: http://gpars.org/guide/index.html
[SimpleJdbc Example]: http://www.brucephillips.name/blog/index.cfm/2010/10/28/Example-Of-Using-Spring-JDBC-Execute-Batch-To-Insert-Multiple-Rows-Into-A-Database-Table
[Zach]:http://grails.1312388.n4.nabble.com/Grails-Hang-with-Bulk-Data-Import-Using-GPars-td3410441.html
[Caching strategy]:http://docs.grails.org/3.1.1/guide/single.html#caching
[EhCache]:http://www.ehcache.org/
[EhCache Configuration]:http://www.ehcache.org/documentation/2.8/configuration/configuration.html
[Hiberante Second Level cache]:https://docs.jboss.org/hibernate/orm/3.3/reference/en/html/performance.html#performance-cache
[Disable cache for a session]:https://forum.hibernate.org/viewtopic.php?f=1&t=964775
