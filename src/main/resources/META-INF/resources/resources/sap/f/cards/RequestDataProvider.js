/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/f/cards/DataProvider","jquery.sap.global","sap/base/Log"],function(D,q,L){"use strict";var m=["no-cors","same-origin","cors"];var M=["GET","POST"];var R=D.extend("sap.f.cards.RequestDataProvider");R.prototype.getData=function(){return this._fetch(this.getSettings().request);};R.prototype._isValidRequest=function(r){if(!r){return false;}if(m.indexOf(r.mode)===-1){return false;}if(M.indexOf(r.method)===-1){return false;}if(typeof r.url!=="string"){return false;}return true;};R.prototype._fetch=function(r){var s="Invalid request";return new Promise(function(a,b){if(!r){L.error(s);b(s);return;}var o={"mode":r.mode||"cors","url":r.url,"method":(r.method&&r.method.toUpperCase())||"GET","data":r.parameters,"headers":r.headers,"timeout":15000,"xhrFields":{"withCredentials":!!r.withCredentials}};if(o.method==="GET"){o.dataType="json";}if(this._isValidRequest(o)){q.ajax(o).done(function(d){a(d);}).fail(function(j,t,e){b(e);});}else{L.error(s);b(s);}}.bind(this));};return R;});
