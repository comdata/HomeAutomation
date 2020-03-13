/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/fl/designtime/appVariant/ChangeModifier","sap/ui/fl/designtime/appVariant/AppVariantModifier","sap/ui/fl/designtime/appVariant/ModuleModifier"],function(C,A,M){"use strict";var a={};a.prepareContent=function(f,n,N,s,S){S=S||sap.ui.fl.Scenario.VersionedAppVariant;return new Promise(function(r,b){if(!f||!n||!N||!s){b("Not all parameters were passed!");}r(f);}).then(M.modify.bind(M,N)).then(C.modify.bind(C,N,s,S)).then(A.modify.bind(A,n));};return a;},false);
