Gorm-tools comes with BeanPathTools util which provides a convenient way for manipulating with object's properties.
See [BeanPathTools](https://github.com/yakworks/gorm-tools/blob/master/plugin/src/main/groovy/gorm/tools/beans/BeanPathTools.groovy)

### Converting object's fields into a map

In order to retrieve properties from an object and place them to a map we can use **buildMapFromPaths**
as shown in the example below:

```groovy
User user = new User(firstName: 'John', lastName: 'Doe' age: 30)

Map userName = BeanPathTools.buildMapFromPaths(user, ['firstName', 'lastName'])
assert userName == [firstName: 'John', lastName: 'Doe']
```

It is possible to specify fields which should be added to the resulting map
by providing a list with property names as the second argument.

The '*' sign represents all properties. Thus in order to get all fields we can use it as shown below:

```groovy
Map userMap = BeanPathTools.buildMapFromPaths(user, ['*'])
assert userMap == [firstName: 'John', lastName: 'Doe' age: 30]
```

It is also works well with nested properties:

```groovy
Address address = new Address(street: 'street', city: 'city')
User user = new User(firstName: 'John', lastName: 'Doe' age: 30, address: address)

Map userMap = BeanPathTools.buildMapFromPaths(user, ['*'])
assert userMap == [firstName: 'John', lastName: 'Doe' age: 30, address: [street: 'street', city: 'city1']]
```