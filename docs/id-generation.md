
### Database ID Generator
Dao plugin comes with an implementation of hibernate identity generator which uses a central table for incremental identity generation.

### NewObjectId table
In order to use identity generator provided by plugin, you need to create NewObjectId table as shown below.

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
        if(!id) id = idGenerator.getNextId('Book.id')
    }
}

```

Identity generator will check in NewObjectId table for keyName ```Book``` If it exists, it will return the value of NextId or else it will insert a new row in NewObjectId table.