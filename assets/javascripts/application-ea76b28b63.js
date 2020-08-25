window["app"] =
/******/ (function(modules) { // webpackBootstrap
/******/ 	// The module cache
/******/ 	var installedModules = {};
/******/
/******/ 	// The require function
/******/ 	function __webpack_require__(moduleId) {
/******/
/******/ 		// Check if module is in cache
/******/ 		if(installedModules[moduleId]) {
/******/ 			return installedModules[moduleId].exports;
/******/ 		}
/******/ 		// Create a new module (and put it into the cache)
/******/ 		var module = installedModules[moduleId] = {
/******/ 			i: moduleId,
/******/ 			l: false,
/******/ 			exports: {}
/******/ 		};
/******/
/******/ 		// Execute the module function
/******/ 		modules[moduleId].call(module.exports, module, module.exports, __webpack_require__);
/******/
/******/ 		// Flag the module as loaded
/******/ 		module.l = true;
/******/
/******/ 		// Return the exports of the module
/******/ 		return module.exports;
/******/ 	}
/******/
/******/
/******/ 	// expose the modules object (__webpack_modules__)
/******/ 	__webpack_require__.m = modules;
/******/
/******/ 	// expose the module cache
/******/ 	__webpack_require__.c = installedModules;
/******/
/******/ 	// define getter function for harmony exports
/******/ 	__webpack_require__.d = function(exports, name, getter) {
/******/ 		if(!__webpack_require__.o(exports, name)) {
/******/ 			Object.defineProperty(exports, name, {
/******/ 				configurable: false,
/******/ 				enumerable: true,
/******/ 				get: getter
/******/ 			});
/******/ 		}
/******/ 	};
/******/
/******/ 	// getDefaultExport function for compatibility with non-harmony modules
/******/ 	__webpack_require__.n = function(module) {
/******/ 		var getter = module && module.__esModule ?
/******/ 			function getDefault() { return module['default']; } :
/******/ 			function getModuleExports() { return module; };
/******/ 		__webpack_require__.d(getter, 'a', getter);
/******/ 		return getter;
/******/ 	};
/******/
/******/ 	// Object.prototype.hasOwnProperty.call
/******/ 	__webpack_require__.o = function(object, property) { return Object.prototype.hasOwnProperty.call(object, property); };
/******/
/******/ 	// __webpack_public_path__
/******/ 	__webpack_require__.p = "";
/******/
/******/ 	// Load entry module and return exports
/******/ 	return __webpack_require__(__webpack_require__.s = 40);
/******/ })
/************************************************************************/
/******/ ([
/* 0 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var store = __webpack_require__(16)('wks');
var uid = __webpack_require__(18);
var _Symbol = __webpack_require__(1).Symbol;
var USE_SYMBOL = typeof _Symbol == 'function';

var $exports = module.exports = function (name) {
  return store[name] || (store[name] = USE_SYMBOL && _Symbol[name] || (USE_SYMBOL ? _Symbol : uid)('Symbol.' + name));
};

$exports.store = store;

/***/ }),
/* 1 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// https://github.com/zloirock/core-js/issues/86#issuecomment-115759028
var global = module.exports = typeof window != 'undefined' && window.Math == Math ? window : typeof self != 'undefined' && self.Math == Math ? self
// eslint-disable-next-line no-new-func
: Function('return this')();
if (typeof __g == 'number') __g = global; // eslint-disable-line no-undef

/***/ }),
/* 2 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var core = module.exports = { version: '2.6.11' };
if (typeof __e == 'number') __e = core; // eslint-disable-line no-undef

/***/ }),
/* 3 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var isObject = __webpack_require__(6);
module.exports = function (it) {
  if (!isObject(it)) throw TypeError(it + ' is not an object!');
  return it;
};

/***/ }),
/* 4 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var dP = __webpack_require__(10);
var createDesc = __webpack_require__(28);
module.exports = __webpack_require__(7) ? function (object, key, value) {
  return dP.f(object, key, createDesc(1, value));
} : function (object, key, value) {
  object[key] = value;
  return object;
};

/***/ }),
/* 5 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var global = __webpack_require__(1);
var hide = __webpack_require__(4);
var has = __webpack_require__(11);
var SRC = __webpack_require__(18)('src');
var $toString = __webpack_require__(45);
var TO_STRING = 'toString';
var TPL = ('' + $toString).split(TO_STRING);

__webpack_require__(2).inspectSource = function (it) {
  return $toString.call(it);
};

(module.exports = function (O, key, val, safe) {
  var isFunction = typeof val == 'function';
  if (isFunction) has(val, 'name') || hide(val, 'name', key);
  if (O[key] === val) return;
  if (isFunction) has(val, SRC) || hide(val, SRC, O[key] ? '' + O[key] : TPL.join(String(key)));
  if (O === global) {
    O[key] = val;
  } else if (!safe) {
    delete O[key];
    hide(O, key, val);
  } else if (O[key]) {
    O[key] = val;
  } else {
    hide(O, key, val);
  }
  // add fake Function#toString for correct work wrapped methods / constructors with methods like LoDash isNative
})(Function.prototype, TO_STRING, function toString() {
  return typeof this == 'function' && this[SRC] || $toString.call(this);
});

/***/ }),
/* 6 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

module.exports = function (it) {
  return (typeof it === 'undefined' ? 'undefined' : _typeof(it)) === 'object' ? it !== null : typeof it === 'function';
};

/***/ }),
/* 7 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// Thank's IE8 for his funny defineProperty
module.exports = !__webpack_require__(27)(function () {
  return Object.defineProperty({}, 'a', { get: function get() {
      return 7;
    } }).a != 7;
});

/***/ }),
/* 8 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = {};

/***/ }),
/* 9 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var toString = {}.toString;

module.exports = function (it) {
  return toString.call(it).slice(8, -1);
};

/***/ }),
/* 10 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var anObject = __webpack_require__(3);
var IE8_DOM_DEFINE = __webpack_require__(43);
var toPrimitive = __webpack_require__(44);
var dP = Object.defineProperty;

exports.f = __webpack_require__(7) ? Object.defineProperty : function defineProperty(O, P, Attributes) {
  anObject(O);
  P = toPrimitive(P, true);
  anObject(Attributes);
  if (IE8_DOM_DEFINE) try {
    return dP(O, P, Attributes);
  } catch (e) {/* empty */}
  if ('get' in Attributes || 'set' in Attributes) throw TypeError('Accessors not supported!');
  if ('value' in Attributes) O[P] = Attributes.value;
  return O;
};

/***/ }),
/* 11 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var hasOwnProperty = {}.hasOwnProperty;
module.exports = function (it, key) {
  return hasOwnProperty.call(it, key);
};

/***/ }),
/* 12 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var global = __webpack_require__(1);
var core = __webpack_require__(2);
var hide = __webpack_require__(4);
var redefine = __webpack_require__(5);
var ctx = __webpack_require__(13);
var PROTOTYPE = 'prototype';

var $export = function $export(type, name, source) {
  var IS_FORCED = type & $export.F;
  var IS_GLOBAL = type & $export.G;
  var IS_STATIC = type & $export.S;
  var IS_PROTO = type & $export.P;
  var IS_BIND = type & $export.B;
  var target = IS_GLOBAL ? global : IS_STATIC ? global[name] || (global[name] = {}) : (global[name] || {})[PROTOTYPE];
  var exports = IS_GLOBAL ? core : core[name] || (core[name] = {});
  var expProto = exports[PROTOTYPE] || (exports[PROTOTYPE] = {});
  var key, own, out, exp;
  if (IS_GLOBAL) source = name;
  for (key in source) {
    // contains in native
    own = !IS_FORCED && target && target[key] !== undefined;
    // export native or passed
    out = (own ? target : source)[key];
    // bind timers to global for call from export context
    exp = IS_BIND && own ? ctx(out, global) : IS_PROTO && typeof out == 'function' ? ctx(Function.call, out) : out;
    // extend global
    if (target) redefine(target, key, out, type & $export.U);
    // export
    if (exports[key] != out) hide(exports, key, exp);
    if (IS_PROTO && expProto[key] != out) expProto[key] = out;
  }
};
global.core = core;
// type bitmap
$export.F = 1; // forced
$export.G = 2; // global
$export.S = 4; // static
$export.P = 8; // proto
$export.B = 16; // bind
$export.W = 32; // wrap
$export.U = 64; // safe
$export.R = 128; // real proto method for `library`
module.exports = $export;

/***/ }),
/* 13 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// optional / simple context binding
var aFunction = __webpack_require__(14);
module.exports = function (fn, that, length) {
  aFunction(fn);
  if (that === undefined) return fn;
  switch (length) {
    case 1:
      return function (a) {
        return fn.call(that, a);
      };
    case 2:
      return function (a, b) {
        return fn.call(that, a, b);
      };
    case 3:
      return function (a, b, c) {
        return fn.call(that, a, b, c);
      };
  }
  return function () /* ...args */{
    return fn.apply(that, arguments);
  };
};

/***/ }),
/* 14 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (it) {
  if (typeof it != 'function') throw TypeError(it + ' is not a function!');
  return it;
};

/***/ }),
/* 15 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// getting tag from 19.1.3.6 Object.prototype.toString()
var cof = __webpack_require__(9);
var TAG = __webpack_require__(0)('toStringTag');
// ES3 wrong here
var ARG = cof(function () {
  return arguments;
}()) == 'Arguments';

// fallback for IE11 Script Access Denied error
var tryGet = function tryGet(it, key) {
  try {
    return it[key];
  } catch (e) {/* empty */}
};

module.exports = function (it) {
  var O, T, B;
  return it === undefined ? 'Undefined' : it === null ? 'Null'
  // @@toStringTag case
  : typeof (T = tryGet(O = Object(it), TAG)) == 'string' ? T
  // builtinTag case
  : ARG ? cof(O)
  // ES3 arguments fallback
  : (B = cof(O)) == 'Object' && typeof O.callee == 'function' ? 'Arguments' : B;
};

/***/ }),
/* 16 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var core = __webpack_require__(2);
var global = __webpack_require__(1);
var SHARED = '__core-js_shared__';
var store = global[SHARED] || (global[SHARED] = {});

(module.exports = function (key, value) {
  return store[key] || (store[key] = value !== undefined ? value : {});
})('versions', []).push({
  version: core.version,
  mode: __webpack_require__(17) ? 'pure' : 'global',
  copyright: '© 2019 Denis Pushkarev (zloirock.ru)'
});

/***/ }),
/* 17 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = false;

/***/ }),
/* 18 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var id = 0;
var px = Math.random();
module.exports = function (key) {
  return 'Symbol('.concat(key === undefined ? '' : key, ')_', (++id + px).toString(36));
};

/***/ }),
/* 19 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var isObject = __webpack_require__(6);
var document = __webpack_require__(1).document;
// typeof document.createElement is 'object' in old IE
var is = isObject(document) && isObject(document.createElement);
module.exports = function (it) {
  return is ? document.createElement(it) : {};
};

/***/ }),
/* 20 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 7.1.4 ToInteger
var ceil = Math.ceil;
var floor = Math.floor;
module.exports = function (it) {
  return isNaN(it = +it) ? 0 : (it > 0 ? floor : ceil)(it);
};

/***/ }),
/* 21 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 7.2.1 RequireObjectCoercible(argument)
module.exports = function (it) {
  if (it == undefined) throw TypeError("Can't call method on  " + it);
  return it;
};

/***/ }),
/* 22 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// to indexed object, toObject with fallback for non-array-like ES3 strings
var IObject = __webpack_require__(52);
var defined = __webpack_require__(21);
module.exports = function (it) {
  return IObject(defined(it));
};

/***/ }),
/* 23 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var shared = __webpack_require__(16)('keys');
var uid = __webpack_require__(18);
module.exports = function (key) {
  return shared[key] || (shared[key] = uid(key));
};

/***/ }),
/* 24 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var def = __webpack_require__(10).f;
var has = __webpack_require__(11);
var TAG = __webpack_require__(0)('toStringTag');

module.exports = function (it, tag, stat) {
  if (it && !has(it = stat ? it : it.prototype, TAG)) def(it, TAG, { configurable: true, value: tag });
};

/***/ }),
/* 25 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

// 25.4.1.5 NewPromiseCapability(C)

var aFunction = __webpack_require__(14);

function PromiseCapability(C) {
  var resolve, reject;
  this.promise = new C(function ($$resolve, $$reject) {
    if (resolve !== undefined || reject !== undefined) throw TypeError('Bad Promise constructor');
    resolve = $$resolve;
    reject = $$reject;
  });
  this.resolve = aFunction(resolve);
  this.reject = aFunction(reject);
}

module.exports.f = function (C) {
  return new PromiseCapability(C);
};

/***/ }),
/* 26 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


Object.defineProperty(exports, "__esModule", {
  value: true
});
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* eslint-disable no-underscore-dangle */
exports.default = /* JSX */{

  /**
   * Create a native DOM node from JSX's intermediate representation
   *
   * @param {string} tag - Tag name
   * @param {?Object} properties - Properties
   * @param {Array<string | number | { __html: string } | Array<HTMLElement>>}
   *   children - Child nodes
   * @return {HTMLElement} Native DOM node
   */
  createElement: function createElement(tag, properties) {
    var el = document.createElement(tag);

    /* Set all properties */
    if (properties) Array.prototype.forEach.call(Object.keys(properties), function (attr) {
      el.setAttribute(attr, properties[attr]);
    });

    /* Iterate child nodes */
    var iterateChildNodes = function iterateChildNodes(nodes) {
      Array.prototype.forEach.call(nodes, function (node) {

        /* Directly append text content */
        if (typeof node === "string" || typeof node === "number") {
          el.textContent += node;

          /* Recurse, if we got an array */
        } else if (Array.isArray(node)) {
          iterateChildNodes(node);

          /* Append raw HTML */
        } else if (typeof node.__html !== "undefined") {
          el.innerHTML += node.__html;

          /* Append regular nodes */
        } else if (node instanceof Node) {
          el.appendChild(node);
        }
      });
    };

    /* Iterate child nodes and return element */

    for (var _len = arguments.length, children = Array(_len > 2 ? _len - 2 : 0), _key = 2; _key < _len; _key++) {
      children[_key - 2] = arguments[_key];
    }

    iterateChildNodes(children);
    return el;
  }
};
module.exports = exports["default"];

/***/ }),
/* 27 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (exec) {
  try {
    return !!exec();
  } catch (e) {
    return true;
  }
};

/***/ }),
/* 28 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (bitmap, value) {
  return {
    enumerable: !(bitmap & 1),
    configurable: !(bitmap & 2),
    writable: !(bitmap & 4),
    value: value
  };
};

/***/ }),
/* 29 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var LIBRARY = __webpack_require__(17);
var $export = __webpack_require__(12);
var redefine = __webpack_require__(5);
var hide = __webpack_require__(4);
var Iterators = __webpack_require__(8);
var $iterCreate = __webpack_require__(48);
var setToStringTag = __webpack_require__(24);
var getPrototypeOf = __webpack_require__(55);
var ITERATOR = __webpack_require__(0)('iterator');
var BUGGY = !([].keys && 'next' in [].keys()); // Safari has buggy iterators w/o `next`
var FF_ITERATOR = '@@iterator';
var KEYS = 'keys';
var VALUES = 'values';

var returnThis = function returnThis() {
  return this;
};

module.exports = function (Base, NAME, Constructor, next, DEFAULT, IS_SET, FORCED) {
  $iterCreate(Constructor, NAME, next);
  var getMethod = function getMethod(kind) {
    if (!BUGGY && kind in proto) return proto[kind];
    switch (kind) {
      case KEYS:
        return function keys() {
          return new Constructor(this, kind);
        };
      case VALUES:
        return function values() {
          return new Constructor(this, kind);
        };
    }return function entries() {
      return new Constructor(this, kind);
    };
  };
  var TAG = NAME + ' Iterator';
  var DEF_VALUES = DEFAULT == VALUES;
  var VALUES_BUG = false;
  var proto = Base.prototype;
  var $native = proto[ITERATOR] || proto[FF_ITERATOR] || DEFAULT && proto[DEFAULT];
  var $default = $native || getMethod(DEFAULT);
  var $entries = DEFAULT ? !DEF_VALUES ? $default : getMethod('entries') : undefined;
  var $anyNative = NAME == 'Array' ? proto.entries || $native : $native;
  var methods, key, IteratorPrototype;
  // Fix native
  if ($anyNative) {
    IteratorPrototype = getPrototypeOf($anyNative.call(new Base()));
    if (IteratorPrototype !== Object.prototype && IteratorPrototype.next) {
      // Set @@toStringTag to native iterators
      setToStringTag(IteratorPrototype, TAG, true);
      // fix for some old engines
      if (!LIBRARY && typeof IteratorPrototype[ITERATOR] != 'function') hide(IteratorPrototype, ITERATOR, returnThis);
    }
  }
  // fix Array#{values, @@iterator}.name in V8 / FF
  if (DEF_VALUES && $native && $native.name !== VALUES) {
    VALUES_BUG = true;
    $default = function values() {
      return $native.call(this);
    };
  }
  // Define iterator
  if ((!LIBRARY || FORCED) && (BUGGY || VALUES_BUG || !proto[ITERATOR])) {
    hide(proto, ITERATOR, $default);
  }
  // Plug for library
  Iterators[NAME] = $default;
  Iterators[TAG] = returnThis;
  if (DEFAULT) {
    methods = {
      values: DEF_VALUES ? $default : getMethod(VALUES),
      keys: IS_SET ? $default : getMethod(KEYS),
      entries: $entries
    };
    if (FORCED) for (key in methods) {
      if (!(key in proto)) redefine(proto, key, methods[key]);
    } else $export($export.P + $export.F * (BUGGY || VALUES_BUG), NAME, methods);
  }
  return methods;
};

/***/ }),
/* 30 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 19.1.2.14 / 15.2.3.14 Object.keys(O)
var $keys = __webpack_require__(51);
var enumBugKeys = __webpack_require__(32);

module.exports = Object.keys || function keys(O) {
  return $keys(O, enumBugKeys);
};

/***/ }),
/* 31 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 7.1.15 ToLength
var toInteger = __webpack_require__(20);
var min = Math.min;
module.exports = function (it) {
  return it > 0 ? min(toInteger(it), 0x1fffffffffffff) : 0; // pow(2, 53) - 1 == 9007199254740991
};

/***/ }),
/* 32 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// IE 8- don't enum bug keys
module.exports = 'constructor,hasOwnProperty,isPrototypeOf,propertyIsEnumerable,toLocaleString,toString,valueOf'.split(',');

/***/ }),
/* 33 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var document = __webpack_require__(1).document;
module.exports = document && document.documentElement;

/***/ }),
/* 34 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 7.3.20 SpeciesConstructor(O, defaultConstructor)
var anObject = __webpack_require__(3);
var aFunction = __webpack_require__(14);
var SPECIES = __webpack_require__(0)('species');
module.exports = function (O, D) {
  var C = anObject(O).constructor;
  var S;
  return C === undefined || (S = anObject(C)[SPECIES]) == undefined ? D : aFunction(S);
};

/***/ }),
/* 35 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var ctx = __webpack_require__(13);
var invoke = __webpack_require__(67);
var html = __webpack_require__(33);
var cel = __webpack_require__(19);
var global = __webpack_require__(1);
var process = global.process;
var setTask = global.setImmediate;
var clearTask = global.clearImmediate;
var MessageChannel = global.MessageChannel;
var Dispatch = global.Dispatch;
var counter = 0;
var queue = {};
var ONREADYSTATECHANGE = 'onreadystatechange';
var defer, channel, port;
var run = function run() {
  var id = +this;
  // eslint-disable-next-line no-prototype-builtins
  if (queue.hasOwnProperty(id)) {
    var fn = queue[id];
    delete queue[id];
    fn();
  }
};
var listener = function listener(event) {
  run.call(event.data);
};
// Node.js 0.9+ & IE10+ has setImmediate, otherwise:
if (!setTask || !clearTask) {
  setTask = function setImmediate(fn) {
    var args = [];
    var i = 1;
    while (arguments.length > i) {
      args.push(arguments[i++]);
    }queue[++counter] = function () {
      // eslint-disable-next-line no-new-func
      invoke(typeof fn == 'function' ? fn : Function(fn), args);
    };
    defer(counter);
    return counter;
  };
  clearTask = function clearImmediate(id) {
    delete queue[id];
  };
  // Node.js 0.8-
  if (__webpack_require__(9)(process) == 'process') {
    defer = function defer(id) {
      process.nextTick(ctx(run, id, 1));
    };
    // Sphere (JS game engine) Dispatch API
  } else if (Dispatch && Dispatch.now) {
    defer = function defer(id) {
      Dispatch.now(ctx(run, id, 1));
    };
    // Browsers with MessageChannel, includes WebWorkers
  } else if (MessageChannel) {
    channel = new MessageChannel();
    port = channel.port2;
    channel.port1.onmessage = listener;
    defer = ctx(port.postMessage, port, 1);
    // Browsers with postMessage, skip WebWorkers
    // IE8 has postMessage, but it's sync & typeof its postMessage is 'object'
  } else if (global.addEventListener && typeof postMessage == 'function' && !global.importScripts) {
    defer = function defer(id) {
      global.postMessage(id + '', '*');
    };
    global.addEventListener('message', listener, false);
    // IE8-
  } else if (ONREADYSTATECHANGE in cel('script')) {
    defer = function defer(id) {
      html.appendChild(cel('script'))[ONREADYSTATECHANGE] = function () {
        html.removeChild(this);
        run.call(id);
      };
    };
    // Rest old browsers
  } else {
    defer = function defer(id) {
      setTimeout(ctx(run, id, 1), 0);
    };
  }
}
module.exports = {
  set: setTask,
  clear: clearTask
};

/***/ }),
/* 36 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (exec) {
  try {
    return { e: false, v: exec() };
  } catch (e) {
    return { e: true, v: e };
  }
};

/***/ }),
/* 37 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var anObject = __webpack_require__(3);
var isObject = __webpack_require__(6);
var newPromiseCapability = __webpack_require__(25);

module.exports = function (C, x) {
  anObject(C);
  if (isObject(x) && x.constructor === C) return x;
  var promiseCapability = newPromiseCapability.f(C);
  var resolve = promiseCapability.resolve;
  resolve(x);
  return promiseCapability.promise;
};

/***/ }),
/* 38 */
/***/ (function(module, exports) {

/* WEBPACK VAR INJECTION */(function(__webpack_amd_options__) {/* globals __webpack_amd_options__ */
module.exports = __webpack_amd_options__;

/* WEBPACK VAR INJECTION */}.call(exports, {}))

/***/ }),
/* 39 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Listener = function () {

  /**
   * Generic event listener
   *
   * @constructor
   *
   * @property {(Array<EventTarget>)} els_ - Event targets
   * @property {Object} handler_- Event handlers
   * @property {Array<string>} events_ - Event names
   * @property {Function} update_ - Update handler
   *
   * @param {?(string|EventTarget|NodeList<EventTarget>)} els -
   *   Selector or Event targets
   * @param {(string|Array<string>)} events - Event names
   * @param {(Object|Function)} handler - Handler to be invoked
   */
  function Listener(els, events, handler) {
    var _this = this;

    _classCallCheck(this, Listener);

    this.els_ = Array.prototype.slice.call(typeof els === "string" ? document.querySelectorAll(els) : [].concat(els));

    /* Set handler as function or directly as object */
    this.handler_ = typeof handler === "function" ? { update: handler } : handler;

    /* Initialize event names and update handler */
    this.events_ = [].concat(events);
    this.update_ = function (ev) {
      return _this.handler_.update(ev);
    };
  }

  /**
   * Register listener for all relevant events
   */


  _createClass(Listener, [{
    key: "listen",
    value: function listen() {
      var _this2 = this;

      this.els_.forEach(function (el) {
        _this2.events_.forEach(function (event) {
          el.addEventListener(event, _this2.update_, false);
        });
      });

      /* Execute setup handler, if implemented */
      if (typeof this.handler_.setup === "function") this.handler_.setup();
    }

    /**
     * Unregister listener for all relevant events
     */

  }, {
    key: "unlisten",
    value: function unlisten() {
      var _this3 = this;

      this.els_.forEach(function (el) {
        _this3.events_.forEach(function (event) {
          el.removeEventListener(event, _this3.update_);
        });
      });

      /* Execute reset handler, if implemented */
      if (typeof this.handler_.reset === "function") this.handler_.reset();
    }
  }]);

  return Listener;
}();

/* harmony default export */ __webpack_exports__["a"] = (Listener);

/***/ }),
/* 40 */
/***/ (function(module, exports, __webpack_require__) {

__webpack_require__(41);
__webpack_require__(75);
__webpack_require__(76);
module.exports = __webpack_require__(77);


/***/ }),
/* 41 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


__webpack_require__(42);
__webpack_require__(46);
__webpack_require__(57);
__webpack_require__(61);
__webpack_require__(73);
__webpack_require__(74);
module.exports = __webpack_require__(2).Promise;

/***/ }),
/* 42 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

// 19.1.3.6 Object.prototype.toString()

var classof = __webpack_require__(15);
var test = {};
test[__webpack_require__(0)('toStringTag')] = 'z';
if (test + '' != '[object z]') {
  __webpack_require__(5)(Object.prototype, 'toString', function toString() {
    return '[object ' + classof(this) + ']';
  }, true);
}

/***/ }),
/* 43 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = !__webpack_require__(7) && !__webpack_require__(27)(function () {
  return Object.defineProperty(__webpack_require__(19)('div'), 'a', { get: function get() {
      return 7;
    } }).a != 7;
});

/***/ }),
/* 44 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 7.1.1 ToPrimitive(input [, PreferredType])
var isObject = __webpack_require__(6);
// instead of the ES6 spec version, we didn't implement @@toPrimitive case
// and the second argument - flag - preferred type is a string
module.exports = function (it, S) {
  if (!isObject(it)) return it;
  var fn, val;
  if (S && typeof (fn = it.toString) == 'function' && !isObject(val = fn.call(it))) return val;
  if (typeof (fn = it.valueOf) == 'function' && !isObject(val = fn.call(it))) return val;
  if (!S && typeof (fn = it.toString) == 'function' && !isObject(val = fn.call(it))) return val;
  throw TypeError("Can't convert object to primitive value");
};

/***/ }),
/* 45 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = __webpack_require__(16)('native-function-to-string', Function.toString);

/***/ }),
/* 46 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var $at = __webpack_require__(47)(true);

// 21.1.3.27 String.prototype[@@iterator]()
__webpack_require__(29)(String, 'String', function (iterated) {
  this._t = String(iterated); // target
  this._i = 0; // next index
  // 21.1.5.2.1 %StringIteratorPrototype%.next()
}, function () {
  var O = this._t;
  var index = this._i;
  var point;
  if (index >= O.length) return { value: undefined, done: true };
  point = $at(O, index);
  this._i += point.length;
  return { value: point, done: false };
});

/***/ }),
/* 47 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var toInteger = __webpack_require__(20);
var defined = __webpack_require__(21);
// true  -> String#at
// false -> String#codePointAt
module.exports = function (TO_STRING) {
  return function (that, pos) {
    var s = String(defined(that));
    var i = toInteger(pos);
    var l = s.length;
    var a, b;
    if (i < 0 || i >= l) return TO_STRING ? '' : undefined;
    a = s.charCodeAt(i);
    return a < 0xd800 || a > 0xdbff || i + 1 === l || (b = s.charCodeAt(i + 1)) < 0xdc00 || b > 0xdfff ? TO_STRING ? s.charAt(i) : a : TO_STRING ? s.slice(i, i + 2) : (a - 0xd800 << 10) + (b - 0xdc00) + 0x10000;
  };
};

/***/ }),
/* 48 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var create = __webpack_require__(49);
var descriptor = __webpack_require__(28);
var setToStringTag = __webpack_require__(24);
var IteratorPrototype = {};

// 25.1.2.1.1 %IteratorPrototype%[@@iterator]()
__webpack_require__(4)(IteratorPrototype, __webpack_require__(0)('iterator'), function () {
  return this;
});

module.exports = function (Constructor, NAME, next) {
  Constructor.prototype = create(IteratorPrototype, { next: descriptor(1, next) });
  setToStringTag(Constructor, NAME + ' Iterator');
};

/***/ }),
/* 49 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 19.1.2.2 / 15.2.3.5 Object.create(O [, Properties])
var anObject = __webpack_require__(3);
var dPs = __webpack_require__(50);
var enumBugKeys = __webpack_require__(32);
var IE_PROTO = __webpack_require__(23)('IE_PROTO');
var Empty = function Empty() {/* empty */};
var PROTOTYPE = 'prototype';

// Create object with fake `null` prototype: use iframe Object with cleared prototype
var _createDict = function createDict() {
  // Thrash, waste and sodomy: IE GC bug
  var iframe = __webpack_require__(19)('iframe');
  var i = enumBugKeys.length;
  var lt = '<';
  var gt = '>';
  var iframeDocument;
  iframe.style.display = 'none';
  __webpack_require__(33).appendChild(iframe);
  iframe.src = 'javascript:'; // eslint-disable-line no-script-url
  // createDict = iframe.contentWindow.Object;
  // html.removeChild(iframe);
  iframeDocument = iframe.contentWindow.document;
  iframeDocument.open();
  iframeDocument.write(lt + 'script' + gt + 'document.F=Object' + lt + '/script' + gt);
  iframeDocument.close();
  _createDict = iframeDocument.F;
  while (i--) {
    delete _createDict[PROTOTYPE][enumBugKeys[i]];
  }return _createDict();
};

module.exports = Object.create || function create(O, Properties) {
  var result;
  if (O !== null) {
    Empty[PROTOTYPE] = anObject(O);
    result = new Empty();
    Empty[PROTOTYPE] = null;
    // add "__proto__" for Object.getPrototypeOf polyfill
    result[IE_PROTO] = O;
  } else result = _createDict();
  return Properties === undefined ? result : dPs(result, Properties);
};

