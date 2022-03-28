const openapi = require("./_data/openapi")
const apiSupport = openapi.apiSupport
const schemaUtils = openapi.schemaUtils
// const env = require("./_data/env")

function getPrettySample(schema, depth){
  let opts = {};
  if(depth) opts = {maxSampleDepth: depth + 2};
  return apiSupport.getPrettySample(schema, openapi.oapi, opts);
}

module.exports = function() {
  //let schemas = apiSupport.sortByKeys(openapi.oapi.components.schemas)
  let schemasByEntity = schemaUtils.groupSchemasByXEntity(openapi.oapi)

  return {
    layout: 'api.njk',
    page_classes: '',
    oapi: openapi.oapi,
    //schemas,
    apiSupport: openapi.apiSupport,
    schemaUtils: openapi.schemaUtils,
    getPrettySample,
    schemasByEntity
  };
};
