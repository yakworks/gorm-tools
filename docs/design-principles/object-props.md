## Object Guidelines

### Property Names

- be consistent
- property names must be camelCase
- Property names must be an ASCII subset. The first character must be a letter, an underscore or a dollar sign, and subsequent characters can be a letter, an underscore, hyphen or a number.
- Array and collection names should be pluralized
- Use Consistent Property Values
- Boolean property values must not be null and have a default, which means they are always shown but never required
- Null values should have their fields removed
  Swagger/OpenAPI, which is in common use, doesn't support null field values (it does allow omitting that field completely if it is not marked as required). However that doesn't prevent clients and servers sending and receiving those fields with null values. Also, in some cases null may be a meaningful value - for example, JSON Merge Patch RFC 7382) using null to indicate property deletion.

- Empty array values should not be null, they can be represented as the the empty list, [].
- Enumerations should be represented as Strings
- Date and date-time property values should conform to RFC 3399
- for "date" use strings matching date-fullyear "-" date-month "-" date-mday, for example: 2015-05-28
- for "date-time" use strings matching full-date "T" full-time, for example 2015-05-28T14:07:17Z
- A zone offset may be used (both, in request and responses) -- this is simply defined by the standards. However, we encourage restricting dates to UTC and without offsets. For example 2015-05-28T14:07:17Z rather than 2015-05-28T14:07:17+00:00. From experience we have learned that zone offsets are not easy to understand and often not correctly handled. Note also that zone offsets are different from local times that might be including daylight saving time. Localization of dates should be done by the services that provide user interfaces, if required.

- When it comes to storage, all dates should be consistently stored in UTC without a zone offset. Localization should be done locally by the services that provide user interfaces, if required.

- Schema based JSON properties that are by design durations and intervals could be strings formatted as recommended by ISO 8601 (Appendix A of RFC 3399 contains a grammar for durations).

- Standards should be used for Language, Country and Currency

ISO 3166-1-alpha2 country (It's "GB", not "UK",
ISO 639-1 language code
BCP-47 (based on ISO 639-1) for language variants
ISO 4217 currency codes

## Naming

- Prefer to use lowercase separate words with hyphens for URI Path Segments. camelCase is ok but not ideal.
see http://www.tothenew.com/blog/customizing-url-formats-in-grails/ and grails.web.url.converter yml property  
Example:  
/shipment-order/1/shipment-order-lines
This applies to concrete path segments and not the names of path parameters. For example shipment_order_id would be ok as a path parameter.
[see here](http://blog.restcase.com/5-basic-rest-api-design-guidelines/) for a good explanation on case

- camelCase or snake_case for Query Parameters

- Prefer Hyphenated-Pascal-Case for HTTP header Fields

- Do not Pluralize Resource Names, keep them the same as controller or use hyphens instead of camelCase if desired.

- May: Use /api as first Path Segment

- Must: Avoid Trailing Slashes

- May: Use Conventional Query Strings

- If you provide query support for sorting, pagination, filtering functions or other actions, use the following standardized naming conventions:

    - `q` — default query parameter (e.g. used by browser tab completion); should have an entity specific alias, like sku
    - `limit` — to restrict the number of entries. See Pagination section. Hint: You can use size as an alternate query string.
    - `cursor` — key-based page start. See Pagination section below.
    - `offset` — numeric offset page start. See Pagination section below. Hint: In combination with limit, you can use `page` as an alternative to offset.
    - `sort` — comma-separated list of fields to sort. To indicate sorting direction, fields my prefixed with + (ascending) or - (descending, default), e.g. /sales-orders?sort=+id
    - `fields` — to retrieve a subset of fields. See Support Filtering of Resource Fields below.
    - `expand` — to expand embedded entities (ie.: inside of an article entity, expand silhouette code into the silhouette object). Implementing “expand” correctly is difficult, so do it with care. See Embedding resources for more details.
