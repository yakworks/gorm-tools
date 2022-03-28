const tools = require("../../lib/tools")

module.exports = {
  rootPath: function(data) {
    //console.log("rootPath data ", data)
    return tools.makeRelative('', data.page.url)
  }
}
