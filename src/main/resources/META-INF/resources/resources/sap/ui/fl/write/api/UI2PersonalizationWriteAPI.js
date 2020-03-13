/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/fl/apply/_internal/ChangesController","sap/ui/fl/Cache"],function(C,a){"use strict";var U={create:function(p){var f=C.getDescriptorFlexControllerInstance(p.selector);p.reference=f.getComponentName();if(!p.reference||!p.containerKey||!p.itemName||!p.content){return Promise.reject(new Error("not all mandatory properties were provided for the storage of the personalization"));}return a.setPersonalization({reference:p.reference,containerKey:p.containerKey,itemName:p.itemName,content:p.content});},deletePersonalization:function(p){var f=C.getDescriptorFlexControllerInstance(p.selector);p.reference=f.getComponentName();if(!p.reference||!p.containerKey||!p.itemName){return Promise.reject(new Error("not all mandatory properties were provided for the deletion of the personalization"));}return a.deletePersonalization(p.reference,p.containerKey,p.itemName);}};return U;},true);
