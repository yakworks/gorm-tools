"use strict";
const JsonPointer = require('json-pointer');
const _ = require("lodash");

/**
 * a simple func to do a shallow dereference (only looks for '$ref' key on the passed in schema) 
 * 
 * @param {object} schema - the object that may contain the $ref
 * @param {*} spec - the full spec to look up the ref
 * @returns 
 */
function shallowDeref(schema, spec) {
  let refObj = {}
  if (schema.$ref) {
    if (!spec) {
      throw new Error('Your schema contains $ref. You must provide full specification in the third parameter.');
    }
    let ref = decodeURIComponent(schema.$ref);
    if (ref.startsWith('#')) {
      ref = ref.substring(1);
    }

    refObj = JsonPointer.get(spec, ref);
  }
  let mergedSchema = _.defaultsDeep(schema, refObj);
  //console.log("mergedSchema ", mergedSchema)
  return mergedSchema
}

module.exports = shallowDeref;
//# sourceMappingURL=index.js.map