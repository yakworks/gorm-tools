const fs = require('fs');
const yaml = require('yaml');
//const SwaggerParser = require("@apidevtools/swagger-parser");
const mergeAllOf = require("./mergeAllOf");
const sampleMaker = require("./sampleMaker")
const tools = require('./tools');

let api = {};

/**
 * Loads api from file name. does some clean up for anyOf to deref and merge first
 * @param {string} apiFile - the yaml file name to load
 * @returns the api object
 */
function getApi() {
  return api;
}

/**
 * Loads api from file name. does some clean up for anyOf to deref and merge first
 * @param {string} apiFile - the yaml file name to load
 * @returns the api object
 */
function loadApi(apiFile) {
  let s = fs.readFileSync(apiFile, 'utf8');
  api = yaml.parse(s);
  mergeAllOf(api, api);
  return api;
}

/**
 * sorts the object by keys
 * @param {object} obj
 * @returns the sorted obj
 */
function sortByKeys(obj) {
  return tools.sortByKeys(obj)
}

function getPrettySample(schema, api, opts) {
  let sampleObj = sampleMaker.getSample(schema, api, opts);
  return tools.prettyJson(sampleObj)
}

module.exports = {
  loadApi,
  sortByKeys,
  getPrettySample,
  getApi
  // getShinsData
}
