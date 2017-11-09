Gorm-tools adds `search(params, closure)` method to domains. It allows to get list of entities restricted by
properties. Params could be passed as JSON string or Map. All restrictions should be under `criteria` keyword, see example 
bellow.

**Example**

```
Org.dao.search([criteria: [name: "Nam%", type: "New"], max: 20]) {gt "id", 5}
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

So we can specify just parameters in criteria block, and if any specific restriction is needed it can be added
with closure

Bellow will be a list of supported syntax for params in json format, which is supported:

```json
{
    criteria: {
      "ponum":"abc", /* if its a single value eq is default, if it contains % then it uses ilike */
      "reconciled": true, /* boolean */
      "tranDate": "2012-04-23T00:00:00.000Z", /* date */
      "customer.id": 101, 
      "customerId": 101, /*works in the same way as `customer.id":101` */
      "customer":{"id":101}, /* or object way */
      "or":{ /*TODO: works only if it is one in `criteria`, and currently only on first level*/
        "customer.name":["ilike()","wal%"],
        "customer.num":["ilike()","wal%"]
      },
      "docType": ["PA","CM"], /* an array means it will use in/inList */  
      "docType": ["in()",["PA","CM"]], /* the above ins would be a short cut for this*/
      "docType": ["in()", "PA" ,"CM"], /* the above ins would be a short cut for this*/
      "tranType.id": ["not in()",[1,2,3]],/* will translate to "not{ in("tranType.id",[1,2,3])]" */
      "refnum": ["ilike()","123%"], /* a case-insensitive 'like' expression */
      "refnum": ["like()","123%"], /* equivalent to SQL like expression */
      "amount": ["between()",0,100], /* between value */
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
  "order":[{"tranDate":"ASC"},{"customer.name","desc"}]
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