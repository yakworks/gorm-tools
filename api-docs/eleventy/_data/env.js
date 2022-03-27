require('dotenv').config();
const sampleMaker = require("../../lib/sampleMaker")

let envOpts = {
  hello: process.env.HELLO || 'Hello not set, but hi, anyway 👋',
  apiFile: 'build/api.yaml',
  sampleOptions: {
    maxSampleDepth: 3,
    maxDepth: 1
  },
  language_tabs: [{ "shell": "Curl" }],
  tocSummary: true
}

//set defaults for sampler
sampleMaker.mergeDefaults(envOpts.sampleOptions)

module.exports = envOpts;
