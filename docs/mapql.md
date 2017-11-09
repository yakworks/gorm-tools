## Overview

The gorm dao's come with a `list(criteriaMap, closure)` method. It allows to get list of entities restricted by
the properties in the `criteriaMap`. The map could be passed as JSON string or Map. All restrictions should be under `criteria` keyword by default, see example
bellow.

Anything in the optional closure will be passed into Gorm/Hibernate criteria closure

The query language is largely based on [Mongo's](https://docs.mongodb.com/manual/reference/operator/query/)
with inspiration from [json-sql](https://github.com/2do2go/json-sql/) as well

**Example**

```
Org.dao.list([
  criteria: [name: "Virgin%", type: "New"],
  order: {name:1},
  max: 20
]){
  gt "id", 5
}
```

The same result can be reached with criteria:

```
Criteria criteria = Org.createCriteria()
criteria.list(max: 20) {
    like "name", "Nam%"
    eq "type", "New"
    gt "id", 5
}
```

## Criteria options

### Logical

|  Op  |            Description             |                                  Examples                                  |
| ---- | ---------------------------------- | -------------------------------------------------------------------------- |
| $and | only needed for special conditions | `$and: [ {name: 'John'}, {age: 12} ]` <br> same as `name: 'John', age: 12` |
| $or  | "ors" them all                     | `$or: [ {name: 'John'}, {age: 12} ]` <br> `$or: {name: 'John', age: 12 }`  |
| $not | ALL not equal, !=, <>              | `$not:{ name: 'John', age: 12`}                                            |
| $nor | ANY not equal                      | `$nor:{ name: 'John', age: 12`}                                            |

## Comparison

|     Op     |                       Description                       |                    Example                     |
| ---------- | ------------------------------------------------------- | ---------------------------------------------- |
| $gt        | >                                                       | `"salary": {"$gt": 10000}`                     |
| $gte       | >=                                                      | `"salary": {"$gte": 10000}`                    |
| $lt        | <                                                       | `"salary": {"$lt": 10000}`                     |
| $lte       | <=                                                      | `"salary": {"$lte": 10000}`                    |
| $between   | Where the property value is between two distinct values | `"age": {"$between": [18, 35]}`                |
| $like      | Equivalent to SQL like expression                       | `"name": {"$like": "Joh%"}`                    |
| $ilike     | A case-insensitive 'like' expression, auto appends `%`  | `"name": {"$ilike": "Joh"}`                    |
| $ne        | not equal, !=, <>                                       | `"age" : {"$ne" : 12}}`                        |
| $in        | Match any value in array                                | `{"field" : {"$in" : [value1, value2, ...]}`   |
| $nin       | Not match any value in array                            | `{"field" : {"$nin" : [value1, value2, ...]}}` |
| $isNull    |                                                         |                                                |
| $isNotNull |                                                         |                                                |

Bellow will be a list of supported syntax for params in json format, which is supported:

something like this should work too when combining

```
amount: {
	$gt: 5.0,
	$lt: 15.50,
	$ne: 9.99
}
```

```json
{
    "criteria": {
      "ponum":"abc", /* if its a single value eq is default, if it contains % then it uses ilike */
      "reconciled": true, /* boolean */
      "tranDate": "2012-04-23T00:00:00.000Z", /* date */
      "customer.id": 101,
      "customerId": 101, /*works in the same way as `customer.id:101` */
      "customer":{"id":101}, /* object way */
      "$or":{ /*TODO: works only if it is one in `criteria`, and currently only on first level*/
        "customer.name":["$ilike": "wal"],
        "customer.num":["$like": "wal%"]
      },
     "$and": [{ //multiple ors would need to look like this in here. I think we need to look here
                //https://github.com/2do2go/json-sql/blob/master/tests/1_select.js#L1228 and see the conditions they use
						$or: {
							name: 'John',
							age: 12
						}
					}, {
						$or: {
							name: 'Mark',
							age: 14
						}
					}]
      "docType": ["PA","CM"], /* an array means it will use in/inList */  
      "docType": ["$in": ["PA","CM"]], /* the above ins would be a short cut for this*/
      //deprecate "docType": ["$in""PA" ,"CM"], /* the above ins would be a short cut for this*/
      "tranType.id": ["$nin":[1,2,3]],/* will translate to "not{ in("tranType.id",[1,2,3])]" */
      "tranType.id": ["$not in":[1,2,3]],/* will translate to "not{ in("tranType.id",[1,2,3])]" */
      "refnum": ["$ilike": "123"], /* a case-insensitive 'like' expression append the % */
      "refnum": ["$like": "123%"], /* equivalent to SQL like expression */
      "amount": ["$between":[0,100]], /* between value */
      "oldAmount.gt()": "origAmount"/* greater than value, the same as bellow*/
      "oldAmount": ["gt()","origAmount"], /* greater than value */
      "oldAmount": ["ge()","origAmount"], /* greater or equal than value */"
      oldAmount": ["lt()","origAmount"], /* less than value */
      "oldAmount": ["le()","origAmount"], /* less or equal than value */
      "amount": ["ne()",50], /*not equal*/
      "status.id": [1,2,3], /* an array means it will use in/inList */
      "status": [{"id":1},{"id":2},{"id":3}], /* an array means it will use in/inList */
      "status": ["isNull()"], /* translates to isNull*/
    },
  "sort":[{"tranDate":"ASC"},{"customer.name","desc"}]
}
```
**Quick Search**

Quick search - ability to search by one string in criteria filters against several domain fields, the value for quick
search can be passed in `quickSearch` or `q` keywords. The list of fields should be specified in static property `quickSearchFields`, see bellow:

```groovy
class Org {
	String name
    Address address

    static quickSearchFields = ["name", "address.city"]
    ...

```
So intelligent search will add `%` automatically for cases when searchable property is String, if quick search string doesn't have it and will apply `ilike` statement
for each field in `quickSearchFields`.

```groovy
Org.dao.search([criteria: [quickSearch: "abc"], max: 20])

```

```groovy
Criteria criteria = Org.createCriteria()
criteria.list(max: 20) {
    or {
        ilike "name", "abc%"
        ilike "address.city", "abc%"
    }
}
```
Keep in mind that quickSearch has higher priority then regular search fields, and if params are
`[criteria: [quickSearch: "abc", id: 5]]`, then `id` restriction will be ignored, but criteria that is passed to search
as closure will be executed in any case

**Count totals**
TODO: add count totals that should work in the same way as search
