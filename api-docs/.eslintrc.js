module.exports = {
    extends: ["eslint:recommended", "prettier"],
    plugins: ["inclusive-language"],
    rules: {
        "inclusive-language/use-inclusive-words": "error"
    },
    parserOptions: {
        ecmaVersion: 2020,
        sourceType: "module"
    },
    env: {
        browser: true,
        node: true,
        es6: true
    },
    globals: {
      "_": false,
      "$": false,
      "jQuery": false
    },
    ignorePatterns: ["!/.*.js", "eleventy/slate/", "bak/", "_site", "/assets/js/", "node_modules"]
};
