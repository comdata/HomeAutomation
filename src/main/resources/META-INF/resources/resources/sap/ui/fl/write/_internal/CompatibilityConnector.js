/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/fl/apply/_internal/Connector","sap/ui/fl/write/_internal/Connector"],function(A,W){"use strict";var C=function(){};C.prototype.loadChanges=function(c){return A.loadFlexData({reference:c.name,appVersion:c.appVersion}).then(function(f){return{changes:f,loadModules:false};});};C.prototype.loadSettings=function(){return W.loadFeatures();};C.prototype.create=function(f,c,i){var F=f;if(!Array.isArray(F)){F=[f];}return W.write({layer:F[0].layer,flexObjects:F,_transport:c,isLegacyVariant:i});};C.prototype.update=function(f,c){return W.update({flexObject:f,layer:f.layer,_transport:c});};C.prototype.deleteChange=function(f,c){return W.remove({flexObject:f,layer:f.layer,_transport:c});};C.prototype.getFlexInfo=function(p){return W.getFlexInfo(p);};C.prototype.resetChanges=function(p){return W.reset({reference:p.sReference,layer:p.sLayer,appVersion:p.sAppVersion,generator:p.sGenerator,selectorIds:p.aSelectorIds,changeTypes:p.aChangeTypes});};return C;},true);
