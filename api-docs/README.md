## Intro

This dir is copied to build and currently our OpenapiGeneratorSpec uses OapiGen
to creates the yaml under the openapi. 

Uses

- openapi-cli from redocly to lint and merge the openapi yamls into 1
- slate as the template for css etc, slate was a template that used ruby
- eleventy is the node based static generator to build the site from the md and slate assets

## Quickstart

To run the full monty, run the `gw rcm-api:integrationTest --tests *OpenapiGeneratorSpec*`
to generate the files into build. Then `cd api-docs`

- `npm install` to download the internet for node
- `npm run start` will run the full monty and serve the page

### scripts (check package.json)

- `npm run serve` runs the eleventy serve without building the api.yaml
- `npm run openapi:bundle` merges the files in a single openapi/dist/api.yaml openapi file


### notes, possible libs removed from package
"json-schema-merge-allof": "^0.8.1",

### Used this much for inspiration
https://github.com/Rebilly/api-definitions
https://www.rebilly.com/docs/dev-docs/api/customers/getcustomercollection
