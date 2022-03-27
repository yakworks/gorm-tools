const URITemplate = require('urijs/src/URITemplate');
// const safejson = require('fast-safe-stringify');
// const sampleMaker = require("./sampleMaker")

//WIP, still needs to be flushed out
function getHeaders(data) {

  let allHeaders = [];
  data.headerParameters = [];
  data.requiredParameters = [];
  let uriTemplateStr = data.method.path.split('?')[0].split('/ /').join('/+/');
  let requiredUriTemplateStr = uriTemplateStr;
  var templateVars = {};

  if (data.consumes.length) {
    var contentType = {};
    contentType.name = 'Content-Type';
    contentType.type = 'string';
    contentType.in = 'header';
    contentType.exampleValues = {};
    contentType.exampleValues.json = "'" + data.consumes[0] + "'";
    contentType.exampleValues.object = data.consumes[0];
    allHeaders.push(contentType);
  }
  if (data.produces.length) {
    var accept = {};
    accept.name = 'Accept';
    accept.type = 'string';
    accept.in = 'header';
    accept.exampleValues = {};
    accept.exampleValues.json = "'" + data.produces[0] + "'";
    accept.exampleValues.object = data.produces[0];
    allHeaders.push(accept);
  }

  if (!Array.isArray(data.parameters)) data.parameters = [];
  data.longDescs = false;

  let effSecurity;
  let existingAuth = allHeaders.find(function (e) {
    return e.name.toLowerCase() === 'authorization';
  });
  if (data.operation.security) {
    if (data.operation.security.length) {
      effSecurity = Object.keys(data.operation.security[0]);
    }
  }
  else if (data.api.security && data.api.security.length) {
    effSecurity = Object.keys(data.api.security[0]);
  }
  if (effSecurity && effSecurity.length && data.api.components && data.api.components.securitySchemes) {
    for (let ess of effSecurity) {
      if (data.api.components.securitySchemes[ess]) {
        let secScheme = data.api.components.securitySchemes[ess];
        if (!existingAuth && ((secScheme.type === 'oauth2') || (secScheme.type === 'openIdConnect') ||
          ((secScheme.type === 'http') && (secScheme.scheme === 'bearer')))) {
          let authHeader = {};
          authHeader.name = 'Authorization';
          authHeader.type = 'string';
          authHeader.in = 'header';
          authHeader.isAuth = true;
          authHeader.exampleValues = {};
          authHeader.exampleValues.object = 'Bearer {access-token}';
          authHeader.exampleValues.json = "'" + authHeader.exampleValues.object + "'";
          allHeaders.push(authHeader);
        }
        else if ((secScheme.type === 'apiKey') && (secScheme.in === 'header')) {
          let authHeader = {};
          authHeader.name = secScheme.name;
          authHeader.type = 'string';
          authHeader.in = 'header';
          authHeader.isAuth = true;
          authHeader.exampleValues = {};
          authHeader.exampleValues.object = 'API_KEY';
          if (data.options.customApiKeyValue) {
            authHeader.exampleValues.object = data.options.customApiKeyValue;
          }
          authHeader.exampleValues.json = "'" + authHeader.exampleValues.object + "'";
          allHeaders.push(authHeader);
        }
      }
    }
  }
  //console.log("uriTemplateStr ", uriTemplateStr)
  let uriTemplate = new URITemplate(uriTemplateStr);
  let requiredUriTemplate = new URITemplate(requiredUriTemplateStr);
  data.uriExample = uriTemplate.expand(templateVars);
  data.requiredUriExample = requiredUriTemplate.expand(templateVars);
  //console.log("data.uriExample ", data.uriExample)
  //console.log("data.requiredUriExample ", data.requiredUriExample)
  //TODO deconstruct and reconstruct to cope w/ spaceDelimited/pipeDelimited

  data.queryString = data.uriExample.substr(data.uriExample.indexOf('?'));
  //console.log("data.queryString ", data.queryString)
  if (!data.queryString.startsWith('?')) data.queryString = '';
  data.queryString = data.queryString.split('%25').join('%');
  data.requiredQueryString = data.requiredUriExample.substr(data.requiredUriExample.indexOf('?'));
  //console.log("data.requiredQueryString ", data.requiredQueryString)
  if (!data.requiredQueryString.startsWith('?')) data.requiredQueryString = '';
  data.requiredQueryString = data.requiredQueryString.split('%25').join('%');
  //console.log("data.requiredQueryString ", data.requiredQueryString)
}

module.exports = {
  getHeaders
};
