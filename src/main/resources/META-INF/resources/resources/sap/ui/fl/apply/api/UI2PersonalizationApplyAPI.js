/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/fl/apply/_internal/ChangesController","sap/ui/fl/Cache"],function(C,a){"use strict";var U={load:function(p){var f=C.getDescriptorFlexControllerInstance(p.selector);p.reference=f.getComponentName();p.appVersion=f.getAppVersion();if(!p.reference||!p.containerKey){return Promise.reject(new Error("not all mandatory properties were provided for the loading of the personalization"));}return a.getPersonalization(p.reference,p.appVersion,p.containerKey,p.itemName);}};return U;},true);