/***/ }),
/* 50 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var dP = __webpack_require__(10);
var anObject = __webpack_require__(3);
var getKeys = __webpack_require__(30);

module.exports = __webpack_require__(7) ? Object.defineProperties : function defineProperties(O, Properties) {
  anObject(O);
  var keys = getKeys(Properties);
  var length = keys.length;
  var i = 0;
  var P;
  while (length > i) {
    dP.f(O, P = keys[i++], Properties[P]);
  }return O;
};

/***/ }),
/* 51 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var has = __webpack_require__(11);
var toIObject = __webpack_require__(22);
var arrayIndexOf = __webpack_require__(53)(false);
var IE_PROTO = __webpack_require__(23)('IE_PROTO');

module.exports = function (object, names) {
  var O = toIObject(object);
  var i = 0;
  var result = [];
  var key;
  for (key in O) {
    if (key != IE_PROTO) has(O, key) && result.push(key);
  } // Don't enum bug & hidden keys
  while (names.length > i) {
    if (has(O, key = names[i++])) {
      ~arrayIndexOf(result, key) || result.push(key);
    }
  }return result;
};

/***/ }),
/* 52 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// fallback for non-array-like ES3 and non-enumerable old V8 strings
var cof = __webpack_require__(9);
// eslint-disable-next-line no-prototype-builtins
module.exports = Object('z').propertyIsEnumerable(0) ? Object : function (it) {
  return cof(it) == 'String' ? it.split('') : Object(it);
};

/***/ }),
/* 53 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// false -> Array#indexOf
// true  -> Array#includes
var toIObject = __webpack_require__(22);
var toLength = __webpack_require__(31);
var toAbsoluteIndex = __webpack_require__(54);
module.exports = function (IS_INCLUDES) {
  return function ($this, el, fromIndex) {
    var O = toIObject($this);
    var length = toLength(O.length);
    var index = toAbsoluteIndex(fromIndex, length);
    var value;
    // Array#includes uses SameValueZero equality algorithm
    // eslint-disable-next-line no-self-compare
    if (IS_INCLUDES && el != el) while (length > index) {
      value = O[index++];
      // eslint-disable-next-line no-self-compare
      if (value != value) return true;
      // Array#indexOf ignores holes, Array#includes - not
    } else for (; length > index; index++) {
      if (IS_INCLUDES || index in O) {
        if (O[index] === el) return IS_INCLUDES || index || 0;
      }
    }return !IS_INCLUDES && -1;
  };
};

/***/ }),
/* 54 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var toInteger = __webpack_require__(20);
var max = Math.max;
var min = Math.min;
module.exports = function (index, length) {
  index = toInteger(index);
  return index < 0 ? max(index + length, 0) : min(index, length);
};

/***/ }),
/* 55 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 19.1.2.9 / 15.2.3.2 Object.getPrototypeOf(O)
var has = __webpack_require__(11);
var toObject = __webpack_require__(56);
var IE_PROTO = __webpack_require__(23)('IE_PROTO');
var ObjectProto = Object.prototype;

module.exports = Object.getPrototypeOf || function (O) {
  O = toObject(O);
  if (has(O, IE_PROTO)) return O[IE_PROTO];
  if (typeof O.constructor == 'function' && O instanceof O.constructor) {
    return O.constructor.prototype;
  }return O instanceof Object ? ObjectProto : null;
};

/***/ }),
/* 56 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 7.1.13 ToObject(argument)
var defined = __webpack_require__(21);
module.exports = function (it) {
  return Object(defined(it));
};

/***/ }),
/* 57 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var $iterators = __webpack_require__(58);
var getKeys = __webpack_require__(30);
var redefine = __webpack_require__(5);
var global = __webpack_require__(1);
var hide = __webpack_require__(4);
var Iterators = __webpack_require__(8);
var wks = __webpack_require__(0);
var ITERATOR = wks('iterator');
var TO_STRING_TAG = wks('toStringTag');
var ArrayValues = Iterators.Array;

var DOMIterables = {
  CSSRuleList: true, // TODO: Not spec compliant, should be false.
  CSSStyleDeclaration: false,
  CSSValueList: false,
  ClientRectList: false,
  DOMRectList: false,
  DOMStringList: false,
  DOMTokenList: true,
  DataTransferItemList: false,
  FileList: false,
  HTMLAllCollection: false,
  HTMLCollection: false,
  HTMLFormElement: false,
  HTMLSelectElement: false,
  MediaList: true, // TODO: Not spec compliant, should be false.
  MimeTypeArray: false,
  NamedNodeMap: false,
  NodeList: true,
  PaintRequestList: false,
  Plugin: false,
  PluginArray: false,
  SVGLengthList: false,
  SVGNumberList: false,
  SVGPathSegList: false,
  SVGPointList: false,
  SVGStringList: false,
  SVGTransformList: false,
  SourceBufferList: false,
  StyleSheetList: true, // TODO: Not spec compliant, should be false.
  TextTrackCueList: false,
  TextTrackList: false,
  TouchList: false
};

for (var collections = getKeys(DOMIterables), i = 0; i < collections.length; i++) {
  var NAME = collections[i];
  var explicit = DOMIterables[NAME];
  var Collection = global[NAME];
  var proto = Collection && Collection.prototype;
  var key;
  if (proto) {
    if (!proto[ITERATOR]) hide(proto, ITERATOR, ArrayValues);
    if (!proto[TO_STRING_TAG]) hide(proto, TO_STRING_TAG, NAME);
    Iterators[NAME] = ArrayValues;
    if (explicit) for (key in $iterators) {
      if (!proto[key]) redefine(proto, key, $iterators[key], true);
    }
  }
}

/***/ }),
/* 58 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var addToUnscopables = __webpack_require__(59);
var step = __webpack_require__(60);
var Iterators = __webpack_require__(8);
var toIObject = __webpack_require__(22);

// 22.1.3.4 Array.prototype.entries()
// 22.1.3.13 Array.prototype.keys()
// 22.1.3.29 Array.prototype.values()
// 22.1.3.30 Array.prototype[@@iterator]()
module.exports = __webpack_require__(29)(Array, 'Array', function (iterated, kind) {
  this._t = toIObject(iterated); // target
  this._i = 0; // next index
  this._k = kind; // kind
  // 22.1.5.2.1 %ArrayIteratorPrototype%.next()
}, function () {
  var O = this._t;
  var kind = this._k;
  var index = this._i++;
  if (!O || index >= O.length) {
    this._t = undefined;
    return step(1);
  }
  if (kind == 'keys') return step(0, index);
  if (kind == 'values') return step(0, O[index]);
  return step(0, [index, O[index]]);
}, 'values');

// argumentsList[@@iterator] is %ArrayProto_values% (9.4.4.6, 9.4.4.7)
Iterators.Arguments = Iterators.Array;

addToUnscopables('keys');
addToUnscopables('values');
addToUnscopables('entries');

/***/ }),
/* 59 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// 22.1.3.31 Array.prototype[@@unscopables]
var UNSCOPABLES = __webpack_require__(0)('unscopables');
var ArrayProto = Array.prototype;
if (ArrayProto[UNSCOPABLES] == undefined) __webpack_require__(4)(ArrayProto, UNSCOPABLES, {});
module.exports = function (key) {
  ArrayProto[UNSCOPABLES][key] = true;
};

/***/ }),
/* 60 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (done, value) {
  return { value: value, done: !!done };
};

/***/ }),
/* 61 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var LIBRARY = __webpack_require__(17);
var global = __webpack_require__(1);
var ctx = __webpack_require__(13);
var classof = __webpack_require__(15);
var $export = __webpack_require__(12);
var isObject = __webpack_require__(6);
var aFunction = __webpack_require__(14);
var anInstance = __webpack_require__(62);
var forOf = __webpack_require__(63);
var speciesConstructor = __webpack_require__(34);
var task = __webpack_require__(35).set;
var microtask = __webpack_require__(68)();
var newPromiseCapabilityModule = __webpack_require__(25);
var perform = __webpack_require__(36);
var userAgent = __webpack_require__(69);
var promiseResolve = __webpack_require__(37);
var PROMISE = 'Promise';
var TypeError = global.TypeError;
var process = global.process;
var versions = process && process.versions;
var v8 = versions && versions.v8 || '';
var $Promise = global[PROMISE];
var isNode = classof(process) == 'process';
var empty = function empty() {/* empty */};
var Internal, newGenericPromiseCapability, OwnPromiseCapability, Wrapper;
var newPromiseCapability = newGenericPromiseCapability = newPromiseCapabilityModule.f;

var USE_NATIVE = !!function () {
  try {
    // correct subclassing with @@species support
    var promise = $Promise.resolve(1);
    var FakePromise = (promise.constructor = {})[__webpack_require__(0)('species')] = function (exec) {
      exec(empty, empty);
    };
    // unhandled rejections tracking support, NodeJS Promise without it fails @@species test
    return (isNode || typeof PromiseRejectionEvent == 'function') && promise.then(empty) instanceof FakePromise
    // v8 6.6 (Node 10 and Chrome 66) have a bug with resolving custom thenables
    // https://bugs.chromium.org/p/chromium/issues/detail?id=830565
    // we can't detect it synchronously, so just check versions
    && v8.indexOf('6.6') !== 0 && userAgent.indexOf('Chrome/66') === -1;
  } catch (e) {/* empty */}
}();

// helpers
var isThenable = function isThenable(it) {
  var then;
  return isObject(it) && typeof (then = it.then) == 'function' ? then : false;
};
var notify = function notify(promise, isReject) {
  if (promise._n) return;
  promise._n = true;
  var chain = promise._c;
  microtask(function () {
    var value = promise._v;
    var ok = promise._s == 1;
    var i = 0;
    var run = function run(reaction) {
      var handler = ok ? reaction.ok : reaction.fail;
      var resolve = reaction.resolve;
      var reject = reaction.reject;
      var domain = reaction.domain;
      var result, then, exited;
      try {
        if (handler) {
          if (!ok) {
            if (promise._h == 2) onHandleUnhandled(promise);
            promise._h = 1;
          }
          if (handler === true) result = value;else {
            if (domain) domain.enter();
            result = handler(value); // may throw
            if (domain) {
              domain.exit();
              exited = true;
            }
          }
          if (result === reaction.promise) {
            reject(TypeError('Promise-chain cycle'));
          } else if (then = isThenable(result)) {
            then.call(result, resolve, reject);
          } else resolve(result);
        } else reject(value);
      } catch (e) {
        if (domain && !exited) domain.exit();
        reject(e);
      }
    };
    while (chain.length > i) {
      run(chain[i++]);
    } // variable length - can't use forEach
    promise._c = [];
    promise._n = false;
    if (isReject && !promise._h) onUnhandled(promise);
  });
};
var onUnhandled = function onUnhandled(promise) {
  task.call(global, function () {
    var value = promise._v;
    var unhandled = isUnhandled(promise);
    var result, handler, console;
    if (unhandled) {
      result = perform(function () {
        if (isNode) {
          process.emit('unhandledRejection', value, promise);
        } else if (handler = global.onunhandledrejection) {
          handler({ promise: promise, reason: value });
        } else if ((console = global.console) && console.error) {
          console.error('Unhandled promise rejection', value);
        }
      });
      // Browsers should not trigger `rejectionHandled` event if it was handled here, NodeJS - should
      promise._h = isNode || isUnhandled(promise) ? 2 : 1;
    }promise._a = undefined;
    if (unhandled && result.e) throw result.v;
  });
};
var isUnhandled = function isUnhandled(promise) {
  return promise._h !== 1 && (promise._a || promise._c).length === 0;
};
var onHandleUnhandled = function onHandleUnhandled(promise) {
  task.call(global, function () {
    var handler;
    if (isNode) {
      process.emit('rejectionHandled', promise);
    } else if (handler = global.onrejectionhandled) {
      handler({ promise: promise, reason: promise._v });
    }
  });
};
var $reject = function $reject(value) {
  var promise = this;
  if (promise._d) return;
  promise._d = true;
  promise = promise._w || promise; // unwrap
  promise._v = value;
  promise._s = 2;
  if (!promise._a) promise._a = promise._c.slice();
  notify(promise, true);
};
var $resolve = function $resolve(value) {
  var promise = this;
  var then;
  if (promise._d) return;
  promise._d = true;
  promise = promise._w || promise; // unwrap
  try {
    if (promise === value) throw TypeError("Promise can't be resolved itself");
    if (then = isThenable(value)) {
      microtask(function () {
        var wrapper = { _w: promise, _d: false }; // wrap
        try {
          then.call(value, ctx($resolve, wrapper, 1), ctx($reject, wrapper, 1));
        } catch (e) {
          $reject.call(wrapper, e);
        }
      });
    } else {
      promise._v = value;
      promise._s = 1;
      notify(promise, false);
    }
  } catch (e) {
    $reject.call({ _w: promise, _d: false }, e); // wrap
  }
};

// constructor polyfill
if (!USE_NATIVE) {
  // 25.4.3.1 Promise(executor)
  $Promise = function Promise(executor) {
    anInstance(this, $Promise, PROMISE, '_h');
    aFunction(executor);
    Internal.call(this);
    try {
      executor(ctx($resolve, this, 1), ctx($reject, this, 1));
    } catch (err) {
      $reject.call(this, err);
    }
  };
  // eslint-disable-next-line no-unused-vars
  Internal = function Promise(executor) {
    this._c = []; // <- awaiting reactions
    this._a = undefined; // <- checked in isUnhandled reactions
    this._s = 0; // <- state
    this._d = false; // <- done
    this._v = undefined; // <- value
    this._h = 0; // <- rejection state, 0 - default, 1 - handled, 2 - unhandled
    this._n = false; // <- notify
  };
  Internal.prototype = __webpack_require__(70)($Promise.prototype, {
    // 25.4.5.3 Promise.prototype.then(onFulfilled, onRejected)
    then: function then(onFulfilled, onRejected) {
      var reaction = newPromiseCapability(speciesConstructor(this, $Promise));
      reaction.ok = typeof onFulfilled == 'function' ? onFulfilled : true;
      reaction.fail = typeof onRejected == 'function' && onRejected;
      reaction.domain = isNode ? process.domain : undefined;
      this._c.push(reaction);
      if (this._a) this._a.push(reaction);
      if (this._s) notify(this, false);
      return reaction.promise;
    },
    // 25.4.5.1 Promise.prototype.catch(onRejected)
    'catch': function _catch(onRejected) {
      return this.then(undefined, onRejected);
    }
  });
  OwnPromiseCapability = function OwnPromiseCapability() {
    var promise = new Internal();
    this.promise = promise;
    this.resolve = ctx($resolve, promise, 1);
    this.reject = ctx($reject, promise, 1);
  };
  newPromiseCapabilityModule.f = newPromiseCapability = function newPromiseCapability(C) {
    return C === $Promise || C === Wrapper ? new OwnPromiseCapability(C) : newGenericPromiseCapability(C);
  };
}

$export($export.G + $export.W + $export.F * !USE_NATIVE, { Promise: $Promise });
__webpack_require__(24)($Promise, PROMISE);
__webpack_require__(71)(PROMISE);
Wrapper = __webpack_require__(2)[PROMISE];

// statics
$export($export.S + $export.F * !USE_NATIVE, PROMISE, {
  // 25.4.4.5 Promise.reject(r)
  reject: function reject(r) {
    var capability = newPromiseCapability(this);
    var $$reject = capability.reject;
    $$reject(r);
    return capability.promise;
  }
});
$export($export.S + $export.F * (LIBRARY || !USE_NATIVE), PROMISE, {
  // 25.4.4.6 Promise.resolve(x)
  resolve: function resolve(x) {
    return promiseResolve(LIBRARY && this === Wrapper ? $Promise : this, x);
  }
});
$export($export.S + $export.F * !(USE_NATIVE && __webpack_require__(72)(function (iter) {
  $Promise.all(iter)['catch'](empty);
})), PROMISE, {
  // 25.4.4.1 Promise.all(iterable)
  all: function all(iterable) {
    var C = this;
    var capability = newPromiseCapability(C);
    var resolve = capability.resolve;
    var reject = capability.reject;
    var result = perform(function () {
      var values = [];
      var index = 0;
      var remaining = 1;
      forOf(iterable, false, function (promise) {
        var $index = index++;
        var alreadyCalled = false;
        values.push(undefined);
        remaining++;
        C.resolve(promise).then(function (value) {
          if (alreadyCalled) return;
          alreadyCalled = true;
          values[$index] = value;
          --remaining || resolve(values);
        }, reject);
      });
      --remaining || resolve(values);
    });
    if (result.e) reject(result.v);
    return capability.promise;
  },
  // 25.4.4.4 Promise.race(iterable)
  race: function race(iterable) {
    var C = this;
    var capability = newPromiseCapability(C);
    var reject = capability.reject;
    var result = perform(function () {
      forOf(iterable, false, function (promise) {
        C.resolve(promise).then(capability.resolve, reject);
      });
    });
    if (result.e) reject(result.v);
    return capability.promise;
  }
});

/***/ }),
/* 62 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


module.exports = function (it, Constructor, name, forbiddenField) {
  if (!(it instanceof Constructor) || forbiddenField !== undefined && forbiddenField in it) {
    throw TypeError(name + ': incorrect invocation!');
  }return it;
};

/***/ }),
/* 63 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var ctx = __webpack_require__(13);
var call = __webpack_require__(64);
var isArrayIter = __webpack_require__(65);
var anObject = __webpack_require__(3);
var toLength = __webpack_require__(31);
var getIterFn = __webpack_require__(66);
var BREAK = {};
var RETURN = {};
var _exports = module.exports = function (iterable, entries, fn, that, ITERATOR) {
  var iterFn = ITERATOR ? function () {
    return iterable;
  } : getIterFn(iterable);
  var f = ctx(fn, that, entries ? 2 : 1);
  var index = 0;
  var length, step, iterator, result;
  if (typeof iterFn != 'function') throw TypeError(iterable + ' is not iterable!');
  // fast case for arrays with default iterator
  if (isArrayIter(iterFn)) for (length = toLength(iterable.length); length > index; index++) {
    result = entries ? f(anObject(step = iterable[index])[0], step[1]) : f(iterable[index]);
    if (result === BREAK || result === RETURN) return result;
  } else for (iterator = iterFn.call(iterable); !(step = iterator.next()).done;) {
    result = call(iterator, f, step.value, entries);
    if (result === BREAK || result === RETURN) return result;
  }
};
_exports.BREAK = BREAK;
_exports.RETURN = RETURN;

/***/ }),
/* 64 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// call something on iterator step with safe closing on error
var anObject = __webpack_require__(3);
module.exports = function (iterator, fn, value, entries) {
  try {
    return entries ? fn(anObject(value)[0], value[1]) : fn(value);
    // 7.4.6 IteratorClose(iterator, completion)
  } catch (e) {
    var ret = iterator['return'];
    if (ret !== undefined) anObject(ret.call(iterator));
    throw e;
  }
};

/***/ }),
/* 65 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// check on default Array iterator
var Iterators = __webpack_require__(8);
var ITERATOR = __webpack_require__(0)('iterator');
var ArrayProto = Array.prototype;

module.exports = function (it) {
  return it !== undefined && (Iterators.Array === it || ArrayProto[ITERATOR] === it);
};

/***/ }),
/* 66 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var classof = __webpack_require__(15);
var ITERATOR = __webpack_require__(0)('iterator');
var Iterators = __webpack_require__(8);
module.exports = __webpack_require__(2).getIteratorMethod = function (it) {
  if (it != undefined) return it[ITERATOR] || it['@@iterator'] || Iterators[classof(it)];
};

/***/ }),
/* 67 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// fast apply, http://jsperf.lnkit.com/fast-apply/5
module.exports = function (fn, args, that) {
                  var un = that === undefined;
                  switch (args.length) {
                                    case 0:
                                                      return un ? fn() : fn.call(that);
                                    case 1:
                                                      return un ? fn(args[0]) : fn.call(that, args[0]);
                                    case 2:
                                                      return un ? fn(args[0], args[1]) : fn.call(that, args[0], args[1]);
                                    case 3:
                                                      return un ? fn(args[0], args[1], args[2]) : fn.call(that, args[0], args[1], args[2]);
                                    case 4:
                                                      return un ? fn(args[0], args[1], args[2], args[3]) : fn.call(that, args[0], args[1], args[2], args[3]);
                  }return fn.apply(that, args);
};

/***/ }),
/* 68 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var global = __webpack_require__(1);
var macrotask = __webpack_require__(35).set;
var Observer = global.MutationObserver || global.WebKitMutationObserver;
var process = global.process;
var Promise = global.Promise;
var isNode = __webpack_require__(9)(process) == 'process';

module.exports = function () {
  var head, last, notify;

  var flush = function flush() {
    var parent, fn;
    if (isNode && (parent = process.domain)) parent.exit();
    while (head) {
      fn = head.fn;
      head = head.next;
      try {
        fn();
      } catch (e) {
        if (head) notify();else last = undefined;
        throw e;
      }
    }last = undefined;
    if (parent) parent.enter();
  };

  // Node.js
  if (isNode) {
    notify = function notify() {
      process.nextTick(flush);
    };
    // browsers with MutationObserver, except iOS Safari - https://github.com/zloirock/core-js/issues/339
  } else if (Observer && !(global.navigator && global.navigator.standalone)) {
    var toggle = true;
    var node = document.createTextNode('');
    new Observer(flush).observe(node, { characterData: true }); // eslint-disable-line no-new
    notify = function notify() {
      node.data = toggle = !toggle;
    };
    // environments with maybe non-completely correct, but existent Promise
  } else if (Promise && Promise.resolve) {
    // Promise.resolve without an argument throws an error in LG WebOS 2
    var promise = Promise.resolve(undefined);
    notify = function notify() {
      promise.then(flush);
    };
    // for other environments - macrotask based on:
    // - setImmediate
    // - MessageChannel
    // - window.postMessag
    // - onreadystatechange
    // - setTimeout
  } else {
    notify = function notify() {
      // strange IE + webpack dev server bug - use .call(global)
      macrotask.call(global, flush);
    };
  }

  return function (fn) {
    var task = { fn: fn, next: undefined };
    if (last) last.next = task;
    if (!head) {
      head = task;
      notify();
    }last = task;
  };
};

/***/ }),
/* 69 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var global = __webpack_require__(1);
var navigator = global.navigator;

module.exports = navigator && navigator.userAgent || '';

/***/ }),
/* 70 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var redefine = __webpack_require__(5);
module.exports = function (target, src, safe) {
  for (var key in src) {
    redefine(target, key, src[key], safe);
  }return target;
};

/***/ }),
/* 71 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var global = __webpack_require__(1);
var dP = __webpack_require__(10);
var DESCRIPTORS = __webpack_require__(7);
var SPECIES = __webpack_require__(0)('species');

module.exports = function (KEY) {
  var C = global[KEY];
  if (DESCRIPTORS && C && !C[SPECIES]) dP.f(C, SPECIES, {
    configurable: true,
    get: function get() {
      return this;
    }
  });
};

/***/ }),
/* 72 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var ITERATOR = __webpack_require__(0)('iterator');
var SAFE_CLOSING = false;

try {
  var riter = [7][ITERATOR]();
  riter['return'] = function () {
    SAFE_CLOSING = true;
  };
  // eslint-disable-next-line no-throw-literal
  Array.from(riter, function () {
    throw 2;
  });
} catch (e) {/* empty */}

module.exports = function (exec, skipClosing) {
  if (!skipClosing && !SAFE_CLOSING) return false;
  var safe = false;
  try {
    var arr = [7];
    var iter = arr[ITERATOR]();
    iter.next = function () {
      return { done: safe = true };
    };
    arr[ITERATOR] = function () {
      return iter;
    };
    exec(arr);
  } catch (e) {/* empty */}
  return safe;
};

/***/ }),
/* 73 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
// https://github.com/tc39/proposal-promise-finally


var $export = __webpack_require__(12);
var core = __webpack_require__(2);
var global = __webpack_require__(1);
var speciesConstructor = __webpack_require__(34);
var promiseResolve = __webpack_require__(37);

$export($export.P + $export.R, 'Promise', { 'finally': function _finally(onFinally) {
    var C = speciesConstructor(this, core.Promise || global.Promise);
    var isFunction = typeof onFinally == 'function';
    return this.then(isFunction ? function (x) {
      return promiseResolve(C, onFinally()).then(function () {
        return x;
      });
    } : onFinally, isFunction ? function (e) {
      return promiseResolve(C, onFinally()).then(function () {
        throw e;
      });
    } : onFinally);
  } });

/***/ }),
/* 74 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";

// https://github.com/tc39/proposal-promise-try

var $export = __webpack_require__(12);
var newPromiseCapability = __webpack_require__(25);
var perform = __webpack_require__(36);

$export($export.S, 'Promise', { 'try': function _try(callbackfn) {
    var promiseCapability = newPromiseCapability.f(this);
    var result = perform(callbackfn);
    (result.e ? promiseCapability.reject : promiseCapability.resolve)(result.v);
    return promiseCapability.promise;
  } });

/***/ }),
/* 75 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


// Polyfill for creating CustomEvents on IE9/10/11

// code pulled from:
// https://github.com/d4tocchini/customevent-polyfill
// https://developer.mozilla.org/en-US/docs/Web/API/CustomEvent#Polyfill

try {
  var ce = new window.CustomEvent('test');
  ce.preventDefault();
  if (ce.defaultPrevented !== true) {
    // IE has problems with .preventDefault() on custom events
    // http://stackoverflow.com/questions/23349191
    throw new Error('Could not prevent default');
  }
} catch (e) {
  var CustomEvent = function CustomEvent(event, params) {
    var evt, origPrevent;
    params = params || {
      bubbles: false,
      cancelable: false,
      detail: undefined
    };

    evt = document.createEvent("CustomEvent");
    evt.initCustomEvent(event, params.bubbles, params.cancelable, params.detail);
    origPrevent = evt.preventDefault;
    evt.preventDefault = function () {
      origPrevent.call(this);
      try {
        Object.defineProperty(this, 'defaultPrevented', {
          get: function get() {
            return true;
          }
        });
      } catch (e) {
        this.defaultPrevented = true;
      }
    };
    return evt;
  };

  CustomEvent.prototype = window.Event.prototype;
  window.CustomEvent = CustomEvent; // expose definition to window
}

/***/ }),
/* 76 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


(function (self) {
  'use strict';

  if (self.fetch) {
    return;
  }

  var support = {
    searchParams: 'URLSearchParams' in self,
    iterable: 'Symbol' in self && 'iterator' in Symbol,
    blob: 'FileReader' in self && 'Blob' in self && function () {
      try {
        new Blob();
        return true;
      } catch (e) {
        return false;
      }
    }(),
    formData: 'FormData' in self,
    arrayBuffer: 'ArrayBuffer' in self
  };

  if (support.arrayBuffer) {
    var viewClasses = ['[object Int8Array]', '[object Uint8Array]', '[object Uint8ClampedArray]', '[object Int16Array]', '[object Uint16Array]', '[object Int32Array]', '[object Uint32Array]', '[object Float32Array]', '[object Float64Array]'];

    var isDataView = function isDataView(obj) {
      return obj && DataView.prototype.isPrototypeOf(obj);
    };

    var isArrayBufferView = ArrayBuffer.isView || function (obj) {
      return obj && viewClasses.indexOf(Object.prototype.toString.call(obj)) > -1;
    };
  }

  function normalizeName(name) {
    if (typeof name !== 'string') {
      name = String(name);
    }
    if (/[^a-z0-9\-#$%&'*+.\^_`|~]/i.test(name)) {
      throw new TypeError('Invalid character in header field name');
    }
    return name.toLowerCase();
  }

  function normalizeValue(value) {
    if (typeof value !== 'string') {
      value = String(value);
    }
    return value;
  }

  // Build a destructive iterator for the value list
  function iteratorFor(items) {
    var iterator = {
      next: function next() {
        var value = items.shift();
        return { done: value === undefined, value: value };
      }
    };

    if (support.iterable) {
      iterator[Symbol.iterator] = function () {
        return iterator;
      };
    }

    return iterator;
  }

  function Headers(headers) {
    this.map = {};

    if (headers instanceof Headers) {
      headers.forEach(function (value, name) {
        this.append(name, value);
      }, this);
    } else if (Array.isArray(headers)) {
      headers.forEach(function (header) {
        this.append(header[0], header[1]);
      }, this);
    } else if (headers) {
      Object.getOwnPropertyNames(headers).forEach(function (name) {
        this.append(name, headers[name]);
      }, this);
    }
  }

  Headers.prototype.append = function (name, value) {
    name = normalizeName(name);
    value = normalizeValue(value);
    var oldValue = this.map[name];
    this.map[name] = oldValue ? oldValue + ',' + value : value;
  };

  Headers.prototype['delete'] = function (name) {
    delete this.map[normalizeName(name)];
  };

  Headers.prototype.get = function (name) {
    name = normalizeName(name);
    return this.has(name) ? this.map[name] : null;
  };

  Headers.prototype.has = function (name) {
    return this.map.hasOwnProperty(normalizeName(name));
  };

  Headers.prototype.set = function (name, value) {
    this.map[normalizeName(name)] = normalizeValue(value);
  };

  Headers.prototype.forEach = function (callback, thisArg) {
    for (var name in this.map) {
      if (this.map.hasOwnProperty(name)) {
        callback.call(thisArg, this.map[name], name, this);
      }
    }
  };

  Headers.prototype.keys = function () {
    var items = [];
    this.forEach(function (value, name) {
      items.push(name);
    });
    return iteratorFor(items);
  };

  Headers.prototype.values = function () {
    var items = [];
    this.forEach(function (value) {
      items.push(value);
    });
    return iteratorFor(items);
  };

  Headers.prototype.entries = function () {
    var items = [];
    this.forEach(function (value, name) {
      items.push([name, value]);
    });
    return iteratorFor(items);
  };

  if (support.iterable) {
    Headers.prototype[Symbol.iterator] = Headers.prototype.entries;
  }

  function consumed(body) {
    if (body.bodyUsed) {
      return Promise.reject(new TypeError('Already read'));
    }
    body.bodyUsed = true;
  }

  function fileReaderReady(reader) {
    return new Promise(function (resolve, reject) {
      reader.onload = function () {
        resolve(reader.result);
      };
      reader.onerror = function () {
        reject(reader.error);
      };
    });
  }

  function readBlobAsArrayBuffer(blob) {
    var reader = new FileReader();
    var promise = fileReaderReady(reader);
    reader.readAsArrayBuffer(blob);
    return promise;
  }

  function readBlobAsText(blob) {
    var reader = new FileReader();
    var promise = fileReaderReady(reader);
    reader.readAsText(blob);
    return promise;
  }

  function readArrayBufferAsText(buf) {
    var view = new Uint8Array(buf);
    var chars = new Array(view.length);

    for (var i = 0; i < view.length; i++) {
      chars[i] = String.fromCharCode(view[i]);
    }
    return chars.join('');
  }

  function bufferClone(buf) {
    if (buf.slice) {
      return buf.slice(0);
    } else {
      var view = new Uint8Array(buf.byteLength);
      view.set(new Uint8Array(buf));
      return view.buffer;
    }
  }

  function Body() {
    this.bodyUsed = false;

    this._initBody = function (body) {
      this._bodyInit = body;
      if (!body) {
        this._bodyText = '';
      } else if (typeof body === 'string') {
        this._bodyText = body;
      } else if (support.blob && Blob.prototype.isPrototypeOf(body)) {
        this._bodyBlob = body;
      } else if (support.formData && FormData.prototype.isPrototypeOf(body)) {
        this._bodyFormData = body;
      } else if (support.searchParams && URLSearchParams.prototype.isPrototypeOf(body)) {
        this._bodyText = body.toString();
      } else if (support.arrayBuffer && support.blob && isDataView(body)) {
        this._bodyArrayBuffer = bufferClone(body.buffer);
        // IE 10-11 can't handle a DataView body.
        this._bodyInit = new Blob([this._bodyArrayBuffer]);
      } else if (support.arrayBuffer && (ArrayBuffer.prototype.isPrototypeOf(body) || isArrayBufferView(body))) {
        this._bodyArrayBuffer = bufferClone(body);
      } else {
        throw new Error('unsupported BodyInit type');
      }

      if (!this.headers.get('content-type')) {
        if (typeof body === 'string') {
          this.headers.set('content-type', 'text/plain;charset=UTF-8');
        } else if (this._bodyBlob && this._bodyBlob.type) {
          this.headers.set('content-type', this._bodyBlob.type);
        } else if (support.searchParams && URLSearchParams.prototype.isPrototypeOf(body)) {
          this.headers.set('content-type', 'application/x-www-form-urlencoded;charset=UTF-8');
        }
      }
    };

    if (support.blob) {
      this.blob = function () {
        var rejected = consumed(this);
        if (rejected) {
          return rejected;
        }

        if (this._bodyBlob) {
          return Promise.resolve(this._bodyBlob);
        } else if (this._bodyArrayBuffer) {
          return Promise.resolve(new Blob([this._bodyArrayBuffer]));
        } else if (this._bodyFormData) {
          throw new Error('could not read FormData body as blob');
        } else {
          return Promise.resolve(new Blob([this._bodyText]));
        }
      };

      this.arrayBuffer = function () {
        if (this._bodyArrayBuffer) {
          return consumed(this) || Promise.resolve(this._bodyArrayBuffer);
        } else {
          return this.blob().then(readBlobAsArrayBuffer);
        }
      };
    }

    this.text = function () {
      var rejected = consumed(this);
      if (rejected) {
        return rejected;
      }

      if (this._bodyBlob) {
        return readBlobAsText(this._bodyBlob);
      } else if (this._bodyArrayBuffer) {
        return Promise.resolve(readArrayBufferAsText(this._bodyArrayBuffer));
      } else if (this._bodyFormData) {
        throw new Error('could not read FormData body as text');
      } else {
        return Promise.resolve(this._bodyText);
      }
    };

    if (support.formData) {
      this.formData = function () {
        return this.text().then(decode);
      };
    }

    this.json = function () {
      return this.text().then(JSON.parse);
    };

    return this;
  }

  // HTTP methods whose capitalization should be normalized
  var methods = ['DELETE', 'GET', 'HEAD', 'OPTIONS', 'POST', 'PUT'];

  function normalizeMethod(method) {
    var upcased = method.toUpperCase();
    return methods.indexOf(upcased) > -1 ? upcased : method;
  }

  function Request(input, options) {
    options = options || {};
    var body = options.body;

    if (input instanceof Request) {
      if (input.bodyUsed) {
        throw new TypeError('Already read');
      }
      this.url = input.url;
      this.credentials = input.credentials;
      if (!options.headers) {
        this.headers = new Headers(input.headers);
      }
      this.method = input.method;
      this.mode = input.mode;
      if (!body && input._bodyInit != null) {
        body = input._bodyInit;
        input.bodyUsed = true;
      }
    } else {
      this.url = String(input);
    }

    this.credentials = options.credentials || this.credentials || 'omit';
    if (options.headers || !this.headers) {
      this.headers = new Headers(options.headers);
    }
    this.method = normalizeMethod(options.method || this.method || 'GET');
    this.mode = options.mode || this.mode || null;
    this.referrer = null;

    if ((this.method === 'GET' || this.method === 'HEAD') && body) {
      throw new TypeError('Body not allowed for GET or HEAD requests');
    }
    this._initBody(body);
  }

  Request.prototype.clone = function () {
    return new Request(this, { body: this._bodyInit });
  };

  function decode(body) {
    var form = new FormData();
    body.trim().split('&').forEach(function (bytes) {
      if (bytes) {
        var split = bytes.split('=');
        var name = split.shift().replace(/\+/g, ' ');
        var value = split.join('=').replace(/\+/g, ' ');
        form.append(decodeURIComponent(name), decodeURIComponent(value));
      }
    });
    return form;
  }

  function parseHeaders(rawHeaders) {
    var headers = new Headers();
    // Replace instances of \r\n and \n followed by at least one space or horizontal tab with a space
    // https://tools.ietf.org/html/rfc7230#section-3.2
    var preProcessedHeaders = rawHeaders.replace(/\r?\n[\t ]+/g, ' ');
    preProcessedHeaders.split(/\r?\n/).forEach(function (line) {
      var parts = line.split(':');
      var key = parts.shift().trim();
      if (key) {
        var value = parts.join(':').trim();
        headers.append(key, value);
      }
    });
    return headers;
  }

  Body.call(Request.prototype);

  function Response(bodyInit, options) {
    if (!options) {
      options = {};
    }

    this.type = 'default';
    this.status = options.status === undefined ? 200 : options.status;
    this.ok = this.status >= 200 && this.status < 300;
    this.statusText = 'statusText' in options ? options.statusText : 'OK';
    this.headers = new Headers(options.headers);
    this.url = options.url || '';
    this._initBody(bodyInit);
  }

  Body.call(Response.prototype);

  Response.prototype.clone = function () {
    return new Response(this._bodyInit, {
      status: this.status,
      statusText: this.statusText,
      headers: new Headers(this.headers),
      url: this.url
    });
  };

  Response.error = function () {
    var response = new Response(null, { status: 0, statusText: '' });
    response.type = 'error';
    return response;
  };

  var redirectStatuses = [301, 302, 303, 307, 308];

  Response.redirect = function (url, status) {
    if (redirectStatuses.indexOf(status) === -1) {
      throw new RangeError('Invalid status code');
    }

    return new Response(null, { status: status, headers: { location: url } });
  };

  self.Headers = Headers;
  self.Request = Request;
  self.Response = Response;

  self.fetch = function (input, init) {
    return new Promise(function (resolve, reject) {
      var request = new Request(input, init);
      var xhr = new XMLHttpRequest();

      xhr.onload = function () {
        var options = {
          status: xhr.status,
          statusText: xhr.statusText,
          headers: parseHeaders(xhr.getAllResponseHeaders() || '')
        };
        options.url = 'responseURL' in xhr ? xhr.responseURL : options.headers.get('X-Request-URL');
        var body = 'response' in xhr ? xhr.response : xhr.responseText;
        resolve(new Response(body, options));
      };

      xhr.onerror = function () {
        reject(new TypeError('Network request failed'));
      };

      xhr.ontimeout = function () {
        reject(new TypeError('Network request failed'));
      };

      xhr.open(request.method, request.url, true);

      if (request.credentials === 'include') {
        xhr.withCredentials = true;
      } else if (request.credentials === 'omit') {
        xhr.withCredentials = false;
      }

      if ('responseType' in xhr && support.blob) {
        xhr.responseType = 'blob';
      }

      request.headers.forEach(function (value, name) {
        xhr.setRequestHeader(name, value);
      });

      xhr.send(typeof request._bodyInit === 'undefined' ? null : request._bodyInit);
    });
  };
  self.fetch.polyfill = true;
})(typeof self !== 'undefined' ? self : undefined);

/***/ }),
/* 77 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
Object.defineProperty(__webpack_exports__, "__esModule", { value: true });
/* WEBPACK VAR INJECTION */(function(Jsx) {/* harmony export (binding) */ __webpack_require__.d(__webpack_exports__, "initialize", function() { return initialize; });
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_clipboard__ = __webpack_require__(78);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_clipboard___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0_clipboard__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_fastclick__ = __webpack_require__(86);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_fastclick___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_fastclick__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__components_Material__ = __webpack_require__(87);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */






/* ----------------------------------------------------------------------------
 * Application
 * ------------------------------------------------------------------------- */

/**
 * Initialize Material for MkDocs
 *
 * @param {Object} config - Configuration
 */
function initialize(config) {
  // eslint-disable-line func-style

  /* Initialize Modernizr and FastClick */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(document, "DOMContentLoaded", function () {
    if (!(document.body instanceof HTMLElement)) throw new ReferenceError();

    /* Attach FastClick to mitigate 300ms delay on touch devices */
    __WEBPACK_IMPORTED_MODULE_1_fastclick___default.a.attach(document.body);

    /* Test for iOS */
    Modernizr.addTest("ios", function () {
      return !!navigator.userAgent.match(/(iPad|iPhone|iPod)/g);
    });

    /* Wrap all data tables for better overflow scrolling */
    var tables = document.querySelectorAll("table:not([class])"); // TODO: this is JSX, we should rename the file
    Array.prototype.forEach.call(tables, function (table) {
      var wrap = Jsx.createElement(
        "div",
        { "class": "md-typeset__scrollwrap" },
        Jsx.createElement("div", { "class": "md-typeset__table" })
      );
      if (table.nextSibling) {
        table.parentNode.insertBefore(wrap, table.nextSibling);
      } else {
        table.parentNode.appendChild(wrap);
      }
      wrap.children[0].appendChild(table);
    });

    /* Clipboard integration */
    if (__WEBPACK_IMPORTED_MODULE_0_clipboard___default.a.isSupported()) {
      var blocks = document.querySelectorAll("div > pre, pre > code");
      Array.prototype.forEach.call(blocks, function (block, index) {
        var id = "__code_" + index;

        /* Create button with message container */
        var button = Jsx.createElement(
          "button",
          { "class": "md-clipboard", title: "Copy to clipboard",
            "data-clipboard-target": "#" + id + " pre, #" + id + " code" },
          Jsx.createElement("span", { "class": "md-clipboard__message" })
        );

        /* Link to block and insert button */
        var parent = block.parentNode;
        parent.id = id;
        parent.insertBefore(button, block);
      });

      /* Initialize Clipboard listener */
      var copy = new __WEBPACK_IMPORTED_MODULE_0_clipboard___default.a(".md-clipboard");

      /* Success handler */
      copy.on("success", function (action) {
        var message = action.trigger.querySelector(".md-clipboard__message");
        if (!(message instanceof HTMLElement)) throw new ReferenceError();

        /* Clear selection and reset debounce logic */
        action.clearSelection();
        if (message.dataset.mdTimer) clearTimeout(parseInt(message.dataset.mdTimer, 10));

        /* Set message indicating success and show it */
        message.classList.add("md-clipboard__message--active");
        message.innerHTML = "Copied to clipboard";

        /* Hide message after two seconds */
        message.dataset.mdTimer = setTimeout(function () {
          message.classList.remove("md-clipboard__message--active");
          message.dataset.mdTimer = "";
        }, 2000).toString();
      });
    }

    /* Polyfill details/summary functionality */
    if (!Modernizr.details) {
      var _blocks = document.querySelectorAll("details > summary");
      Array.prototype.forEach.call(_blocks, function (summary) {
        summary.addEventListener("click", function (ev) {
          var details = ev.target.parentNode;
          if (details.hasAttribute("open")) {
            details.removeAttribute("open");
          } else {
            details.setAttribute("open", "");
          }
        });
      });
    }

    /* Force 1px scroll offset to trigger overflow scrolling */
    if (Modernizr.ios) {
      var scrollable = document.querySelectorAll("[data-md-scrollfix]");
      Array.prototype.forEach.call(scrollable, function (item) {
        item.addEventListener("touchstart", function () {
          var top = item.scrollTop;

          /* We're at the top of the container */
          if (top === 0) {
            item.scrollTop = 1;

            /* We're at the bottom of the container */
          } else if (top + item.offsetHeight === item.scrollHeight) {
            item.scrollTop = top - 1;
          }
        });
      });
    }
  }).listen();

  /* Component: header shadow toggle */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(min-width: 1220px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, ["scroll", "resize", "orientationchange"], new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Header.Shadow("[data-md-component=container]", "[data-md-component=header]")));

  /* Component: tabs visibility toggle */
  if (document.querySelector("[data-md-component=tabs]")) new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, ["scroll", "resize", "orientationchange"], new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Tabs.Toggle("[data-md-component=tabs]")).listen();

  /* Component: sidebar with navigation */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(min-width: 1220px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, ["scroll", "resize", "orientationchange"], new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Sidebar.Position("[data-md-component=navigation]", "[data-md-component=header]")));

  /* Component: sidebar with table of contents (missing on 404 page) */
  if (document.querySelector("[data-md-component=toc]")) new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(min-width: 960px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, ["scroll", "resize", "orientationchange"], new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Sidebar.Position("[data-md-component=toc]", "[data-md-component=header]")));

  /* Component: link blurring for table of contents */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(min-width: 960px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, "scroll", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Nav.Blur("[data-md-component=toc] [href]")));

  /* Component: collapsible elements for navigation */
  var collapsibles = document.querySelectorAll("[data-md-component=collapsible]");
  Array.prototype.forEach.call(collapsibles, function (collapse) {
    new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(min-width: 1220px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(collapse.previousElementSibling, "click", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Nav.Collapse(collapse)));
  });

  /* Component: active pane monitor for iOS scrolling fixes */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(max-width: 1219px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-component=navigation] [data-md-toggle]", "change", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Nav.Scrolling("[data-md-component=navigation] nav")));

  /* Component: search body lock for mobile */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(max-width: 959px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-toggle=search]", "change", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Search.Lock("[data-md-toggle=search]")));

  /* Component: search results */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-component=query]", ["focus", "keyup", "change"], new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Search.Result("[data-md-component=result]", function () {
    return fetch(config.url.base + "/" + (config.version < "0.17" ? "mkdocs" : "search") + "/search_index.json", {
      credentials: "same-origin"
    }).then(function (response) {
      return response.json();
    }).then(function (data) {
      return data.docs.map(function (doc) {
        doc.location = config.url.base + doc.location;
        return doc;
      });
    });
  })).listen();

  /* Listener: close drawer when anchor links are clicked */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(max-width: 959px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-component=navigation] [href^='#']", "click", function () {
    var toggle = document.querySelector("[data-md-toggle=drawer]");
    if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
    if (toggle.checked) {
      toggle.checked = false;
      toggle.dispatchEvent(new CustomEvent("change"));
    }
  }));

  /* Listener: focus input after form reset */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-component=reset]", "click", function () {
    setTimeout(function () {
      var query = document.querySelector("[data-md-component=query]");
      if (!(query instanceof HTMLInputElement)) throw new ReferenceError();
      query.focus();
    }, 10);
  }).listen();

  /* Listener: focus input after opening search */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-toggle=search]", "change", function (ev) {
    setTimeout(function (toggle) {
      if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
      if (toggle.checked) {
        var query = document.querySelector("[data-md-component=query]");
        if (!(query instanceof HTMLInputElement)) throw new ReferenceError();
        query.focus();
      }
    }, 400, ev.target);
  }).listen();

  /* Listener: open search on focus */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.MatchMedia("(min-width: 960px)", new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener("[data-md-component=query]", "focus", function () {
    var toggle = document.querySelector("[data-md-toggle=search]");
    if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
    if (!toggle.checked) {
      toggle.checked = true;
      toggle.dispatchEvent(new CustomEvent("change"));
    }
  }));

  /* Listener: keyboard handlers */ // eslint-disable-next-line complexity
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, "keydown", function (ev) {
    // TODO: split up into component to reduce complexity
    var toggle = document.querySelector("[data-md-toggle=search]");
    if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
    var query = document.querySelector("[data-md-component=query]");
    if (!(query instanceof HTMLInputElement)) throw new ReferenceError();

    /* Abort if meta key (macOS) or ctrl key (Windows) is pressed */
    if (ev.metaKey || ev.ctrlKey) return;

    /* Search is open */
    if (toggle.checked) {

      /* Enter: prevent form submission */
      if (ev.keyCode === 13) {
        if (query === document.activeElement) {
          ev.preventDefault();

          /* Go to current active/focused link */
          var focus = document.querySelector("[data-md-component=search] [href][data-md-state=active]");
          if (focus instanceof HTMLLinkElement) {
            window.location = focus.getAttribute("href");

            /* Close search */
            toggle.checked = false;
            toggle.dispatchEvent(new CustomEvent("change"));
            query.blur();
          }
        }

        /* Escape: close search */
      } else if (ev.keyCode === 27) {
        toggle.checked = false;
        toggle.dispatchEvent(new CustomEvent("change"));
        query.blur();

        /* Horizontal arrows and backspace: focus input */
      } else if ([8, 37, 39].indexOf(ev.keyCode) !== -1) {
        if (query !== document.activeElement) query.focus();

        /* Vertical arrows and tab: select previous or next search result */
      } else if ([9, 38, 40].indexOf(ev.keyCode) !== -1) {
        var map = ev.shiftKey ? 38 : 40;
        var key = ev.keyCode === 9 ? map : ev.keyCode;

        /* Retrieve all results */
        var links = Array.prototype.slice.call(document.querySelectorAll("[data-md-component=query], [data-md-component=search] [href]"));

        /* Retrieve current active/focused result */
        var _focus = links.find(function (link) {
          if (!(link instanceof HTMLElement)) throw new ReferenceError();
          return link.dataset.mdState === "active";
        });
        if (_focus) _focus.dataset.mdState = "";

        /* Calculate index depending on direction, add length to form ring */
        var index = Math.max(0, (links.indexOf(_focus) + links.length + (key === 38 ? -1 : +1)) % links.length);

        /* Set active state and focus */
        if (links[index]) {
          links[index].dataset.mdState = "active";
          links[index].focus();
        }

        /* Prevent scrolling of page */
        ev.preventDefault();
        ev.stopPropagation();

        /* Return false prevents the cursor position from changing */
        return false;
      }

      /* Search is closed and we're not inside a form */
    } else if (document.activeElement && !document.activeElement.form) {

      /* F/S: Open search if not in input field */
      // if (ev.keyCode === 70 || ev.keyCode === 83) {
      //   query.focus()
      //   ev.preventDefault()
      // }
    }
  }).listen();

  /* Listener: focus query if in search is open and character is typed */
  new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Event.Listener(window, "keypress", function () {
    var toggle = document.querySelector("[data-md-toggle=search]");
    if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
    if (toggle.checked) {
      var query = document.querySelector("[data-md-component=query]");
      if (!(query instanceof HTMLInputElement)) throw new ReferenceError();
      if (query !== document.activeElement) query.focus();
    }
  }).listen()

  /* Retrieve facts for the given repository type */
  ;(function () {
    var el = document.querySelector("[data-md-source]");
    if (!el) return Promise.resolve([]);else if (!(el instanceof HTMLAnchorElement)) throw new ReferenceError();
    switch (el.dataset.mdSource) {
      case "github":
        return new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Source.Adapter.GitHub(el).fetch();
      default:
        return Promise.resolve([]);
    }

    /* Render repository information */
  })().then(function (facts) {
    var sources = document.querySelectorAll("[data-md-source]");
    Array.prototype.forEach.call(sources, function (source) {
      new __WEBPACK_IMPORTED_MODULE_2__components_Material__["a" /* default */].Source.Repository(source).initialize(facts);
    });
  });
}

