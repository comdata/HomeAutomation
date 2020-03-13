/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/base/util/merge"],function(m){"use strict";function f(r){var v={};r.some(function(R){if(R.variantSection&&Object.keys(R.variantSection).length>0){v=R.variantSection;return true;}});return v;}function c(){return{variantManagementChanges:{},variants:[]};}function a(v,V){var r=v[V];if(!r){r=C._createStandardVariant(V);v[V]=r;}return r;}function g(r){if(!r){return[];}return r.controlChanges.map(function(p){return p;});}function b(r){if(!r){return{};}var v={};Object.keys(r.variantChanges).forEach(function(s){v[s]=r.variantChanges[s].map(function(p){return p;});});return v;}function d(v,V){var p=m({},v);V.forEach(function(q){var s=q.variantReference;var r;if(s){r=a(p,s);}p[q.fileName]={content:q,controlChanges:g(r),variantChanges:b(r)};});return p;}function e(v,V){var p=m({},v);V.forEach(function(q){p[q.variantReference]=p[q.variantReference]||C._createStandardVariant(q.variantReference);p[q.variantReference].controlChanges.push(q);});return p;}function h(v,V){var p=m({},v);V.forEach(function(q){p[q.selector.id]=p[q.selector.id]||C._createStandardVariant(q.selector.id);var r=p[q.selector.id].variantChanges[q.changeType]||[];r.push(q);p[q.selector.id].variantChanges[q.changeType]=r;});return p;}function i(r,v){var R=m({},r);Object.keys(v).forEach(function(V){var p=v[V];var s=p.content.variantManagementReference;if(!R.variantSection[s]){R.variantSection[s]=c();}R.variantSection[s].variants.push(p);});return R;}function j(r,v){var R=m({},r);v.forEach(function(p){var V=p.selector.id;if(!R.variantSection[V]){R.variantSection[V]=c();}var s=p.changeType;if(!R.variantSection[V].variantManagementChanges[s]){R.variantSection[V].variantManagementChanges[s]=[];}R.variantSection[V].variantManagementChanges[s].push(p);});return R;}function k(r){var R=m({},r);Object.keys(R.variantSection).forEach(function(v){var V=R.variantSection[v];var s=V.variants.findIndex(function(p){return p.content.variantReference===v;});var S;if(s===-1){S=C._createStandardVariant(v);}else{S=V.variants[s];V.variants.splice(s,1);}V.variants.unshift(S);});return R;}function l(r){var v=[];r.forEach(function(R){v=v.concat(R.variantManagementChanges);});return v;}function n(r){var v={};r.forEach(function(R){v=d(v,R.variants);v=e(v,R.variantDependentControlChanges);v=h(v,R.variantChanges);});return v;}function o(r,v,V){r=i(r,v);r=j(r,V);r=k(r);return r;}var C={};C._concatChanges=function(r){var p=[];r.forEach(function(R){p=p.concat(R.changes);});var q=[];p=p.filter(function(s){var F=s.fileName;var t=q.indexOf(F)!==-1;if(t){return false;}q.push(F);return true;});return p;};C.merge=function(p){var r={changes:C._concatChanges(p.responses),variantSection:{}};if(p.variantSectionSufficient){r.variantSection=f(p.responses);}else{var v=n(p.responses);var V=l(p.responses);r=o(r,v,V);}return r;};C._createStandardVariant=function(v){return{content:{fileName:v,variantManagementReference:v,content:{title:"STANDARD_VARIANT_TITLE"},favorite:true,visible:true},variantChanges:{},controlChanges:[]};};return C;});
