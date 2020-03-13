/*!
* OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
*/
sap.ui.define(["./CustomElementBase"],function(C){"use strict";function e(c,a){function U(){return C.apply(this,arguments);}U.prototype=Object.create(C.prototype);U.prototype.constructor=U;Object.assign(U,C);var d=[];if(c==="ui-integration-card"){d.push("ui-integration-host-configuration");}U.define(c,a,d);}return{registerTag:function registerTag(t,a){e(t,a);}};});
