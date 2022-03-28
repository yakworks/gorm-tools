/**
 * main data class that helps load
 */
const schemaUtils = require("../../lib/schemaUtils")
const apiSupport = require("../../lib/apiSupport")
const pathUtils = require("../../lib/pathUtils")
const codeUtils = require("../../lib/codeUtils")
const sampleMaker = require("../../lib/sampleMaker")
const shallowDeref = require("../../lib/shallowDeref")
const env = require("./env")

let apiFile = env.apiFile;
let oapi = apiSupport.loadApi(apiFile);

module.exports = {
    oapi,
    apiSupport,
    schemaUtils,
    shallowDeref,
    pathUtils,
    codeUtils,
    sampleMaker
}
