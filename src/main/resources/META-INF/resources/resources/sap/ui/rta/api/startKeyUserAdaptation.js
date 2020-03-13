/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/rta/RuntimeAuthoring","sap/ui/base/ManagedObject","sap/ui/fl/write/api/FeaturesAPI","sap/base/Log"],function(R,M,F,L){"use strict";return function(p){if(!(p.rootControl instanceof M)){return Promise.reject(new Error("An invalid root control was passed"));}return F.isKeyUser().then(function(i){if(!i){throw new Error("Key user rights are not available");}Object.assign(p,{flexSettings:{developerMode:false,layer:"CUSTOMER"},validateAppVersion:true});var r=new R(p);r.attachEvent('stop',function(){r.destroy();});return r.start();}).catch(function(e){L.error("UI Adaptation could not be started",e.message);throw e;});};});