/* ----------------------------------------------------------------------------
 * Exports
 * ------------------------------------------------------------------------- */


/* WEBPACK VAR INJECTION */}.call(__webpack_exports__, __webpack_require__(26)))

/***/ }),
/* 78 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;

var _typeof2 = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

(function (global, factory) {
    if (true) {
        !(__WEBPACK_AMD_DEFINE_ARRAY__ = [module, __webpack_require__(79), __webpack_require__(81), __webpack_require__(82)], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if (typeof exports !== "undefined") {
        factory(module, require('./clipboard-action'), require('tiny-emitter'), require('good-listener'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.clipboardAction, global.tinyEmitter, global.goodListener);
        global.clipboard = mod.exports;
    }
})(undefined, function (module, _clipboardAction, _tinyEmitter, _goodListener) {
    'use strict';

    var _clipboardAction2 = _interopRequireDefault(_clipboardAction);

    var _tinyEmitter2 = _interopRequireDefault(_tinyEmitter);

    var _goodListener2 = _interopRequireDefault(_goodListener);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    var _typeof = typeof Symbol === "function" && _typeof2(Symbol.iterator) === "symbol" ? function (obj) {
        return typeof obj === 'undefined' ? 'undefined' : _typeof2(obj);
    } : function (obj) {
        return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj === 'undefined' ? 'undefined' : _typeof2(obj);
    };

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    function _possibleConstructorReturn(self, call) {
        if (!self) {
            throw new ReferenceError("this hasn't been initialised - super() hasn't been called");
        }

        return call && ((typeof call === 'undefined' ? 'undefined' : _typeof2(call)) === "object" || typeof call === "function") ? call : self;
    }

    function _inherits(subClass, superClass) {
        if (typeof superClass !== "function" && superClass !== null) {
            throw new TypeError("Super expression must either be null or a function, not " + (typeof superClass === 'undefined' ? 'undefined' : _typeof2(superClass)));
        }

        subClass.prototype = Object.create(superClass && superClass.prototype, {
            constructor: {
                value: subClass,
                enumerable: false,
                writable: true,
                configurable: true
            }
        });
        if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass;
    }

    var Clipboard = function (_Emitter) {
        _inherits(Clipboard, _Emitter);

        /**
         * @param {String|HTMLElement|HTMLCollection|NodeList} trigger
         * @param {Object} options
         */
        function Clipboard(trigger, options) {
            _classCallCheck(this, Clipboard);

            var _this = _possibleConstructorReturn(this, (Clipboard.__proto__ || Object.getPrototypeOf(Clipboard)).call(this));

            _this.resolveOptions(options);
            _this.listenClick(trigger);
            return _this;
        }

        /**
         * Defines if attributes would be resolved using internal setter functions
         * or custom functions that were passed in the constructor.
         * @param {Object} options
         */

        _createClass(Clipboard, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = typeof options.action === 'function' ? options.action : this.defaultAction;
                this.target = typeof options.target === 'function' ? options.target : this.defaultTarget;
                this.text = typeof options.text === 'function' ? options.text : this.defaultText;
                this.container = _typeof(options.container) === 'object' ? options.container : document.body;
            }
        }, {
            key: 'listenClick',
            value: function listenClick(trigger) {
                var _this2 = this;

                this.listener = (0, _goodListener2.default)(trigger, 'click', function (e) {
                    return _this2.onClick(e);
                });
            }
        }, {
            key: 'onClick',
            value: function onClick(e) {
                var trigger = e.delegateTarget || e.currentTarget;

                if (this.clipboardAction) {
                    this.clipboardAction = null;
                }

                this.clipboardAction = new _clipboardAction2.default({
                    action: this.action(trigger),
                    target: this.target(trigger),
                    text: this.text(trigger),
                    container: this.container,
                    trigger: trigger,
                    emitter: this
                });
            }
        }, {
            key: 'defaultAction',
            value: function defaultAction(trigger) {
                return getAttributeValue('action', trigger);
            }
        }, {
            key: 'defaultTarget',
            value: function defaultTarget(trigger) {
                var selector = getAttributeValue('target', trigger);

                if (selector) {
                    return document.querySelector(selector);
                }
            }
        }, {
            key: 'defaultText',
            value: function defaultText(trigger) {
                return getAttributeValue('text', trigger);
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.listener.destroy();

                if (this.clipboardAction) {
                    this.clipboardAction.destroy();
                    this.clipboardAction = null;
                }
            }
        }], [{
            key: 'isSupported',
            value: function isSupported() {
                var action = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : ['copy', 'cut'];

                var actions = typeof action === 'string' ? [action] : action;
                var support = !!document.queryCommandSupported;

                actions.forEach(function (action) {
                    support = support && !!document.queryCommandSupported(action);
                });

                return support;
            }
        }]);

        return Clipboard;
    }(_tinyEmitter2.default);

    /**
     * Helper function to retrieve attribute value.
     * @param {String} suffix
     * @param {Element} element
     */
    function getAttributeValue(suffix, element) {
        var attribute = 'data-clipboard-' + suffix;

        if (!element.hasAttribute(attribute)) {
            return;
        }

        return element.getAttribute(attribute);
    }

    module.exports = Clipboard;
});

/***/ }),
/* 79 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_ARRAY__, __WEBPACK_AMD_DEFINE_RESULT__;

var _typeof2 = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

(function (global, factory) {
    if (true) {
        !(__WEBPACK_AMD_DEFINE_ARRAY__ = [module, __webpack_require__(80)], __WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.apply(exports, __WEBPACK_AMD_DEFINE_ARRAY__)) : __WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if (typeof exports !== "undefined") {
        factory(module, require('select'));
    } else {
        var mod = {
            exports: {}
        };
        factory(mod, global.select);
        global.clipboardAction = mod.exports;
    }
})(undefined, function (module, _select) {
    'use strict';

    var _select2 = _interopRequireDefault(_select);

    function _interopRequireDefault(obj) {
        return obj && obj.__esModule ? obj : {
            default: obj
        };
    }

    var _typeof = typeof Symbol === "function" && _typeof2(Symbol.iterator) === "symbol" ? function (obj) {
        return typeof obj === 'undefined' ? 'undefined' : _typeof2(obj);
    } : function (obj) {
        return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj === 'undefined' ? 'undefined' : _typeof2(obj);
    };

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    var _createClass = function () {
        function defineProperties(target, props) {
            for (var i = 0; i < props.length; i++) {
                var descriptor = props[i];
                descriptor.enumerable = descriptor.enumerable || false;
                descriptor.configurable = true;
                if ("value" in descriptor) descriptor.writable = true;
                Object.defineProperty(target, descriptor.key, descriptor);
            }
        }

        return function (Constructor, protoProps, staticProps) {
            if (protoProps) defineProperties(Constructor.prototype, protoProps);
            if (staticProps) defineProperties(Constructor, staticProps);
            return Constructor;
        };
    }();

    var ClipboardAction = function () {
        /**
         * @param {Object} options
         */
        function ClipboardAction(options) {
            _classCallCheck(this, ClipboardAction);

            this.resolveOptions(options);
            this.initSelection();
        }

        /**
         * Defines base properties passed from constructor.
         * @param {Object} options
         */

        _createClass(ClipboardAction, [{
            key: 'resolveOptions',
            value: function resolveOptions() {
                var options = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : {};

                this.action = options.action;
                this.container = options.container;
                this.emitter = options.emitter;
                this.target = options.target;
                this.text = options.text;
                this.trigger = options.trigger;

                this.selectedText = '';
            }
        }, {
            key: 'initSelection',
            value: function initSelection() {
                if (this.text) {
                    this.selectFake();
                } else if (this.target) {
                    this.selectTarget();
                }
            }
        }, {
            key: 'selectFake',
            value: function selectFake() {
                var _this = this;

                var isRTL = document.documentElement.getAttribute('dir') == 'rtl';

                this.removeFake();

                this.fakeHandlerCallback = function () {
                    return _this.removeFake();
                };
                this.fakeHandler = this.container.addEventListener('click', this.fakeHandlerCallback) || true;

                this.fakeElem = document.createElement('textarea');
                // Prevent zooming on iOS
                this.fakeElem.style.fontSize = '12pt';
                // Reset box model
                this.fakeElem.style.border = '0';
                this.fakeElem.style.padding = '0';
                this.fakeElem.style.margin = '0';
                // Move element out of screen horizontally
                this.fakeElem.style.position = 'absolute';
                this.fakeElem.style[isRTL ? 'right' : 'left'] = '-9999px';
                // Move element to the same position vertically
                var yPosition = window.pageYOffset || document.documentElement.scrollTop;
                this.fakeElem.style.top = yPosition + 'px';

                this.fakeElem.setAttribute('readonly', '');
                this.fakeElem.value = this.text;

                this.container.appendChild(this.fakeElem);

                this.selectedText = (0, _select2.default)(this.fakeElem);
                this.copyText();
            }
        }, {
            key: 'removeFake',
            value: function removeFake() {
                if (this.fakeHandler) {
                    this.container.removeEventListener('click', this.fakeHandlerCallback);
                    this.fakeHandler = null;
                    this.fakeHandlerCallback = null;
                }

                if (this.fakeElem) {
                    this.container.removeChild(this.fakeElem);
                    this.fakeElem = null;
                }
            }
        }, {
            key: 'selectTarget',
            value: function selectTarget() {
                this.selectedText = (0, _select2.default)(this.target);
                this.copyText();
            }
        }, {
            key: 'copyText',
            value: function copyText() {
                var succeeded = void 0;

                try {
                    succeeded = document.execCommand(this.action);
                } catch (err) {
                    succeeded = false;
                }

                this.handleResult(succeeded);
            }
        }, {
            key: 'handleResult',
            value: function handleResult(succeeded) {
                this.emitter.emit(succeeded ? 'success' : 'error', {
                    action: this.action,
                    text: this.selectedText,
                    trigger: this.trigger,
                    clearSelection: this.clearSelection.bind(this)
                });
            }
        }, {
            key: 'clearSelection',
            value: function clearSelection() {
                if (this.trigger) {
                    this.trigger.focus();
                }

                window.getSelection().removeAllRanges();
            }
        }, {
            key: 'destroy',
            value: function destroy() {
                this.removeFake();
            }
        }, {
            key: 'action',
            set: function set() {
                var action = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'copy';

                this._action = action;

                if (this._action !== 'copy' && this._action !== 'cut') {
                    throw new Error('Invalid "action" value, use either "copy" or "cut"');
                }
            },
            get: function get() {
                return this._action;
            }
        }, {
            key: 'target',
            set: function set(target) {
                if (target !== undefined) {
                    if (target && (typeof target === 'undefined' ? 'undefined' : _typeof(target)) === 'object' && target.nodeType === 1) {
                        if (this.action === 'copy' && target.hasAttribute('disabled')) {
                            throw new Error('Invalid "target" attribute. Please use "readonly" instead of "disabled" attribute');
                        }

                        if (this.action === 'cut' && (target.hasAttribute('readonly') || target.hasAttribute('disabled'))) {
                            throw new Error('Invalid "target" attribute. You can\'t cut text from elements with "readonly" or "disabled" attributes');
                        }

                        this._target = target;
                    } else {
                        throw new Error('Invalid "target" value, use a valid Element');
                    }
                }
            },
            get: function get() {
                return this._target;
            }
        }]);

        return ClipboardAction;
    }();

    module.exports = ClipboardAction;
});

/***/ }),
/* 80 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


function select(element) {
    var selectedText;

    if (element.nodeName === 'SELECT') {
        element.focus();

        selectedText = element.value;
    } else if (element.nodeName === 'INPUT' || element.nodeName === 'TEXTAREA') {
        var isReadOnly = element.hasAttribute('readonly');

        if (!isReadOnly) {
            element.setAttribute('readonly', '');
        }

        element.select();
        element.setSelectionRange(0, element.value.length);

        if (!isReadOnly) {
            element.removeAttribute('readonly');
        }

        selectedText = element.value;
    } else {
        if (element.hasAttribute('contenteditable')) {
            element.focus();
        }

        var selection = window.getSelection();
        var range = document.createRange();

        range.selectNodeContents(element);
        selection.removeAllRanges();
        selection.addRange(range);

        selectedText = selection.toString();
    }

    return selectedText;
}

module.exports = select;

/***/ }),
/* 81 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


function E() {
  // Keep this empty so it's easier to inherit from
  // (via https://github.com/lipsmack from https://github.com/scottcorgan/tiny-emitter/issues/3)
}

E.prototype = {
  on: function on(name, callback, ctx) {
    var e = this.e || (this.e = {});

    (e[name] || (e[name] = [])).push({
      fn: callback,
      ctx: ctx
    });

    return this;
  },

  once: function once(name, callback, ctx) {
    var self = this;
    function listener() {
      self.off(name, listener);
      callback.apply(ctx, arguments);
    };

    listener._ = callback;
    return this.on(name, listener, ctx);
  },

  emit: function emit(name) {
    var data = [].slice.call(arguments, 1);
    var evtArr = ((this.e || (this.e = {}))[name] || []).slice();
    var i = 0;
    var len = evtArr.length;

    for (i; i < len; i++) {
      evtArr[i].fn.apply(evtArr[i].ctx, data);
    }

    return this;
  },

  off: function off(name, callback) {
    var e = this.e || (this.e = {});
    var evts = e[name];
    var liveEvents = [];

    if (evts && callback) {
      for (var i = 0, len = evts.length; i < len; i++) {
        if (evts[i].fn !== callback && evts[i].fn._ !== callback) liveEvents.push(evts[i]);
      }
    }

    // Remove event from queue to prevent memory leak
    // Suggested by https://github.com/lazd
    // Ref: https://github.com/scottcorgan/tiny-emitter/commit/c6ebfaa9bc973b33d110a84a307742b7cf94c953#commitcomment-5024910

    liveEvents.length ? e[name] = liveEvents : delete e[name];

    return this;
  }
};

module.exports = E;
module.exports.TinyEmitter = E;

/***/ }),
/* 82 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var is = __webpack_require__(83);
var delegate = __webpack_require__(84);

/**
 * Validates all params and calls the right
 * listener function based on its target type.
 *
 * @param {String|HTMLElement|HTMLCollection|NodeList} target
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listen(target, type, callback) {
    if (!target && !type && !callback) {
        throw new Error('Missing required arguments');
    }

    if (!is.string(type)) {
        throw new TypeError('Second argument must be a String');
    }

    if (!is.fn(callback)) {
        throw new TypeError('Third argument must be a Function');
    }

    if (is.node(target)) {
        return listenNode(target, type, callback);
    } else if (is.nodeList(target)) {
        return listenNodeList(target, type, callback);
    } else if (is.string(target)) {
        return listenSelector(target, type, callback);
    } else {
        throw new TypeError('First argument must be a String, HTMLElement, HTMLCollection, or NodeList');
    }
}

/**
 * Adds an event listener to a HTML element
 * and returns a remove listener function.
 *
 * @param {HTMLElement} node
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNode(node, type, callback) {
    node.addEventListener(type, callback);

    return {
        destroy: function destroy() {
            node.removeEventListener(type, callback);
        }
    };
}

/**
 * Add an event listener to a list of HTML elements
 * and returns a remove listener function.
 *
 * @param {NodeList|HTMLCollection} nodeList
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenNodeList(nodeList, type, callback) {
    Array.prototype.forEach.call(nodeList, function (node) {
        node.addEventListener(type, callback);
    });

    return {
        destroy: function destroy() {
            Array.prototype.forEach.call(nodeList, function (node) {
                node.removeEventListener(type, callback);
            });
        }
    };
}

/**
 * Add an event listener to a selector
 * and returns a remove listener function.
 *
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Object}
 */
function listenSelector(selector, type, callback) {
    return delegate(document.body, selector, type, callback);
}

module.exports = listen;

/***/ }),
/* 83 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


/**
 * Check if argument is a HTML element.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.node = function (value) {
  return value !== undefined && value instanceof HTMLElement && value.nodeType === 1;
};

/**
 * Check if argument is a list of HTML elements.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.nodeList = function (value) {
  var type = Object.prototype.toString.call(value);

  return value !== undefined && (type === '[object NodeList]' || type === '[object HTMLCollection]') && 'length' in value && (value.length === 0 || exports.node(value[0]));
};

/**
 * Check if argument is a string.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.string = function (value) {
  return typeof value === 'string' || value instanceof String;
};

/**
 * Check if argument is a function.
 *
 * @param {Object} value
 * @return {Boolean}
 */
exports.fn = function (value) {
  var type = Object.prototype.toString.call(value);

  return type === '[object Function]';
};

/***/ }),
/* 84 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var closest = __webpack_require__(85);

/**
 * Delegates event to a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @param {Boolean} useCapture
 * @return {Object}
 */
function _delegate(element, selector, type, callback, useCapture) {
    var listenerFn = listener.apply(this, arguments);

    element.addEventListener(type, listenerFn, useCapture);

    return {
        destroy: function destroy() {
            element.removeEventListener(type, listenerFn, useCapture);
        }
    };
}

/**
 * Delegates event to a selector.
 *
 * @param {Element|String|Array} [elements]
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @param {Boolean} useCapture
 * @return {Object}
 */
function delegate(elements, selector, type, callback, useCapture) {
    // Handle the regular Element usage
    if (typeof elements.addEventListener === 'function') {
        return _delegate.apply(null, arguments);
    }

    // Handle Element-less usage, it defaults to global delegation
    if (typeof type === 'function') {
        // Use `document` as the first parameter, then apply arguments
        // This is a short way to .unshift `arguments` without running into deoptimizations
        return _delegate.bind(null, document).apply(null, arguments);
    }

    // Handle Selector-based usage
    if (typeof elements === 'string') {
        elements = document.querySelectorAll(elements);
    }

    // Handle Array-like based usage
    return Array.prototype.map.call(elements, function (element) {
        return _delegate(element, selector, type, callback, useCapture);
    });
}

/**
 * Finds closest match and invokes callback.
 *
 * @param {Element} element
 * @param {String} selector
 * @param {String} type
 * @param {Function} callback
 * @return {Function}
 */
function listener(element, selector, type, callback) {
    return function (e) {
        e.delegateTarget = closest(e.target, selector);

        if (e.delegateTarget) {
            callback.call(element, e);
        }
    };
}

module.exports = delegate;

/***/ }),
/* 85 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var DOCUMENT_NODE_TYPE = 9;

/**
 * A polyfill for Element.matches()
 */
if (typeof Element !== 'undefined' && !Element.prototype.matches) {
    var proto = Element.prototype;

    proto.matches = proto.matchesSelector || proto.mozMatchesSelector || proto.msMatchesSelector || proto.oMatchesSelector || proto.webkitMatchesSelector;
}

/**
 * Finds the closest parent that matches a selector.
 *
 * @param {Element} element
 * @param {String} selector
 * @return {Function}
 */
function closest(element, selector) {
    while (element && element.nodeType !== DOCUMENT_NODE_TYPE) {
        if (typeof element.matches === 'function' && element.matches(selector)) {
            return element;
        }
        element = element.parentNode;
    }
}

module.exports = closest;

