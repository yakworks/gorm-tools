const tools = require('./tools');

/**
 * groups all the path's keys by the tags and returns the object of form
 * {tagKey: [{pathKey: ..., verb: ...}, etc..]}
 * @param {object} oapi - the full open api spec
 * @returns the object with the x-entity as keys
 */
function groupSchemasByXEntity(oapi){
  let schemas = tools.sortByKeys(oapi.components.schemas)
  let entitySchemas = {};
  const sKeys = Object.keys(schemas);

  sKeys.forEach((skey) => {
    let schema = schemas[skey]
    let entity = schema['x-entity']
    if (!entitySchemas[entity]) {
      entitySchemas[entity] = {};
    }
    entitySchemas[entity][skey] = schema
  });
  return entitySchemas
}

/**
 * build html for prop name with required
 */
function getPropName(propName, schemaProps) {
  let snippet = `<code class="property">${propName}</code>`
  if(isRequired(propName, schemaProps)) snippet += '<code class="required">required</code>'
  return snippet
}

/**
 * returns the type for display with constraints and enum details
 */
function getTypeWithMeta(prop, schemaPath) {
  if(!prop) return ''
  let snippet = `<code class="type">${getType(prop,schemaPath)}</code>`
  if(prop.default) snippet += ` | <code>default: ${prop.default}</code> `
  let constraints = getContraints(prop)
  if(constraints){
    snippet += constraints
  }
  //enums
  if(prop.enum){
    snippet += ' | enum: '
    for (let val of prop.enum) {
      snippet += `<code>${val}</code>, `
    }
  }
  return snippet
}

/**
 * gets the constraints in markdown format seperated by |
 *
 * @param {*} prop the property object
 * @returns
 */
function getContraints(prop) {
  if (!prop) return '';
  let validation = '';
  let valList = [
    'maxLength', 'minLength', 'pattern', 'multipleOf', 'minimum', 'maximum', 'minItems', 'maxItems'
  ]
  for (let valName of valList) {
    if(prop[valName]){
      validation = validation + `<code>${valName}: ${prop[valName]}</code> `
    }
  }
  //prefix with the | seperator
  if (validation !== '') validation = ' | ' + validation;
  return validation
}

/**
 * checks the required array for prop name
 */
function isRequired(propName, schema){
   return schema.required && schema.required.indexOf(propName) !== -1
}

/**
 * returns the type to display. If its a $ref or an array with $ref then
 * returns a markdown link to the schema anchor which will be relative to the rootPath prop
 * @param {object} prop - the prop object
 * @param {string} rootPath - the prop object
 */
function getType(prop, rootPath){
  //console.log("getType prop ", prop)
  let type = prop.type
  if(prop.$ref){
    type = 'object( ' + getSchemaLink(prop.$ref, rootPath) + ' )';
  } else if((prop.type === 'array') && prop.items && prop.items.$ref){
    type = 'array[ ' + getSchemaLink(prop.items.$ref, rootPath) + ' ]';
  } else {
    if (prop.format) type = type + '('+prop.format+')';
  }
  if (prop.nullable === true) {
    type += '¦null';
  }
  return type
}

/**
 * builds a markdown link for the schema $ref
 * @param {object} prop - the prop object
 * @param {string} rootPath - the prop object
 */
function getSchemaLink(refName, rootPath){
  let schemaName = refName.replace('#/components/schemas/','');
  rootPath = rootPath ? (rootPath + '/') : ''
  let href = rootPath + '#schema_' + schemaName.toLowerCase()
  return `<a href="${href}">${schemaName}</a>`
}

/**
 * string rep of read-only or write-only and future use for acl
 */
function getRestrictions(prop){
  if (prop.readOnly) return 'read-only';
  if (prop.writeOnly) return 'write-only';
}

function toPrimitive(v) {
  if (typeof v === 'object') { // including arrays
      return JSON.stringify(v);
  }
  return v;
}

module.exports = {
  groupSchemasByXEntity,
  getContraints,
  getType,
  isRequired,
  toPrimitive,
  getRestrictions,
  getTypeWithMeta,
  getPropName,
  getSchemaLink
}
