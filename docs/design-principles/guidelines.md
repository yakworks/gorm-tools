
see:
https://zalando.github.io/restful-api-guidelines/tooling/Tooling.html

## API as a Product

We are transforming to a platform comprising a rich set of products following a Software as a Platform (SaaS) model for our business partners. We want to deliver products to our customers which can be consumed like a service.

Platform products provide their functionality via (public) APIs; hence, the design of our APIs should be based on the API as a Product principle:

- Treat your API as product and understand the needs of its customers
- Take ownership and advocate for the customer and continuous improvement
- Emphasize easy understanding, discovery and usage of APIs; design APIs irresistible for client engineers
- Actively improve and maintain API consistency over the long term
- Make use of customer feedback and provide service level support
- RESTful API as a Product makes the difference between enterprise integration business and agile, innovative product service business built on a platform of APIs.
- are easy to understand and learn
- are general and abstracted from specific implementation and use cases
- have a common look and feel
- follow a consistent RESTful style and syntax

Based on your concrete customer use cases, you should carefully check the trade-offs of API design variants and avoid short-term server side implementation optimizations at the expense of unnecessary client side obligations and have a high attention on API quality and client developer experience.

API as a Product is closely related to our API First principle (see next chapter) which is more focussed on how we engineer high quality APIs.

## API Design Principles

REST is centered around business (data) entities exposed as resources that are identified via URIs and can be manipulated via standardized CRUD-like methods using different representations, self-descriptive messages and hypermedia. RESTful APIs tend to be less use-case specific and comes with less rigid client / server coupling and are more suitable as a platform interface being open for diverse client applications.

- We prefer REST-based APIs with JSON payloads
- We prefer systems to be truly RESTful
- We apply the RESTful web service principles to all kind of application components, whether they provide functionality via the Internet or via the intranet as larger application elements.
- We strive to build interoperating distributed systems that different teams can evolve in parallel.

An important principle for (RESTful) API design and usage is Postel's Law, aka the Robustness Principle

- Be liberal in what you accept, be conservative in what you send

Readings: Read the following to gain additional insight on the RESTful service architecture paradigm and general RESTful API design style:

## Security

TODO