/***/ }),
/* 86 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
var __WEBPACK_AMD_DEFINE_RESULT__;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

;(function () {
	'use strict';

	/**
  * @preserve FastClick: polyfill to remove click delays on browsers with touch UIs.
  *
  * @codingstandard ftlabs-jsv2
  * @copyright The Financial Times Limited [All Rights Reserved]
  * @license MIT License (see LICENSE.txt)
  */

	/*jslint browser:true, node:true*/
	/*global define, Event, Node*/

	/**
  * Instantiate fast-clicking listeners on the specified layer.
  *
  * @constructor
  * @param {Element} layer The layer to listen on
  * @param {Object} [options={}] The options to override the defaults
  */

	function FastClick(layer, options) {
		var oldOnClick;

		options = options || {};

		/**
   * Whether a click is currently being tracked.
   *
   * @type boolean
   */
		this.trackingClick = false;

		/**
   * Timestamp for when click tracking started.
   *
   * @type number
   */
		this.trackingClickStart = 0;

		/**
   * The element being tracked for a click.
   *
   * @type EventTarget
   */
		this.targetElement = null;

		/**
   * X-coordinate of touch start event.
   *
   * @type number
   */
		this.touchStartX = 0;

		/**
   * Y-coordinate of touch start event.
   *
   * @type number
   */
		this.touchStartY = 0;

		/**
   * ID of the last touch, retrieved from Touch.identifier.
   *
   * @type number
   */
		this.lastTouchIdentifier = 0;

		/**
   * Touchmove boundary, beyond which a click will be cancelled.
   *
   * @type number
   */
		this.touchBoundary = options.touchBoundary || 10;

		/**
   * The FastClick layer.
   *
   * @type Element
   */
		this.layer = layer;

		/**
   * The minimum time between tap(touchstart and touchend) events
   *
   * @type number
   */
		this.tapDelay = options.tapDelay || 200;

		/**
   * The maximum time for a tap
   *
   * @type number
   */
		this.tapTimeout = options.tapTimeout || 700;

		if (FastClick.notNeeded(layer)) {
			return;
		}

		// Some old versions of Android don't have Function.prototype.bind
		function bind(method, context) {
			return function () {
				return method.apply(context, arguments);
			};
		}

		var methods = ['onMouse', 'onClick', 'onTouchStart', 'onTouchMove', 'onTouchEnd', 'onTouchCancel'];
		var context = this;
		for (var i = 0, l = methods.length; i < l; i++) {
			context[methods[i]] = bind(context[methods[i]], context);
		}

		// Set up event handlers as required
		if (deviceIsAndroid) {
			layer.addEventListener('mouseover', this.onMouse, true);
			layer.addEventListener('mousedown', this.onMouse, true);
			layer.addEventListener('mouseup', this.onMouse, true);
		}

		layer.addEventListener('click', this.onClick, true);
		layer.addEventListener('touchstart', this.onTouchStart, false);
		layer.addEventListener('touchmove', this.onTouchMove, false);
		layer.addEventListener('touchend', this.onTouchEnd, false);
		layer.addEventListener('touchcancel', this.onTouchCancel, false);

		// Hack is required for browsers that don't support Event#stopImmediatePropagation (e.g. Android 2)
		// which is how FastClick normally stops click events bubbling to callbacks registered on the FastClick
		// layer when they are cancelled.
		if (!Event.prototype.stopImmediatePropagation) {
			layer.removeEventListener = function (type, callback, capture) {
				var rmv = Node.prototype.removeEventListener;
				if (type === 'click') {
					rmv.call(layer, type, callback.hijacked || callback, capture);
				} else {
					rmv.call(layer, type, callback, capture);
				}
			};

			layer.addEventListener = function (type, callback, capture) {
				var adv = Node.prototype.addEventListener;
				if (type === 'click') {
					adv.call(layer, type, callback.hijacked || (callback.hijacked = function (event) {
						if (!event.propagationStopped) {
							callback(event);
						}
					}), capture);
				} else {
					adv.call(layer, type, callback, capture);
				}
			};
		}

		// If a handler is already declared in the element's onclick attribute, it will be fired before
		// FastClick's onClick handler. Fix this by pulling out the user-defined handler function and
		// adding it as listener.
		if (typeof layer.onclick === 'function') {

			// Android browser on at least 3.2 requires a new reference to the function in layer.onclick
			// - the old one won't work if passed to addEventListener directly.
			oldOnClick = layer.onclick;
			layer.addEventListener('click', function (event) {
				oldOnClick(event);
			}, false);
			layer.onclick = null;
		}
	}

	/**
 * Windows Phone 8.1 fakes user agent string to look like Android and iPhone.
 *
 * @type boolean
 */
	var deviceIsWindowsPhone = navigator.userAgent.indexOf("Windows Phone") >= 0;

	/**
  * Android requires exceptions.
  *
  * @type boolean
  */
	var deviceIsAndroid = navigator.userAgent.indexOf('Android') > 0 && !deviceIsWindowsPhone;

	/**
  * iOS requires exceptions.
  *
  * @type boolean
  */
	var deviceIsIOS = /iP(ad|hone|od)/.test(navigator.userAgent) && !deviceIsWindowsPhone;

	/**
  * iOS 4 requires an exception for select elements.
  *
  * @type boolean
  */
	var deviceIsIOS4 = deviceIsIOS && /OS 4_\d(_\d)?/.test(navigator.userAgent);

	/**
  * iOS 6.0-7.* requires the target element to be manually derived
  *
  * @type boolean
  */
	var deviceIsIOSWithBadTarget = deviceIsIOS && /OS [6-7]_\d/.test(navigator.userAgent);

	/**
  * BlackBerry requires exceptions.
  *
  * @type boolean
  */
	var deviceIsBlackBerry10 = navigator.userAgent.indexOf('BB10') > 0;

	/**
  * Determine whether a given element requires a native click.
  *
  * @param {EventTarget|Element} target Target DOM element
  * @returns {boolean} Returns true if the element needs a native click
  */
	FastClick.prototype.needsClick = function (target) {
		switch (target.nodeName.toLowerCase()) {

			// Don't send a synthetic click to disabled inputs (issue #62)
			case 'button':
			case 'select':
			case 'textarea':
				if (target.disabled) {
					return true;
				}

				break;
			case 'input':

				// File inputs need real clicks on iOS 6 due to a browser bug (issue #68)
				if (deviceIsIOS && target.type === 'file' || target.disabled) {
					return true;
				}

				break;
			case 'label':
			case 'iframe': // iOS8 homescreen apps can prevent events bubbling into frames
			case 'video':
				return true;
		}

		return (/\bneedsclick\b/.test(target.className)
		);
	};

	/**
  * Determine whether a given element requires a call to focus to simulate click into element.
  *
  * @param {EventTarget|Element} target Target DOM element
  * @returns {boolean} Returns true if the element requires a call to focus to simulate native click.
  */
	FastClick.prototype.needsFocus = function (target) {
		switch (target.nodeName.toLowerCase()) {
			case 'textarea':
				return true;
			case 'select':
				return !deviceIsAndroid;
			case 'input':
				switch (target.type) {
					case 'button':
					case 'checkbox':
					case 'file':
					case 'image':
					case 'radio':
					case 'submit':
						return false;
				}

				// No point in attempting to focus disabled inputs
				return !target.disabled && !target.readOnly;
			default:
				return (/\bneedsfocus\b/.test(target.className)
				);
		}
	};

	/**
  * Send a click event to the specified element.
  *
  * @param {EventTarget|Element} targetElement
  * @param {Event} event
  */
	FastClick.prototype.sendClick = function (targetElement, event) {
		var clickEvent, touch;

		// On some Android devices activeElement needs to be blurred otherwise the synthetic click will have no effect (#24)
		if (document.activeElement && document.activeElement !== targetElement) {
			document.activeElement.blur();
		}

		touch = event.changedTouches[0];

		// Synthesise a click event, with an extra attribute so it can be tracked
		clickEvent = document.createEvent('MouseEvents');
		clickEvent.initMouseEvent(this.determineEventType(targetElement), true, true, window, 1, touch.screenX, touch.screenY, touch.clientX, touch.clientY, false, false, false, false, 0, null);
		clickEvent.forwardedTouchEvent = true;
		targetElement.dispatchEvent(clickEvent);
	};

	FastClick.prototype.determineEventType = function (targetElement) {

		//Issue #159: Android Chrome Select Box does not open with a synthetic click event
		if (deviceIsAndroid && targetElement.tagName.toLowerCase() === 'select') {
			return 'mousedown';
		}

		return 'click';
	};

	/**
  * @param {EventTarget|Element} targetElement
  */
	FastClick.prototype.focus = function (targetElement) {
		var length;

		// Issue #160: on iOS 7, some input elements (e.g. date datetime month) throw a vague TypeError on setSelectionRange. These elements don't have an integer value for the selectionStart and selectionEnd properties, but unfortunately that can't be used for detection because accessing the properties also throws a TypeError. Just check the type instead. Filed as Apple bug #15122724.
		if (deviceIsIOS && targetElement.setSelectionRange && targetElement.type.indexOf('date') !== 0 && targetElement.type !== 'time' && targetElement.type !== 'month') {
			length = targetElement.value.length;
			targetElement.setSelectionRange(length, length);
		} else {
			targetElement.focus();
		}
	};

	/**
  * Check whether the given target element is a child of a scrollable layer and if so, set a flag on it.
  *
  * @param {EventTarget|Element} targetElement
  */
	FastClick.prototype.updateScrollParent = function (targetElement) {
		var scrollParent, parentElement;

		scrollParent = targetElement.fastClickScrollParent;

		// Attempt to discover whether the target element is contained within a scrollable layer. Re-check if the
		// target element was moved to another parent.
		if (!scrollParent || !scrollParent.contains(targetElement)) {
			parentElement = targetElement;
			do {
				if (parentElement.scrollHeight > parentElement.offsetHeight) {
					scrollParent = parentElement;
					targetElement.fastClickScrollParent = parentElement;
					break;
				}

				parentElement = parentElement.parentElement;
			} while (parentElement);
		}

		// Always update the scroll top tracker if possible.
		if (scrollParent) {
			scrollParent.fastClickLastScrollTop = scrollParent.scrollTop;
		}
	};

	/**
  * @param {EventTarget} targetElement
  * @returns {Element|EventTarget}
  */
	FastClick.prototype.getTargetElementFromEventTarget = function (eventTarget) {

		// On some older browsers (notably Safari on iOS 4.1 - see issue #56) the event target may be a text node.
		if (eventTarget.nodeType === Node.TEXT_NODE) {
			return eventTarget.parentNode;
		}

		return eventTarget;
	};

	/**
  * On touch start, record the position and scroll offset.
  *
  * @param {Event} event
  * @returns {boolean}
  */
	FastClick.prototype.onTouchStart = function (event) {
		var targetElement, touch, selection;

		// Ignore multiple touches, otherwise pinch-to-zoom is prevented if both fingers are on the FastClick element (issue #111).
		if (event.targetTouches.length > 1) {
			return true;
		}

		targetElement = this.getTargetElementFromEventTarget(event.target);
		touch = event.targetTouches[0];

		if (deviceIsIOS) {

			// Only trusted events will deselect text on iOS (issue #49)
			selection = window.getSelection();
			if (selection.rangeCount && !selection.isCollapsed) {
				return true;
			}

			if (!deviceIsIOS4) {

				// Weird things happen on iOS when an alert or confirm dialog is opened from a click event callback (issue #23):
				// when the user next taps anywhere else on the page, new touchstart and touchend events are dispatched
				// with the same identifier as the touch event that previously triggered the click that triggered the alert.
				// Sadly, there is an issue on iOS 4 that causes some normal touch events to have the same identifier as an
				// immediately preceeding touch event (issue #52), so this fix is unavailable on that platform.
				// Issue 120: touch.identifier is 0 when Chrome dev tools 'Emulate touch events' is set with an iOS device UA string,
				// which causes all touch events to be ignored. As this block only applies to iOS, and iOS identifiers are always long,
				// random integers, it's safe to to continue if the identifier is 0 here.
				if (touch.identifier && touch.identifier === this.lastTouchIdentifier) {
					event.preventDefault();
					return false;
				}

				this.lastTouchIdentifier = touch.identifier;

				// If the target element is a child of a scrollable layer (using -webkit-overflow-scrolling: touch) and:
				// 1) the user does a fling scroll on the scrollable layer
				// 2) the user stops the fling scroll with another tap
				// then the event.target of the last 'touchend' event will be the element that was under the user's finger
				// when the fling scroll was started, causing FastClick to send a click event to that layer - unless a check
				// is made to ensure that a parent layer was not scrolled before sending a synthetic click (issue #42).
				this.updateScrollParent(targetElement);
			}
		}

		this.trackingClick = true;
		this.trackingClickStart = event.timeStamp;
		this.targetElement = targetElement;

		this.touchStartX = touch.pageX;
		this.touchStartY = touch.pageY;

		// Prevent phantom clicks on fast double-tap (issue #36)
		if (event.timeStamp - this.lastClickTime < this.tapDelay) {
			event.preventDefault();
		}

		return true;
	};

	/**
  * Based on a touchmove event object, check whether the touch has moved past a boundary since it started.
  *
  * @param {Event} event
  * @returns {boolean}
  */
	FastClick.prototype.touchHasMoved = function (event) {
		var touch = event.changedTouches[0],
		    boundary = this.touchBoundary;

		if (Math.abs(touch.pageX - this.touchStartX) > boundary || Math.abs(touch.pageY - this.touchStartY) > boundary) {
			return true;
		}

		return false;
	};

	/**
  * Update the last position.
  *
  * @param {Event} event
  * @returns {boolean}
  */
	FastClick.prototype.onTouchMove = function (event) {
		if (!this.trackingClick) {
			return true;
		}

		// If the touch has moved, cancel the click tracking
		if (this.targetElement !== this.getTargetElementFromEventTarget(event.target) || this.touchHasMoved(event)) {
			this.trackingClick = false;
			this.targetElement = null;
		}

		return true;
	};

	/**
  * Attempt to find the labelled control for the given label element.
  *
  * @param {EventTarget|HTMLLabelElement} labelElement
  * @returns {Element|null}
  */
	FastClick.prototype.findControl = function (labelElement) {

		// Fast path for newer browsers supporting the HTML5 control attribute
		if (labelElement.control !== undefined) {
			return labelElement.control;
		}

		// All browsers under test that support touch events also support the HTML5 htmlFor attribute
		if (labelElement.htmlFor) {
			return document.getElementById(labelElement.htmlFor);
		}

		// If no for attribute exists, attempt to retrieve the first labellable descendant element
		// the list of which is defined here: http://www.w3.org/TR/html5/forms.html#category-label
		return labelElement.querySelector('button, input:not([type=hidden]), keygen, meter, output, progress, select, textarea');
	};

	/**
  * On touch end, determine whether to send a click event at once.
  *
  * @param {Event} event
  * @returns {boolean}
  */
	FastClick.prototype.onTouchEnd = function (event) {
		var forElement,
		    trackingClickStart,
		    targetTagName,
		    scrollParent,
		    touch,
		    targetElement = this.targetElement;

		if (!this.trackingClick) {
			return true;
		}

		// Prevent phantom clicks on fast double-tap (issue #36)
		if (event.timeStamp - this.lastClickTime < this.tapDelay) {
			this.cancelNextClick = true;
			return true;
		}

		if (event.timeStamp - this.trackingClickStart > this.tapTimeout) {
			return true;
		}

		// Reset to prevent wrong click cancel on input (issue #156).
		this.cancelNextClick = false;

		this.lastClickTime = event.timeStamp;

		trackingClickStart = this.trackingClickStart;
		this.trackingClick = false;
		this.trackingClickStart = 0;

		// On some iOS devices, the targetElement supplied with the event is invalid if the layer
		// is performing a transition or scroll, and has to be re-detected manually. Note that
		// for this to function correctly, it must be called *after* the event target is checked!
		// See issue #57; also filed as rdar://13048589 .
		if (deviceIsIOSWithBadTarget) {
			touch = event.changedTouches[0];

			// In certain cases arguments of elementFromPoint can be negative, so prevent setting targetElement to null
			targetElement = document.elementFromPoint(touch.pageX - window.pageXOffset, touch.pageY - window.pageYOffset) || targetElement;
			targetElement.fastClickScrollParent = this.targetElement.fastClickScrollParent;
		}

		targetTagName = targetElement.tagName.toLowerCase();
		if (targetTagName === 'label') {
			forElement = this.findControl(targetElement);
			if (forElement) {
				this.focus(targetElement);
				if (deviceIsAndroid) {
					return false;
				}

				targetElement = forElement;
			}
		} else if (this.needsFocus(targetElement)) {

			// Case 1: If the touch started a while ago (best guess is 100ms based on tests for issue #36) then focus will be triggered anyway. Return early and unset the target element reference so that the subsequent click will be allowed through.
			// Case 2: Without this exception for input elements tapped when the document is contained in an iframe, then any inputted text won't be visible even though the value attribute is updated as the user types (issue #37).
			if (event.timeStamp - trackingClickStart > 100 || deviceIsIOS && window.top !== window && targetTagName === 'input') {
				this.targetElement = null;
				return false;
			}

			this.focus(targetElement);
			this.sendClick(targetElement, event);

			// Select elements need the event to go through on iOS 4, otherwise the selector menu won't open.
			// Also this breaks opening selects when VoiceOver is active on iOS6, iOS7 (and possibly others)
			if (!deviceIsIOS || targetTagName !== 'select') {
				this.targetElement = null;
				event.preventDefault();
			}

			return false;
		}

		if (deviceIsIOS && !deviceIsIOS4) {

			// Don't send a synthetic click event if the target element is contained within a parent layer that was scrolled
			// and this tap is being used to stop the scrolling (usually initiated by a fling - issue #42).
			scrollParent = targetElement.fastClickScrollParent;
			if (scrollParent && scrollParent.fastClickLastScrollTop !== scrollParent.scrollTop) {
				return true;
			}
		}

		// Prevent the actual click from going though - unless the target node is marked as requiring
		// real clicks or if it is in the whitelist in which case only non-programmatic clicks are permitted.
		if (!this.needsClick(targetElement)) {
			event.preventDefault();
			this.sendClick(targetElement, event);
		}

		return false;
	};

	/**
  * On touch cancel, stop tracking the click.
  *
  * @returns {void}
  */
	FastClick.prototype.onTouchCancel = function () {
		this.trackingClick = false;
		this.targetElement = null;
	};

	/**
  * Determine mouse events which should be permitted.
  *
  * @param {Event} event
  * @returns {boolean}
  */
	FastClick.prototype.onMouse = function (event) {

		// If a target element was never set (because a touch event was never fired) allow the event
		if (!this.targetElement) {
			return true;
		}

		if (event.forwardedTouchEvent) {
			return true;
		}

		// Programmatically generated events targeting a specific element should be permitted
		if (!event.cancelable) {
			return true;
		}

		// Derive and check the target element to see whether the mouse event needs to be permitted;
		// unless explicitly enabled, prevent non-touch click events from triggering actions,
		// to prevent ghost/doubleclicks.
		if (!this.needsClick(this.targetElement) || this.cancelNextClick) {

			// Prevent any user-added listeners declared on FastClick element from being fired.
			if (event.stopImmediatePropagation) {
				event.stopImmediatePropagation();
			} else {

				// Part of the hack for browsers that don't support Event#stopImmediatePropagation (e.g. Android 2)
				event.propagationStopped = true;
			}

			// Cancel the event
			event.stopPropagation();
			event.preventDefault();

			return false;
		}

		// If the mouse event is permitted, return true for the action to go through.
		return true;
	};

	/**
  * On actual clicks, determine whether this is a touch-generated click, a click action occurring
  * naturally after a delay after a touch (which needs to be cancelled to avoid duplication), or
  * an actual click which should be permitted.
  *
  * @param {Event} event
  * @returns {boolean}
  */
	FastClick.prototype.onClick = function (event) {
		var permitted;

		// It's possible for another FastClick-like library delivered with third-party code to fire a click event before FastClick does (issue #44). In that case, set the click-tracking flag back to false and return early. This will cause onTouchEnd to return early.
		if (this.trackingClick) {
			this.targetElement = null;
			this.trackingClick = false;
			return true;
		}

		// Very odd behaviour on iOS (issue #18): if a submit element is present inside a form and the user hits enter in the iOS simulator or clicks the Go button on the pop-up OS keyboard the a kind of 'fake' click event will be triggered with the submit-type input element as the target.
		if (event.target.type === 'submit' && event.detail === 0) {
			return true;
		}

		permitted = this.onMouse(event);

		// Only unset targetElement if the click is not permitted. This will ensure that the check for !targetElement in onMouse fails and the browser's click doesn't go through.
		if (!permitted) {
			this.targetElement = null;
		}

		// If clicks are permitted, return true for the action to go through.
		return permitted;
	};

	/**
  * Remove all FastClick's event listeners.
  *
  * @returns {void}
  */
	FastClick.prototype.destroy = function () {
		var layer = this.layer;

		if (deviceIsAndroid) {
			layer.removeEventListener('mouseover', this.onMouse, true);
			layer.removeEventListener('mousedown', this.onMouse, true);
			layer.removeEventListener('mouseup', this.onMouse, true);
		}

		layer.removeEventListener('click', this.onClick, true);
		layer.removeEventListener('touchstart', this.onTouchStart, false);
		layer.removeEventListener('touchmove', this.onTouchMove, false);
		layer.removeEventListener('touchend', this.onTouchEnd, false);
		layer.removeEventListener('touchcancel', this.onTouchCancel, false);
	};

	/**
  * Check whether FastClick is needed.
  *
  * @param {Element} layer The layer to listen on
  */
	FastClick.notNeeded = function (layer) {
		var metaViewport;
		var chromeVersion;
		var blackberryVersion;
		var firefoxVersion;

		// Devices that don't support touch don't need FastClick
		if (typeof window.ontouchstart === 'undefined') {
			return true;
		}

		// Chrome version - zero for other browsers
		chromeVersion = +(/Chrome\/([0-9]+)/.exec(navigator.userAgent) || [, 0])[1];

		if (chromeVersion) {

			if (deviceIsAndroid) {
				metaViewport = document.querySelector('meta[name=viewport]');

				if (metaViewport) {
					// Chrome on Android with user-scalable="no" doesn't need FastClick (issue #89)
					if (metaViewport.content.indexOf('user-scalable=no') !== -1) {
						return true;
					}
					// Chrome 32 and above with width=device-width or less don't need FastClick
					if (chromeVersion > 31 && document.documentElement.scrollWidth <= window.outerWidth) {
						return true;
					}
				}

				// Chrome desktop doesn't need FastClick (issue #15)
			} else {
				return true;
			}
		}

		if (deviceIsBlackBerry10) {
			blackberryVersion = navigator.userAgent.match(/Version\/([0-9]*)\.([0-9]*)/);

			// BlackBerry 10.3+ does not require Fastclick library.
			// https://github.com/ftlabs/fastclick/issues/251
			if (blackberryVersion[1] >= 10 && blackberryVersion[2] >= 3) {
				metaViewport = document.querySelector('meta[name=viewport]');

				if (metaViewport) {
					// user-scalable=no eliminates click delay.
					if (metaViewport.content.indexOf('user-scalable=no') !== -1) {
						return true;
					}
					// width=device-width (or less than device-width) eliminates click delay.
					if (document.documentElement.scrollWidth <= window.outerWidth) {
						return true;
					}
				}
			}
		}

		// IE10 with -ms-touch-action: none or manipulation, which disables double-tap-to-zoom (issue #97)
		if (layer.style.msTouchAction === 'none' || layer.style.touchAction === 'manipulation') {
			return true;
		}

		// Firefox version - zero for other browsers
		firefoxVersion = +(/Firefox\/([0-9]+)/.exec(navigator.userAgent) || [, 0])[1];

		if (firefoxVersion >= 27) {
			// Firefox 27+ does not have tap delay if the content is not zoomable - https://bugzilla.mozilla.org/show_bug.cgi?id=922896

			metaViewport = document.querySelector('meta[name=viewport]');
			if (metaViewport && (metaViewport.content.indexOf('user-scalable=no') !== -1 || document.documentElement.scrollWidth <= window.outerWidth)) {
				return true;
			}
		}

		// IE11: prefixed -ms-touch-action is no longer supported and it's recomended to use non-prefixed version
		// http://msdn.microsoft.com/en-us/library/windows/apps/Hh767313.aspx
		if (layer.style.touchAction === 'none' || layer.style.touchAction === 'manipulation') {
			return true;
		}

		return false;
	};

	/**
  * Factory method for creating a FastClick object
  *
  * @param {Element} layer The layer to listen on
  * @param {Object} [options={}] The options to override the defaults
  */
	FastClick.attach = function (layer, options) {
		return new FastClick(layer, options);
	};

	if ("function" === 'function' && _typeof(__webpack_require__(38)) === 'object' && __webpack_require__(38)) {

		// AMD. Register as an anonymous module.
		!(__WEBPACK_AMD_DEFINE_RESULT__ = (function () {
			return FastClick;
		}).call(exports, __webpack_require__, exports, module),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
	} else if (typeof module !== 'undefined' && module.exports) {
		module.exports = FastClick.attach;
		module.exports.FastClick = FastClick;
	} else {
		window.FastClick = FastClick;
	}
})();

/***/ }),
/* 87 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Material_Event__ = __webpack_require__(88);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__Material_Header__ = __webpack_require__(90);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__Material_Nav__ = __webpack_require__(92);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_3__Material_Search__ = __webpack_require__(96);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_4__Material_Sidebar__ = __webpack_require__(103);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_5__Material_Source__ = __webpack_require__(105);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_6__Material_Tabs__ = __webpack_require__(111);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */









/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Event: __WEBPACK_IMPORTED_MODULE_0__Material_Event__["a" /* default */],
  Header: __WEBPACK_IMPORTED_MODULE_1__Material_Header__["a" /* default */],
  Nav: __WEBPACK_IMPORTED_MODULE_2__Material_Nav__["a" /* default */],
  Search: __WEBPACK_IMPORTED_MODULE_3__Material_Search__["a" /* default */],
  Sidebar: __WEBPACK_IMPORTED_MODULE_4__Material_Sidebar__["a" /* default */],
  Source: __WEBPACK_IMPORTED_MODULE_5__Material_Source__["a" /* default */],
  Tabs: __WEBPACK_IMPORTED_MODULE_6__Material_Tabs__["a" /* default */]
});

/***/ }),
/* 88 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Event_Listener__ = __webpack_require__(39);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__Event_MatchMedia__ = __webpack_require__(89);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */




/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Listener: __WEBPACK_IMPORTED_MODULE_0__Event_Listener__["a" /* default */],
  MatchMedia: __WEBPACK_IMPORTED_MODULE_1__Event_MatchMedia__["a" /* default */]
});

/***/ }),
/* 89 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Listener__ = __webpack_require__(39);
function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

 // eslint-disable-line no-unused-vars

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var MatchMedia =

/**
 * Media query listener
 *
 * This class listens for state changes of media queries and automatically
 * switches the given listeners on or off.
 *
 * @constructor
 *
 * @property {Function} handler_ - Media query event handler
 *
 * @param {string} query - Media query to test for
 * @param {Listener} listener - Event listener
 */
function MatchMedia(query, listener) {
  _classCallCheck(this, MatchMedia);

  this.handler_ = function (mq) {
    if (mq.matches) listener.listen();else listener.unlisten();
  };

  /* Initialize media query listener */
  var media = window.matchMedia(query);
  media.addListener(this.handler_);

  /* Always check at initialization */
  this.handler_(media);
};

/* harmony default export */ __webpack_exports__["a"] = (MatchMedia);

/***/ }),
/* 90 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Header_Shadow__ = __webpack_require__(91);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */



/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Shadow: __WEBPACK_IMPORTED_MODULE_0__Header_Shadow__["a" /* default */]
});

/***/ }),
/* 91 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Shadow = function () {

  /**
   * Show or hide header shadow depending on page y-offset
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Content container
   * @property {HTMLElement} header_ - Header
   * @property {number} height_ - Offset height of previous nodes
   * @property {boolean} active_ - Header shadow state
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   * @param {(string|HTMLElement)} header - Selector or HTML element
   */
  function Shadow(el, header) {
    _classCallCheck(this, Shadow);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLElement) || !(ref.parentNode instanceof HTMLElement)) throw new ReferenceError();
    this.el_ = ref.parentNode;

    /* Retrieve header */
    ref = typeof header === "string" ? document.querySelector(header) : header;
    if (!(ref instanceof HTMLElement)) throw new ReferenceError();
    this.header_ = ref;

    /* Initialize height and state */
    this.height_ = 0;
    this.active_ = false;
  }

  /**
   * Calculate total height of previous nodes
   */


  _createClass(Shadow, [{
    key: "setup",
    value: function setup() {
      var current = this.el_;
      while (current = current.previousElementSibling) {
        if (!(current instanceof HTMLElement)) throw new ReferenceError();
        this.height_ += current.offsetHeight;
      }
      this.update();
    }

    /**
     * Update shadow state
     */

  }, {
    key: "update",
    value: function update() {
      var active = window.pageYOffset >= this.height_;
      if (active !== this.active_) this.header_.dataset.mdState = (this.active_ = active) ? "shadow" : "";
    }

    /**
     * Reset shadow state
     */

  }, {
    key: "reset",
    value: function reset() {
      this.header_.dataset.mdState = "";
      this.height_ = 0;
      this.active_ = false;
    }
  }]);

  return Shadow;
}();

/* harmony default export */ __webpack_exports__["a"] = (Shadow);

/***/ }),
/* 92 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Nav_Blur__ = __webpack_require__(93);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__Nav_Collapse__ = __webpack_require__(94);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_2__Nav_Scrolling__ = __webpack_require__(95);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */





/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Blur: __WEBPACK_IMPORTED_MODULE_0__Nav_Blur__["a" /* default */],
  Collapse: __WEBPACK_IMPORTED_MODULE_1__Nav_Collapse__["a" /* default */],
  Scrolling: __WEBPACK_IMPORTED_MODULE_2__Nav_Scrolling__["a" /* default */]
});

/***/ }),
/* 93 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Blur = function () {

  /**
   * Blur links within the table of contents above current page y-offset
   *
   * @constructor
   *
   * @property {NodeList<HTMLElement>} els_ - Table of contents links
   * @property {Array<HTMLElement>} anchors_ - Referenced anchor nodes
   * @property {number} index_ - Current link index
   * @property {number} offset_ - Current page y-offset
   * @property {boolean} dir_ - Scroll direction change
   *
   * @param {(string|NodeList<HTMLElement>)} els - Selector or HTML elements
   */
  function Blur(els) {
    _classCallCheck(this, Blur);

    this.els_ = typeof els === "string" ? document.querySelectorAll(els) : els;

    /* Initialize index and page y-offset */
    this.index_ = 0;
    this.offset_ = window.pageYOffset;

    /* Necessary state to correctly reset the index */
    this.dir_ = false;

    /* Index anchor node offsets for fast lookup */
    this.anchors_ = [].reduce.call(this.els_, function (anchors, el) {
      return anchors.concat(document.getElementById(el.hash.substring(1)) || []);
    }, []);
  }

  /**
   * Initialize blur states
   */


  _createClass(Blur, [{
    key: "setup",
    value: function setup() {
      this.update();
    }

    /**
     * Update blur states
     *
     * Deduct the static offset of the header (56px) and sidebar offset (24px),
     * see _permalinks.scss for more information.
     */

  }, {
    key: "update",
    value: function update() {
      var offset = window.pageYOffset;
      var dir = this.offset_ - offset < 0;

      /* Hack: reset index if direction changed to catch very fast scrolling,
         because otherwise we would have to register a timer and that sucks */
      if (this.dir_ !== dir) this.index_ = dir ? this.index_ = 0 : this.index_ = this.els_.length - 1;

      /* Exit when there are no anchors */
      if (this.anchors_.length === 0) return;

      /* Scroll direction is down */
      if (this.offset_ <= offset) {
        for (var i = this.index_ + 1; i < this.els_.length; i++) {
          if (this.anchors_[i].offsetTop - (56 + 24) <= offset) {
            if (i > 0) this.els_[i - 1].dataset.mdState = "blur";
            this.index_ = i;
          } else {
            break;
          }
        }

        /* Scroll direction is up */
      } else {
        for (var _i = this.index_; _i >= 0; _i--) {
          if (this.anchors_[_i].offsetTop - (56 + 24) > offset) {
            if (_i > 0) this.els_[_i - 1].dataset.mdState = "";
          } else {
            this.index_ = _i;
            break;
          }
        }
      }

      /* Remember current offset and direction for next iteration */
      this.offset_ = offset;
      this.dir_ = dir;
    }

    /**
     * Reset blur states
     */

  }, {
    key: "reset",
    value: function reset() {
      Array.prototype.forEach.call(this.els_, function (el) {
        el.dataset.mdState = "";
      });

      /* Reset index and page y-offset */
      this.index_ = 0;
      this.offset_ = window.pageYOffset;
    }
  }]);

  return Blur;
}();

/* harmony default export */ __webpack_exports__["a"] = (Blur);

/***/ }),
/* 94 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Collapse = function () {

  /**
   * Expand or collapse navigation on toggle
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Navigation list
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   */
  function Collapse(el) {
    _classCallCheck(this, Collapse);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLElement)) throw new ReferenceError();
    this.el_ = ref;
  }

  /**
   * Animate expand and collapse smoothly
   *
   * Internet Explorer 11 is very slow at recognizing changes on the dataset
   * which results in the menu not expanding or collapsing properly. THerefore,
   * for reasons of compatibility, the attribute accessors are used.
   */


  _createClass(Collapse, [{
    key: "update",
    value: function update() {
      var _this = this;

      var current = this.el_.getBoundingClientRect().height;

      /* Expanded, so collapse */
      if (current) {
        this.el_.style.maxHeight = current + "px";
        requestAnimationFrame(function () {
          _this.el_.setAttribute("data-md-state", "animate");
          _this.el_.style.maxHeight = "0px";
        });

        /* Collapsed, so expand */
      } else {
        this.el_.setAttribute("data-md-state", "expand");
        this.el_.style.maxHeight = "";

        /* Read height and unset pseudo-toggled state */
        var height = this.el_.getBoundingClientRect().height;
        this.el_.removeAttribute("data-md-state");

        /* Set initial state and animate */
        this.el_.style.maxHeight = "0px";
        requestAnimationFrame(function () {
          _this.el_.setAttribute("data-md-state", "animate");
          _this.el_.style.maxHeight = height + "px";
        });
      }

      /* Remove state on end of transition */
      var end = function end(ev) {
        var target = ev.target;
        if (!(target instanceof HTMLElement)) throw new ReferenceError();

        /* Reset height and state */
        target.removeAttribute("data-md-state");
        target.style.maxHeight = "";

        /* Only fire once, so directly remove event listener */
        target.removeEventListener("transitionend", end);
      };
      this.el_.addEventListener("transitionend", end, false);
    }

    /**
     * Reset height and pseudo-toggled state
     */

  }, {
    key: "reset",
    value: function reset() {
      this.el_.dataset.mdState = "";
      this.el_.style.maxHeight = "";
    }
  }]);

  return Collapse;
}();

