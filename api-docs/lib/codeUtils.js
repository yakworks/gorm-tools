/**
 * helpers for the code examples
 */

//const _ = require("lodash");

/**
 *  if {foo: "bar"} is passed in then returns foo key
 */
 function getFirstKey(obj){
  return Object.keys(obj)[0]
}

/**
 * checks a params list to see if it has q, used for code examples
 */
function hasQ(params){
  if(!params) return false;
  let qfound = false;
  for (let p of params) {
    if (p.name === 'q') qfound = true;
  }
  return qfound;
}

/**
 * checks if 'PUT','POST','PATCH'
 */
function isEditVerb(verb){
  return ['PUT','POST','PATCH'].indexOf(verb.toUpperCase()) > -1
}

module.exports = {
  getFirstKey,
  hasQ,
  isEditVerb
}
