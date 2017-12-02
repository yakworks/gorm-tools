
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
TODO

TODO Describe the BatchIdGenerator and how to configure.
