const safejson = require('fast-safe-stringify');
const sampler = require('openapi-sampler');
const recurse = require('reftools/lib/recurse.js').recurse;
const visit = require('reftools/lib/visit.js').visit;
const circularClone = require('reftools/lib/clone.js').circularClone;
const yaml = require('yaml');
const _ = require("lodash");

const defaultOptions = {
  maxSampleDepth: 3
}

function mergeDefaults(opts){
  _.merge(defaultOptions, opts)
}

const contentTypes = {
  xml: ['^(application|text|image){1}\\/(.*\\+){0,1}xml(;){0,1}(\\s){0,}(charset=.*){0,}$'],
  json: ['^(application|text){1}\\/(.*\\+){0,1}json(;){0,1}(\\s){0,}(charset=.*){0,}$'],
  yaml: ['application/x-yaml', 'text/x-yaml'],
  form: ['multipart/form-data', 'application/x-www-form-urlencoded', 'application/octet-stream'],
  text: ['text/plain', 'text/html']
};

/**
 * checks the validTypes array to see if it matches the contentTypes for the ctype
 * @param {array} validTypes - the array of valid types
 * @param {string} ctype - string type, ex: 'json'
 * @returns true/false
 */
function doContentType(validTypes, ctype) {
  for (let type of validTypes) {
      for (let target of contentTypes[ctype]||[]) {
          if (type.match(target)) return true;
      }
  }
  return false;
}

function getBodyParameterExamples(validTypes, obj) {
  let content = '';
  if (doContentType(validTypes, 'json')) {
      content += '```json\n';
      content += safejson(obj, null, 2) + '\n';
      content += '```\n\n';
  }
  if (doContentType(validTypes, 'yaml')) {
      content += '```yaml\n';
      content += yaml.stringify(obj) + '\n';
      content += '```\n\n';
  }
  return content;
}

function clean(obj) {
  if (typeof obj === 'undefined') return {};
  visit(obj,{},{filter:function(obj,key){
      if (!key.startsWith('x-widdershins')) return obj[key];
  }});
  return obj;
}

function convertExample(ex) {
  if (typeof ex === 'string') {
      try {
          return yaml.parse(ex);
      }
      catch (e) {
          return ex;
      }
  }
  else return ex;
}

function getResponseExamples(operation, oapi, options) {
  let content = '';
  let examples = [];
  let autoDone = {};
  for (let resp in operation.responses) {
    if (resp.startsWith('x-')) continue;

    let response = operation.responses[resp];
    for (let ct in response.content) {
      let contentType = response.content[ct];
      let cta = [ct];
      // support embedded examples
      if (contentType.examples) {
        for (let ctei in contentType.examples) {
          let example = contentType.examples[ctei];
          examples.push({ description: example.description || response.description, value: clean(convertExample(example.value)), cta: cta });
        }
      }
      else if (contentType.example) {
        examples.push({ description: resp + ' ' + 'Response', value: clean(convertExample(contentType.example)), cta: cta });
      }
      else if (contentType.schema) {
        let obj = contentType.schema;
        let autoCT = '';
        if (doContentType(cta, 'json')) autoCT = 'json';
        if (doContentType(cta, 'yaml')) autoCT = 'yaml';
        if (doContentType(cta, 'xml')) autoCT = 'xml';
        if (doContentType(cta, 'text')) autoCT = 'text';

        if (!autoDone[autoCT]) {
          autoDone[autoCT] = true;
          let opts = _.merge({}, defaultOptions, options || {}, { skipWriteOnly: true, quiet: true })
          let sample = getSample(obj, oapi, opts)
          examples.push({ description: resp + ' ' + 'Response', value: sample, cta: cta });
        }
      }
    }

  }
  let lastDesc = '';
  for (let example of examples) {
    if (example.description && example.description !== lastDesc) {
      content += '> ' + example.description + '\n\n';
      lastDesc = example.description;
    }
    if (doContentType(example.cta, 'json')) {
      content += '```json\n';
      content += safejson(example.value, null, 2) + '\n';
      content += '```\n\n';
    }
    if (doContentType(example.cta, 'yaml')) {
      content += '```yaml\n';
      content += yaml.stringify(example.value) + '\n';
      content += '```\n\n';
    }
  }
  return content;
}

function getSample(orig, api, options) {

  if (orig && orig.example) return orig.example;

  let obj = circularClone(orig);
  let defs = api; //Object.assign({},api,orig);
  //console.log("defaultOptions ", defaultOptions)
  let opts = _.defaults({}, options || {}, defaultOptions)
  //console.log("getSample opts ", opts)
  if (obj) {
    try {
      var sample = sampler.sample(obj, opts, defs); // was api
      //console.log("sampler.sample success")
      if (sample && typeof sample.$ref !== 'undefined') {
        console.log("sample has $ref ")
        obj = JSON.parse(safejson(orig));
        sample = sampler.sample(obj, opts, defs);
      }
      if (typeof sample !== 'undefined') {
        if (sample === null || !Object.keys(sample).length){
          console.log("sample is null or no keys")
          sample = sampler.sample({ type: 'object', properties: { anonymous: obj } }, opts, defs).anonymous;
        }
      }
    }
    catch (ex) {
      console.error('# sampler ' + ex.message);
    }
  }
  let result = clean(sample);
  result = strim(result, opts.maxSampleDepth - 2);
  //console.log("result ", result)
  return result;
}

function strim(obj, maxDepth) {
  if (maxDepth <= 0) return obj;
  recurse(obj,{identityDetection:true},function(obj,key,state){
      if (state.depth >= maxDepth) {
          if (Array.isArray(state.parent[state.pkey])) {
              state.parent[state.pkey] = [];
          }
          else if (typeof state.parent[state.pkey] === 'object') {
              state.parent[state.pkey] = {};
          }
      }
  });
  return obj;
}

module.exports = {
  defaultOptions,
  mergeDefaults,
  getBodyParameterExamples,
  getResponseExamples,
  getSample
}
