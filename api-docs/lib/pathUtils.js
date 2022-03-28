const shallowDeref = require("./shallowDeref")
// const schemaUtils = require("./schemaUtils")
const codeUtils = require("./codeUtils")
const safejson = require('fast-safe-stringify');
// const yaml = require('yaml');
const statusCodes = require('./statusCodes.json');
const sampleMaker = require("./sampleMaker")
const _ = require("lodash");

/**
 * groups all the path's keys by the tags and returns the object of form
 * {tagKey: [{pathKey: ..., verb: ...}, etc..]}
 * @param {object} api - the full open api spec
 * @returns the object with the tags as keys
 */
function groupPathsByTag(api){
  let pathsByTag = {};
  // convert paths to key's array
  const pathKeys = Object.keys(api.paths);
  // iterate over paths
  pathKeys.forEach((pathKey) => {
    let pathInfo = api.paths[pathKey]
    let verbKeys = ['get', 'put', 'patch', 'post', 'delete', 'options', 'head', 'trace']
    for (var opKey in pathInfo) {
      if(!verbKeys.includes(opKey)) continue;
      let operation = pathInfo[opKey]
      let tagName = 'Defaults' //Default in case there is no tags which should not really happen
      if (operation.tags && operation.tags.length > 0) {
        tagName = operation.tags[0]
      }
      if (!pathsByTag[tagName]) {
        pathsByTag[tagName] = [];
      }
      pathsByTag[tagName].push({pathKey: pathKey, verb: opKey})
    }
  });
  return pathsByTag
}

/**
* turns the responses object into an array with items that can be iterated
* over to display the response table. does lookups to add links to ietf.org based on code
* and moves schema up from content
*
* @param {object} operation - the operation with the responses
* @param {object*} oapi - the full api doc
* @returns the array
*/
function getResponsesArray(operation, oapi) {
  let responseArray = [];
  let responses = operation.responses
  for (let r in responses) {
    let response = responses[r];
    shallowDeref(response, oapi)

    let entry = _.clone(response)
    entry.code = r;
    if(entry.description) entry.description = entry.description.trim()

    entry.name = r;
    let codeInfo = _.find(statusCodes, { 'code': r });
    if(codeInfo){
      entry.name = `<a href="${codeInfo.spec_href}">${codeInfo.phrase}</a>`;
    }

    _.forEach(response.content, (contentType) => {
      let schema = contentType.schema
      if (schema) {
        entry.schema = schema;
      }
    });
    //entry.content = response.content;
    //entry.links = response.links;
    responseArray.push(entry);
  }
  return responseArray;
}

function derefParameters(operation, oapi){
  if(operation.parameters && operation.parameters.length > 0){
    for (let p of operation.parameters ) {
      shallowDeref(p, oapi)
    }
  }
}

function derefResponses(operation, oapi){
  let responses = operation.responses
  for (let statusCode in responses) {
    let resp = operation.responses[statusCode]
    shallowDeref(resp, oapi)
  }
}

/**
 * fakes up a parameter entry for the body so it can be added to the parameters list
 */
function mockBodyParam(bodyParameter) {
  let param = {};
  if (bodyParameter && bodyParameter.schema) {
    param.in = 'body';
    param.schema = bodyParameter.schema;
    param.name = 'body';
    param.required = bodyParameter.required;
    param.description = bodyParameter.description;
    //param.refName = bodyParameter.refName;
  }
  return param;
}

/**
 * clones the body param and moves the schema up from the content.
 * then generates the example for it
 */
function getRequestBodyParam(operation, oapi, options) {
  let consumes = [];

  let requestBody = _.clone(operation.requestBody);
  if (requestBody) {
    requestBody.required = requestBody.required || false;

    let exampleValues = {};
    for (var contentType in requestBody.content) {
      if (exampleValues.object) continue; //if its already setup

      let reqContent = requestBody.content[contentType]
      consumes.push(contentType);
      requestBody.contentType = contentType;
      if (reqContent.schema) {
        requestBody.schema = reqContent.schema;
      }

      if (reqContent.examples) {
        let key1 = Object.keys(reqContent.examples)[0];
        let firstExample = reqContent.examples[key1]
        exampleValues.object = firstExample.value;
        exampleValues.description = firstExample.description;
      }
      else {
        let opts = _.defaults({ skipReadOnly: true, quiet: true }, options || {})
        exampleValues.object = sampleMaker.getSample(reqContent.schema, oapi, opts);
      }

      if (typeof exampleValues.object === 'object') {
        exampleValues.json = safejson(exampleValues.object, null, 2);
      }
      else {
        exampleValues.json = exampleValues.object;
      }
    } //end content loop

    requestBody.examples = exampleValues
    requestBody.consumes = consumes
    requestBody.exampleMd = sampleMaker.getBodyParameterExamples(consumes, exampleValues.object)
  } // end if (op.requestBody)

  return requestBody;
}

/**
 * builds and returns a method object for the path and verb
 * @param {string} pathKey - the key to look up in openapi paths
 * @param {string} verb - the verb(get,post, etc) which is the key under the path
 */
function getMethod(pathKey, verb, oapi){
  let path = oapi.paths[pathKey]
  let operation = path[verb]
  let opId = operation.operationId
  let responses = getResponsesArray(operation, oapi)
  derefParameters(operation, oapi)
  let bodyParameter = getRequestBodyParam(operation, oapi)

  let parameters = []
  //console.log("operation.parameters ", operation.parameters)
  if(operation.parameters) {
    parameters.push(...operation.parameters)
  }
  let dummyBodyParam = mockBodyParam(bodyParameter)
  if(dummyBodyParam) parameters.push(dummyBodyParam)

  let hasResponseSchemas = false;
  for (let response of responses ) {
    if (response.schema) hasResponseSchemas = true;
  }
  let serverUrl
  if(oapi.servers) serverUrl = oapi.servers[0].url

  let xExamples = operation['x-codeSamples']

  let method = {
    pathKey,
    verb,
    verbUpper: verb.toUpperCase(),
    isEditVerb: codeUtils.isEditVerb(verb),
    path,
    opId,
    operation,
    responses,
    hasResponseSchemas,
    parameters,
    bodyParameter,
    responseExamples: sampleMaker.getResponseExamples(operation, oapi),
    hasQ: codeUtils.hasQ(parameters),
    serverUrl,
    xExamples
  }
  return method
}

module.exports = {
  getResponsesArray,
  groupPathsByTag,
  getMethod,
  derefParameters,
  derefResponses
}
