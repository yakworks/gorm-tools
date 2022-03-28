const openapi = require("./_data/openapi")
const env = require("./_data/env")
const pathUtils = openapi.pathUtils
const codeUtils = openapi.codeUtils

let oapi = openapi.oapi

function getTagDesc(api, tagName){
  let tagDesc = '';

  // iterate over tags
  api.tags.forEach((tagObj) => {
    if(tagObj.name == tagName){
      tagDesc = tagObj.description
    }
  })
  return tagDesc
}

/**
 * calls
 */
function getMethod(pathKey, verb){
  return pathUtils.getMethod(pathKey, verb, oapi)
}

module.exports = function() {
  //let schemas = apiSupport.sortByKeys(openapi.oapi.components.schemas)
  //let shins = getShinsData()
  let tagPaths = pathUtils.groupPathsByTag(oapi)
  //console.log("tagPaths ", tagPaths)
  return {
    layout: 'api.njk',
    page_classes: '',
    oapi: oapi,
    apiSupport: openapi.apiSupport,
    schemaUtils: openapi.schemaUtils,
    pathUtils,
    codeUtils,
    tagPaths,
    getMethod,
    getTagDesc,
    language_tabs: env.language_tabs
  };
};

