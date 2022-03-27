"use strict";
const _ = require("lodash");
const JsonPointer = require('json-pointer');
//const stringify = require('json-stringify-safe');

/**
 * dereferences only allOf and then merges allOf
 *
 * @param {Object} inputSchema the starting schema
 * @param {Object} schemaSpec used for recursion, full spec to look up refs on
 * @returns the inputSpec
 */
function mergeAllOf(inputSchema, schemaSpec) {
  if (inputSchema && typeof inputSchema === 'object') {
    if (Object.keys(inputSchema).length > 0) {
      if (inputSchema.allOf) {
        let allOf = inputSchema.allOf;
        let allOfList = []
        allOf.forEach((subSchema) => {
          //let subSchemaToUse = subSchema
          if (subSchema.$ref) {
            subSchema = deref(subSchema, schemaSpec);
          }
          subSchema = mergeAllOf(subSchema, schemaSpec)
          allOfList.push(subSchema)
        });
        //inputSchema.allOf = allOfList
        // console.log("allOfList", stringify(allOfList, null, 2));
        delete inputSchema.allOf;
        var nested = _.mergeWith.apply(_, [{}].concat(allOfList, [customizer]));
        inputSchema = _.defaultsDeep(inputSchema, nested, customizer);
        //console.log("inputSchema", stringify(inputSchema, null, 2))
      }
      Object.keys(inputSchema).forEach(function (key) {
        inputSchema[key] = mergeAllOf(inputSchema[key], schemaSpec);
      });
    }
  }
  return inputSchema;
}
var customizer = function (objValue, srcValue) {
  if (_.isArray(objValue)) {
    return _.union(objValue, srcValue);
  }
  return;
};

function deref(schema, spec) {
  if (schema.$ref) {
    if (!spec) {
      throw new Error('Your schema contains $ref. You must provide full specification in the third parameter.');
    }
    let ref = decodeURIComponent(schema.$ref);
    if (ref.startsWith('#')) {
      ref = ref.substring(1);
    }

    return JsonPointer.get(spec, ref);
  }
}
module.exports = mergeAllOf;
//# sourceMappingURL=index.js.map
