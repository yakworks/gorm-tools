Gorm-tools comes with BeanPathTools util which provides a convenient way for manipulating with object's properties.
See [BeanPathTools]{.new-tab}

### Getting specified object's properties to a map

In order to retrieve properties from an object and place them to a map we can use ```buildMapFromPaths```
We can possible to specify fields which should be added to the resulting map
by providing a list with property names as the second argument.

```groovy

    User user = new User(firstName: 'John', lastName: 'Doe' age: 30)

    Map userName = BeanPathTools.buildMapFromPaths(user, ['firstName', 'lastName'])
    assert userName == [firstName: 'John', lastName: 'Doe']

```

### Getting nested properties:

It is also works well with nested properties. To get a nested property we need to specify
a full path including parent field names divided with the ( ``` . ``` ) sign. See example below:

```groovy

    Address address = new Address(street1: 'street1', street2: 'street2' zip: '123456', city: 'city')
    User user = new User(firstName: 'John', lastName: 'Doe' age: 30, address: address)

    Map street = BeanPathTools.buildMapFromPaths(user, ['address.street1', 'address.street2'])
    assert street == [address: [street1: 'street1', street2: 'street2']]

    Map fullAddress = BeanPathTools.buildMapFromPaths(user, ['address.*'])
    assert fullAddress == [address: [street1: 'street1', street2: 'street2' zip: '123456', city: 'city']]

```

### Getting all object's properties to a map

> :memo: in case of a domain instance the method looks only for persistent properties

The ( ```*``` )  sign represents all properties. Thus in order to get all fields we can use it as shown below:

```groovy

    Map userMap = BeanPathTools.buildMapFromPaths(user, ['*'])
    assert userMap == [firstName: 'John', lastName: 'Doe' age: 30]

```



[BeanPathTools]:https://yakworks.github.io/gorm-tools/api/gorm/tools/beans/BeanPathTools.html
