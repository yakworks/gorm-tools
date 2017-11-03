Gorm-tools provides a convenient way for iterating over records which correspond to a given SQL request.
See [ScrollableQuery](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/jdbc/ScrollableQuery.groovy)

### Execute a closure for each record

As you can see in the example below, we can specify the SQL query and provide the closure which is called for each record:

```groovy
    ScrollableQuery scrollableQuery = new ScrollableQuery(new ColumnMapRowMapper(), dataSource, 50)

    scrollableQuery.eachRow("select * from ScrollableQueryTest") { Object row ->
        println row
    }
```

### Execute a closure for each batch of records

Using ```eachBatch``` we can execute a closure for a batch of records.
This closure is called for a specified number of records. For example, code below prints size of each batch
(which is 5) to console:

```groovy

    scrollableQuery.eachBatch("select * from ScrollableQueryTest", 5) { List batch ->
        println "batchSize=${batch.size()}"
    }

```

### Fetching a list of all records:

> NOTE: This method holds all rows in memory, so this should not be used if there is going to be large number of rows.

```groovy

    List values = scrollableQuery.rows("select * from ScrollableQueryTest where value='test'")

```
