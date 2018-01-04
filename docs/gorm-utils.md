Gorm-tools provides two utils to work with domains.
See [GormUtils]{.new-tab}
and [GormMetaUtils]{.new-tab}

## GormUtils

### Coping properties from source to target domain instance

Using ```copyProperties``` we can copy some of properties from one domain to another:

```groovy
    User user = new User(firstName: 'John', lastName: 'Doe' age: 30)
    User user2 = new User()
    User user3 = new User()

    GormUtils.copyProperties(user, user2, false, 'firstName')
    GormUtils.copyProperties(user, user3, false, 'firstName', 'age')

    assert user2.firstName == user.firstName
    assert user2.age != user.age

    assert user3.firstName == user.firstName
    assert user3.age == user.age
```
In this example ```user``` is the source object, ```user2``` and ```user3``` are targets.
If the 3rd argument is false the method will override a target value even if it's not null.
Then we can specify properties to copy from the source object.

### Fetching a nested property using a string name

GormUtils allows us to get a nested property from an object using a string as a path:

```groovy
    Address address = new Address(street: 'street', city: 'city')
    User user = new User(firstName: 'John', lastName: 'Doe' age: 30, address: address)

    String street = GormUtils.getPropertyValue(person, 'address.street')

    assert street == address.street
```
As you can see we can specify nested properties simply by adding a ( ``` . ``` ) symbol to a parent's property name.

### Copying a domain instance

```groovy
    User user = new User(firstName: 'John', lastName: 'Doe' age: 30)

    User copy = GormUtils.copyDomain(User, user)
    assert copy.firstName == 'John'
    assert copy.lastName == 'Doe'
    assert copy.age == 30
```
or with existing object

```groovy
    User copy = GormUtils.copyDomain(new User(), user)
    assert copy.firstName == 'John'
    assert copy.lastName == 'Doe'
    assert copy.age == 30
```

## GormMetaUtils

### Getting a domain class

```groovy
    // using a fully qualified name with package
    GormMetaUtils.getDomainClass("gorm.tools.Org")

    // using a domain instance
    GormMetaUtils.getDomainClass(new Org())

    // using a class
    GormMetaUtils.getDomainClass(Org)

```

### Getting a persistent entity

```groovy
    // using a fully qualified name with package
    GormMetaUtils.getPersistentEntity("gorm.tools.Org")

    // using a domain instance
    GormMetaUtils.getPersistentEntity(new Org())

    // using a class
    GormMetaUtils.getPersistentEntity(Org)
```

[GormUtils]:https://yakworks.github.io/gorm-tools/api/gorm/tools/GormUtils.html
[GormMetaUtils]:https://yakworks.github.io/gorm-tools/api/gorm/tools/GormMetaUtils.html
