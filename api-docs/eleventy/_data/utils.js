'use strict';
const cheerio = require('cheerio');
const markdownItAnchor = require('markdown-it-anchor');
const markdownItPrism = require('markdown-it-prism');
const tools = require("../../lib/tools")

const markdown = require('markdown-it')({
  html: true,
  linkify: true,
  typographer: true
}).use(markdownItAnchor, {})
  .use(markdownItPrism)


function language_array(language_tabs) {
  let result = [];
  for (let lang in language_tabs) {
    if (typeof language_tabs[lang] === 'object') {
      result.push(Object.keys(language_tabs[lang])[0]);
    }
    else {
      result.push(language_tabs[lang]);
    }
  }
  return JSON.stringify(result).split('"').join('&quot;');
}

function lang_tag(lang) {
  let key = Object.keys(lang)[0]
  return `<a href="#" data-language-name="${key}">
    ${lang[key]}
    </a>`
}

function toc_data(content, headingLevel) {
  const $ = cheerio.load(content);
  const result = [];
  let h1,h2;
  headingLevel = (headingLevel || 2);
  $(':header').each(function(){
    const tag = $(this).get(0).tagName.toLowerCase();
    const entry = {class: ''};
    if (tag === 'h1') {
      let el = $(this)
      entry.id = el.attr('id');
      entry.title = el.text();
      if(el.hasClass('tag-group')) entry.class = 'tag-group'
      if(el.text() === 'Schemas') entry.class = 'tag-group'
      el.data('foo', 'bar');
      entry.op = el.data('op');
      entry.content = el.html();
      entry.children = [];
      h1 = entry;
      result.push(entry);
    }
    if (tag === 'h2') {
      let child = {};
      child.id = $(this).attr('id');
      entry.title = $(this).text();
      let op = $(this).attr('data-op');
      if(op === 'delete') op = 'del'
      child.op = op;
      child.content = $(this).html();
      child.children = [];
      h2 = child;
      if (h1) h1.children.push(child);
    }
    if ((headingLevel >= 3) && (tag === 'h3')) {
      let child = {};
      child.id = $(this).attr('id');
      entry.title = $(this).text();
      child.content = $(this).html();
      child.children = [];
      //h3 = child;
      if (h2) h2.children.push(child);
    }
  });
  return result;
}

function md(content) {
  if(!content) return ''
  return markdown.render(content);
}

function mdInline(content) {
  if(!content) return ''
  return markdown.renderInline(content);
}

function prettyJson(obj) {
  return tools.prettyJson(obj);
}

module.exports = {
  language_array,
  lang_tag,
  toc_data,
  md,
  mdInline,
  prettyJson,
};

