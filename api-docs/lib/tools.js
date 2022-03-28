/**
 * Common Utilities and helpers
 */
const stringify = require('fast-safe-stringify');
const path = require('path');

/**
 * sorts the object by keys making sure that _ and - are kept together and sorted to top
 * so foo, fooBar, foo_1, foo_2, fooBar_1, fooBars would be [foo, foo_1, foo_2, fooBar, fooBar_1, fooBars]
 * @param {object} obj
 * @returns the sorted obj
 */
function sortByKeys(obj) {

  //special sort so the _ are higher
  let sortUscore = (a, b) => {
    //dot are already sorted higher that alpha/numbers so just replace _ so they come first
    a = a.replace("_", ".");
    b = b.replace("_", ".");
    return -(a < b) || +(a > b);
    //return a.localeCompare(b);
  }
  return Object.keys(obj)
    .sort(sortUscore)
    .reduce((res, key) => (res[key] = obj[key], res), {})
}

function prettyJson(obj) {
  return stringify(obj, null, 2);
}

/**
 * makes path relative to
 *
 * @param {string} url
 * @param {string}} pageUrl
 * @returns
 */
 function makeRelative(url, pageUrl){
  return path.join(
    './',
    pageUrl.split('/').reduce((a, b) => a + (b && '../')),
    url
  )
}

module.exports = {
  prettyJson,
  sortByKeys,
  makeRelative
}