/* harmony default export */ __webpack_exports__["a"] = (Collapse);

/***/ }),
/* 95 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Scrolling = function () {

  /**
   * Set overflow scrolling on the current active pane (for iOS)
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Primary navigation
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   */
  function Scrolling(el) {
    _classCallCheck(this, Scrolling);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLElement)) throw new ReferenceError();
    this.el_ = ref;
  }

  /**
   * Setup panes
   */


  _createClass(Scrolling, [{
    key: "setup",
    value: function setup() {

      /* Initially set overflow scrolling on main pane */
      var main = this.el_.children[this.el_.children.length - 1];
      main.style.webkitOverflowScrolling = "touch";

      /* Find all toggles and check which one is active */
      var toggles = this.el_.querySelectorAll("[data-md-toggle]");
      Array.prototype.forEach.call(toggles, function (toggle) {
        if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
        if (toggle.checked) {

          /* Find corresponding navigational pane */
          var pane = toggle.nextElementSibling;
          if (!(pane instanceof HTMLElement)) throw new ReferenceError();
          while (pane.tagName !== "NAV" && pane.nextElementSibling) {
            pane = pane.nextElementSibling;
          } /* Check references */
          if (!(toggle.parentNode instanceof HTMLElement) || !(toggle.parentNode.parentNode instanceof HTMLElement)) throw new ReferenceError();

          /* Find current and parent list elements */
          var parent = toggle.parentNode.parentNode;
          var target = pane.children[pane.children.length - 1];

          /* Always reset all lists when transitioning */
          parent.style.webkitOverflowScrolling = "";
          target.style.webkitOverflowScrolling = "touch";
        }
      });
    }

    /**
     * Update active panes
     *
     * @param {Event} ev - Change event
     */

  }, {
    key: "update",
    value: function update(ev) {
      var target = ev.target;
      if (!(target instanceof HTMLElement)) throw new ReferenceError();

      /* Find corresponding navigational pane */
      var pane = target.nextElementSibling;
      if (!(pane instanceof HTMLElement)) throw new ReferenceError();
      while (pane.tagName !== "NAV" && pane.nextElementSibling) {
        pane = pane.nextElementSibling;
      } /* Check references */
      if (!(target.parentNode instanceof HTMLElement) || !(target.parentNode.parentNode instanceof HTMLElement)) throw new ReferenceError();

      /* Find parent and active panes */
      var parent = target.parentNode.parentNode;
      var active = pane.children[pane.children.length - 1];

      /* Always reset all lists when transitioning */
      parent.style.webkitOverflowScrolling = "";
      active.style.webkitOverflowScrolling = "";

      /* Set overflow scrolling on parent pane */
      if (!target.checked) {
        var end = function end() {
          if (pane instanceof HTMLElement) {
            parent.style.webkitOverflowScrolling = "touch";
            pane.removeEventListener("transitionend", end);
          }
        };
        pane.addEventListener("transitionend", end, false);
      }

      /* Set overflow scrolling on active pane */
      if (target.checked) {
        var _end = function _end() {
          if (pane instanceof HTMLElement) {
            active.style.webkitOverflowScrolling = "touch";
            pane.removeEventListener("transitionend", _end);
          }
        };
        pane.addEventListener("transitionend", _end, false);
      }
    }

    /**
     * Reset panes
     */

  }, {
    key: "reset",
    value: function reset() {

      /* Reset overflow scrolling on main pane */
      this.el_.children[1].style.webkitOverflowScrolling = "";

      /* Find all toggles and check which one is active */
      var toggles = this.el_.querySelectorAll("[data-md-toggle]");
      Array.prototype.forEach.call(toggles, function (toggle) {
        if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
        if (toggle.checked) {

          /* Find corresponding navigational pane */
          var pane = toggle.nextElementSibling;
          if (!(pane instanceof HTMLElement)) throw new ReferenceError();
          while (pane.tagName !== "NAV" && pane.nextElementSibling) {
            pane = pane.nextElementSibling;
          } /* Check references */
          if (!(toggle.parentNode instanceof HTMLElement) || !(toggle.parentNode.parentNode instanceof HTMLElement)) throw new ReferenceError();

          /* Find parent and active panes */
          var parent = toggle.parentNode.parentNode;
          var active = pane.children[pane.children.length - 1];

          /* Always reset all lists when transitioning */
          parent.style.webkitOverflowScrolling = "";
          active.style.webkitOverflowScrolling = "";
        }
      });
    }
  }]);

  return Scrolling;
}();

/* harmony default export */ __webpack_exports__["a"] = (Scrolling);

/***/ }),
/* 96 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Search_Lock__ = __webpack_require__(97);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__Search_Result__ = __webpack_require__(98);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */




/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Lock: __WEBPACK_IMPORTED_MODULE_0__Search_Lock__["a" /* default */],
  Result: __WEBPACK_IMPORTED_MODULE_1__Search_Result__["a" /* default */]
});

/***/ }),
/* 97 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Lock = function () {

  /**
   * Lock body for full-screen search modal
   *
   * @constructor
   *
   * @property {HTMLInputElement} el_ - Lock toggle
   * @property {HTMLElement} lock_ - Element to lock (document body)
   * @property {number} offset_ - Current page y-offset
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   */
  function Lock(el) {
    _classCallCheck(this, Lock);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLInputElement)) throw new ReferenceError();
    this.el_ = ref;

    /* Retrieve element to lock (= body) */
    if (!document.body) throw new ReferenceError();
    this.lock_ = document.body;
  }

  /**
   * Setup locked state
   */


  _createClass(Lock, [{
    key: "setup",
    value: function setup() {
      this.update();
    }

    /**
     * Update locked state
     */

  }, {
    key: "update",
    value: function update() {
      var _this = this;

      /* Entering search mode */
      if (this.el_.checked) {
        this.offset_ = window.pageYOffset;

        /* Scroll to top after transition, to omit flickering */
        setTimeout(function () {
          window.scrollTo(0, 0);

          /* Lock body after finishing transition */
          if (_this.el_.checked) {
            _this.lock_.dataset.mdState = "lock";
          }
        }, 400);

        /* Exiting search mode */
      } else {
        this.lock_.dataset.mdState = "";

        /* Scroll to former position, but wait for 100ms to prevent flashes on
           iOS. A short timeout seems to do the trick */
        setTimeout(function () {
          if (typeof _this.offset_ !== "undefined") window.scrollTo(0, _this.offset_);
        }, 100);
      }
    }

    /**
     * Reset locked state and page y-offset
     */

  }, {
    key: "reset",
    value: function reset() {
      if (this.lock_.dataset.mdState === "lock") window.scrollTo(0, this.offset_);
      this.lock_.dataset.mdState = "";
    }
  }]);

  return Lock;
}();

/* harmony default export */ __webpack_exports__["a"] = (Lock);

/***/ }),
/* 98 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(Jsx) {/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_escape_string_regexp__ = __webpack_require__(99);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_escape_string_regexp___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0_escape_string_regexp__);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr__ = __webpack_require__(100);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr__);
var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _toConsumableArray(arr) { if (Array.isArray(arr)) { for (var i = 0, arr2 = Array(arr.length); i < arr.length; i++) { arr2[i] = arr[i]; } return arr2; } else { return Array.from(arr); } }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */




/* ----------------------------------------------------------------------------
 * Functions
 * ------------------------------------------------------------------------- */

/**
 * Truncate a string after the given number of character
 *
 * This is not a reasonable approach, since the summaries kind of suck. It
 * would be better to create something more intelligent, highlighting the
 * search occurrences and making a better summary out of it.
 *
 * @param {string} string - String to be truncated
 * @param {number} n - Number of characters
 * @return {string} Truncated string
 */
var truncate = function truncate(string, n) {
  var i = n;
  if (string.length > i) {
    while (string[i] !== " " && --i > 0) {}
    return string.substring(0, i) + "...";
  }
  return string;
};

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Result = function () {

  /**
   * Perform search and update results on keyboard events
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Search result container
   * @property {(Array<Object>|Function)} data_ - Raw document data
   * @property {Object} docs_ - Indexed documents
   * @property {HTMLElement} meta_ - Search meta information
   * @property {HTMLElement} list_ - Search result list
   * @property {Array<string>} lang_ - Search languages
   * @property {Object} message_ - Search result messages
   * @property {Object} index_ - Search index
   * @property {Array<Function>} stack_ - Search result stack
   * @property {string} value_ - Last input value
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   * @param {(Array<Object>|Function)} data - Function providing data or array
   */
  function Result(el, data) {
    _classCallCheck(this, Result);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLElement)) throw new ReferenceError();
    this.el_ = ref;

    /* Retrieve metadata and list element */

    var _Array$prototype$slic = Array.prototype.slice.call(this.el_.children),
        _Array$prototype$slic2 = _slicedToArray(_Array$prototype$slic, 2),
        meta = _Array$prototype$slic2[0],
        list = _Array$prototype$slic2[1];

    /* Set data, metadata and list elements */


    this.data_ = data;
    this.meta_ = meta;
    this.list_ = list;

    /* Load messages for metadata display */
    this.message_ = {
      placeholder: this.meta_.textContent,
      none: this.meta_.dataset.mdLangResultNone,
      one: this.meta_.dataset.mdLangResultOne,
      other: this.meta_.dataset.mdLangResultOther

      /* Override tokenizer separator, if given */
    };if (this.el_.dataset.mdLangTokenizer.length) __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.tokenizer.separator = this.el_.dataset.mdLangTokenizer;

    /* Load search languages */
    this.lang_ = this.el_.dataset.mdLangSearch.split(",").filter(Boolean).map(function (lang) {
      return lang.trim();
    });
  }

  /**
   * Update search results
   *
   * @param {Event} ev - Input or focus event
   */


  _createClass(Result, [{
    key: "update",
    value: function update(ev) {
      var _this = this;

      /* Initialize index, if this has not be done yet */
      if (ev.type === "focus" && !this.index_) {

        /* Initialize index */
        var init = function init(data) {

          /* Preprocess and index sections and documents */
          _this.docs_ = data.reduce(function (docs, doc) {
            var _doc$location$split = doc.location.split("#"),
                _doc$location$split2 = _slicedToArray(_doc$location$split, 2),
                path = _doc$location$split2[0],
                hash = _doc$location$split2[1];

            /* Associate section with parent document */


            if (hash) {
              doc.parent = docs.get(path);

              /* Override page title with document title if first section */
              if (doc.parent && !doc.parent.done) {
                doc.parent.title = doc.title;
                doc.parent.text = doc.text;
                doc.parent.done = true;
              }
            }

            /* Some cleanup on the text */
            doc.text = doc.text.replace(/\n/g, " ") /* Remove newlines */
            .replace(/\s+/g, " ") /* Compact whitespace */
            .replace(/\s+([,.:;!?])/g, /* Correct punctuation */
            function (_, char) {
              return char;
            });

            /* Index sections and documents, but skip top-level headline */
            if (!doc.parent || doc.parent.title !== doc.title) docs.set(doc.location, doc);
            return docs;
          }, new Map());

          /* eslint-disable no-invalid-this */
          var docs = _this.docs_,
              lang = _this.lang_;

          /* Create stack and index */
          _this.stack_ = [];
          _this.index_ = __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default()(function () {
            var _this2 = this;

            /* Remove stemmer, as it cripples search experience */
            this.pipeline.reset();
            this.pipeline.add(__WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.trimmer, __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.stopWordFilter);

            /* Set up alternate search languages */
            if (lang.length === 1) {
              this.use(__WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a[lang[0]]);
            } else if (lang.length > 1) {
              this.use(__WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.multiLanguage.apply(__WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a, _toConsumableArray(lang)));
            }

            /* Index fields */
            this.field("title", { boost: 10 });
            this.field("text");
            this.ref("location");

            /* Index documents */
            docs.forEach(function (doc) {
              return _this2.add(doc);
            });
          });

          /* Register event handler for lazy rendering */
          var container = _this.el_.parentNode;
          if (!(container instanceof HTMLElement)) throw new ReferenceError();
          container.addEventListener("scroll", function () {
            while (_this.stack_.length && container.scrollTop + container.offsetHeight >= container.scrollHeight - 16) {
              _this.stack_.splice(0, 10).forEach(function (render) {
                return render();
              });
            }
          });
        };
        /* eslint-enable no-invalid-this */

        /* Initialize index after short timeout to account for transition */
        setTimeout(function () {
          return typeof _this.data_ === "function" ? _this.data_().then(init) : init(_this.data_);
        }, 250);

        /* Execute search on new input event */
      } else if (ev.type === "focus" || ev.type === "keyup") {
        var target = ev.target;
        if (!(target instanceof HTMLInputElement)) throw new ReferenceError();

        /* Abort early, if index is not build or input hasn't changed */
        if (!this.index_ || target.value === this.value_) return;

        /* Clear current list */
        while (this.list_.firstChild) {
          this.list_.removeChild(this.list_.firstChild);
        } /* Abort early, if search input is empty */
        this.value_ = target.value;
        if (this.value_.length === 0) {
          this.meta_.textContent = this.message_.placeholder;
          return;
        }

        /* Perform search on index and group sections by document */
        var result = this.index_

        /* Append trailing wildcard to all terms for prefix querying */
        .query(function (query) {
          _this.value_.toLowerCase().split(" ").filter(Boolean).forEach(function (term) {
            query.term(term, { wildcard: __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.Query.wildcard.TRAILING });
          });
        })

        /* Process query results */
        .reduce(function (items, item) {
          var doc = _this.docs_.get(item.ref);
          if (doc.parent) {
            var ref = doc.parent.location;
            items.set(ref, (items.get(ref) || []).concat(item));
          } else {
            var _ref = doc.location;
            items.set(_ref, items.get(_ref) || []);
          }
          return items;
        }, new Map());

        /* Assemble regular expressions for matching */
        var query = __WEBPACK_IMPORTED_MODULE_0_escape_string_regexp___default()(this.value_.trim()).replace(new RegExp(__WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.tokenizer.separator, "img"), "|");
        var match = new RegExp("(^|" + __WEBPACK_IMPORTED_MODULE_1_expose_loader_lunr_lunr___default.a.tokenizer.separator + ")(" + query + ")", "img");
        var highlight = function highlight(_, separator, token) {
          return separator + "<em>" + token + "</em>";
        };

        /* Reset stack and render results */
        this.stack_ = [];
        result.forEach(function (items, ref) {
          var _stack_;

          var doc = _this.docs_.get(ref);

          /* Render article */
          var article = Jsx.createElement(
            "li",
            { "class": "md-search-result__item" },
            Jsx.createElement(
              "a",
              { href: doc.location, title: doc.title,
                "class": "md-search-result__link" },
              Jsx.createElement(
                "article",
                { "class": "md-search-result__article md-search-result__article--document" },
                Jsx.createElement(
                  "h1",
                  { "class": "md-search-result__title" },
                  { __html: doc.title.replace(match, highlight) }
                ),
                doc.text.length ? Jsx.createElement(
                  "p",
                  { "class": "md-search-result__teaser" },
                  { __html: doc.text.replace(match, highlight) }
                ) : {}
              )
            )
          );

          /* Render sections for article */
          var sections = items.map(function (item) {
            return function () {
              var section = _this.docs_.get(item.ref);
              article.appendChild(Jsx.createElement(
                "a",
                { href: section.location, title: section.title,
                  "class": "md-search-result__link", "data-md-rel": "anchor" },
                Jsx.createElement(
                  "article",
                  { "class": "md-search-result__article" },
                  Jsx.createElement(
                    "h1",
                    { "class": "md-search-result__title" },
                    { __html: section.title.replace(match, highlight) }
                  ),
                  section.text.length ? Jsx.createElement(
                    "p",
                    { "class": "md-search-result__teaser" },
                    { __html: truncate(section.text.replace(match, highlight), 400)
                    }
                  ) : {}
                )
              ));
            };
          });

          /* Push articles and section renderers onto stack */
          (_stack_ = _this.stack_).push.apply(_stack_, [function () {
            return _this.list_.appendChild(article);
          }].concat(_toConsumableArray(sections)));
        });

        /* Gradually add results as long as the height of the container grows */
        var container = this.el_.parentNode;
        if (!(container instanceof HTMLElement)) throw new ReferenceError();
        while (this.stack_.length && container.offsetHeight >= container.scrollHeight - 16) {
          this.stack_.shift()();
        } /* Bind click handlers for anchors */
        var anchors = this.list_.querySelectorAll("[data-md-rel=anchor]");
        Array.prototype.forEach.call(anchors, function (anchor) {
          anchor.addEventListener("click", function (ev2) {
            var toggle = document.querySelector("[data-md-toggle=search]");
            if (!(toggle instanceof HTMLInputElement)) throw new ReferenceError();
            if (toggle.checked) {
              toggle.checked = false;
              toggle.dispatchEvent(new CustomEvent("change"));
            }

            /* Hack: prevent default, as the navigation needs to be delayed due
               to the search body lock on mobile */
            ev2.preventDefault();
            setTimeout(function () {
              document.location.href = anchor.href;
            }, 100);
          });
        });

        /* Update search metadata */
        switch (result.size) {
          case 0:
            this.meta_.textContent = this.message_.none;
            break;
          case 1:
            this.meta_.textContent = this.message_.one;
            break;
          default:
            this.meta_.textContent = this.message_.other.replace("#", result.size);
        }
      }
    }
  }]);

  return Result;
}();

/* harmony default export */ __webpack_exports__["a"] = (Result);
/* WEBPACK VAR INJECTION */}.call(__webpack_exports__, __webpack_require__(26)))

/***/ }),
/* 99 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var matchOperatorsRe = /[|\\{}()[\]^$+*?.]/g;

module.exports = function (str) {
	if (typeof str !== 'string') {
		throw new TypeError('Expected a string');
	}

	return str.replace(matchOperatorsRe, '\\$&');
};

/***/ }),
/* 100 */
/***/ (function(module, exports, __webpack_require__) {

/* WEBPACK VAR INJECTION */(function(global) {module.exports = global["lunr"] = __webpack_require__(102);
/* WEBPACK VAR INJECTION */}.call(exports, __webpack_require__(101)))

/***/ }),
/* 101 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";


var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var g;

// This works in non-strict mode
g = function () {
	return this;
}();

try {
	// This works if eval is allowed (see CSP)
	g = g || Function("return this")() || (1, eval)("this");
} catch (e) {
	// This works if the window reference is available
	if ((typeof window === "undefined" ? "undefined" : _typeof(window)) === "object") g = window;
}

// g can still be undefined, but nothing to do about it...
// We return undefined, instead of nothing here, so it's
// easier to handle this case. if(!global) { ...}

module.exports = g;

/***/ }),
/* 102 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_RESULT__;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

/**
 * lunr - http://lunrjs.com - A bit like Solr, but much smaller and not as bright - 2.3.8
 * Copyright (C) 2019 Oliver Nightingale
 * @license MIT
 */

