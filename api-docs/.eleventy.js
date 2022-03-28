'use strict';

const syntaxHighlight = require("@11ty/eleventy-plugin-syntaxhighlight");
const markdownIt = require('markdown-it');
const markdownItAnchor = require('markdown-it-anchor');
const tools = require('./lib/tools');

module.exports = function(eleventyConfig) {
  const src = process.env.SLATEDIR || 'eleventy';
  eleventyConfig.addPassthroughCopy(src+"/slate/css/*.css");
  eleventyConfig.addPassthroughCopy(src+"/slate/js");
  eleventyConfig.addPassthroughCopy(src+"/slate/img");
  eleventyConfig.addPassthroughCopy(src+"/slate/fonts");
  eleventyConfig.addPlugin(syntaxHighlight);
  eleventyConfig.setLibrary("md",
    markdownIt({
      html: true,
      linkify: true,
      typographer: true
    }).use(markdownItAnchor, {})
  );

  eleventyConfig.addFilter('relativeUrl', function (url) {
    //console.log("this.ctx ", this.ctx)
    return tools.makeRelative(url, this.ctx.page.url)
  })

  eleventyConfig.addShortcode("slateImage", function(url, alt) {
    //console.log("this ", this)
    let imgSrc = tools.makeRelative(`/slate/img/${url}`, this.page.url)
    return '<img src="'+imgSrc+'" alt="'+(alt||'Image')+'">';
    //return `<img src="${eleventyConfig.getFilter("url")(url)}" alt="${alt}">`;
  });

  eleventyConfig.addShortcode("logo", function(data) {
    //console.log("data ", data)
    let imgSrc = tools.makeRelative("/slate/img/logo-light.svg", this.page.url)
    return `<a href="https://9ci.com">
    <img src="${imgSrc}" alt="9ci logo" style="max-height: 100px; width: 100%;">
    </a>`;
  });

  return {
    dir: {
      input: src,
      output: "build/_site"
    }
  };
};
