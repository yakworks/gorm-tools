
## Data binding using MapBinder
Plugin comes with a [MapBinder][]{.new-tab} Which is used by a Repository to perform databinding.
Plugin configures [EntityMapBinder][]{.new-tab} as default implementation of [MapBinder][]{.new-tab}. [EntityMapBinder][]{.new-tab} is similar 
to grails data binder in the sense that it uses registered value converters and fallbacks to spring ConversionService.
However entityMapBinder is optimized to convert most commonly encountered property types such as Numbers and Dates 
without going through the converters, thus resulting in faster performance.

**Example**

```groovy
class SomeService {
    @Autowired
    EntityMapBinder binder

    void foo(Map params) {
        Org org = new Org()
        binder.bind(org, params)
    }
}

```

**Using custom MapBinder**  
By default all Repositories use the default [EntityMapBinder][]{.new-tab} for databinding. However when a Repository is explicitly 
created for a domain class, and if required, a custom MapBinder implementation can be used to perform databinding as per the need.

```groovy

class CustomMapBinder implements MapBinder {

    public <T> GormEntity<T> bind(Map args, Object target, Map<String, Object> source, BindAction bindAction) {
        //implement  
    }

    public <T> GormEntity<T> bind(Object target, Map<String, Object> source, BindAction bindAction) {
        //implement
    }

}
```
then register the bean 

```java
beans = {
    customMapBinder(CustomMapBinder) 
}
```

```groovy
class OrgRepo implements GormRepo<Org> {
    
    @Autowired
    @Qualifier("customMapBinder")
    CustomMapBinder mapBinder
    
    .........   
}

```

This will make the OrgRepo use CustomMapBinder for data binding.


## Binding Associations
Gorm tools MapBinder handles associations little differently than Grails data binder for performance reasons.
Associations are handled differently for create vs update.  

- For create, if the association belongs to the domain which is being bound, new instance will be created. Else if the map contains id, reference to existing instance will be set.
- Update never creates a new instance, if the map contains id reference to existing instance will be set, it will do deep databinding on associated object only if the association belongs to the object which is being bound. 
   
Example

```groovy

class Author {
    String name
}

class Category {
  String name
}

class Book {
    String name
    Category category
    
    static belongsTo = [author:Author]
}

```

Given the above domain model, when creating a new book. It will not create new instance of Author or category, but will set reference to existing instance if id is provided in parameter.

```groovy

Book.create([name:"Grails In Action", author:[id:1], category:[id:1]]) 

```

It will create new book and set the author and category to existing records with provided id.

However a new book can be created when creating a new author, because book belongs to Author.

So following will create a new book

```groovy

Author.create(name:"test", book:[name:"Grails in action"])

```

When updating an instance, the association will be updated only if it belongsTo the domain which is being updated.

```groovy

Author.update(id:1, book:[id:1, name: "updated"])

```

Above will update the author, set book with id 1 and update book.name to 'updated'

When updating book

```groovy
Book.update(id:1, author:[id:2, name:"updated"])
```

The above snippet will update the book, set its author to instance with id 2, but will not update the author.name. Because author does not belong to book.

[MapBinder]: https://yakworks.github.io/gorm-tools/api/gorm/tools/databinding/MapBinder.html
[EntityMapBinder]: https://yakworks.github.io/gorm-tools/api/gorm/tools/databinding/EntityMapBinder.html
