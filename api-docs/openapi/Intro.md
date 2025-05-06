
The 9ci API specification for interoperable cloud-based processing of Accounts Receivable datasets.

# Intro

Welcome to the 9ci RESTful API reference documentation.

Representational State Transfer (REST) APIs are service endpoints that support sets of HTTP operations (methods), which provide create, retrieve, update, or delete access to 9ci's application resources. It returns HTTP response codes to indicate errors. It also accepts and returns JSON in the HTTP body. You can use your preferred
HTTP/REST library for your programming language. This document contains the most commonly
integrated resources.

The api.yaml file is available [here](api.yaml).

# API Principles

## Language

In the specification the key words “MUST”, “MUST NOT”, “REQUIRED”, “SHALL”, “SHALL NOT”, “SHOULD”, “SHOULD NOT”, “RECOMMENDED”, “MAY”, and “OPTIONAL” in this document are to be interpreted as described in [RFC 2119](http://tools.ietf.org/html/rfc2119) and [RFC 8174](http://tools.ietf.org/html/rfc8174).

## Casing

Unless otherwise stated the API is **case sensitive**.

All names SHOULD be written in camel case, i.e. words are separated by a capital letter and no spaces, with all endpoints and property names starting with a lower case. Example: `helloWorld`. All names MAY also be written in snake_case or kebab-case, i.e. words are separated with one underscore character (_) or one dash character (-) and no spaces, with all letters lower-cased BUT they should not be combined, the follwing would be invalid `foo_bar-baz`. Example: `hello_world`. HTTP header fields follow their respective casing conventions, e.g. `Content-Type` or `9ci-Costs`, despite being case-insensitive according to [RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.2).

## HTTP REST

This uses [HTTP REST](https://en.wikipedia.org/wiki/Representational_state_transfer) [Level 2](https://martinfowler.com/articles/richardsonMaturityModel.html#level2) for communication between client and back-end server.

Public APIs are available via HTTPS only. 

Endpoints are made use meaningful HTTP verbs (e.g. GET, POST, PUT, PATCH, DELETE) whenever technically possible. If there is a need to transfer big chunks of data for a GET requests to the back-end, POST requests MAY be used as a replacement as they support to send data via request body. 

NOTE: Unless otherwise stated, PATCH requests are only defined to work on direct (first-level) children of the full JSON object. Therefore, changing a property on a deeper level of the full JSON object always requires to send the whole JSON object defined by the first-level property.

Naming of endpoints follow the REST principles. Therefore, endpoints are centered around a resource. Resource identifiers SHALL be named with a noun in singular form except for plural actions that can not be modelled with the regular HTTP verbs. Single actions MUST be single endpoints with a single HTTP verb (POST is RECOMMENDED) and no other endpoints beneath it.

## JSON

The API uses JSON for request and response bodies whenever feasible. Services use JSON as the default encoding. Other encodings can be requested using [Content Negotiation](https://www.w3.org/Protocols/rfc2616/rfc2616-sec12.html). Clients and servers MUST NOT rely on the order in which properties appears in JSON. Collections usually don't include nested JSON objects if those information can be requested from the individual resources.

### Temporal date-time data

Date, time, intervals and durations are formatted based on ISO 8601 or its profile [RFC 3339](https://www.ietf.org/rfc/rfc3339) whenever there is an appropriate encoding available in the standard. All temporal data are specified based on the Gregorian calendar.

# Status codes

The 9ci API usually uses the following HTTP status codes for successful requests: 

- **200 OK**:
  Indicates a successful request **with** a response body being sent.
- **201 Created**
  Indicates a successful request that successfully created a new resource. Sends a `Location` header to the newly created resource **without** a response body.
- **202 Accepted**
  Indicates a successful request that successfully queued the creation of a new resource, but it has not been created yet. The response is sent **without** a response body.
- **204 No Content**:
  Indicates a successful request **without** a response body being sent.

The 9ci API has some commonly used HTTP status codes for failed requests: 

- **400 Bad Request**:
  The back-end responds with this error code whenever the error has its origin on client side and no other HTTP status code in the 400 range is suitable.

- **401 Unauthorized**:
  The client did not provide any authentication details for a resource requiring authentication or the provided authentication details are not correct.

- **403 Forbidden**:
  The client did provided correct authentication details, but the privileges/permissions of the provided credentials do not allow to request the resource.

- **404 Not Found**:
  The resource specified by the path does not exist, i.e. one of the resources belonging to the specified identifiers are not available at the back-end.
  *Note:* Unsupported endpoints MUST use HTTP status code 501.

- **409 CONFLICT**:
  OptimisticLockingFailureException, version did not match for an edited item and someone or something else 
  changed the data since it was last retrieved

- **422 UNPROCESSABLE_ENTITY**:
  The server had validation errors on a post or put

- **500 Internal Server Error**:
  The error has its origin on server side and no other status code in the 500 range is suitable.

## Errors

9ci follows and extends Problem Details for HTTP APIs [RFC 7807](https://tools.ietf.org/html/rfc7807). It defines a Problem JSON object using the media type
`application/problem+json` to provide an extensible human and machine readable
failure information beyond the HTTP response status code to transports the
failure kind (`type` / `title`) and the failure cause and location (`instant` /
`detail`). To make best use of this additional failure information, every
endpoint must be capable of returning a Problem JSON on client usage errors
({4xx} status codes) as well as server side processing errors ({5xx} status
codes).

*Hint:* The media type `application/problem+json` is often not implemented as
a subset of `application/json` by libraries and services! Thus clients need to
include `application/problem+json` in the {Accept}-Header to trigger delivery
of the extended failure information.

The OpenAPI schema definition of the Problem JSON object can be found
https://opensource.zalando.com/restful-api-guidelines/models/problem-1.0.1.yaml[on
GitHub].

### Error Handling

The success of requests will be indicated using [HTTP status codes](https://tools.ietf.org/html/rfc7231#section-6) according to [RFC 7231](https://tools.ietf.org/html/rfc7231).

If the API responds with a status code between 100 and 399 the back-end indicates that the request has been handled successfully.

If a HTTP status code in the 400 range is returned, the client SHOULD NOT repeat the request without modifications. For HTTP status code in the 500 range, the client MAY repeat the same request later.

All HTTP status codes defined in RFC 7231 in the 400 and 500 ranges can be used as 9ci error code in addition to the most used status codes mentioned here. Responding with 9ci error codes 400 and 500 SHOULD be avoided in favor of any more specific standardized or proprietary 9ci error code.

In general an error is communicated with a status code between 400 and 599. Client errors are defined as a client passing invalid data to the service and the service *correctly* rejecting that data. Examples include invalid credentials, incorrect parameters, unknown versions, or similar. These are generally "4xx" HTTP error codes and are the result of a client passing incorrect or invalid data. 

Server errors are defined as the server failing to correctly return in response to a valid client request. These are generally "5xx" HTTP error codes. 

###  Problem JSON object

A JSON Problem object will be sent with all responses that have a status code between 400 and 599.

```
   HTTP/1.1 422 UNPROCESSABLE_ENTITY
   Content-Type: application/problem+json
   Content-Language: en

{
  "status": 422,
  "code": "arTran.post.error",
  "title": "ArTran Validation Error",
  "detail": "Lines dont balance and must sum to ArTran.origAmount",
  "fieldErrors": [...]
}
```

``` json
{
  "id": "123",
  "code": "SampleError",
  "message": "A sample error message.",
  "url": "https://example.9ci.org/docs/errors/SampleError"
}
```

# CORS

> Cross-origin resource sharing (CORS) is a mechanism that allows restricted resources [...] on a web page to be requested from another domain outside the domain from which the first resource was served. [...]

> CORS defines a way in which a browser and server can interact to determine whether or not it is safe to allow the cross-origin request. It allows for more freedom and functionality than purely same-origin requests, but is more secure than simply allowing all cross-origin requests.

Source: [https://en.wikipedia.org/wiki/Cross-origin_resource_sharing](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing)

9ci-based back-ends are usually hosted on a different domain / host than the client that is requesting data from the back-end. Therefore most requests to the back-end are blocked by all modern browsers. This leads to the problem that the JavaScript library and any browser-based application can't access back-ends. Therefore, all back-end providers SHOULD support CORS to enable browser-based applications to access back-ends. [CORS is a recommendation of the W3C organization](https://www.w3.org/TR/cors/). The following chapters will explain how back-end providers can implement CORS support.

> **Tip**: Most servers can send the required headers and the responses to the OPTIONS requests automatically for all endpoints. Otherwise you may also use a proxy server to add the headers and OPTIONS responses.

# API Queries

## URL Parameters overview

| Param       | Description                                                                     | Examples                                                                |
|-------------|---------------------------------------------------------------------------------|-------------------------------------------------------------------------|
| q           | Fuzzy search when alone and string                                              | `https://rcm-api.9ci.io/api/ar/customer?q=walm`                         |
|             | Mango query                                                                     | `https://rcm-api.9ci.io/api/ar/customer?q={name:"walm*"}`               |
| qSearch     | Fuzzy search when also specifying a q object                                    | `https://rcm-api.9ci.io/api/ar/customer?qSearch=walm&q={type:"PARENT"}` |
| max         | max item in data                                                                | `..customer?q=wal&max=100`                                              |
| sort        | sorts fields                                                                    | `..customer?sort=name` or `..customer?sort={type:"asc",name:"desc}`     |
| page        | the page number to return, when max is small enough to create mutiple pages     | `..customer?page=2`                                                     |
| format      | csv it the currently supported format, more may be added in future              | `..customer?format=csv`                                                 |
| totals      | returns sum totals for the resource in addition to , varies depending on entity | `..customer?totals=true`                                                |
| projections | the aggregates (sum, count, min, max) and groupbys for projection queries       | `..customer?projections=[type, {org.calc.totalDue:sum}]`                |

## Mango Q Query Language

9ci has an easy and dynamic way to query via a rest api using a simple map object.
It allows to get paged collection of entities restricted by
the properties in the `q` param. The object passed to q should be passed as JSON. 

### Logical

|  Op  |      Description      |                                              Examples                                              |
| ---- | --------------------- | -------------------------------------------------------------------------------------------------- |
| $and | default               | `$and: [ {"name": "Belak"}, {"status": "D"} ]` <br> equivalent to `"name": "Belak", "status": "D"` |
| $or  | "ors" them all        | `$or: [ {"name": "Belak"}, {"fork": true} ]` <br> `$or: {"name": "Belak", "fork": true }`          |
| $not | ALL not equal, !=, <> | `$not:{ "status": "Destroyed", "dateStatus": "2371" }`                                             |
| $nor | ANY one is not equal  | `$nor:{ "name": "Romulan", "fork": 12`}                                                               |

### Comparison

|     Op     |           Description            |                   Example                    |
| ---------- | -------------------------------- | -------------------------------------------- |
| $gt        | >  greater than                  | `"cargo": {"$gt": 10000}`                    |
| $gte       | >= greater than or equal         | `"cargo": {"$gte": 10000}`                   |
| $lt        | <  less than                     | `"cargo": {"$lt": 10000}`                    |
| $lte       | <= less than or equal            | `"cargo": {"$lte": 10000}`                   |
| $between   | between two distinct values      | `"dateStatus": {"$between": [2300, 2400]}`   |
| $like      | like expression                  | `"name": {"$like": "Rom%"}`                  |
| $ilike     | like auto-append %               | `"name": {"$ilike": "rom"}`                  |
| $eq        | = equal, concieince for builders | `"salary": {"$eq": 10}` \| `"salary": 10`    |
| $ne        | not equal, !=, <>                | `"age" : {"$ne" : 12}}`                      |
| $in        | Match any value in array         | `"field" : {"$in" : [value1, value2, ...]`   |
| $nin       | Not match any value in array     | `"field" : {"$nin" : [value1, value2, ...]}` |
| $isNull    | Value is null                    | `"name": "$isNull" \|  `"name": null         |
| $isNotNull | Value is not null                | `"name": "$isNotNull" \| `"name":{$ne: null} |

### Fields

|  Op   |    Description    |                Example                 |
| ----- | ----------------- | -------------------------------------- |
| $gtf  | >  another field  | `"cargo": {"$gtf": "maxCargo"}`        |
| $gtef | >= field          | `"cargo": {"$gtef": "maxCargo"}`       |
| $ltf  | <  field          | `"cargo": {"$ltf": "maxCargo"}`        |
| $ltef | <= field          | `"cargo": {"$ltef": "maxCargo"}`       |
| $eqf  | = field           | `"cargo": {"$eqf": "controlTotal"}`    |
| $nef  | not equal, !=, <> | `"cargo" : {"$nef" : "controlTotal"}}` |

## Examples

Below will be a list of supported syntax for parameters in json format, which is supported:
Assume we are running these on Contacts.

``` js
{
  "firstName": "Kar%", /* if it ends with % then it will use an ilike */
  "lastName": "Johnson", //no % so its straight up
  "num": {"$ilike": "a1497%"}, /* a case-insensitive 'like' expression appends the % */
  "isPrimary": true, /* boolean */
  "createdDate": "2019-05-16", // dates
  "birthday": "1957-07-26" // dates
  "jobTitle": {"$eqf": "$comments"} //equals another field in set
  "$sort":"name" // asc by default
}
```

For those familiar with sql, this would produce something akin to

```sql
  .. firstName ilike "Kar%" 
  AND lastName="Johnson" 
  AND num ilike "a1497%" 
  AND isPrimary = true
  AND createdDate = '2019-05-16'
  AND birthday = '1957-07-26'
  AND jobTitle = comments
  ORDER BY name ASC;
```

# Patching Association Collections

When working with collection assoctiaions, such as Tags, there is some special logic using the `op` field.
valid values for op are `update`, `remove`, `replace` and `add`

On a put or post `replace` is the default

```js
# On a put or post `replace` is the default
# thie example would add or replace the array
{
  "id": 123,
  tags: [
    {"id": 1}, {"id": 2}
  ]
}
```

```js
# op:update with empty array to remove all
{
  "id": 123,
  tags: {
    "op": "update", data: []
  }
}
```

```js
# op:remove one or more existing
{
  "id": 123,
  "tags": {
    {"op":"remove": "ids": [1,2]}
  }
}
```

```js
# op:update will append to existing. 
# this example will add tag.id=3 to the 
# list of existing tags

{
  "id": 123,
  "tags": {
    "op": "update", data: [
      {"id": 3}
    ]
  }
}
```

# Authentication

9ci supports Bearer based token authentication.

Using `curl`
```bash
# using curl
curl -H 'Content-Type: application/json' -X POST \
		-d '{\"username\":\"someUser\",\"password\":\"secret_knock\"}' \
		https://rcm-api.9ci.io/api/login

# using httpie
http -b POST admin:123@localhost:8080/api/oauth/token
#would return something like
{
    "username": "someUser",
    "roles": [
        "Administrator"
    ],
    "token_type": "Bearer",
    "access_token": "4g2134lkjlkj1324....."
}

# copy the access_token token into future requests
curl -G -H 'Authorization: Bearer 4g2134lkjlkj1324.....' https://rcm-api.9ci.io/api/ar/customer/1

```