;(function () {

  /**
   * A convenience function for configuring and constructing
   * a new lunr Index.
   *
   * A lunr.Builder instance is created and the pipeline setup
   * with a trimmer, stop word filter and stemmer.
   *
   * This builder object is yielded to the configuration function
   * that is passed as a parameter, allowing the list of fields
   * and other builder parameters to be customised.
   *
   * All documents _must_ be added within the passed config function.
   *
   * @example
   * var idx = lunr(function () {
   *   this.field('title')
   *   this.field('body')
   *   this.ref('id')
   *
   *   documents.forEach(function (doc) {
   *     this.add(doc)
   *   }, this)
   * })
   *
   * @see {@link lunr.Builder}
   * @see {@link lunr.Pipeline}
   * @see {@link lunr.trimmer}
   * @see {@link lunr.stopWordFilter}
   * @see {@link lunr.stemmer}
   * @namespace {function} lunr
   */
  var lunr = function lunr(config) {
    var builder = new lunr.Builder();

    builder.pipeline.add(lunr.trimmer, lunr.stopWordFilter, lunr.stemmer);

    builder.searchPipeline.add(lunr.stemmer);

    config.call(builder, builder);
    return builder.build();
  };

  lunr.version = "2.3.8";
  /*!
   * lunr.utils
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * A namespace containing utils for the rest of the lunr library
   * @namespace lunr.utils
   */
  lunr.utils = {};

  /**
   * Print a warning message to the console.
   *
   * @param {String} message The message to be printed.
   * @memberOf lunr.utils
   * @function
   */
  lunr.utils.warn = function (global) {
    /* eslint-disable no-console */
    return function (message) {
      if (global.console && console.warn) {
        console.warn(message);
      }
    };
    /* eslint-enable no-console */
  }(this);

  /**
   * Convert an object to a string.
   *
   * In the case of `null` and `undefined` the function returns
   * the empty string, in all other cases the result of calling
   * `toString` on the passed object is returned.
   *
   * @param {Any} obj The object to convert to a string.
   * @return {String} string representation of the passed object.
   * @memberOf lunr.utils
   */
  lunr.utils.asString = function (obj) {
    if (obj === void 0 || obj === null) {
      return "";
    } else {
      return obj.toString();
    }
  };

  /**
   * Clones an object.
   *
   * Will create a copy of an existing object such that any mutations
   * on the copy cannot affect the original.
   *
   * Only shallow objects are supported, passing a nested object to this
   * function will cause a TypeError.
   *
   * Objects with primitives, and arrays of primitives are supported.
   *
   * @param {Object} obj The object to clone.
   * @return {Object} a clone of the passed object.
   * @throws {TypeError} when a nested object is passed.
   * @memberOf Utils
   */
  lunr.utils.clone = function (obj) {
    if (obj === null || obj === undefined) {
      return obj;
    }

    var clone = Object.create(null),
        keys = Object.keys(obj);

    for (var i = 0; i < keys.length; i++) {
      var key = keys[i],
          val = obj[key];

      if (Array.isArray(val)) {
        clone[key] = val.slice();
        continue;
      }

      if (typeof val === 'string' || typeof val === 'number' || typeof val === 'boolean') {
        clone[key] = val;
        continue;
      }

      throw new TypeError("clone is not deep and does not support nested objects");
    }

    return clone;
  };
  lunr.FieldRef = function (docRef, fieldName, stringValue) {
    this.docRef = docRef;
    this.fieldName = fieldName;
    this._stringValue = stringValue;
  };

  lunr.FieldRef.joiner = "/";

  lunr.FieldRef.fromString = function (s) {
    var n = s.indexOf(lunr.FieldRef.joiner);

    if (n === -1) {
      throw "malformed field ref string";
    }

    var fieldRef = s.slice(0, n),
        docRef = s.slice(n + 1);

    return new lunr.FieldRef(docRef, fieldRef, s);
  };

  lunr.FieldRef.prototype.toString = function () {
    if (this._stringValue == undefined) {
      this._stringValue = this.fieldName + lunr.FieldRef.joiner + this.docRef;
    }

    return this._stringValue;
  };
  /*!
   * lunr.Set
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * A lunr set.
   *
   * @constructor
   */
  lunr.Set = function (elements) {
    this.elements = Object.create(null);

    if (elements) {
      this.length = elements.length;

      for (var i = 0; i < this.length; i++) {
        this.elements[elements[i]] = true;
      }
    } else {
      this.length = 0;
    }
  };

  /**
   * A complete set that contains all elements.
   *
   * @static
   * @readonly
   * @type {lunr.Set}
   */
  lunr.Set.complete = {
    intersect: function intersect(other) {
      return other;
    },

    union: function union(other) {
      return other;
    },

    contains: function contains() {
      return true;
    }

    /**
     * An empty set that contains no elements.
     *
     * @static
     * @readonly
     * @type {lunr.Set}
     */
  };lunr.Set.empty = {
    intersect: function intersect() {
      return this;
    },

    union: function union(other) {
      return other;
    },

    contains: function contains() {
      return false;
    }

    /**
     * Returns true if this set contains the specified object.
     *
     * @param {object} object - Object whose presence in this set is to be tested.
     * @returns {boolean} - True if this set contains the specified object.
     */
  };lunr.Set.prototype.contains = function (object) {
    return !!this.elements[object];
  };

  /**
   * Returns a new set containing only the elements that are present in both
   * this set and the specified set.
   *
   * @param {lunr.Set} other - set to intersect with this set.
   * @returns {lunr.Set} a new set that is the intersection of this and the specified set.
   */

  lunr.Set.prototype.intersect = function (other) {
    var a,
        b,
        elements,
        intersection = [];

    if (other === lunr.Set.complete) {
      return this;
    }

    if (other === lunr.Set.empty) {
      return other;
    }

    if (this.length < other.length) {
      a = this;
      b = other;
    } else {
      a = other;
      b = this;
    }

    elements = Object.keys(a.elements);

    for (var i = 0; i < elements.length; i++) {
      var element = elements[i];
      if (element in b.elements) {
        intersection.push(element);
      }
    }

    return new lunr.Set(intersection);
  };

  /**
   * Returns a new set combining the elements of this and the specified set.
   *
   * @param {lunr.Set} other - set to union with this set.
   * @return {lunr.Set} a new set that is the union of this and the specified set.
   */

  lunr.Set.prototype.union = function (other) {
    if (other === lunr.Set.complete) {
      return lunr.Set.complete;
    }

    if (other === lunr.Set.empty) {
      return this;
    }

    return new lunr.Set(Object.keys(this.elements).concat(Object.keys(other.elements)));
  };
  /**
   * A function to calculate the inverse document frequency for
   * a posting. This is shared between the builder and the index
   *
   * @private
   * @param {object} posting - The posting for a given term
   * @param {number} documentCount - The total number of documents.
   */
  lunr.idf = function (posting, documentCount) {
    var documentsWithTerm = 0;

    for (var fieldName in posting) {
      if (fieldName == '_index') continue; // Ignore the term index, its not a field
      documentsWithTerm += Object.keys(posting[fieldName]).length;
    }

    var x = (documentCount - documentsWithTerm + 0.5) / (documentsWithTerm + 0.5);

    return Math.log(1 + Math.abs(x));
  };

  /**
   * A token wraps a string representation of a token
   * as it is passed through the text processing pipeline.
   *
   * @constructor
   * @param {string} [str=''] - The string token being wrapped.
   * @param {object} [metadata={}] - Metadata associated with this token.
   */
  lunr.Token = function (str, metadata) {
    this.str = str || "";
    this.metadata = metadata || {};
  };

  /**
   * Returns the token string that is being wrapped by this object.
   *
   * @returns {string}
   */
  lunr.Token.prototype.toString = function () {
    return this.str;
  };

  /**
   * A token update function is used when updating or optionally
   * when cloning a token.
   *
   * @callback lunr.Token~updateFunction
   * @param {string} str - The string representation of the token.
   * @param {Object} metadata - All metadata associated with this token.
   */

  /**
   * Applies the given function to the wrapped string token.
   *
   * @example
   * token.update(function (str, metadata) {
   *   return str.toUpperCase()
   * })
   *
   * @param {lunr.Token~updateFunction} fn - A function to apply to the token string.
   * @returns {lunr.Token}
   */
  lunr.Token.prototype.update = function (fn) {
    this.str = fn(this.str, this.metadata);
    return this;
  };

  /**
   * Creates a clone of this token. Optionally a function can be
   * applied to the cloned token.
   *
   * @param {lunr.Token~updateFunction} [fn] - An optional function to apply to the cloned token.
   * @returns {lunr.Token}
   */
  lunr.Token.prototype.clone = function (fn) {
    fn = fn || function (s) {
      return s;
    };
    return new lunr.Token(fn(this.str, this.metadata), this.metadata);
  };
  /*!
   * lunr.tokenizer
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * A function for splitting a string into tokens ready to be inserted into
   * the search index. Uses `lunr.tokenizer.separator` to split strings, change
   * the value of this property to change how strings are split into tokens.
   *
   * This tokenizer will convert its parameter to a string by calling `toString` and
   * then will split this string on the character in `lunr.tokenizer.separator`.
   * Arrays will have their elements converted to strings and wrapped in a lunr.Token.
   *
   * Optional metadata can be passed to the tokenizer, this metadata will be cloned and
   * added as metadata to every token that is created from the object to be tokenized.
   *
   * @static
   * @param {?(string|object|object[])} obj - The object to convert into tokens
   * @param {?object} metadata - Optional metadata to associate with every token
   * @returns {lunr.Token[]}
   * @see {@link lunr.Pipeline}
   */
  lunr.tokenizer = function (obj, metadata) {
    if (obj == null || obj == undefined) {
      return [];
    }

    if (Array.isArray(obj)) {
      return obj.map(function (t) {
        return new lunr.Token(lunr.utils.asString(t).toLowerCase(), lunr.utils.clone(metadata));
      });
    }

    var str = obj.toString().toLowerCase(),
        len = str.length,
        tokens = [];

    for (var sliceEnd = 0, sliceStart = 0; sliceEnd <= len; sliceEnd++) {
      var char = str.charAt(sliceEnd),
          sliceLength = sliceEnd - sliceStart;

      if (char.match(lunr.tokenizer.separator) || sliceEnd == len) {

        if (sliceLength > 0) {
          var tokenMetadata = lunr.utils.clone(metadata) || {};
          tokenMetadata["position"] = [sliceStart, sliceLength];
          tokenMetadata["index"] = tokens.length;

          tokens.push(new lunr.Token(str.slice(sliceStart, sliceEnd), tokenMetadata));
        }

        sliceStart = sliceEnd + 1;
      }
    }

    return tokens;
  };

  /**
   * The separator used to split a string into tokens. Override this property to change the behaviour of
   * `lunr.tokenizer` behaviour when tokenizing strings. By default this splits on whitespace and hyphens.
   *
   * @static
   * @see lunr.tokenizer
   */
  lunr.tokenizer.separator = /[\s\-]+/;
  /*!
   * lunr.Pipeline
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * lunr.Pipelines maintain an ordered list of functions to be applied to all
   * tokens in documents entering the search index and queries being ran against
   * the index.
   *
   * An instance of lunr.Index created with the lunr shortcut will contain a
   * pipeline with a stop word filter and an English language stemmer. Extra
   * functions can be added before or after either of these functions or these
   * default functions can be removed.
   *
   * When run the pipeline will call each function in turn, passing a token, the
   * index of that token in the original list of all tokens and finally a list of
   * all the original tokens.
   *
   * The output of functions in the pipeline will be passed to the next function
   * in the pipeline. To exclude a token from entering the index the function
   * should return undefined, the rest of the pipeline will not be called with
   * this token.
   *
   * For serialisation of pipelines to work, all functions used in an instance of
   * a pipeline should be registered with lunr.Pipeline. Registered functions can
   * then be loaded. If trying to load a serialised pipeline that uses functions
   * that are not registered an error will be thrown.
   *
   * If not planning on serialising the pipeline then registering pipeline functions
   * is not necessary.
   *
   * @constructor
   */
  lunr.Pipeline = function () {
    this._stack = [];
  };

  lunr.Pipeline.registeredFunctions = Object.create(null);

  /**
   * A pipeline function maps lunr.Token to lunr.Token. A lunr.Token contains the token
   * string as well as all known metadata. A pipeline function can mutate the token string
   * or mutate (or add) metadata for a given token.
   *
   * A pipeline function can indicate that the passed token should be discarded by returning
   * null, undefined or an empty string. This token will not be passed to any downstream pipeline
   * functions and will not be added to the index.
   *
   * Multiple tokens can be returned by returning an array of tokens. Each token will be passed
   * to any downstream pipeline functions and all will returned tokens will be added to the index.
   *
   * Any number of pipeline functions may be chained together using a lunr.Pipeline.
   *
   * @interface lunr.PipelineFunction
   * @param {lunr.Token} token - A token from the document being processed.
   * @param {number} i - The index of this token in the complete list of tokens for this document/field.
   * @param {lunr.Token[]} tokens - All tokens for this document/field.
   * @returns {(?lunr.Token|lunr.Token[])}
   */

  /**
   * Register a function with the pipeline.
   *
   * Functions that are used in the pipeline should be registered if the pipeline
   * needs to be serialised, or a serialised pipeline needs to be loaded.
   *
   * Registering a function does not add it to a pipeline, functions must still be
   * added to instances of the pipeline for them to be used when running a pipeline.
   *
   * @param {lunr.PipelineFunction} fn - The function to check for.
   * @param {String} label - The label to register this function with
   */
  lunr.Pipeline.registerFunction = function (fn, label) {
    if (label in this.registeredFunctions) {
      lunr.utils.warn('Overwriting existing registered function: ' + label);
    }

    fn.label = label;
    lunr.Pipeline.registeredFunctions[fn.label] = fn;
  };

  /**
   * Warns if the function is not registered as a Pipeline function.
   *
   * @param {lunr.PipelineFunction} fn - The function to check for.
   * @private
   */
  lunr.Pipeline.warnIfFunctionNotRegistered = function (fn) {
    var isRegistered = fn.label && fn.label in this.registeredFunctions;

    if (!isRegistered) {
      lunr.utils.warn('Function is not registered with pipeline. This may cause problems when serialising the index.\n', fn);
    }
  };

  /**
   * Loads a previously serialised pipeline.
   *
   * All functions to be loaded must already be registered with lunr.Pipeline.
   * If any function from the serialised data has not been registered then an
   * error will be thrown.
   *
   * @param {Object} serialised - The serialised pipeline to load.
   * @returns {lunr.Pipeline}
   */
  lunr.Pipeline.load = function (serialised) {
    var pipeline = new lunr.Pipeline();

    serialised.forEach(function (fnName) {
      var fn = lunr.Pipeline.registeredFunctions[fnName];

      if (fn) {
        pipeline.add(fn);
      } else {
        throw new Error('Cannot load unregistered function: ' + fnName);
      }
    });

    return pipeline;
  };

  /**
   * Adds new functions to the end of the pipeline.
   *
   * Logs a warning if the function has not been registered.
   *
   * @param {lunr.PipelineFunction[]} functions - Any number of functions to add to the pipeline.
   */
  lunr.Pipeline.prototype.add = function () {
    var fns = Array.prototype.slice.call(arguments);

    fns.forEach(function (fn) {
      lunr.Pipeline.warnIfFunctionNotRegistered(fn);
      this._stack.push(fn);
    }, this);
  };

  /**
   * Adds a single function after a function that already exists in the
   * pipeline.
   *
   * Logs a warning if the function has not been registered.
   *
   * @param {lunr.PipelineFunction} existingFn - A function that already exists in the pipeline.
   * @param {lunr.PipelineFunction} newFn - The new function to add to the pipeline.
   */
  lunr.Pipeline.prototype.after = function (existingFn, newFn) {
    lunr.Pipeline.warnIfFunctionNotRegistered(newFn);

    var pos = this._stack.indexOf(existingFn);
    if (pos == -1) {
      throw new Error('Cannot find existingFn');
    }

    pos = pos + 1;
    this._stack.splice(pos, 0, newFn);
  };

  /**
   * Adds a single function before a function that already exists in the
   * pipeline.
   *
   * Logs a warning if the function has not been registered.
   *
   * @param {lunr.PipelineFunction} existingFn - A function that already exists in the pipeline.
   * @param {lunr.PipelineFunction} newFn - The new function to add to the pipeline.
   */
  lunr.Pipeline.prototype.before = function (existingFn, newFn) {
    lunr.Pipeline.warnIfFunctionNotRegistered(newFn);

    var pos = this._stack.indexOf(existingFn);
    if (pos == -1) {
      throw new Error('Cannot find existingFn');
    }

    this._stack.splice(pos, 0, newFn);
  };

  /**
   * Removes a function from the pipeline.
   *
   * @param {lunr.PipelineFunction} fn The function to remove from the pipeline.
   */
  lunr.Pipeline.prototype.remove = function (fn) {
    var pos = this._stack.indexOf(fn);
    if (pos == -1) {
      return;
    }

    this._stack.splice(pos, 1);
  };

  /**
   * Runs the current list of functions that make up the pipeline against the
   * passed tokens.
   *
   * @param {Array} tokens The tokens to run through the pipeline.
   * @returns {Array}
   */
  lunr.Pipeline.prototype.run = function (tokens) {
    var stackLength = this._stack.length;

    for (var i = 0; i < stackLength; i++) {
      var fn = this._stack[i];
      var memo = [];

      for (var j = 0; j < tokens.length; j++) {
        var result = fn(tokens[j], j, tokens);

        if (result === null || result === void 0 || result === '') continue;

        if (Array.isArray(result)) {
          for (var k = 0; k < result.length; k++) {
            memo.push(result[k]);
          }
        } else {
          memo.push(result);
        }
      }

      tokens = memo;
    }

    return tokens;
  };

  /**
   * Convenience method for passing a string through a pipeline and getting
   * strings out. This method takes care of wrapping the passed string in a
   * token and mapping the resulting tokens back to strings.
   *
   * @param {string} str - The string to pass through the pipeline.
   * @param {?object} metadata - Optional metadata to associate with the token
   * passed to the pipeline.
   * @returns {string[]}
   */
  lunr.Pipeline.prototype.runString = function (str, metadata) {
    var token = new lunr.Token(str, metadata);

    return this.run([token]).map(function (t) {
      return t.toString();
    });
  };

  /**
   * Resets the pipeline by removing any existing processors.
   *
   */
  lunr.Pipeline.prototype.reset = function () {
    this._stack = [];
  };

  /**
   * Returns a representation of the pipeline ready for serialisation.
   *
   * Logs a warning if the function has not been registered.
   *
   * @returns {Array}
   */
  lunr.Pipeline.prototype.toJSON = function () {
    return this._stack.map(function (fn) {
      lunr.Pipeline.warnIfFunctionNotRegistered(fn);

      return fn.label;
    });
  };
  /*!
   * lunr.Vector
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * A vector is used to construct the vector space of documents and queries. These
   * vectors support operations to determine the similarity between two documents or
   * a document and a query.
   *
   * Normally no parameters are required for initializing a vector, but in the case of
   * loading a previously dumped vector the raw elements can be provided to the constructor.
   *
   * For performance reasons vectors are implemented with a flat array, where an elements
   * index is immediately followed by its value. E.g. [index, value, index, value]. This
   * allows the underlying array to be as sparse as possible and still offer decent
   * performance when being used for vector calculations.
   *
   * @constructor
   * @param {Number[]} [elements] - The flat list of element index and element value pairs.
   */
  lunr.Vector = function (elements) {
    this._magnitude = 0;
    this.elements = elements || [];
  };

  /**
   * Calculates the position within the vector to insert a given index.
   *
   * This is used internally by insert and upsert. If there are duplicate indexes then
   * the position is returned as if the value for that index were to be updated, but it
   * is the callers responsibility to check whether there is a duplicate at that index
   *
   * @param {Number} insertIdx - The index at which the element should be inserted.
   * @returns {Number}
   */
  lunr.Vector.prototype.positionForIndex = function (index) {
    // For an empty vector the tuple can be inserted at the beginning
    if (this.elements.length == 0) {
      return 0;
    }

    var start = 0,
        end = this.elements.length / 2,
        sliceLength = end - start,
        pivotPoint = Math.floor(sliceLength / 2),
        pivotIndex = this.elements[pivotPoint * 2];

    while (sliceLength > 1) {
      if (pivotIndex < index) {
        start = pivotPoint;
      }

      if (pivotIndex > index) {
        end = pivotPoint;
      }

      if (pivotIndex == index) {
        break;
      }

      sliceLength = end - start;
      pivotPoint = start + Math.floor(sliceLength / 2);
      pivotIndex = this.elements[pivotPoint * 2];
    }

    if (pivotIndex == index) {
      return pivotPoint * 2;
    }

    if (pivotIndex > index) {
      return pivotPoint * 2;
    }

    if (pivotIndex < index) {
      return (pivotPoint + 1) * 2;
    }
  };

  /**
   * Inserts an element at an index within the vector.
   *
   * Does not allow duplicates, will throw an error if there is already an entry
   * for this index.
   *
   * @param {Number} insertIdx - The index at which the element should be inserted.
   * @param {Number} val - The value to be inserted into the vector.
   */
  lunr.Vector.prototype.insert = function (insertIdx, val) {
    this.upsert(insertIdx, val, function () {
      throw "duplicate index";
    });
  };

  /**
   * Inserts or updates an existing index within the vector.
   *
   * @param {Number} insertIdx - The index at which the element should be inserted.
   * @param {Number} val - The value to be inserted into the vector.
   * @param {function} fn - A function that is called for updates, the existing value and the
   * requested value are passed as arguments
   */
  lunr.Vector.prototype.upsert = function (insertIdx, val, fn) {
    this._magnitude = 0;
    var position = this.positionForIndex(insertIdx);

    if (this.elements[position] == insertIdx) {
      this.elements[position + 1] = fn(this.elements[position + 1], val);
    } else {
      this.elements.splice(position, 0, insertIdx, val);
    }
  };

  /**
   * Calculates the magnitude of this vector.
   *
   * @returns {Number}
   */
  lunr.Vector.prototype.magnitude = function () {
    if (this._magnitude) return this._magnitude;

    var sumOfSquares = 0,
        elementsLength = this.elements.length;

    for (var i = 1; i < elementsLength; i += 2) {
      var val = this.elements[i];
      sumOfSquares += val * val;
    }

    return this._magnitude = Math.sqrt(sumOfSquares);
  };

  /**
   * Calculates the dot product of this vector and another vector.
   *
   * @param {lunr.Vector} otherVector - The vector to compute the dot product with.
   * @returns {Number}
   */
  lunr.Vector.prototype.dot = function (otherVector) {
    var dotProduct = 0,
        a = this.elements,
        b = otherVector.elements,
        aLen = a.length,
        bLen = b.length,
        aVal = 0,
        bVal = 0,
        i = 0,
        j = 0;

    while (i < aLen && j < bLen) {
      aVal = a[i], bVal = b[j];
      if (aVal < bVal) {
        i += 2;
      } else if (aVal > bVal) {
        j += 2;
      } else if (aVal == bVal) {
        dotProduct += a[i + 1] * b[j + 1];
        i += 2;
        j += 2;
      }
    }

    return dotProduct;
  };

  /**
   * Calculates the similarity between this vector and another vector.
   *
   * @param {lunr.Vector} otherVector - The other vector to calculate the
   * similarity with.
   * @returns {Number}
   */
  lunr.Vector.prototype.similarity = function (otherVector) {
    return this.dot(otherVector) / this.magnitude() || 0;
  };

  /**
   * Converts the vector to an array of the elements within the vector.
   *
   * @returns {Number[]}
   */
  lunr.Vector.prototype.toArray = function () {
    var output = new Array(this.elements.length / 2);

    for (var i = 1, j = 0; i < this.elements.length; i += 2, j++) {
      output[j] = this.elements[i];
    }

    return output;
  };

  /**
   * A JSON serializable representation of the vector.
   *
   * @returns {Number[]}
   */
  lunr.Vector.prototype.toJSON = function () {
    return this.elements;
  };
  /* eslint-disable */
  /*!
   * lunr.stemmer
   * Copyright (C) 2019 Oliver Nightingale
   * Includes code from - http://tartarus.org/~martin/PorterStemmer/js.txt
   */

  /**
   * lunr.stemmer is an english language stemmer, this is a JavaScript
   * implementation of the PorterStemmer taken from http://tartarus.org/~martin
   *
   * @static
   * @implements {lunr.PipelineFunction}
   * @param {lunr.Token} token - The string to stem
   * @returns {lunr.Token}
   * @see {@link lunr.Pipeline}
   * @function
   */
  lunr.stemmer = function () {
    var step2list = {
      "ational": "ate",
      "tional": "tion",
      "enci": "ence",
      "anci": "ance",
      "izer": "ize",
      "bli": "ble",
      "alli": "al",
      "entli": "ent",
      "eli": "e",
      "ousli": "ous",
      "ization": "ize",
      "ation": "ate",
      "ator": "ate",
      "alism": "al",
      "iveness": "ive",
      "fulness": "ful",
      "ousness": "ous",
      "aliti": "al",
      "iviti": "ive",
      "biliti": "ble",
      "logi": "log"
    },
        step3list = {
      "icate": "ic",
      "ative": "",
      "alize": "al",
      "iciti": "ic",
      "ical": "ic",
      "ful": "",
      "ness": ""
    },
        c = "[^aeiou]",
        // consonant
    v = "[aeiouy]",
        // vowel
    C = c + "[^aeiouy]*",
        // consonant sequence
    V = v + "[aeiou]*",
        // vowel sequence

    mgr0 = "^(" + C + ")?" + V + C,
        // [C]VC... is m>0
    meq1 = "^(" + C + ")?" + V + C + "(" + V + ")?$",
        // [C]VC[V] is m=1
    mgr1 = "^(" + C + ")?" + V + C + V + C,
        // [C]VCVC... is m>1
    s_v = "^(" + C + ")?" + v; // vowel in stem

    var re_mgr0 = new RegExp(mgr0);
    var re_mgr1 = new RegExp(mgr1);
    var re_meq1 = new RegExp(meq1);
    var re_s_v = new RegExp(s_v);

    var re_1a = /^(.+?)(ss|i)es$/;
    var re2_1a = /^(.+?)([^s])s$/;
    var re_1b = /^(.+?)eed$/;
    var re2_1b = /^(.+?)(ed|ing)$/;
    var re_1b_2 = /.$/;
    var re2_1b_2 = /(at|bl|iz)$/;
    var re3_1b_2 = new RegExp("([^aeiouylsz])\\1$");
    var re4_1b_2 = new RegExp("^" + C + v + "[^aeiouwxy]$");

    var re_1c = /^(.+?[^aeiou])y$/;
    var re_2 = /^(.+?)(ational|tional|enci|anci|izer|bli|alli|entli|eli|ousli|ization|ation|ator|alism|iveness|fulness|ousness|aliti|iviti|biliti|logi)$/;

    var re_3 = /^(.+?)(icate|ative|alize|iciti|ical|ful|ness)$/;

    var re_4 = /^(.+?)(al|ance|ence|er|ic|able|ible|ant|ement|ment|ent|ou|ism|ate|iti|ous|ive|ize)$/;
    var re2_4 = /^(.+?)(s|t)(ion)$/;

    var re_5 = /^(.+?)e$/;
    var re_5_1 = /ll$/;
    var re3_5 = new RegExp("^" + C + v + "[^aeiouwxy]$");

    var porterStemmer = function porterStemmer(w) {
      var stem, suffix, firstch, re, re2, re3, re4;

      if (w.length < 3) {
        return w;
      }

      firstch = w.substr(0, 1);
      if (firstch == "y") {
        w = firstch.toUpperCase() + w.substr(1);
      }

      // Step 1a
      re = re_1a;
      re2 = re2_1a;

      if (re.test(w)) {
        w = w.replace(re, "$1$2");
      } else if (re2.test(w)) {
        w = w.replace(re2, "$1$2");
      }

      // Step 1b
      re = re_1b;
      re2 = re2_1b;
      if (re.test(w)) {
        var fp = re.exec(w);
        re = re_mgr0;
        if (re.test(fp[1])) {
          re = re_1b_2;
          w = w.replace(re, "");
        }
      } else if (re2.test(w)) {
        var fp = re2.exec(w);
        stem = fp[1];
        re2 = re_s_v;
        if (re2.test(stem)) {
          w = stem;
          re2 = re2_1b_2;
          re3 = re3_1b_2;
          re4 = re4_1b_2;
          if (re2.test(w)) {
            w = w + "e";
          } else if (re3.test(w)) {
            re = re_1b_2;w = w.replace(re, "");
          } else if (re4.test(w)) {
            w = w + "e";
          }
        }
      }

      // Step 1c - replace suffix y or Y by i if preceded by a non-vowel which is not the first letter of the word (so cry -> cri, by -> by, say -> say)
      re = re_1c;
      if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        w = stem + "i";
      }

      // Step 2
      re = re_2;
      if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        suffix = fp[2];
        re = re_mgr0;
        if (re.test(stem)) {
          w = stem + step2list[suffix];
        }
      }

      // Step 3
      re = re_3;
      if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        suffix = fp[2];
        re = re_mgr0;
        if (re.test(stem)) {
          w = stem + step3list[suffix];
        }
      }

      // Step 4
      re = re_4;
      re2 = re2_4;
      if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        re = re_mgr1;
        if (re.test(stem)) {
          w = stem;
        }
      } else if (re2.test(w)) {
        var fp = re2.exec(w);
        stem = fp[1] + fp[2];
        re2 = re_mgr1;
        if (re2.test(stem)) {
          w = stem;
        }
      }

      // Step 5
      re = re_5;
      if (re.test(w)) {
        var fp = re.exec(w);
        stem = fp[1];
        re = re_mgr1;
        re2 = re_meq1;
        re3 = re3_5;
        if (re.test(stem) || re2.test(stem) && !re3.test(stem)) {
          w = stem;
        }
      }

      re = re_5_1;
      re2 = re_mgr1;
      if (re.test(w) && re2.test(w)) {
        re = re_1b_2;
        w = w.replace(re, "");
      }

      // and turn initial Y back to y

      if (firstch == "y") {
        w = firstch.toLowerCase() + w.substr(1);
      }

      return w;
    };

    return function (token) {
      return token.update(porterStemmer);
    };
  }();

  lunr.Pipeline.registerFunction(lunr.stemmer, 'stemmer');
  /*!
   * lunr.stopWordFilter
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * lunr.generateStopWordFilter builds a stopWordFilter function from the provided
   * list of stop words.
   *
   * The built in lunr.stopWordFilter is built using this generator and can be used
   * to generate custom stopWordFilters for applications or non English languages.
   *
   * @function
   * @param {Array} token The token to pass through the filter
   * @returns {lunr.PipelineFunction}
   * @see lunr.Pipeline
   * @see lunr.stopWordFilter
   */
  lunr.generateStopWordFilter = function (stopWords) {
    var words = stopWords.reduce(function (memo, stopWord) {
      memo[stopWord] = stopWord;
      return memo;
    }, {});

    return function (token) {
      if (token && words[token.toString()] !== token.toString()) return token;
    };
  };

  /**
   * lunr.stopWordFilter is an English language stop word list filter, any words
   * contained in the list will not be passed through the filter.
   *
   * This is intended to be used in the Pipeline. If the token does not pass the
   * filter then undefined will be returned.
   *
   * @function
   * @implements {lunr.PipelineFunction}
   * @params {lunr.Token} token - A token to check for being a stop word.
   * @returns {lunr.Token}
   * @see {@link lunr.Pipeline}
   */
  lunr.stopWordFilter = lunr.generateStopWordFilter(['a', 'able', 'about', 'across', 'after', 'all', 'almost', 'also', 'am', 'among', 'an', 'and', 'any', 'are', 'as', 'at', 'be', 'because', 'been', 'but', 'by', 'can', 'cannot', 'could', 'dear', 'did', 'do', 'does', 'either', 'else', 'ever', 'every', 'for', 'from', 'get', 'got', 'had', 'has', 'have', 'he', 'her', 'hers', 'him', 'his', 'how', 'however', 'i', 'if', 'in', 'into', 'is', 'it', 'its', 'just', 'least', 'let', 'like', 'likely', 'may', 'me', 'might', 'most', 'must', 'my', 'neither', 'no', 'nor', 'not', 'of', 'off', 'often', 'on', 'only', 'or', 'other', 'our', 'own', 'rather', 'said', 'say', 'says', 'she', 'should', 'since', 'so', 'some', 'than', 'that', 'the', 'their', 'them', 'then', 'there', 'these', 'they', 'this', 'tis', 'to', 'too', 'twas', 'us', 'wants', 'was', 'we', 'were', 'what', 'when', 'where', 'which', 'while', 'who', 'whom', 'why', 'will', 'with', 'would', 'yet', 'you', 'your']);

  lunr.Pipeline.registerFunction(lunr.stopWordFilter, 'stopWordFilter');
  /*!
   * lunr.trimmer
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * lunr.trimmer is a pipeline function for trimming non word
   * characters from the beginning and end of tokens before they
   * enter the index.
   *
   * This implementation may not work correctly for non latin
   * characters and should either be removed or adapted for use
   * with languages with non-latin characters.
   *
   * @static
   * @implements {lunr.PipelineFunction}
   * @param {lunr.Token} token The token to pass through the filter
   * @returns {lunr.Token}
   * @see lunr.Pipeline
   */
  lunr.trimmer = function (token) {
    return token.update(function (s) {
      return s.replace(/^\W+/, '').replace(/\W+$/, '');
    });
  };

  lunr.Pipeline.registerFunction(lunr.trimmer, 'trimmer');
  /*!
   * lunr.TokenSet
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * A token set is used to store the unique list of all tokens
   * within an index. Token sets are also used to represent an
   * incoming query to the index, this query token set and index
   * token set are then intersected to find which tokens to look
   * up in the inverted index.
   *
   * A token set can hold multiple tokens, as in the case of the
   * index token set, or it can hold a single token as in the
   * case of a simple query token set.
   *
   * Additionally token sets are used to perform wildcard matching.
   * Leading, contained and trailing wildcards are supported, and
   * from this edit distance matching can also be provided.
   *
   * Token sets are implemented as a minimal finite state automata,
   * where both common prefixes and suffixes are shared between tokens.
   * This helps to reduce the space used for storing the token set.
   *
   * @constructor
   */
  lunr.TokenSet = function () {
    this.final = false;
    this.edges = {};
    this.id = lunr.TokenSet._nextId;
    lunr.TokenSet._nextId += 1;
  };

  /**
   * Keeps track of the next, auto increment, identifier to assign
   * to a new tokenSet.
   *
   * TokenSets require a unique identifier to be correctly minimised.
   *
   * @private
   */
  lunr.TokenSet._nextId = 1;

  /**
   * Creates a TokenSet instance from the given sorted array of words.
   *
   * @param {String[]} arr - A sorted array of strings to create the set from.
   * @returns {lunr.TokenSet}
   * @throws Will throw an error if the input array is not sorted.
   */
  lunr.TokenSet.fromArray = function (arr) {
    var builder = new lunr.TokenSet.Builder();

    for (var i = 0, len = arr.length; i < len; i++) {
      builder.insert(arr[i]);
    }

    builder.finish();
    return builder.root;
  };

  /**
   * Creates a token set from a query clause.
   *
   * @private
   * @param {Object} clause - A single clause from lunr.Query.
   * @param {string} clause.term - The query clause term.
   * @param {number} [clause.editDistance] - The optional edit distance for the term.
   * @returns {lunr.TokenSet}
   */
  lunr.TokenSet.fromClause = function (clause) {
    if ('editDistance' in clause) {
      return lunr.TokenSet.fromFuzzyString(clause.term, clause.editDistance);
    } else {
      return lunr.TokenSet.fromString(clause.term);
    }
  };

  /**
   * Creates a token set representing a single string with a specified
   * edit distance.
   *
   * Insertions, deletions, substitutions and transpositions are each
   * treated as an edit distance of 1.
   *
   * Increasing the allowed edit distance will have a dramatic impact
   * on the performance of both creating and intersecting these TokenSets.
   * It is advised to keep the edit distance less than 3.
   *
   * @param {string} str - The string to create the token set from.
   * @param {number} editDistance - The allowed edit distance to match.
   * @returns {lunr.Vector}
   */
  lunr.TokenSet.fromFuzzyString = function (str, editDistance) {
    var root = new lunr.TokenSet();

    var stack = [{
      node: root,
      editsRemaining: editDistance,
      str: str
    }];

    while (stack.length) {
      var frame = stack.pop();

      // no edit
      if (frame.str.length > 0) {
        var char = frame.str.charAt(0),
            noEditNode;

        if (char in frame.node.edges) {
          noEditNode = frame.node.edges[char];
        } else {
          noEditNode = new lunr.TokenSet();
          frame.node.edges[char] = noEditNode;
        }

        if (frame.str.length == 1) {
          noEditNode.final = true;
        }

        stack.push({
          node: noEditNode,
          editsRemaining: frame.editsRemaining,
          str: frame.str.slice(1)
        });
      }

      if (frame.editsRemaining == 0) {
        continue;
      }

      // insertion
      if ("*" in frame.node.edges) {
        var insertionNode = frame.node.edges["*"];
      } else {
        var insertionNode = new lunr.TokenSet();
        frame.node.edges["*"] = insertionNode;
      }

      if (frame.str.length == 0) {
        insertionNode.final = true;
      }

      stack.push({
        node: insertionNode,
        editsRemaining: frame.editsRemaining - 1,
        str: frame.str
      });

      // deletion
      // can only do a deletion if we have enough edits remaining
      // and if there are characters left to delete in the string
      if (frame.str.length > 1) {
        stack.push({
          node: frame.node,
          editsRemaining: frame.editsRemaining - 1,
          str: frame.str.slice(1)
        });
      }

      // deletion
      // just removing the last character from the str
      if (frame.str.length == 1) {
        frame.node.final = true;
      }

      // substitution
      // can only do a substitution if we have enough edits remaining
      // and if there are characters left to substitute
      if (frame.str.length >= 1) {
        if ("*" in frame.node.edges) {
          var substitutionNode = frame.node.edges["*"];
        } else {
          var substitutionNode = new lunr.TokenSet();
          frame.node.edges["*"] = substitutionNode;
        }

        if (frame.str.length == 1) {
          substitutionNode.final = true;
        }

        stack.push({
          node: substitutionNode,
          editsRemaining: frame.editsRemaining - 1,
          str: frame.str.slice(1)
        });
      }

      // transposition
      // can only do a transposition if there are edits remaining
      // and there are enough characters to transpose
      if (frame.str.length > 1) {
        var charA = frame.str.charAt(0),
            charB = frame.str.charAt(1),
            transposeNode;

        if (charB in frame.node.edges) {
          transposeNode = frame.node.edges[charB];
        } else {
          transposeNode = new lunr.TokenSet();
          frame.node.edges[charB] = transposeNode;
        }

        if (frame.str.length == 1) {
          transposeNode.final = true;
        }

        stack.push({
          node: transposeNode,
          editsRemaining: frame.editsRemaining - 1,
          str: charA + frame.str.slice(2)
        });
      }
    }

    return root;
  };

  /**
   * Creates a TokenSet from a string.
   *
   * The string may contain one or more wildcard characters (*)
   * that will allow wildcard matching when intersecting with
   * another TokenSet.
   *
   * @param {string} str - The string to create a TokenSet from.
   * @returns {lunr.TokenSet}
   */
  lunr.TokenSet.fromString = function (str) {
    var node = new lunr.TokenSet(),
        root = node;

    /*
     * Iterates through all characters within the passed string
     * appending a node for each character.
     *
     * When a wildcard character is found then a self
     * referencing edge is introduced to continually match
     * any number of any characters.
     */
    for (var i = 0, len = str.length; i < len; i++) {
      var char = str[i],
          final = i == len - 1;

      if (char == "*") {
        node.edges[char] = node;
        node.final = final;
      } else {
        var next = new lunr.TokenSet();
        next.final = final;

        node.edges[char] = next;
        node = next;
      }
    }

    return root;
  };

  /**
   * Converts this TokenSet into an array of strings
   * contained within the TokenSet.
   *
   * This is not intended to be used on a TokenSet that
   * contains wildcards, in these cases the results are
   * undefined and are likely to cause an infinite loop.
   *
   * @returns {string[]}
   */
  lunr.TokenSet.prototype.toArray = function () {
    var words = [];

    var stack = [{
      prefix: "",
      node: this
    }];

    while (stack.length) {
      var frame = stack.pop(),
          edges = Object.keys(frame.node.edges),
          len = edges.length;

      if (frame.node.final) {
        /* In Safari, at this point the prefix is sometimes corrupted, see:
         * https://github.com/olivernn/lunr.js/issues/279 Calling any
         * String.prototype method forces Safari to "cast" this string to what
         * it's supposed to be, fixing the bug. */
        frame.prefix.charAt(0);
        words.push(frame.prefix);
      }

      for (var i = 0; i < len; i++) {
        var edge = edges[i];

        stack.push({
          prefix: frame.prefix.concat(edge),
          node: frame.node.edges[edge]
        });
      }
    }

    return words;
  };

  /**
   * Generates a string representation of a TokenSet.
   *
   * This is intended to allow TokenSets to be used as keys
   * in objects, largely to aid the construction and minimisation
   * of a TokenSet. As such it is not designed to be a human
   * friendly representation of the TokenSet.
   *
   * @returns {string}
   */
  lunr.TokenSet.prototype.toString = function () {
    // NOTE: Using Object.keys here as this.edges is very likely
    // to enter 'hash-mode' with many keys being added
    //
    // avoiding a for-in loop here as it leads to the function
    // being de-optimised (at least in V8). From some simple
    // benchmarks the performance is comparable, but allowing
    // V8 to optimize may mean easy performance wins in the future.

    if (this._str) {
      return this._str;
    }

    var str = this.final ? '1' : '0',
        labels = Object.keys(this.edges).sort(),
        len = labels.length;

    for (var i = 0; i < len; i++) {
      var label = labels[i],
          node = this.edges[label];

      str = str + label + node.id;
    }

    return str;
  };

  /**
   * Returns a new TokenSet that is the intersection of
   * this TokenSet and the passed TokenSet.
   *
   * This intersection will take into account any wildcards
   * contained within the TokenSet.
   *
   * @param {lunr.TokenSet} b - An other TokenSet to intersect with.
   * @returns {lunr.TokenSet}
   */
  lunr.TokenSet.prototype.intersect = function (b) {
    var output = new lunr.TokenSet(),
        frame = undefined;

    var stack = [{
      qNode: b,
      output: output,
      node: this
    }];

    while (stack.length) {
      frame = stack.pop();

      // NOTE: As with the #toString method, we are using
      // Object.keys and a for loop instead of a for-in loop
      // as both of these objects enter 'hash' mode, causing
      // the function to be de-optimised in V8
      var qEdges = Object.keys(frame.qNode.edges),
          qLen = qEdges.length,
          nEdges = Object.keys(frame.node.edges),
          nLen = nEdges.length;

      for (var q = 0; q < qLen; q++) {
        var qEdge = qEdges[q];

        for (var n = 0; n < nLen; n++) {
          var nEdge = nEdges[n];

          if (nEdge == qEdge || qEdge == '*') {
            var node = frame.node.edges[nEdge],
                qNode = frame.qNode.edges[qEdge],
                final = node.final && qNode.final,
                next = undefined;

            if (nEdge in frame.output.edges) {
              // an edge already exists for this character
              // no need to create a new node, just set the finality
              // bit unless this node is already final
              next = frame.output.edges[nEdge];
              next.final = next.final || final;
            } else {
              // no edge exists yet, must create one
              // set the finality bit and insert it
              // into the output
              next = new lunr.TokenSet();
              next.final = final;
              frame.output.edges[nEdge] = next;
            }

            stack.push({
              qNode: qNode,
              output: next,
              node: node
            });
          }
        }
      }
    }

    return output;
  };
  lunr.TokenSet.Builder = function () {
    this.previousWord = "";
    this.root = new lunr.TokenSet();
    this.uncheckedNodes = [];
    this.minimizedNodes = {};
  };

  lunr.TokenSet.Builder.prototype.insert = function (word) {
    var node,
        commonPrefix = 0;

    if (word < this.previousWord) {
      throw new Error("Out of order word insertion");
    }

    for (var i = 0; i < word.length && i < this.previousWord.length; i++) {
      if (word[i] != this.previousWord[i]) break;
      commonPrefix++;
    }

    this.minimize(commonPrefix);

    if (this.uncheckedNodes.length == 0) {
      node = this.root;
    } else {
      node = this.uncheckedNodes[this.uncheckedNodes.length - 1].child;
    }

    for (var i = commonPrefix; i < word.length; i++) {
      var nextNode = new lunr.TokenSet(),
          char = word[i];

      node.edges[char] = nextNode;

      this.uncheckedNodes.push({
        parent: node,
        char: char,
        child: nextNode
      });

      node = nextNode;
    }

    node.final = true;
    this.previousWord = word;
  };

  lunr.TokenSet.Builder.prototype.finish = function () {
    this.minimize(0);
  };

  lunr.TokenSet.Builder.prototype.minimize = function (downTo) {
    for (var i = this.uncheckedNodes.length - 1; i >= downTo; i--) {
      var node = this.uncheckedNodes[i],
          childKey = node.child.toString();

      if (childKey in this.minimizedNodes) {
        node.parent.edges[node.char] = this.minimizedNodes[childKey];
      } else {
        // Cache the key for this node since
        // we know it can't change anymore
        node.child._str = childKey;

        this.minimizedNodes[childKey] = node.child;
      }

      this.uncheckedNodes.pop();
    }
  };
  /*!
   * lunr.Index
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * An index contains the built index of all documents and provides a query interface
   * to the index.
   *
   * Usually instances of lunr.Index will not be created using this constructor, instead
   * lunr.Builder should be used to construct new indexes, or lunr.Index.load should be
   * used to load previously built and serialized indexes.
   *
   * @constructor
   * @param {Object} attrs - The attributes of the built search index.
   * @param {Object} attrs.invertedIndex - An index of term/field to document reference.
   * @param {Object<string, lunr.Vector>} attrs.fieldVectors - Field vectors
   * @param {lunr.TokenSet} attrs.tokenSet - An set of all corpus tokens.
   * @param {string[]} attrs.fields - The names of indexed document fields.
   * @param {lunr.Pipeline} attrs.pipeline - The pipeline to use for search terms.
   */
  lunr.Index = function (attrs) {
    this.invertedIndex = attrs.invertedIndex;
    this.fieldVectors = attrs.fieldVectors;
    this.tokenSet = attrs.tokenSet;
    this.fields = attrs.fields;
    this.pipeline = attrs.pipeline;
  };

  /**
   * A result contains details of a document matching a search query.
   * @typedef {Object} lunr.Index~Result
   * @property {string} ref - The reference of the document this result represents.
   * @property {number} score - A number between 0 and 1 representing how similar this document is to the query.
   * @property {lunr.MatchData} matchData - Contains metadata about this match including which term(s) caused the match.
   */

  /**
   * Although lunr provides the ability to create queries using lunr.Query, it also provides a simple
   * query language which itself is parsed into an instance of lunr.Query.
   *
   * For programmatically building queries it is advised to directly use lunr.Query, the query language
   * is best used for human entered text rather than program generated text.
   *
   * At its simplest queries can just be a single term, e.g. `hello`, multiple terms are also supported
   * and will be combined with OR, e.g `hello world` will match documents that contain either 'hello'
   * or 'world', though those that contain both will rank higher in the results.
   *
   * Wildcards can be included in terms to match one or more unspecified characters, these wildcards can
   * be inserted anywhere within the term, and more than one wildcard can exist in a single term. Adding
   * wildcards will increase the number of documents that will be found but can also have a negative
   * impact on query performance, especially with wildcards at the beginning of a term.
   *
   * Terms can be restricted to specific fields, e.g. `title:hello`, only documents with the term
   * hello in the title field will match this query. Using a field not present in the index will lead
   * to an error being thrown.
   *
   * Modifiers can also be added to terms, lunr supports edit distance and boost modifiers on terms. A term
   * boost will make documents matching that term score higher, e.g. `foo^5`. Edit distance is also supported
   * to provide fuzzy matching, e.g. 'hello~2' will match documents with hello with an edit distance of 2.
   * Avoid large values for edit distance to improve query performance.
   *
   * Each term also supports a presence modifier. By default a term's presence in document is optional, however
   * this can be changed to either required or prohibited. For a term's presence to be required in a document the
   * term should be prefixed with a '+', e.g. `+foo bar` is a search for documents that must contain 'foo' and
   * optionally contain 'bar'. Conversely a leading '-' sets the terms presence to prohibited, i.e. it must not
   * appear in a document, e.g. `-foo bar` is a search for documents that do not contain 'foo' but may contain 'bar'.
   *
   * To escape special characters the backslash character '\' can be used, this allows searches to include
   * characters that would normally be considered modifiers, e.g. `foo\~2` will search for a term "foo~2" instead
   * of attempting to apply a boost of 2 to the search term "foo".
   *
   * @typedef {string} lunr.Index~QueryString
   * @example <caption>Simple single term query</caption>
   * hello
   * @example <caption>Multiple term query</caption>
   * hello world
   * @example <caption>term scoped to a field</caption>
   * title:hello
   * @example <caption>term with a boost of 10</caption>
   * hello^10
   * @example <caption>term with an edit distance of 2</caption>
   * hello~2
   * @example <caption>terms with presence modifiers</caption>
   * -foo +bar baz
   */

  /**
   * Performs a search against the index using lunr query syntax.
   *
   * Results will be returned sorted by their score, the most relevant results
   * will be returned first.  For details on how the score is calculated, please see
   * the {@link https://lunrjs.com/guides/searching.html#scoring|guide}.
   *
   * For more programmatic querying use lunr.Index#query.
   *
   * @param {lunr.Index~QueryString} queryString - A string containing a lunr query.
   * @throws {lunr.QueryParseError} If the passed query string cannot be parsed.
   * @returns {lunr.Index~Result[]}
   */
  lunr.Index.prototype.search = function (queryString) {
    return this.query(function (query) {
      var parser = new lunr.QueryParser(queryString, query);
      parser.parse();
    });
  };

  /**
   * A query builder callback provides a query object to be used to express
   * the query to perform on the index.
   *
   * @callback lunr.Index~queryBuilder
   * @param {lunr.Query} query - The query object to build up.
   * @this lunr.Query
   */

  /**
   * Performs a query against the index using the yielded lunr.Query object.
   *
   * If performing programmatic queries against the index, this method is preferred
   * over lunr.Index#search so as to avoid the additional query parsing overhead.
   *
   * A query object is yielded to the supplied function which should be used to
   * express the query to be run against the index.
   *
   * Note that although this function takes a callback parameter it is _not_ an
   * asynchronous operation, the callback is just yielded a query object to be
   * customized.
   *
   * @param {lunr.Index~queryBuilder} fn - A function that is used to build the query.
   * @returns {lunr.Index~Result[]}
   */
  lunr.Index.prototype.query = function (fn) {
    // for each query clause
    // * process terms
    // * expand terms from token set
    // * find matching documents and metadata
    // * get document vectors
    // * score documents

    var query = new lunr.Query(this.fields),
        matchingFields = Object.create(null),
        queryVectors = Object.create(null),
        termFieldCache = Object.create(null),
        requiredMatches = Object.create(null),
        prohibitedMatches = Object.create(null);

    /*
     * To support field level boosts a query vector is created per
     * field. An empty vector is eagerly created to support negated
     * queries.
     */
    for (var i = 0; i < this.fields.length; i++) {
      queryVectors[this.fields[i]] = new lunr.Vector();
    }

    fn.call(query, query);

    for (var i = 0; i < query.clauses.length; i++) {
      /*
       * Unless the pipeline has been disabled for this term, which is
       * the case for terms with wildcards, we need to pass the clause
       * term through the search pipeline. A pipeline returns an array
       * of processed terms. Pipeline functions may expand the passed
       * term, which means we may end up performing multiple index lookups
       * for a single query term.
       */
      var clause = query.clauses[i],
          terms = null,
          clauseMatches = lunr.Set.complete;

      if (clause.usePipeline) {
        terms = this.pipeline.runString(clause.term, {
          fields: clause.fields
        });
      } else {
        terms = [clause.term];
      }

      for (var m = 0; m < terms.length; m++) {
        var term = terms[m];

        /*
         * Each term returned from the pipeline needs to use the same query
         * clause object, e.g. the same boost and or edit distance. The
         * simplest way to do this is to re-use the clause object but mutate
         * its term property.
         */
        clause.term = term;

        /*
         * From the term in the clause we create a token set which will then
         * be used to intersect the indexes token set to get a list of terms
         * to lookup in the inverted index
         */
        var termTokenSet = lunr.TokenSet.fromClause(clause),
            expandedTerms = this.tokenSet.intersect(termTokenSet).toArray();

        /*
         * If a term marked as required does not exist in the tokenSet it is
         * impossible for the search to return any matches. We set all the field
         * scoped required matches set to empty and stop examining any further
         * clauses.
         */
        if (expandedTerms.length === 0 && clause.presence === lunr.Query.presence.REQUIRED) {
          for (var k = 0; k < clause.fields.length; k++) {
            var field = clause.fields[k];
            requiredMatches[field] = lunr.Set.empty;
          }

          break;
        }

        for (var j = 0; j < expandedTerms.length; j++) {
          /*
           * For each term get the posting and termIndex, this is required for
           * building the query vector.
           */
          var expandedTerm = expandedTerms[j],
              posting = this.invertedIndex[expandedTerm],
              termIndex = posting._index;

          for (var k = 0; k < clause.fields.length; k++) {
            /*
             * For each field that this query term is scoped by (by default
             * all fields are in scope) we need to get all the document refs
             * that have this term in that field.
             *
             * The posting is the entry in the invertedIndex for the matching
             * term from above.
             */
            var field = clause.fields[k],
                fieldPosting = posting[field],
                matchingDocumentRefs = Object.keys(fieldPosting),
                termField = expandedTerm + "/" + field,
                matchingDocumentsSet = new lunr.Set(matchingDocumentRefs);

            /*
             * if the presence of this term is required ensure that the matching
             * documents are added to the set of required matches for this clause.
             *
             */
            if (clause.presence == lunr.Query.presence.REQUIRED) {
              clauseMatches = clauseMatches.union(matchingDocumentsSet);

              if (requiredMatches[field] === undefined) {
                requiredMatches[field] = lunr.Set.complete;
              }
            }

            /*
             * if the presence of this term is prohibited ensure that the matching
             * documents are added to the set of prohibited matches for this field,
             * creating that set if it does not yet exist.
             */
            if (clause.presence == lunr.Query.presence.PROHIBITED) {
              if (prohibitedMatches[field] === undefined) {
                prohibitedMatches[field] = lunr.Set.empty;
              }

              prohibitedMatches[field] = prohibitedMatches[field].union(matchingDocumentsSet);

              /*
               * Prohibited matches should not be part of the query vector used for
               * similarity scoring and no metadata should be extracted so we continue
               * to the next field
               */
              continue;
            }

            /*
             * The query field vector is populated using the termIndex found for
             * the term and a unit value with the appropriate boost applied.
             * Using upsert because there could already be an entry in the vector
             * for the term we are working with. In that case we just add the scores
             * together.
             */
            queryVectors[field].upsert(termIndex, clause.boost, function (a, b) {
              return a + b;
            });

            /**
             * If we've already seen this term, field combo then we've already collected
             * the matching documents and metadata, no need to go through all that again
             */
            if (termFieldCache[termField]) {
              continue;
            }

            for (var l = 0; l < matchingDocumentRefs.length; l++) {
              /*
               * All metadata for this term/field/document triple
               * are then extracted and collected into an instance
               * of lunr.MatchData ready to be returned in the query
               * results
               */
              var matchingDocumentRef = matchingDocumentRefs[l],
                  matchingFieldRef = new lunr.FieldRef(matchingDocumentRef, field),
                  metadata = fieldPosting[matchingDocumentRef],
                  fieldMatch;

              if ((fieldMatch = matchingFields[matchingFieldRef]) === undefined) {
                matchingFields[matchingFieldRef] = new lunr.MatchData(expandedTerm, field, metadata);
              } else {
                fieldMatch.add(expandedTerm, field, metadata);
              }
            }

            termFieldCache[termField] = true;
          }
        }
      }

      /**
       * If the presence was required we need to update the requiredMatches field sets.
       * We do this after all fields for the term have collected their matches because
       * the clause terms presence is required in _any_ of the fields not _all_ of the
       * fields.
       */
      if (clause.presence === lunr.Query.presence.REQUIRED) {
        for (var k = 0; k < clause.fields.length; k++) {
          var field = clause.fields[k];
          requiredMatches[field] = requiredMatches[field].intersect(clauseMatches);
        }
      }
    }

    /**
     * Need to combine the field scoped required and prohibited
     * matching documents into a global set of required and prohibited
     * matches
     */
    var allRequiredMatches = lunr.Set.complete,
        allProhibitedMatches = lunr.Set.empty;

    for (var i = 0; i < this.fields.length; i++) {
      var field = this.fields[i];

      if (requiredMatches[field]) {
        allRequiredMatches = allRequiredMatches.intersect(requiredMatches[field]);
      }

      if (prohibitedMatches[field]) {
        allProhibitedMatches = allProhibitedMatches.union(prohibitedMatches[field]);
      }
    }

    var matchingFieldRefs = Object.keys(matchingFields),
        results = [],
        matches = Object.create(null);

    /*
     * If the query is negated (contains only prohibited terms)
     * we need to get _all_ fieldRefs currently existing in the
     * index. This is only done when we know that the query is
     * entirely prohibited terms to avoid any cost of getting all
     * fieldRefs unnecessarily.
     *
     * Additionally, blank MatchData must be created to correctly
     * populate the results.
     */
    if (query.isNegated()) {
      matchingFieldRefs = Object.keys(this.fieldVectors);

      for (var i = 0; i < matchingFieldRefs.length; i++) {
        var matchingFieldRef = matchingFieldRefs[i];
        var fieldRef = lunr.FieldRef.fromString(matchingFieldRef);
        matchingFields[matchingFieldRef] = new lunr.MatchData();
      }
    }

    for (var i = 0; i < matchingFieldRefs.length; i++) {
      /*
       * Currently we have document fields that match the query, but we
       * need to return documents. The matchData and scores are combined
       * from multiple fields belonging to the same document.
       *
       * Scores are calculated by field, using the query vectors created
       * above, and combined into a final document score using addition.
       */
      var fieldRef = lunr.FieldRef.fromString(matchingFieldRefs[i]),
          docRef = fieldRef.docRef;

      if (!allRequiredMatches.contains(docRef)) {
        continue;
      }

      if (allProhibitedMatches.contains(docRef)) {
        continue;
      }

      var fieldVector = this.fieldVectors[fieldRef],
          score = queryVectors[fieldRef.fieldName].similarity(fieldVector),
          docMatch;

      if ((docMatch = matches[docRef]) !== undefined) {
        docMatch.score += score;
        docMatch.matchData.combine(matchingFields[fieldRef]);
      } else {
        var match = {
          ref: docRef,
          score: score,
          matchData: matchingFields[fieldRef]
        };
        matches[docRef] = match;
        results.push(match);
      }
    }

    /*
     * Sort the results objects by score, highest first.
     */
    return results.sort(function (a, b) {
      return b.score - a.score;
    });
  };

  /**
   * Prepares the index for JSON serialization.
   *
   * The schema for this JSON blob will be described in a
   * separate JSON schema file.
   *
   * @returns {Object}
   */
  lunr.Index.prototype.toJSON = function () {
    var invertedIndex = Object.keys(this.invertedIndex).sort().map(function (term) {
      return [term, this.invertedIndex[term]];
    }, this);

    var fieldVectors = Object.keys(this.fieldVectors).map(function (ref) {
      return [ref, this.fieldVectors[ref].toJSON()];
    }, this);

    return {
      version: lunr.version,
      fields: this.fields,
      fieldVectors: fieldVectors,
      invertedIndex: invertedIndex,
      pipeline: this.pipeline.toJSON()
    };
  };

  /**
   * Loads a previously serialized lunr.Index
   *
   * @param {Object} serializedIndex - A previously serialized lunr.Index
   * @returns {lunr.Index}
   */
  lunr.Index.load = function (serializedIndex) {
    var attrs = {},
        fieldVectors = {},
        serializedVectors = serializedIndex.fieldVectors,
        invertedIndex = Object.create(null),
        serializedInvertedIndex = serializedIndex.invertedIndex,
        tokenSetBuilder = new lunr.TokenSet.Builder(),
        pipeline = lunr.Pipeline.load(serializedIndex.pipeline);

    if (serializedIndex.version != lunr.version) {
      lunr.utils.warn("Version mismatch when loading serialised index. Current version of lunr '" + lunr.version + "' does not match serialized index '" + serializedIndex.version + "'");
    }

    for (var i = 0; i < serializedVectors.length; i++) {
      var tuple = serializedVectors[i],
          ref = tuple[0],
          elements = tuple[1];

      fieldVectors[ref] = new lunr.Vector(elements);
    }

    for (var i = 0; i < serializedInvertedIndex.length; i++) {
      var tuple = serializedInvertedIndex[i],
          term = tuple[0],
          posting = tuple[1];

      tokenSetBuilder.insert(term);
      invertedIndex[term] = posting;
    }

    tokenSetBuilder.finish();

    attrs.fields = serializedIndex.fields;

    attrs.fieldVectors = fieldVectors;
    attrs.invertedIndex = invertedIndex;
    attrs.tokenSet = tokenSetBuilder.root;
    attrs.pipeline = pipeline;

    return new lunr.Index(attrs);
  };
  /*!
   * lunr.Builder
   * Copyright (C) 2019 Oliver Nightingale
   */

  /**
   * lunr.Builder performs indexing on a set of documents and
   * returns instances of lunr.Index ready for querying.
   *
   * All configuration of the index is done via the builder, the
   * fields to index, the document reference, the text processing
   * pipeline and document scoring parameters are all set on the
   * builder before indexing.
   *
   * @constructor
   * @property {string} _ref - Internal reference to the document reference field.
   * @property {string[]} _fields - Internal reference to the document fields to index.
   * @property {object} invertedIndex - The inverted index maps terms to document fields.
   * @property {object} documentTermFrequencies - Keeps track of document term frequencies.
   * @property {object} documentLengths - Keeps track of the length of documents added to the index.
   * @property {lunr.tokenizer} tokenizer - Function for splitting strings into tokens for indexing.
   * @property {lunr.Pipeline} pipeline - The pipeline performs text processing on tokens before indexing.
   * @property {lunr.Pipeline} searchPipeline - A pipeline for processing search terms before querying the index.
   * @property {number} documentCount - Keeps track of the total number of documents indexed.
   * @property {number} _b - A parameter to control field length normalization, setting this to 0 disabled normalization, 1 fully normalizes field lengths, the default value is 0.75.
   * @property {number} _k1 - A parameter to control how quickly an increase in term frequency results in term frequency saturation, the default value is 1.2.
   * @property {number} termIndex - A counter incremented for each unique term, used to identify a terms position in the vector space.
   * @property {array} metadataWhitelist - A list of metadata keys that have been whitelisted for entry in the index.
   */
  lunr.Builder = function () {
    this._ref = "id";
    this._fields = Object.create(null);
    this._documents = Object.create(null);
    this.invertedIndex = Object.create(null);
    this.fieldTermFrequencies = {};
    this.fieldLengths = {};
    this.tokenizer = lunr.tokenizer;
    this.pipeline = new lunr.Pipeline();
    this.searchPipeline = new lunr.Pipeline();
    this.documentCount = 0;
    this._b = 0.75;
    this._k1 = 1.2;
    this.termIndex = 0;
    this.metadataWhitelist = [];
  };

  /**
   * Sets the document field used as the document reference. Every document must have this field.
   * The type of this field in the document should be a string, if it is not a string it will be
   * coerced into a string by calling toString.
   *
   * The default ref is 'id'.
   *
   * The ref should _not_ be changed during indexing, it should be set before any documents are
   * added to the index. Changing it during indexing can lead to inconsistent results.
   *
   * @param {string} ref - The name of the reference field in the document.
   */
  lunr.Builder.prototype.ref = function (ref) {
    this._ref = ref;
  };

  /**
   * A function that is used to extract a field from a document.
   *
   * Lunr expects a field to be at the top level of a document, if however the field
   * is deeply nested within a document an extractor function can be used to extract
   * the right field for indexing.
   *
   * @callback fieldExtractor
   * @param {object} doc - The document being added to the index.
   * @returns {?(string|object|object[])} obj - The object that will be indexed for this field.
   * @example <caption>Extracting a nested field</caption>
   * function (doc) { return doc.nested.field }
   */

  /**
   * Adds a field to the list of document fields that will be indexed. Every document being
   * indexed should have this field. Null values for this field in indexed documents will
   * not cause errors but will limit the chance of that document being retrieved by searches.
   *
   * All fields should be added before adding documents to the index. Adding fields after
   * a document has been indexed will have no effect on already indexed documents.
   *
   * Fields can be boosted at build time. This allows terms within that field to have more
   * importance when ranking search results. Use a field boost to specify that matches within
   * one field are more important than other fields.
   *
   * @param {string} fieldName - The name of a field to index in all documents.
   * @param {object} attributes - Optional attributes associated with this field.
   * @param {number} [attributes.boost=1] - Boost applied to all terms within this field.
   * @param {fieldExtractor} [attributes.extractor] - Function to extract a field from a document.
   * @throws {RangeError} fieldName cannot contain unsupported characters '/'
   */
  lunr.Builder.prototype.field = function (fieldName, attributes) {
    if (/\//.test(fieldName)) {
      throw new RangeError("Field '" + fieldName + "' contains illegal character '/'");
    }

    this._fields[fieldName] = attributes || {};
  };

  /**
   * A parameter to tune the amount of field length normalisation that is applied when
   * calculating relevance scores. A value of 0 will completely disable any normalisation
   * and a value of 1 will fully normalise field lengths. The default is 0.75. Values of b
   * will be clamped to the range 0 - 1.
   *
   * @param {number} number - The value to set for this tuning parameter.
   */
  lunr.Builder.prototype.b = function (number) {
    if (number < 0) {
      this._b = 0;
    } else if (number > 1) {
      this._b = 1;
    } else {
      this._b = number;
    }
  };

  /**
   * A parameter that controls the speed at which a rise in term frequency results in term
   * frequency saturation. The default value is 1.2. Setting this to a higher value will give
   * slower saturation levels, a lower value will result in quicker saturation.
   *
   * @param {number} number - The value to set for this tuning parameter.
   */
  lunr.Builder.prototype.k1 = function (number) {
    this._k1 = number;
  };

  /**
   * Adds a document to the index.
   *
   * Before adding fields to the index the index should have been fully setup, with the document
   * ref and all fields to index already having been specified.
   *
   * The document must have a field name as specified by the ref (by default this is 'id') and
   * it should have all fields defined for indexing, though null or undefined values will not
   * cause errors.
   *
   * Entire documents can be boosted at build time. Applying a boost to a document indicates that
   * this document should rank higher in search results than other documents.
   *
   * @param {object} doc - The document to add to the index.
   * @param {object} attributes - Optional attributes associated with this document.
   * @param {number} [attributes.boost=1] - Boost applied to all terms within this document.
   */
  lunr.Builder.prototype.add = function (doc, attributes) {
    var docRef = doc[this._ref],
        fields = Object.keys(this._fields);

    this._documents[docRef] = attributes || {};
    this.documentCount += 1;

    for (var i = 0; i < fields.length; i++) {
      var fieldName = fields[i],
          extractor = this._fields[fieldName].extractor,
          field = extractor ? extractor(doc) : doc[fieldName],
          tokens = this.tokenizer(field, {
        fields: [fieldName]
      }),
          terms = this.pipeline.run(tokens),
          fieldRef = new lunr.FieldRef(docRef, fieldName),
          fieldTerms = Object.create(null);

      this.fieldTermFrequencies[fieldRef] = fieldTerms;
      this.fieldLengths[fieldRef] = 0;

      // store the length of this field for this document
      this.fieldLengths[fieldRef] += terms.length;

      // calculate term frequencies for this field
      for (var j = 0; j < terms.length; j++) {
        var term = terms[j];

        if (fieldTerms[term] == undefined) {
          fieldTerms[term] = 0;
        }

        fieldTerms[term] += 1;

        // add to inverted index
        // create an initial posting if one doesn't exist
        if (this.invertedIndex[term] == undefined) {
          var posting = Object.create(null);
          posting["_index"] = this.termIndex;
          this.termIndex += 1;

          for (var k = 0; k < fields.length; k++) {
            posting[fields[k]] = Object.create(null);
          }

          this.invertedIndex[term] = posting;
        }

        // add an entry for this term/fieldName/docRef to the invertedIndex
        if (this.invertedIndex[term][fieldName][docRef] == undefined) {
          this.invertedIndex[term][fieldName][docRef] = Object.create(null);
        }

        // store all whitelisted metadata about this token in the
        // inverted index
        for (var l = 0; l < this.metadataWhitelist.length; l++) {
          var metadataKey = this.metadataWhitelist[l],
              metadata = term.metadata[metadataKey];

          if (this.invertedIndex[term][fieldName][docRef][metadataKey] == undefined) {
            this.invertedIndex[term][fieldName][docRef][metadataKey] = [];
          }

          this.invertedIndex[term][fieldName][docRef][metadataKey].push(metadata);
        }
      }
    }
  };

  /**
   * Calculates the average document length for this index
   *
   * @private
   */
  lunr.Builder.prototype.calculateAverageFieldLengths = function () {

    var fieldRefs = Object.keys(this.fieldLengths),
        numberOfFields = fieldRefs.length,
        accumulator = {},
        documentsWithField = {};

    for (var i = 0; i < numberOfFields; i++) {
      var fieldRef = lunr.FieldRef.fromString(fieldRefs[i]),
          field = fieldRef.fieldName;

      documentsWithField[field] || (documentsWithField[field] = 0);
      documentsWithField[field] += 1;

      accumulator[field] || (accumulator[field] = 0);
      accumulator[field] += this.fieldLengths[fieldRef];
    }

    var fields = Object.keys(this._fields);

    for (var i = 0; i < fields.length; i++) {
      var fieldName = fields[i];
      accumulator[fieldName] = accumulator[fieldName] / documentsWithField[fieldName];
    }

    this.averageFieldLength = accumulator;
  };

  /**
   * Builds a vector space model of every document using lunr.Vector
   *
   * @private
   */
  lunr.Builder.prototype.createFieldVectors = function () {
    var fieldVectors = {},
        fieldRefs = Object.keys(this.fieldTermFrequencies),
        fieldRefsLength = fieldRefs.length,
        termIdfCache = Object.create(null);

    for (var i = 0; i < fieldRefsLength; i++) {
      var fieldRef = lunr.FieldRef.fromString(fieldRefs[i]),
          fieldName = fieldRef.fieldName,
          fieldLength = this.fieldLengths[fieldRef],
          fieldVector = new lunr.Vector(),
          termFrequencies = this.fieldTermFrequencies[fieldRef],
          terms = Object.keys(termFrequencies),
          termsLength = terms.length;

      var fieldBoost = this._fields[fieldName].boost || 1,
          docBoost = this._documents[fieldRef.docRef].boost || 1;

      for (var j = 0; j < termsLength; j++) {
        var term = terms[j],
            tf = termFrequencies[term],
            termIndex = this.invertedIndex[term]._index,
            idf,
            score,
            scoreWithPrecision;

        if (termIdfCache[term] === undefined) {
          idf = lunr.idf(this.invertedIndex[term], this.documentCount);
          termIdfCache[term] = idf;
        } else {
          idf = termIdfCache[term];
        }

        score = idf * ((this._k1 + 1) * tf) / (this._k1 * (1 - this._b + this._b * (fieldLength / this.averageFieldLength[fieldName])) + tf);
        score *= fieldBoost;
        score *= docBoost;
        scoreWithPrecision = Math.round(score * 1000) / 1000;
        // Converts 1.23456789 to 1.234.
        // Reducing the precision so that the vectors take up less
        // space when serialised. Doing it now so that they behave
        // the same before and after serialisation. Also, this is
        // the fastest approach to reducing a number's precision in
        // JavaScript.

        fieldVector.insert(termIndex, scoreWithPrecision);
      }

      fieldVectors[fieldRef] = fieldVector;
    }

    this.fieldVectors = fieldVectors;
  };

  /**
   * Creates a token set of all tokens in the index using lunr.TokenSet
   *
   * @private
   */
  lunr.Builder.prototype.createTokenSet = function () {
    this.tokenSet = lunr.TokenSet.fromArray(Object.keys(this.invertedIndex).sort());
  };

  /**
   * Builds the index, creating an instance of lunr.Index.
   *
   * This completes the indexing process and should only be called
   * once all documents have been added to the index.
   *
   * @returns {lunr.Index}
   */
  lunr.Builder.prototype.build = function () {
    this.calculateAverageFieldLengths();
    this.createFieldVectors();
    this.createTokenSet();

    return new lunr.Index({
      invertedIndex: this.invertedIndex,
      fieldVectors: this.fieldVectors,
      tokenSet: this.tokenSet,
      fields: Object.keys(this._fields),
      pipeline: this.searchPipeline
    });
  };

  /**
   * Applies a plugin to the index builder.
   *
   * A plugin is a function that is called with the index builder as its context.
   * Plugins can be used to customise or extend the behaviour of the index
   * in some way. A plugin is just a function, that encapsulated the custom
   * behaviour that should be applied when building the index.
   *
   * The plugin function will be called with the index builder as its argument, additional
   * arguments can also be passed when calling use. The function will be called
   * with the index builder as its context.
   *
   * @param {Function} plugin The plugin to apply.
   */
  lunr.Builder.prototype.use = function (fn) {
    var args = Array.prototype.slice.call(arguments, 1);
    args.unshift(this);
    fn.apply(this, args);
  };
  /**
   * Contains and collects metadata about a matching document.
   * A single instance of lunr.MatchData is returned as part of every
   * lunr.Index~Result.
   *
   * @constructor
   * @param {string} term - The term this match data is associated with
   * @param {string} field - The field in which the term was found
   * @param {object} metadata - The metadata recorded about this term in this field
   * @property {object} metadata - A cloned collection of metadata associated with this document.
   * @see {@link lunr.Index~Result}
   */
  lunr.MatchData = function (term, field, metadata) {
    var clonedMetadata = Object.create(null),
        metadataKeys = Object.keys(metadata || {});

    // Cloning the metadata to prevent the original
    // being mutated during match data combination.
    // Metadata is kept in an array within the inverted
    // index so cloning the data can be done with
    // Array#slice
    for (var i = 0; i < metadataKeys.length; i++) {
      var key = metadataKeys[i];
      clonedMetadata[key] = metadata[key].slice();
    }

    this.metadata = Object.create(null);

    if (term !== undefined) {
      this.metadata[term] = Object.create(null);
      this.metadata[term][field] = clonedMetadata;
    }
  };

  /**
   * An instance of lunr.MatchData will be created for every term that matches a
   * document. However only one instance is required in a lunr.Index~Result. This
   * method combines metadata from another instance of lunr.MatchData with this
   * objects metadata.
   *
   * @param {lunr.MatchData} otherMatchData - Another instance of match data to merge with this one.
   * @see {@link lunr.Index~Result}
   */
  lunr.MatchData.prototype.combine = function (otherMatchData) {
    var terms = Object.keys(otherMatchData.metadata);

    for (var i = 0; i < terms.length; i++) {
      var term = terms[i],
          fields = Object.keys(otherMatchData.metadata[term]);

      if (this.metadata[term] == undefined) {
        this.metadata[term] = Object.create(null);
      }

      for (var j = 0; j < fields.length; j++) {
        var field = fields[j],
            keys = Object.keys(otherMatchData.metadata[term][field]);

        if (this.metadata[term][field] == undefined) {
          this.metadata[term][field] = Object.create(null);
        }

        for (var k = 0; k < keys.length; k++) {
          var key = keys[k];

          if (this.metadata[term][field][key] == undefined) {
            this.metadata[term][field][key] = otherMatchData.metadata[term][field][key];
          } else {
            this.metadata[term][field][key] = this.metadata[term][field][key].concat(otherMatchData.metadata[term][field][key]);
          }
        }
      }
    }
  };

  /**
   * Add metadata for a term/field pair to this instance of match data.
   *
   * @param {string} term - The term this match data is associated with
   * @param {string} field - The field in which the term was found
   * @param {object} metadata - The metadata recorded about this term in this field
   */
  lunr.MatchData.prototype.add = function (term, field, metadata) {
    if (!(term in this.metadata)) {
      this.metadata[term] = Object.create(null);
      this.metadata[term][field] = metadata;
      return;
    }

    if (!(field in this.metadata[term])) {
      this.metadata[term][field] = metadata;
      return;
    }

    var metadataKeys = Object.keys(metadata);

    for (var i = 0; i < metadataKeys.length; i++) {
      var key = metadataKeys[i];

      if (key in this.metadata[term][field]) {
        this.metadata[term][field][key] = this.metadata[term][field][key].concat(metadata[key]);
      } else {
        this.metadata[term][field][key] = metadata[key];
      }
    }
  };
  /**
   * A lunr.Query provides a programmatic way of defining queries to be performed
   * against a {@link lunr.Index}.
   *
   * Prefer constructing a lunr.Query using the {@link lunr.Index#query} method
   * so the query object is pre-initialized with the right index fields.
   *
   * @constructor
   * @property {lunr.Query~Clause[]} clauses - An array of query clauses.
   * @property {string[]} allFields - An array of all available fields in a lunr.Index.
   */
  lunr.Query = function (allFields) {
    this.clauses = [];
    this.allFields = allFields;
  };

  /**
   * Constants for indicating what kind of automatic wildcard insertion will be used when constructing a query clause.
   *
   * This allows wildcards to be added to the beginning and end of a term without having to manually do any string
   * concatenation.
   *
   * The wildcard constants can be bitwise combined to select both leading and trailing wildcards.
   *
   * @constant
   * @default
   * @property {number} wildcard.NONE - The term will have no wildcards inserted, this is the default behaviour
   * @property {number} wildcard.LEADING - Prepend the term with a wildcard, unless a leading wildcard already exists
   * @property {number} wildcard.TRAILING - Append a wildcard to the term, unless a trailing wildcard already exists
   * @see lunr.Query~Clause
   * @see lunr.Query#clause
   * @see lunr.Query#term
   * @example <caption>query term with trailing wildcard</caption>
   * query.term('foo', { wildcard: lunr.Query.wildcard.TRAILING })
   * @example <caption>query term with leading and trailing wildcard</caption>
   * query.term('foo', {
   *   wildcard: lunr.Query.wildcard.LEADING | lunr.Query.wildcard.TRAILING
   * })
   */

  lunr.Query.wildcard = new String("*");
  lunr.Query.wildcard.NONE = 0;
  lunr.Query.wildcard.LEADING = 1;
  lunr.Query.wildcard.TRAILING = 2;

  /**
   * Constants for indicating what kind of presence a term must have in matching documents.
   *
   * @constant
   * @enum {number}
   * @see lunr.Query~Clause
   * @see lunr.Query#clause
   * @see lunr.Query#term
   * @example <caption>query term with required presence</caption>
   * query.term('foo', { presence: lunr.Query.presence.REQUIRED })
   */
  lunr.Query.presence = {
    /**
     * Term's presence in a document is optional, this is the default value.
     */
    OPTIONAL: 1,

    /**
     * Term's presence in a document is required, documents that do not contain
     * this term will not be returned.
     */
    REQUIRED: 2,

    /**
     * Term's presence in a document is prohibited, documents that do contain
     * this term will not be returned.
     */
    PROHIBITED: 3

    /**
     * A single clause in a {@link lunr.Query} contains a term and details on how to
     * match that term against a {@link lunr.Index}.
     *
     * @typedef {Object} lunr.Query~Clause
     * @property {string[]} fields - The fields in an index this clause should be matched against.
     * @property {number} [boost=1] - Any boost that should be applied when matching this clause.
     * @property {number} [editDistance] - Whether the term should have fuzzy matching applied, and how fuzzy the match should be.
     * @property {boolean} [usePipeline] - Whether the term should be passed through the search pipeline.
     * @property {number} [wildcard=lunr.Query.wildcard.NONE] - Whether the term should have wildcards appended or prepended.
     * @property {number} [presence=lunr.Query.presence.OPTIONAL] - The terms presence in any matching documents.
     */

    /**
     * Adds a {@link lunr.Query~Clause} to this query.
     *
     * Unless the clause contains the fields to be matched all fields will be matched. In addition
     * a default boost of 1 is applied to the clause.
     *
     * @param {lunr.Query~Clause} clause - The clause to add to this query.
     * @see lunr.Query~Clause
     * @returns {lunr.Query}
     */
  };lunr.Query.prototype.clause = function (clause) {
    if (!('fields' in clause)) {
      clause.fields = this.allFields;
    }

    if (!('boost' in clause)) {
      clause.boost = 1;
    }

    if (!('usePipeline' in clause)) {
      clause.usePipeline = true;
    }

    if (!('wildcard' in clause)) {
      clause.wildcard = lunr.Query.wildcard.NONE;
    }

    if (clause.wildcard & lunr.Query.wildcard.LEADING && clause.term.charAt(0) != lunr.Query.wildcard) {
      clause.term = "*" + clause.term;
    }

    if (clause.wildcard & lunr.Query.wildcard.TRAILING && clause.term.slice(-1) != lunr.Query.wildcard) {
      clause.term = "" + clause.term + "*";
    }

    if (!('presence' in clause)) {
      clause.presence = lunr.Query.presence.OPTIONAL;
    }

    this.clauses.push(clause);

    return this;
  };

  /**
   * A negated query is one in which every clause has a presence of
   * prohibited. These queries require some special processing to return
   * the expected results.
   *
   * @returns boolean
   */
  lunr.Query.prototype.isNegated = function () {
    for (var i = 0; i < this.clauses.length; i++) {
      if (this.clauses[i].presence != lunr.Query.presence.PROHIBITED) {
        return false;
      }
    }

    return true;
  };

  /**
   * Adds a term to the current query, under the covers this will create a {@link lunr.Query~Clause}
   * to the list of clauses that make up this query.
   *
   * The term is used as is, i.e. no tokenization will be performed by this method. Instead conversion
   * to a token or token-like string should be done before calling this method.
   *
   * The term will be converted to a string by calling `toString`. Multiple terms can be passed as an
   * array, each term in the array will share the same options.
   *
   * @param {object|object[]} term - The term(s) to add to the query.
   * @param {object} [options] - Any additional properties to add to the query clause.
   * @returns {lunr.Query}
   * @see lunr.Query#clause
   * @see lunr.Query~Clause
   * @example <caption>adding a single term to a query</caption>
   * query.term("foo")
   * @example <caption>adding a single term to a query and specifying search fields, term boost and automatic trailing wildcard</caption>
   * query.term("foo", {
   *   fields: ["title"],
   *   boost: 10,
   *   wildcard: lunr.Query.wildcard.TRAILING
   * })
   * @example <caption>using lunr.tokenizer to convert a string to tokens before using them as terms</caption>
   * query.term(lunr.tokenizer("foo bar"))
   */
  lunr.Query.prototype.term = function (term, options) {
    if (Array.isArray(term)) {
      term.forEach(function (t) {
        this.term(t, lunr.utils.clone(options));
      }, this);
      return this;
    }

    var clause = options || {};
    clause.term = term.toString();

    this.clause(clause);

    return this;
  };
  lunr.QueryParseError = function (message, start, end) {
    this.name = "QueryParseError";
    this.message = message;
    this.start = start;
    this.end = end;
  };

  lunr.QueryParseError.prototype = new Error();
  lunr.QueryLexer = function (str) {
    this.lexemes = [];
    this.str = str;
    this.length = str.length;
    this.pos = 0;
    this.start = 0;
    this.escapeCharPositions = [];
  };

  lunr.QueryLexer.prototype.run = function () {
    var state = lunr.QueryLexer.lexText;

    while (state) {
      state = state(this);
    }
  };

  lunr.QueryLexer.prototype.sliceString = function () {
    var subSlices = [],
        sliceStart = this.start,
        sliceEnd = this.pos;

    for (var i = 0; i < this.escapeCharPositions.length; i++) {
      sliceEnd = this.escapeCharPositions[i];
      subSlices.push(this.str.slice(sliceStart, sliceEnd));
      sliceStart = sliceEnd + 1;
    }

    subSlices.push(this.str.slice(sliceStart, this.pos));
    this.escapeCharPositions.length = 0;

    return subSlices.join('');
  };

  lunr.QueryLexer.prototype.emit = function (type) {
    this.lexemes.push({
      type: type,
      str: this.sliceString(),
      start: this.start,
      end: this.pos
    });

    this.start = this.pos;
  };

  lunr.QueryLexer.prototype.escapeCharacter = function () {
    this.escapeCharPositions.push(this.pos - 1);
    this.pos += 1;
  };

  lunr.QueryLexer.prototype.next = function () {
    if (this.pos >= this.length) {
      return lunr.QueryLexer.EOS;
    }

    var char = this.str.charAt(this.pos);
    this.pos += 1;
    return char;
  };

  lunr.QueryLexer.prototype.width = function () {
    return this.pos - this.start;
  };

  lunr.QueryLexer.prototype.ignore = function () {
    if (this.start == this.pos) {
      this.pos += 1;
    }

    this.start = this.pos;
  };

  lunr.QueryLexer.prototype.backup = function () {
    this.pos -= 1;
  };

  lunr.QueryLexer.prototype.acceptDigitRun = function () {
    var char, charCode;

    do {
      char = this.next();
      charCode = char.charCodeAt(0);
    } while (charCode > 47 && charCode < 58);

    if (char != lunr.QueryLexer.EOS) {
      this.backup();
    }
  };

  lunr.QueryLexer.prototype.more = function () {
    return this.pos < this.length;
  };

  lunr.QueryLexer.EOS = 'EOS';
  lunr.QueryLexer.FIELD = 'FIELD';
  lunr.QueryLexer.TERM = 'TERM';
  lunr.QueryLexer.EDIT_DISTANCE = 'EDIT_DISTANCE';
  lunr.QueryLexer.BOOST = 'BOOST';
  lunr.QueryLexer.PRESENCE = 'PRESENCE';

  lunr.QueryLexer.lexField = function (lexer) {
    lexer.backup();
    lexer.emit(lunr.QueryLexer.FIELD);
    lexer.ignore();
    return lunr.QueryLexer.lexText;
  };

  lunr.QueryLexer.lexTerm = function (lexer) {
    if (lexer.width() > 1) {
      lexer.backup();
      lexer.emit(lunr.QueryLexer.TERM);
    }

    lexer.ignore();

    if (lexer.more()) {
      return lunr.QueryLexer.lexText;
    }
  };

  lunr.QueryLexer.lexEditDistance = function (lexer) {
    lexer.ignore();
    lexer.acceptDigitRun();
    lexer.emit(lunr.QueryLexer.EDIT_DISTANCE);
    return lunr.QueryLexer.lexText;
  };

  lunr.QueryLexer.lexBoost = function (lexer) {
    lexer.ignore();
    lexer.acceptDigitRun();
    lexer.emit(lunr.QueryLexer.BOOST);
    return lunr.QueryLexer.lexText;
  };

  lunr.QueryLexer.lexEOS = function (lexer) {
    if (lexer.width() > 0) {
      lexer.emit(lunr.QueryLexer.TERM);
    }
  };

  // This matches the separator used when tokenising fields
  // within a document. These should match otherwise it is
  // not possible to search for some tokens within a document.
  //
  // It is possible for the user to change the separator on the
  // tokenizer so it _might_ clash with any other of the special
  // characters already used within the search string, e.g. :.
  //
  // This means that it is possible to change the separator in
  // such a way that makes some words unsearchable using a search
  // string.
  lunr.QueryLexer.termSeparator = lunr.tokenizer.separator;

  lunr.QueryLexer.lexText = function (lexer) {
    while (true) {
      var char = lexer.next();

      if (char == lunr.QueryLexer.EOS) {
        return lunr.QueryLexer.lexEOS;
      }

      // Escape character is '\'
      if (char.charCodeAt(0) == 92) {
        lexer.escapeCharacter();
        continue;
      }

      if (char == ":") {
        return lunr.QueryLexer.lexField;
      }

      if (char == "~") {
        lexer.backup();
        if (lexer.width() > 0) {
          lexer.emit(lunr.QueryLexer.TERM);
        }
        return lunr.QueryLexer.lexEditDistance;
      }

      if (char == "^") {
        lexer.backup();
        if (lexer.width() > 0) {
          lexer.emit(lunr.QueryLexer.TERM);
        }
        return lunr.QueryLexer.lexBoost;
      }

      // "+" indicates term presence is required
      // checking for length to ensure that only
      // leading "+" are considered
      if (char == "+" && lexer.width() === 1) {
        lexer.emit(lunr.QueryLexer.PRESENCE);
        return lunr.QueryLexer.lexText;
      }

      // "-" indicates term presence is prohibited
      // checking for length to ensure that only
      // leading "-" are considered
      if (char == "-" && lexer.width() === 1) {
        lexer.emit(lunr.QueryLexer.PRESENCE);
        return lunr.QueryLexer.lexText;
      }

      if (char.match(lunr.QueryLexer.termSeparator)) {
        return lunr.QueryLexer.lexTerm;
      }
    }
  };

  lunr.QueryParser = function (str, query) {
    this.lexer = new lunr.QueryLexer(str);
    this.query = query;
    this.currentClause = {};
    this.lexemeIdx = 0;
  };

  lunr.QueryParser.prototype.parse = function () {
    this.lexer.run();
    this.lexemes = this.lexer.lexemes;

    var state = lunr.QueryParser.parseClause;

    while (state) {
      state = state(this);
    }

    return this.query;
  };

  lunr.QueryParser.prototype.peekLexeme = function () {
    return this.lexemes[this.lexemeIdx];
  };

  lunr.QueryParser.prototype.consumeLexeme = function () {
    var lexeme = this.peekLexeme();
    this.lexemeIdx += 1;
    return lexeme;
  };

  lunr.QueryParser.prototype.nextClause = function () {
    var completedClause = this.currentClause;
    this.query.clause(completedClause);
    this.currentClause = {};
  };

  lunr.QueryParser.parseClause = function (parser) {
    var lexeme = parser.peekLexeme();

    if (lexeme == undefined) {
      return;
    }

    switch (lexeme.type) {
      case lunr.QueryLexer.PRESENCE:
        return lunr.QueryParser.parsePresence;
      case lunr.QueryLexer.FIELD:
        return lunr.QueryParser.parseField;
      case lunr.QueryLexer.TERM:
        return lunr.QueryParser.parseTerm;
      default:
        var errorMessage = "expected either a field or a term, found " + lexeme.type;

        if (lexeme.str.length >= 1) {
          errorMessage += " with value '" + lexeme.str + "'";
        }

        throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }
  };

  lunr.QueryParser.parsePresence = function (parser) {
    var lexeme = parser.consumeLexeme();

    if (lexeme == undefined) {
      return;
    }

    switch (lexeme.str) {
      case "-":
        parser.currentClause.presence = lunr.Query.presence.PROHIBITED;
        break;
      case "+":
        parser.currentClause.presence = lunr.Query.presence.REQUIRED;
        break;
      default:
        var errorMessage = "unrecognised presence operator'" + lexeme.str + "'";
        throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }

    var nextLexeme = parser.peekLexeme();

    if (nextLexeme == undefined) {
      var errorMessage = "expecting term or field, found nothing";
      throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }

    switch (nextLexeme.type) {
      case lunr.QueryLexer.FIELD:
        return lunr.QueryParser.parseField;
      case lunr.QueryLexer.TERM:
        return lunr.QueryParser.parseTerm;
      default:
        var errorMessage = "expecting term or field, found '" + nextLexeme.type + "'";
        throw new lunr.QueryParseError(errorMessage, nextLexeme.start, nextLexeme.end);
    }
  };

  lunr.QueryParser.parseField = function (parser) {
    var lexeme = parser.consumeLexeme();

    if (lexeme == undefined) {
      return;
    }

    if (parser.query.allFields.indexOf(lexeme.str) == -1) {
      var possibleFields = parser.query.allFields.map(function (f) {
        return "'" + f + "'";
      }).join(', '),
          errorMessage = "unrecognised field '" + lexeme.str + "', possible fields: " + possibleFields;

      throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }

    parser.currentClause.fields = [lexeme.str];

    var nextLexeme = parser.peekLexeme();

    if (nextLexeme == undefined) {
      var errorMessage = "expecting term, found nothing";
      throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }

    switch (nextLexeme.type) {
      case lunr.QueryLexer.TERM:
        return lunr.QueryParser.parseTerm;
      default:
        var errorMessage = "expecting term, found '" + nextLexeme.type + "'";
        throw new lunr.QueryParseError(errorMessage, nextLexeme.start, nextLexeme.end);
    }
  };

  lunr.QueryParser.parseTerm = function (parser) {
    var lexeme = parser.consumeLexeme();

    if (lexeme == undefined) {
      return;
    }

    parser.currentClause.term = lexeme.str.toLowerCase();

    if (lexeme.str.indexOf("*") != -1) {
      parser.currentClause.usePipeline = false;
    }

    var nextLexeme = parser.peekLexeme();

    if (nextLexeme == undefined) {
      parser.nextClause();
      return;
    }

    switch (nextLexeme.type) {
      case lunr.QueryLexer.TERM:
        parser.nextClause();
        return lunr.QueryParser.parseTerm;
      case lunr.QueryLexer.FIELD:
        parser.nextClause();
        return lunr.QueryParser.parseField;
      case lunr.QueryLexer.EDIT_DISTANCE:
        return lunr.QueryParser.parseEditDistance;
      case lunr.QueryLexer.BOOST:
        return lunr.QueryParser.parseBoost;
      case lunr.QueryLexer.PRESENCE:
        parser.nextClause();
        return lunr.QueryParser.parsePresence;
      default:
        var errorMessage = "Unexpected lexeme type '" + nextLexeme.type + "'";
        throw new lunr.QueryParseError(errorMessage, nextLexeme.start, nextLexeme.end);
    }
  };

  lunr.QueryParser.parseEditDistance = function (parser) {
    var lexeme = parser.consumeLexeme();

    if (lexeme == undefined) {
      return;
    }

    var editDistance = parseInt(lexeme.str, 10);

    if (isNaN(editDistance)) {
      var errorMessage = "edit distance must be numeric";
      throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }

    parser.currentClause.editDistance = editDistance;

    var nextLexeme = parser.peekLexeme();

    if (nextLexeme == undefined) {
      parser.nextClause();
      return;
    }

    switch (nextLexeme.type) {
      case lunr.QueryLexer.TERM:
        parser.nextClause();
        return lunr.QueryParser.parseTerm;
      case lunr.QueryLexer.FIELD:
        parser.nextClause();
        return lunr.QueryParser.parseField;
      case lunr.QueryLexer.EDIT_DISTANCE:
        return lunr.QueryParser.parseEditDistance;
      case lunr.QueryLexer.BOOST:
        return lunr.QueryParser.parseBoost;
      case lunr.QueryLexer.PRESENCE:
        parser.nextClause();
        return lunr.QueryParser.parsePresence;
      default:
        var errorMessage = "Unexpected lexeme type '" + nextLexeme.type + "'";
        throw new lunr.QueryParseError(errorMessage, nextLexeme.start, nextLexeme.end);
    }
  };

  lunr.QueryParser.parseBoost = function (parser) {
    var lexeme = parser.consumeLexeme();

    if (lexeme == undefined) {
      return;
    }

    var boost = parseInt(lexeme.str, 10);

    if (isNaN(boost)) {
      var errorMessage = "boost must be numeric";
      throw new lunr.QueryParseError(errorMessage, lexeme.start, lexeme.end);
    }

    parser.currentClause.boost = boost;

    var nextLexeme = parser.peekLexeme();

    if (nextLexeme == undefined) {
      parser.nextClause();
      return;
    }

    switch (nextLexeme.type) {
      case lunr.QueryLexer.TERM:
        parser.nextClause();
        return lunr.QueryParser.parseTerm;
      case lunr.QueryLexer.FIELD:
        parser.nextClause();
        return lunr.QueryParser.parseField;
      case lunr.QueryLexer.EDIT_DISTANCE:
        return lunr.QueryParser.parseEditDistance;
      case lunr.QueryLexer.BOOST:
        return lunr.QueryParser.parseBoost;
      case lunr.QueryLexer.PRESENCE:
        parser.nextClause();
        return lunr.QueryParser.parsePresence;
      default:
        var errorMessage = "Unexpected lexeme type '" + nextLexeme.type + "'";
        throw new lunr.QueryParseError(errorMessage, nextLexeme.start, nextLexeme.end);
    }
  }

  /**
   * export the module via AMD, CommonJS or as a browser global
   * Export code from https://github.com/umdjs/umd/blob/master/returnExports.js
   */
  ;(function (root, factory) {
    if (true) {
      // AMD. Register as an anonymous module.
      !(__WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.call(exports, __webpack_require__, exports, module)) :
				__WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
    } else if ((typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object') {
      /**
       * Node. Does not work with strict CommonJS, but
       * only CommonJS-like enviroments that support module.exports,
       * like Node.
       */
      module.exports = factory();
    } else {
      // Browser globals (root is window)
      root.lunr = factory();
    }
  })(this, function () {
    /**
     * Just return a value to define the module export.
     * This example returns an object, but the module
     * can return a function as the exported value.
     */
    return lunr;
  });
})();

