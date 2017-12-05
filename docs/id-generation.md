
### Database ID Generator
Dao plugin comes with an implementation of hibernate identity generator for a cross database and NoSQL way to assign Long ids from an in memory incrementor. It uses a central table to track the last used id for each table. This helps a lot when dealing with associations and relationships. It also increases the performance of batch inserts.

### NewObjectId table
The table name is configurable and will get created if it does not exist. You can also create the table as shown below and add indexes if desired.

```sql
create table NewObjectId
(
	KeyName varchar(255) not null primary key,
	NextId bigint not null
)
;

```

Here key name will be the name of the tables and NextId is the next id to return for the given table.

### Configure Identity generator globally.
The identity generator can be configured globally in application.groovy as shown below.


```groovy
grails {

	gorm.default.mapping = {
		id column: 'id', generator:'gorm.tools.idgen.SpringIdGenerator'
	}
}

```

This will use the SpringIdGenerator for all domains in the application. However it can be done per domain too if required.

### Using IdGenerator programmatically
Plugin defines a bean with name idGenerator that can be used to programmatically generate new Ids.
Here is an example domain class.

```groovy
class Book {
    transient idGenerator

    def beforeInsert() {
        if(!id) id = idGenerator.getNextId('Book.id') // or idGenerator.getNextId(this)
    }
}

```

Identity generator will check in NewObjectId table for keyName ```Book``` If it exists, it will return the value of NextId or else it will insert a new row in NewObjectId table.

### How it works

By default the following beans are enabled.

**jdbcIdGenerator**  
A Jdbc implementation of the IdGenerator, it uses NewObjectId central table to query the new ids.

**idGenerator(BatchIdGenerator)**  
Idgenerator implementation that caches a range of values in memory by the key name. It caches a batch of id for each key and increments in memory thus provides better performance.
Internally it uses jdbcIdGenerator to query for next batch of ids.

BatchIdGenerator by default uses allocationSize size of 100. Which can be changed by overriding the spring bean as shown below.

```groovy
 idGenerator(BatchIdGenerator){
    generator = ref("jdbcIdGenerator")
    allocationSize = 50
  }

```

Dao plugin by default configures the BatchIdGenerator as default idgenerator. If you need to use another idgenerator or provide a custom implementation, you can override the **idGenerator** spring bean.

**jdbcTemplate**
Dao plugin also configures JdbcTemplate which can be used for low level jdbc access. It uses TransactionAwareDataSourceProxy so the queries run through the jdbcTemplate will be part of the current transaction.
