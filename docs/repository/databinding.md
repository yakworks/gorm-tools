
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
How associations are handled depends on if the associated domain belongs to the domain which is being bound.
If the domain being bound is owning side of association and value is of type map, a new instance of associated domain is created.
If the association does not use belongsTo then existing instance is loaded if the map contains the id.
The databinding on associated domain will be performed only if it belongs to the domain which is being bound

   
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
It will create new book and set the author and category to existing records with provided id.


```groovy

Book.create([name:"Grails In Action", author:[id:1], category:[id:1]]) 

```

However a new book can be created when creating a new author, because book belongs to Author.

So following will create a new book instance and set author.book to this new instance.

```groovy

Author.create(name:"test", book:[name:"Grails in action"])

```

[MapBinder]: https://yakworks.github.io/gorm-tools/api/gorm/tools/databinding/MapBinder.html
[EntityMapBinder]: https://yakworks.github.io/gorm-tools/api/gorm/tools/databinding/EntityMapBinder.html