/***/ }),
/* 103 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Sidebar_Position__ = __webpack_require__(104);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */



/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Position: __WEBPACK_IMPORTED_MODULE_0__Sidebar_Position__["a" /* default */]
});

/***/ }),
/* 104 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Position = function () {

  /**
   * Set sidebars to locked state and limit height to parent node
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Sidebar
   * @property {HTMLElement} parent_ - Sidebar container
   * @property {HTMLElement} header_ - Header
   * @property {number} height_ - Current sidebar height
   * @property {number} offset_ - Current page y-offset
   * @property {boolean} pad_ - Pad when header is fixed
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   * @param {(string|HTMLElement)} header - Selector or HTML element
   */
  function Position(el, header) {
    _classCallCheck(this, Position);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLElement) || !(ref.parentNode instanceof HTMLElement)) throw new ReferenceError();
    this.el_ = ref;
    this.parent_ = ref.parentNode;

    /* Retrieve header */
    ref = typeof header === "string" ? document.querySelector(header) : header;
    if (!(ref instanceof HTMLElement)) throw new ReferenceError();
    this.header_ = ref;

    /* Initialize current height and test whether header is fixed */
    this.height_ = 0;
    this.pad_ = window.getComputedStyle(this.header_).position === "fixed";
  }

  /**
   * Initialize sidebar state
   */


  _createClass(Position, [{
    key: "setup",
    value: function setup() {
      var top = Array.prototype.reduce.call(this.parent_.children, function (offset, child) {
        return Math.max(offset, child.offsetTop);
      }, 0);

      /* Set lock offset for element with largest top offset */
      this.offset_ = top - (this.pad_ ? this.header_.offsetHeight : 0);
      this.update();
    }

    /**
     * Update locked state and height
     *
     * The inner height of the window (= the visible area) is the maximum
     * possible height for the stretching sidebar. This height must be deducted
     * by the height of the fixed header (56px). Depending on the page y-offset,
     * the top offset of the sidebar must be taken into account, as well as the
     * case where the window is scrolled beyond the sidebar container.
     *
     * @param {Event?} ev - Event
     */

  }, {
    key: "update",
    value: function update(ev) {
      var offset = window.pageYOffset;
      var visible = window.innerHeight;

      /* Update offset, in case window is resized */
      if (ev && ev.type === "resize") this.setup();

      /* Set bounds of sidebar container - must be calculated on every run, as
         the height of the content might change due to loading images etc. */
      var bounds = {
        top: this.pad_ ? this.header_.offsetHeight : 0,
        bottom: this.parent_.offsetTop + this.parent_.offsetHeight

        /* Calculate new offset and height */
      };var height = visible - bounds.top - Math.max(0, this.offset_ - offset) - Math.max(0, offset + visible - bounds.bottom);

      /* If height changed, update element */
      if (height !== this.height_) this.el_.style.height = (this.height_ = height) + "px";

      /* Sidebar should be locked, as we're below parent offset */
      if (offset >= this.offset_) {
        if (this.el_.dataset.mdState !== "lock") this.el_.dataset.mdState = "lock";

        /* Sidebar should be unlocked, if locked */
      } else if (this.el_.dataset.mdState === "lock") {
        this.el_.dataset.mdState = "";
      }
    }

    /**
     * Reset locked state and height
     */

  }, {
    key: "reset",
    value: function reset() {
      this.el_.dataset.mdState = "";
      this.el_.style.height = "";
      this.height_ = 0;
    }
  }]);

  return Position;
}();

