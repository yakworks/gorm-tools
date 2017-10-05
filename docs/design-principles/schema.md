# Common Data Types

Definitions of data objects that are good candidates for wider usage:

## May Use a Common Money Object

Use the following common money structure:

    Money:
      type: object
      properties:
        amount:
          type: number
          description: Amount expressed as a decimal number of major currency units
          format: decimal
          example: 99.95
        currency:
          type: string
          description: 3 letter currency code as defined by ISO-4217
          format: iso-4217
          example: EUR
      required:
        - amount
        - currency

The decimal values for "amount" describe unit and subunit of the currency in a single value, where
the digits before the decimal point are for the major unit and the digits after the decimal point are
for the minor unit. Note that some business cases (e.g. transactions in Bitcoin) call for a higher
precision, so applications must be prepared to accept values with unlimited precision, unless
explicitly stated otherwise in the API specification.
Examples for correct representations (in EUR):

- `42.20` or `42.2` = 42 Euros, 20 Cent
- `0.23` = 23 Cent
- `42.0` or `42` = 42 Euros
- `1024.42` = 1024 Euros, 42 Cent
- `1024.4225` = 1024 Euros, 42.25 Cent

Make sure that you don’t convert the “amount” field to `float` / `double` types when implementing
this interface in a specific language or when doing calculations. Otherwise, you might lose
precision. Instead, use exact formats like
Java’s [`BigDecimal`](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html).
See [Stack Overflow](http://stackoverflow.com/a/3730040/342852) for more info.

Some JSON parsers (NodeJS’s, for example) convert numbers to floats by default. After discussing the
[pros and cons](https://docs.google.com/spreadsheets/d/12wTj-2w39f69XZGwRDrosNc1yWPwQpGgEs_DCt5ODaQ),
we’ve decided on "decimal" as our amount format. It is not a standard OpenAPI format, but should
help us to avoid parsing numbers as float / doubles.


## Use common field names and semantics

There exist a variety of field types that are required in multiple places. To achieve consistency across all API implementations, you must use common field names and semantics whenever applicable.

### Generic Fields

There are some data fields that come up again and again in API data:

- `id`: the identity of the object. If used, IDs must opaque strings and not numbers. IDs are unique within some documented context, are stable and don't change for a given object once assigned, and are never recycled cross entities.

- `xyzId`: an attribute within one object holding the identifier of another object must use a name that corresponds to the type of the referenced object or the relationship to the referenced object followed by `Id` (e.g. `customerId` not `customer_number`; `parentNodeId` for the reference to a parent node from a child node, even if both have the type `Node`)

- `createdDate`: when the object was created. If used, this must be a `date-time` construct.

- `updateDate`: when the object was updated. If used, this must be a `date-time` construct.

- `_type`: the kind of thing this object is. If used, the type of this field should be a string. Types allow runtime information on the entity provided that otherwise requires examining the Open API file.

Example JSON schema:

    tree_node:
      type: object
      properties:
        id:
          description: the identifier of this node
          type: string
        createdDate:
          description: when got this node created
          type: string
          format: 'date-time'
        editedDate:
          description: when got this node last updated
          type: string
          format: 'date-time'
        \_type:
          type: string
          enum: [ 'LEAF', 'NODE' ]
        parentNodeId:
          description: the identifier of the parent node of this node
          type: string
      example:
        id: '123435'
        created: '2017-04-12T23:20:50.52Z'
        modified: '2017-04-12T23:20:50.52Z'
        type: 'LEAF'
        parent_node_id: '534321'

These properties are not always strictly necessary, but making them idiomatic allows API client developers to build up a common understanding of Zalando's resources. There is very little utility for API consumers in having different names or value types for these fields across APIs.

### Address Fields

Address structures play a role in different functional and use-case contexts, including country
variances. All attributes that relate to address information should follow the naming
and semantics defined below.

    addressee:
        description:
          a (natural or legal) person that gets addressed
        type: object
        required:
          - firstName
          - lastName
          - street
          - city
          - zip
          - countryCode
        properties:
          salutation:
            description: |
              a salutation and/or title used for personal contacts to some addressee;
              not to be confused with the gender information!
            type: string
            example: Mr
          firstName:
            description: |
              given name(s) or first name(s) of a person; may also include the middle names.
            type: string
            example: Hans Dieter
          lastName:
            description: |
              family name(s) or surname(s) of a person
            type: string
            example: Mustermann
          businessName:
            description: |
              company name of the business organization. Used when a business is the actual
              addressee; for personal shipments to office addresses, use `care_of` instead.
            type: string
            example: Consulting Services GmbH

    address:
        description:
          an address of a location/destination
        type: object
        properties:
          street:
            description: |
              the full street address including house number and street name
            type: string
            example: Schönhauser Allee 103
          additional:
            description: |
              further details like building name, suite, apartment number, etc.
            type: string
            example: 2. Hinterhof rechts
          city:
            description: |
              name of the city / locality
            type: string
            example: Berlin
          zip:
            description: |
              zip code or postal code
            type: string
            example: 14265
          countryCode:
            description: |
              the country code according to
              [iso-3166-1-alpha-2](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)
            type: string
            example: DE
        required:
          - street
          - city
          - zip
          - countryCode

Grouping and cardinality of fields in specific data types may vary based on the specific use case
(e.g. combining addressee and address fields into a single type when modeling an address label vs distinct addressee and address types when modeling users and their addresses).

## {{ book.must }} Follow Hypertext Control Conventions

APIs that provide hypertext controls (links) to interconnect API resources must follow
the conventions for naming and modeling of hypertext controls as defined in section [Hypermedia](../hyper-media/Hypermedia.html).

## {{ book.must }} Use Problem JSON

[RFC 7807](http://tools.ietf.org/html/rfc7807) defines the media type `application/problem+json`.
Operations should return that (together with a suitable status code) when any problem
occurred during processing and you can give more details than the status code itself
can supply, whether it be caused by the client or the server (i.e. both for 4xx or 5xx errors).

A previous version of this guideline (before the publication of that RFC and the
registration of the media type) told to return `application/x.problem+json` in these
cases (with the same contents).
Servers for APIs defined before this change should pay attention to the `Accept` header sent
by the client and set the `Content-Type` header of the problem response correspondingly.
Clients of such APIs should accept both media types.

APIs may define custom problems types with extension properties, according to their specific needs.

The Open API schema definition can be found [on github](https://zalando.github.io/problem/schema.yaml).
You can reference it by using:

```yaml
responses:
  503:
    description: Service Unavailable
    schema:
      $ref: 'https://zalando.github.io/problem/schema.yaml#/Problem'

```

## {{ book.must }} Do not expose Stack Traces

Stack traces contain implementation details that are not part of an API, and on which clients
should never rely. Moreover, stack traces can leak sensitive information that partners and third
parties are not allowed to receive and may disclose insights about vulnerabilities to attackers.

## Data Formats

## {{ book.must }} Use JSON to Encode Structured Data

Use JSON-encoded body payload for transferring structured data.
The JSON payload must follow [RFC-7159](https://tools.ietf.org/html/rfc7159) by having
(if possible) a serialized object as the top-level structure, since it would allow for future extension.
This also applies for collection resources where one naturally would assume an array. See the
[pagination](../pagination/Pagination.md#could-use-pagination-links-where-applicable) section for an example.

## {{ book.may }} Use non JSON Media Types for Binary Data or Alternative Content Representations

Other media types may be used in following cases:
* Transferring binary data or data whose structure is not relevant. This is the case if payload structure
is not interpreted and consumed by clients as is. Example of such use case is downloading images
in formats JPG, PNG, GIF.
* In addition to JSON version alternative data representations (e.g. in formats PDF, DOC, XML)
may be made available through content negotiation.

## {{ book.must }} Use Standard Date and Time Formats

###JSON Payload
Read more about date and time format in [Json Guideline](../json-guidelines/JsonGuidelines.md#date-property-values-should-conform-to-rfc-3399).

###HTTP headers
Http headers including the proprietary headers. Use the [HTTP date format defined in RFC 7231](http://tools.ietf.org/html/rfc7231#section-7.1.1.1).

## {{ book.may }} Use Standards for Country, Language and Currency Codes

Use the following standard formats for country, language and currency codes:

* [ISO 3166-1-alpha2 country codes](https://en.wikipedia.org/wiki/ISO_3166-1_alpha-2)

     * (It is “GB”, not “UK”, even though “UK” has seen some use at Zalando)

* [ISO 639-1 language code](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes)

    * [BCP-47](https://tools.ietf.org/html/bcp47) (based on ISO 639-1) for language variants

* [ISO 4217 currency codes](https://en.wikipedia.org/wiki/ISO_4217)

## {{ book.must }} Define Format for Type Number and Integer

Whenever an API defines a property of type `number` or `integer`, the precision must be defined by the format as follows to prevent clients from guessing the precision incorrectly, and thereby changing the value unintentionally:

| type    | format  | specified value range                                 |
|---------|---------|-------------------------------------------------------|
| integer | int32   | integer between -2<sup>31</sup> and 2<sup>31</sup>-1  |
| integer | int64   | integer between -2<sup>63</sup> and 2<sup>63</sup>-1  |
| integer | bigint  | arbitrarily large signed integer number               |
| number  | float   | IEEE 754-2008/ISO 60559:2011 binary64 decimal number  |
| number  | double  | IEEE 754-2008/ISO 60559:2011 binary128 decimal number |
| number  | decimal | arbitrarily precise signed decimal number             |

The precision must be translated by clients and servers into the most specific language types. E.g. for the following definitions the most specific language types in Java will translate to `BigDecimal` for `Money.amount` and `int` or `Integer` for the `OrderList.page_size`:

```yaml
Money:
  type: object
  properties:
    amount:
      type: number
      description: Amount expressed as a decimal number of major currency units
      format: decimal
      example: 99.95
   ...

OrderList:
  type: object
  properties:
    pageSize:
      type: integer
      description: Number of orders in list
      format: int32
      example: 42
```

## {{ book.should }} Prefer standard Media type name `application/json`

Previously, this guideline allowed the use of custom media types like `application/x.zalando.article+json`.
This usage is not recommended anymore and should be avoided, except where it is necessary for cases
of [media type versioning](../compatibility/Compatibility.md#must-use-media-type-versioning). Instead, the standard media type name `application/json` (or [`application/problem+json` for HTTP error details](http://zalando.github.io/restful-api-guidelines/common-data-types/CommonDataTypes.html#must-use-problem-json)) should be used for JSON-formatted data.

Custom media types with subtypes beginning with `x` bring no advantage compared to the standard media type for JSON, and make automated processing more difficult. They are also [discouraged by RFC 6838](https://tools.ietf.org/html/rfc6838#section-3.4).
