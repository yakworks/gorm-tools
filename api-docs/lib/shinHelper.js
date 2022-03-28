/**
 * old helpers to use widdershins, .... no longer used, kept for reference
 */
const shins = require('../widdershins/lib/openapi3');
const clone = require('reftools/lib/clone.js').circularClone;
const pathUtils = require("./pathUtils");
const apiSupport = require("./apiSupport");

let oapi = apiSupport.getApi()

function setupShinsForMethod(shins, tag, pathKey, verb, operation) {
  //need to check how much of this shins needs, it uses data as the global
  let resource = shins.resources[tag]
  //console.log("resource ", resource)
  let method = resource.methods[operation.operationId]
  pathUtils.derefParameters(operation, oapi)
  //console.log("method ", method)
  shins.method = method;
  shins.operationUniqueSlug = operation.operationId;
  shins.operation = operation;
  //used in code template
  shins.methodUpper = verb.toUpperCase();
  shins.url = shins.utils.slashes(shins.baseUrl + pathKey);
  shins.parameters = shins.operation.parameters;
  shins.enums = [];
  shins.utils.fakeProdCons(shins);
  shins.utils.fakeBodyParameter(shins);
  shins.utils.mergePathParameters(shins);
  shins.utils.getParameters(shins);

  pathUtils.derefResponses(operation, oapi)
  shins.responses = shins.utils.getResponses(shins);
  shins.responseSchemas = false;
  for (let response of shins.responses) {
    if (response.content) shins.responseSchemas = true;
  }
}

function getShinsData(api) {
  let shinsOpts = {
    maxDepth: 2,
    tocSummary: true,
    user_templates: "./widdershins/templates",
    sample: true,
    codeSamples: true,
    language_tabs: [{ "shell": "Curl"}]
  };
  return shins.getDataObject(clone(api), shinsOpts);
}

module.exports = {
  setupShinsForMethod,
  getShinsData
};
