/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/base/Object","sap/ui/test/Opa5","sap/ui/test/OpaPlugin","sap/ui/test/actions/Press","sap/ui/test/_LogCollector","sap/ui/test/_OpaLogger","sap/ui/thirdparty/jquery","sap/ui/base/ManagedObjectMetadata"],function(U,O,a,P,_,b,$,M){"use strict";var p=new a();var c=U.extend("sap.ui.test._ControlFinder",{});var l=b.getLogger("sap.ui.test._ControlFinder");var L=_.getInstance('^((?!autowaiter).)*$');var d=[];c._findControls=function(o){if(e(o)){try{return c._findControls(f(o));}catch(E){l.error(E);return[];}}else{var C=p._getFilteredControlsByDeclaration(o);if(C===a.FILTER_FOUND_NO_CONTROLS){return[];}else{return $.isArray(C)?C:[C];}}};c._findElements=function(o){L.start();var C=c._findControls(o);var G=function(h){return new P().$(h)[0]||h.getDomRef();};var E=C.map(function(h){switch(o.interaction){case"root":return h.getDomRef();case"focus":return h.getFocusDomRef();case"press":var i=new P()._getAdapter(h);return h.$(i)[0];case"auto":return G(h);default:i=o.interaction&&o.interaction.idSuffix;return i?h.$(i)[0]:G(h);}});d.push(L.getAndClearLog());L.stop();return E;};c._getControlForElement=function(E){var s=Object.prototype.toString.call(E)==="[object String]"?document.getElementById(E):E;var h=c._getIdentifiedDOMElement(s).control();return h&&h[0];};c._getControlProperty=function(C,s){var h=$.extend({},C.mProperties,{id:C.getId()});return Object.keys(h).indexOf(s)>-1?h[s]:null;};c._getDomElementIDSuffix=function(E,C){var s=E.id;var D="-";if(!M.isGeneratedId(s)){var S=C.getId().length;return s.charAt(S)===D&&s.substring(S+1);}};c._getIdentifiedDOMElement=function(s){return $(s).closest("[data-sap-ui]");};c._getLatestLog=function(){return d&&d.pop();};function e(o){return o.ancestor||o.descendant;}function f(o){var h={};if(o.ancestor){var A=g(o);var i=c._findControls(A)[0];if(i){h.ancestor=i;delete o.ancestor;}else{throw new Error("Ancestor not found using selector: "+JSON.stringify(A));}}if(o.descendant){var D=c._findControls(o.descendant)[0];if(D){h.descendant=D;delete o.descendant;}else{throw new Error("Descendant not found using selector: "+JSON.stringify(o.descendant));}}if($.isEmptyObject(h)){return o;}else{return $.extend({},o,{matchers:h});}}function g(o){if($.isArray(o.ancestor)){return{id:o.ancestor[0]};}else{return o.ancestor;}}return c;});
