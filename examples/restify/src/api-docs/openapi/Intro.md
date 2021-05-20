
The 9ci API specification for interoperable cloud-based processing of Accounts Receivable datasets.

# API Principles

## Language

In the specification the key words “MUST”, “MUST NOT”, “REQUIRED”, “SHALL”, “SHALL NOT”, “SHOULD”, “SHOULD NOT”, “RECOMMENDED”, “MAY”, and “OPTIONAL” in this document are to be interpreted as described in [RFC 2119](http://tools.ietf.org/html/rfc2119) and [RFC 8174](http://tools.ietf.org/html/rfc8174).

## Casing

Unless otherwise stated the API works **case sensitive**.

All names SHOULD be written in camel case, i.e. words are separated by a capital letter and no spaces, with all endpoints and property names starting with a lower case. Example: `helloWorld`. All names MAY be written in snake case, i.e. words are separated with one underscore character (_) and no spaces, with all letters lower-cased. Example: `hello_world`. HTTP header fields follow their respective casing conventions, e.g. `Content-Type` or `9ci-Costs`, despite being case-insensitive according to [RFC 7230](https://tools.ietf.org/html/rfc7230#section-3.2).

## HTTP / REST

This uses [HTTP REST](https://en.wikipedia.org/wiki/Representational_state_transfer) [Level 2](https://martinfowler.com/articles/richardsonMaturityModel.html#level2) for communication between client and back-end server.

Public APIs MUST be available via HTTPS only. 

Endpoints are made use meaningful HTTP verbs (e.g. GET, POST, PUT, PATCH, DELETE) whenever technically possible. If there is a need to transfer big chunks of data for a GET requests to the back-end, POST requests MAY be used as a replacement as they support to send data via request body. 

NOTE: Unless otherwise stated, PATCH requests are only defined to work on direct (first-level) children of the full JSON object. Therefore, changing a property on a deeper level of the full JSON object always requires to send the whole JSON object defined by the first-level property.

Naming of endpoints follow the REST principles. Therefore, endpoints are centered around a resource. Resource identifiers SHALL be named with a noun in singular form except for plural actions that can not be modelled with the regular HTTP verbs. Single actions MUST be single endpoints with a single HTTP verb (POST is RECOMMENDED) and no other endpoints beneath it.

## JSON

The API uses JSON for request and response bodies whenever feasible. Services use JSON as the default encoding. Other encodings can be requested using [Content Negotiation](https://www.w3.org/Protocols/rfc2616/rfc2616-sec12.html). Clients and servers MUST NOT rely on the order in which properties appears in JSON. Collections usually don't include nested JSON objects if those information can be requested from the individual resources.

## Web Linking

The API is designed in a way that to most entities (e.g. collections and processes) a set of links can be added. These can be alternate representations, e.g. data discovery via OGC WCS or OGC CSW, references to a license, references to actual raw data for downloading, detailed information about pre-processing and more. Clients should allow users to follow the links.

Whenever links are utilized in the API, the description explains which relation (`rel` property) types are commonly used.
A [list of standardized link relations types is provided by IANA](https://www.iana.org/assignments/link-relations/link-relations.xhtml) and the API tries to align whenever feasible.

Some very common relation types - usually not mentioned explicitly in the description of `links` fields - are:

1. `self`: which allows link to the location that the resource can be (permanently) found online.This is particularly useful when the data is data is made available offline, so that the downstream user knows where the data has come from.

2. `alternate`: An alternative representation of the resource, may it be another metadata standard the data is available in or simply a human-readable version in HTML or PDF.

3. `about`: A resource that is related or further explains the resource, e.g. a user guide.

## Error Handling

The success of requests MUST be indicated using [HTTP status codes](https://tools.ietf.org/html/rfc7231#section-6) according to [RFC 7231](https://tools.ietf.org/html/rfc7231).

If the API responds with a status code between 100 and 399 the back-end indicates that the request has been handled successfully.

In general an error is communicated with a status code between 400 and 599. Client errors are defined as a client passing invalid data to the service and the service *correctly* rejecting that data. Examples include invalid credentials, incorrect parameters, unknown versions, or similar. These are generally "4xx" HTTP error codes and are the result of a client passing incorrect or invalid data. Client errors do *not* contribute to overall API availability. 

Server errors are defined as the server failing to correctly return in response to a valid client request. These are generally "5xx" HTTP error codes. Server errors *do* contribute to the overall API availability. Calls that fail due to rate limiting or quota failures MUST NOT count as server errors. 

### JSON error object

A JSON error object SHOULD be sent with all responses that have a status code between 400 and 599.

``` json
{
  "id": "123",
  "code": "SampleError",
  "message": "A sample error message.",
  "url": "https://example.9ci.org/docs/errors/SampleError"
}
```

Sending `code` and `message` is REQUIRED. 

* A back-end MAY add a free-form `id` (unique identifier) to the error response to be able to log and track errors with further non-disclosable details.
* The `code` is either one of the [standardized textual 9ci error codes](errors.json) or a proprietary error code.
* The `message` explains the reason the server is rejecting the request. For "4xx" error codes the message explains how the client needs to modify the request.

  By default the message MUST be sent in English language. Content Negotiation is used to localize the error messages: If an `Accept-Language` header is sent by the client and a translation is available, the message should be translated accordingly and the `Content-Language` header must be present in the response. See "[How to localize your API](http://apiux.com/2013/04/25/how-to-localize-your-api/)" for more information.
* `url` is an OPTIONAL attribute and contains a link to a resource that is explaining the error and potential solutions in-depth.

### Standardized status codes

The 9ci API usually uses the following HTTP status codes for successful requests: 

#### Unauthorized
<RedocResponse pointer={"#/components/responses/Unauthorized"} />

<PullRight>
This part will appear in the right pane.
</PullRight>

<PullRight>

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

</PullRight>


If a HTTP status code in the 400 range is returned, the client SHOULD NOT repeat the request without modifications. For HTTP status code in the 500 range, the client MAY repeat the same request later.

All HTTP status codes defined in RFC 7231 in the 400 and 500 ranges can be used as 9ci error code in addition to the most used status codes mentioned here. Responding with 9ci error codes 400 and 500 SHOULD be avoided in favor of any more specific standardized or proprietary 9ci error code.

## Temporal date/time data

Date, time, intervals and durations are formatted based on ISO 8601 or its profile [RFC 3339](https://www.ietf.org/rfc/rfc3339) whenever there is an appropriate encoding available in the standard. All temporal data are specified based on the Gregorian calendar.

## CORS

> Cross-origin resource sharing (CORS) is a mechanism that allows restricted resources [...] on a web page to be requested from another domain outside the domain from which the first resource was served. [...]
> CORS defines a way in which a browser and server can interact to determine whether or not it is safe to allow the cross-origin request. It allows for more freedom and functionality than purely same-origin requests, but is more secure than simply allowing all cross-origin requests.

Source: [https://en.wikipedia.org/wiki/Cross-origin_resource_sharing](https://en.wikipedia.org/wiki/Cross-origin_resource_sharing)

9ci-based back-ends are usually hosted on a different domain / host than the client that is requesting data from the back-end. Therefore most requests to the back-end are blocked by all modern browsers. This leads to the problem that the JavaScript library and any browser-based application can't access back-ends. Therefore, all back-end providers SHOULD support CORS to enable browser-based applications to access back-ends. [CORS is a recommendation of the W3C organization](https://www.w3.org/TR/cors/). The following chapters will explain how back-end providers can implement CORS support.

**Tip**: Most servers can send the required headers and the responses to the OPTIONS requests automatically for all endpoints. Otherwise you may also use a proxy server to add the headers and OPTIONS responses.

### CORS headers

The following headers MUST be included with every response:

| Name                             | Description                                                  | Example |
| -------------------------------- | ------------------------------------------------------------ | ------- |
| Access-Control-Allow-Origin      | Allowed origin for the request, including protocol, host and port or `*` for all origins. It is RECOMMENDED to return the value `*` to allow requests from browser-based implementations such as the Web Editor. | `*` |
| Access-Control-Expose-Headers    | Some endpoints require to send additional HTTP response headers such as `9ci-Identifier` and `Location`. To make these headers available to browser-based clients, they MUST be white-listed with this CORS header. The following HTTP headers are white-listed by browsers and MUST NOT be included: `Cache-Control`, `Content-Language`, `Content-Length`, `Content-Type`, `Expires`, `Last-Modified` and `Pragma`. At least the following headers MUST be listed in this version of the 9ci API: `Link`, `Location`, `9ci-Costs` and `9ci-Identifier`. | `Link, Location, 9ci-Costs, 9ci-Identifier` |



#### Example request and response

Request:

```http
POST /api/v1/jobs HTTP/1.1
Host: client.9ci.io
Origin: https://client.org:8080
Authorization: Bearer basic//ZXhhbXBsZTpleGFtcGxl
```

Response:

```http
HTTP/1.1 201 Created
Access-Control-Allow-Origin: *
Access-Control-Expose-Headers: Location, OpenEO-Identifier, OpenEO-Costs, Link
Content-Type: application/json
Location: https://client.9ci.io/api/v1/jobs/abc123
9ci-Identifier: abc123
```

## OPTIONS method

All endpoints must respond to the `OPTIONS` HTTP method. This is a response for the preflight requests made by web browsers before sending the actual request (e.g. `POST /jobs`). It needs to respond with a status code of `204` and no response body.
**In addition** to the HTTP headers shown in the table above, the following HTTP headers MUST be included with every response to an `OPTIONS` request:

| Name                             | Description                                                  | Example |
| -------------------------------- | ------------------------------------------------------------ | ------- |
| Access-Control-Allow-Headers     | Comma-separated list of HTTP headers allowed to be sent with the actual (non-preflight) request. MUST contain at least `Authorization` if any kind of authorization is implemented by the back-end. | `Authorization, Content-Type` |
| Access-Control-Allow-Methods     | Comma-separated list of HTTP methods allowed to be requested. Back-ends MUST list all implemented HTTP methods for the endpoint. | `OPTIONS, GET, POST, PATCH, PUT, DELETE` |
| Content-Type                     | SHOULD return the content type delivered by the request that the permission is requested for. | `application/json` |

### Example request and response

Request:

```http
OPTIONS /api/v1/jobs HTTP/1.1
Host: client.9ci.io
Origin: https://client.org:8080
Access-Control-Request-Method: POST 
Access-Control-Request-Headers: Authorization, Content-Type
```

Note that the `Access-Control-Request-*` headers are automatically attached to the requests by the browsers.

Response:

```http
HTTP/1.1 204 No Content
Access-Control-Allow-Origin: *
Access-Control-Allow-Methods: OPTIONS, GET, POST, PATCH, PUT, DELETE
Access-Control-Allow-Headers: Authorization, Content-Type
Access-Control-Expose-Headers: Location, 9ci-Identifier, 9ci-Costs, Link
Content-Type: application/json
```

# Authentication

The 9ci API offers two forms of authentication by default:
* OpenID Connect (recommended) at `GET /credentials/oidc`
* Basic at `GET /credentials/basic`
  
After authentication with any of the methods listed above, the tokens obtained during the authentication workflows can be sent to protected endpoints in subsequent requests.

Further authentication methods MAY be added by back-ends.

<SecurityDefinitions />