/* harmony default export */ __webpack_exports__["a"] = (Position);

/***/ }),
/* 105 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Source_Adapter__ = __webpack_require__(106);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_1__Source_Repository__ = __webpack_require__(110);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */




/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Adapter: __WEBPACK_IMPORTED_MODULE_0__Source_Adapter__["a" /* default */],
  Repository: __WEBPACK_IMPORTED_MODULE_1__Source_Repository__["a" /* default */]
});

/***/ }),
/* 106 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Adapter_GitHub__ = __webpack_require__(107);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */



/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  GitHub: __WEBPACK_IMPORTED_MODULE_0__Adapter_GitHub__["a" /* default */]
});

/***/ }),
/* 107 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Abstract__ = __webpack_require__(108);
var _slicedToArray = function () { function sliceIterator(arr, i) { var _arr = []; var _n = true; var _d = false; var _e = undefined; try { for (var _i = arr[Symbol.iterator](), _s; !(_n = (_s = _i.next()).done); _n = true) { _arr.push(_s.value); if (i && _arr.length === i) break; } } catch (err) { _d = true; _e = err; } finally { try { if (!_n && _i["return"]) _i["return"](); } finally { if (_d) throw _e; } } return _arr; } return function (arr, i) { if (Array.isArray(arr)) { return arr; } else if (Symbol.iterator in Object(arr)) { return sliceIterator(arr, i); } else { throw new TypeError("Invalid attempt to destructure non-iterable instance"); } }; }();

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */



/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var GitHub = function (_Abstract) {
  _inherits(GitHub, _Abstract);

  /**
   * Retrieve repository information from GitHub
   *
   * @constructor
   *
   * @property {string} name_ - Name of the repository
   *
   * @param {(string|HTMLAnchorElement)} el - Selector or HTML element
   */
  function GitHub(el) {
    _classCallCheck(this, GitHub);

    /* Extract user (and repository name) from URL, as we have to query for all
       repositories, to omit 404 errors for private repositories */
    var _this = _possibleConstructorReturn(this, (GitHub.__proto__ || Object.getPrototypeOf(GitHub)).call(this, el));

    var matches = /^.+github\.com\/([^/]+)\/?([^/]+)?.*$/.exec(_this.base_);
    if (matches && matches.length === 3) {
      var _matches = _slicedToArray(matches, 3),
          user = _matches[1],
          name = _matches[2];

      /* Initialize base URL and repository name */


      _this.base_ = "https://api.github.com/users/" + user + "/repos";
      _this.name_ = name;
    }
    return _this;
  }

  /**
   * Fetch relevant repository information from GitHub
   *
   * @return {Promise<Array<string>>} Promise returning an array of facts
   */


  _createClass(GitHub, [{
    key: "fetch_",
    value: function fetch_() {
      var _this2 = this;

      var paginate = function paginate() {
        var page = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;

        return fetch(_this2.base_ + "?per_page=30&page=" + page).then(function (response) {
          return response.json();
        }).then(function (data) {
          if (!(data instanceof Array)) throw new TypeError();

          /* Display number of stars and forks, if repository is given */
          if (_this2.name_) {
            var repo = data.find(function (item) {
              return item.name === _this2.name_;
            });
            if (!repo && data.length === 30) return paginate(page + 1);

            /* If we found a repo, extract the facts */
            return repo ? [_this2.format_(repo.stargazers_count) + " Stars", _this2.format_(repo.forks_count) + " Forks"] : [];

            /* Display number of repositories, otherwise */
          } else {
            return [data.length + " Repositories"];
          }
        });
      };

      /* Paginate through repos */
      return paginate();
    }
  }]);

  return GitHub;
}(__WEBPACK_IMPORTED_MODULE_0__Abstract__["a" /* default */]);

/* harmony default export */ __webpack_exports__["a"] = (GitHub);

/***/ }),
/* 108 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_js_cookie__ = __webpack_require__(109);
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0_js_cookie___default = __webpack_require__.n(__WEBPACK_IMPORTED_MODULE_0_js_cookie__);
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */



/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Abstract = function () {

  /**
   * Retrieve repository information
   *
   * @constructor
   *
   * @property {HTMLAnchorElement} el_ - Link to repository
   * @property {string} base_ - API base URL
   * @property {number} salt_ - Unique identifier
   *
   * @param {(string|HTMLAnchorElement)} el - Selector or HTML element
   */
  function Abstract(el) {
    _classCallCheck(this, Abstract);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLAnchorElement)) throw new ReferenceError();
    this.el_ = ref;

    /* Retrieve base URL */
    this.base_ = this.el_.href;
    this.salt_ = this.hash_(this.base_);
  }

  /**
   * Retrieve data from Cookie or fetch from respective API
   *
   * @return {Promise<Array<string>>} Promise that returns an array of facts
   */


  _createClass(Abstract, [{
    key: "fetch",
    value: function fetch() {
      var _this = this;

      return new Promise(function (resolve) {
        var cached = __WEBPACK_IMPORTED_MODULE_0_js_cookie___default.a.getJSON(_this.salt_ + ".cache-source");
        if (typeof cached !== "undefined") {
          resolve(cached);

          /* If the data is not cached in a cookie, invoke fetch and set
             a cookie that automatically expires in 15 minutes */
        } else {
          _this.fetch_().then(function (data) {
            __WEBPACK_IMPORTED_MODULE_0_js_cookie___default.a.set(_this.salt_ + ".cache-source", data, { expires: 1 / 96 });
            resolve(data);
          });
        }
      });
    }

    /**
     * Abstract private function that fetches relevant repository information
     *
     * @abstract
     */

  }, {
    key: "fetch_",
    value: function fetch_() {
      throw new Error("fetch_(): Not implemented");
    }

    /**
     * Format a number with suffix
     *
     * @param {number} number - Number to format
     * @return {string} Formatted number
     */

  }, {
    key: "format_",
    value: function format_(number) {
      if (number > 10000) return (number / 1000).toFixed(0) + "k";else if (number > 1000) return (number / 1000).toFixed(1) + "k";
      return "" + number;
    }

    /**
     * Simple hash function
     *
     * Taken from http://stackoverflow.com/a/7616484/1065584
     *
     * @param {string} str - Input string
     * @return {number} Hashed string
     */

  }, {
    key: "hash_",
    value: function hash_(str) {
      var hash = 0;
      if (str.length === 0) return hash;
      for (var i = 0, len = str.length; i < len; i++) {
        hash = (hash << 5) - hash + str.charCodeAt(i);
        hash |= 0; // Convert to 32bit integer
      }
      return hash;
    }
  }]);

  return Abstract;
}();

/* harmony default export */ __webpack_exports__["a"] = (Abstract);

/***/ }),
/* 109 */
/***/ (function(module, exports, __webpack_require__) {

"use strict";
var __WEBPACK_AMD_DEFINE_FACTORY__, __WEBPACK_AMD_DEFINE_RESULT__;

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

/*!
 * JavaScript Cookie v2.2.1
 * https://github.com/js-cookie/js-cookie
 *
 * Copyright 2006, 2015 Klaus Hartl & Fagner Brack
 * Released under the MIT license
 */
;(function (factory) {
	var registeredInModuleLoader;
	if (true) {
		!(__WEBPACK_AMD_DEFINE_FACTORY__ = (factory),
				__WEBPACK_AMD_DEFINE_RESULT__ = (typeof __WEBPACK_AMD_DEFINE_FACTORY__ === 'function' ?
				(__WEBPACK_AMD_DEFINE_FACTORY__.call(exports, __webpack_require__, exports, module)) :
				__WEBPACK_AMD_DEFINE_FACTORY__),
				__WEBPACK_AMD_DEFINE_RESULT__ !== undefined && (module.exports = __WEBPACK_AMD_DEFINE_RESULT__));
		registeredInModuleLoader = true;
	}
	if (( false ? 'undefined' : _typeof(exports)) === 'object') {
		module.exports = factory();
		registeredInModuleLoader = true;
	}
	if (!registeredInModuleLoader) {
		var OldCookies = window.Cookies;
		var api = window.Cookies = factory();
		api.noConflict = function () {
			window.Cookies = OldCookies;
			return api;
		};
	}
})(function () {
	function extend() {
		var i = 0;
		var result = {};
		for (; i < arguments.length; i++) {
			var attributes = arguments[i];
			for (var key in attributes) {
				result[key] = attributes[key];
			}
		}
		return result;
	}

	function decode(s) {
		return s.replace(/(%[0-9A-Z]{2})+/g, decodeURIComponent);
	}

	function init(converter) {
		function api() {}

		function set(key, value, attributes) {
			if (typeof document === 'undefined') {
				return;
			}

			attributes = extend({
				path: '/'
			}, api.defaults, attributes);

			if (typeof attributes.expires === 'number') {
				attributes.expires = new Date(new Date() * 1 + attributes.expires * 864e+5);
			}

			// We're using "expires" because "max-age" is not supported by IE
			attributes.expires = attributes.expires ? attributes.expires.toUTCString() : '';

			try {
				var result = JSON.stringify(value);
				if (/^[\{\[]/.test(result)) {
					value = result;
				}
			} catch (e) {}

			value = converter.write ? converter.write(value, key) : encodeURIComponent(String(value)).replace(/%(23|24|26|2B|3A|3C|3E|3D|2F|3F|40|5B|5D|5E|60|7B|7D|7C)/g, decodeURIComponent);

			key = encodeURIComponent(String(key)).replace(/%(23|24|26|2B|5E|60|7C)/g, decodeURIComponent).replace(/[\(\)]/g, escape);

			var stringifiedAttributes = '';
			for (var attributeName in attributes) {
				if (!attributes[attributeName]) {
					continue;
				}
				stringifiedAttributes += '; ' + attributeName;
				if (attributes[attributeName] === true) {
					continue;
				}

				// Considers RFC 6265 section 5.2:
				// ...
				// 3.  If the remaining unparsed-attributes contains a %x3B (";")
				//     character:
				// Consume the characters of the unparsed-attributes up to,
				// not including, the first %x3B (";") character.
				// ...
				stringifiedAttributes += '=' + attributes[attributeName].split(';')[0];
			}

			return document.cookie = key + '=' + value + stringifiedAttributes;
		}

		function get(key, json) {
			if (typeof document === 'undefined') {
				return;
			}

			var jar = {};
			// To prevent the for loop in the first place assign an empty array
			// in case there are no cookies at all.
			var cookies = document.cookie ? document.cookie.split('; ') : [];
			var i = 0;

			for (; i < cookies.length; i++) {
				var parts = cookies[i].split('=');
				var cookie = parts.slice(1).join('=');

				if (!json && cookie.charAt(0) === '"') {
					cookie = cookie.slice(1, -1);
				}

				try {
					var name = decode(parts[0]);
					cookie = (converter.read || converter)(cookie, name) || decode(cookie);

					if (json) {
						try {
							cookie = JSON.parse(cookie);
						} catch (e) {}
					}

					jar[name] = cookie;

					if (key === name) {
						break;
					}
				} catch (e) {}
			}

			return key ? jar[key] : jar;
		}

		api.set = set;
		api.get = function (key) {
			return get(key, false /* read as raw */);
		};
		api.getJSON = function (key) {
			return get(key, true /* read as json */);
		};
		api.remove = function (key, attributes) {
			set(key, '', extend(attributes, {
				expires: -1
			}));
		};

		api.defaults = {};

		api.withConverter = init;

		return api;
	}

	return init(function () {});
});

/***/ }),
/* 110 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* WEBPACK VAR INJECTION */(function(Jsx) {var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Repository = function () {

  /**
   * Render repository information
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Repository information
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   */
  function Repository(el) {
    _classCallCheck(this, Repository);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof HTMLElement)) throw new ReferenceError();
    this.el_ = ref;
  }

  /**
   * Initialize the repository
   *
   * @param {Array<string>} facts - Facts to be rendered
   */


  _createClass(Repository, [{
    key: "initialize",
    value: function initialize(facts) {
      if (facts.length && this.el_.children.length) this.el_.children[this.el_.children.length - 1].appendChild(Jsx.createElement(
        "ul",
        { "class": "md-source__facts" },
        facts.map(function (fact) {
          return Jsx.createElement(
            "li",
            { "class": "md-source__fact" },
            fact
          );
        })
      ));

      /* Finish rendering with animation */
      this.el_.dataset.mdState = "done";
    }
  }]);

  return Repository;
}();

/* harmony default export */ __webpack_exports__["a"] = (Repository);
/* WEBPACK VAR INJECTION */}.call(__webpack_exports__, __webpack_require__(26)))

/***/ }),
/* 111 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
/* harmony import */ var __WEBPACK_IMPORTED_MODULE_0__Tabs_Toggle__ = __webpack_require__(112);
/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */



/* ----------------------------------------------------------------------------
 * Module
 * ------------------------------------------------------------------------- */

/* harmony default export */ __webpack_exports__["a"] = ({
  Toggle: __WEBPACK_IMPORTED_MODULE_0__Tabs_Toggle__["a" /* default */]
});

/***/ }),
/* 112 */
/***/ (function(module, __webpack_exports__, __webpack_require__) {

"use strict";
var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

/*
 * Copyright (c) 2016-2017 Martin Donath <martin.donath@squidfunk.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to
 * deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or
 * sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS
 * IN THE SOFTWARE.
 */

/* ----------------------------------------------------------------------------
 * Class
 * ------------------------------------------------------------------------- */

var Toggle = function () {

  /**
   * Toggle tabs visibility depending on page y-offset
   *
   * @constructor
   *
   * @property {HTMLElement} el_ - Content container
   * @property {number} offset_ - Toggle page-y offset
   * @property {boolean} active_ - Tabs visibility
   *
   * @param {(string|HTMLElement)} el - Selector or HTML element
   */
  function Toggle(el) {
    _classCallCheck(this, Toggle);

    var ref = typeof el === "string" ? document.querySelector(el) : el;
    if (!(ref instanceof Node)) throw new ReferenceError();
    this.el_ = ref;

    /* Initialize offset and state */
    this.offset_ = 5;
    this.active_ = false;
  }

  /**
   * Update visibility
   */


  _createClass(Toggle, [{
    key: "update",
    value: function update() {
      var active = window.pageYOffset >= this.offset_;
      if (active !== this.active_) this.el_.dataset.mdState = (this.active_ = active) ? "hidden" : "";
    }

    /**
     * Reset visibility
     */

  }, {
    key: "reset",
    value: function reset() {
      this.el_.dataset.mdState = "";
      this.active_ = false;
    }
  }]);

  return Toggle;
}();

/* harmony default export */ __webpack_exports__["a"] = (Toggle);

/***/ })
/******/ ]);