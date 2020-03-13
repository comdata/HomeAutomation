/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
(function(w){"use strict";var c,C;var s=document.currentScript||document.querySelector("script[src*='/sap-ui-integration.js']");function b(){if(w.sap&&w.sap.ui&&w.sap.ui.getCore){c=w.sap.ui.getCore();return a();}w.sap.ui.require(['sap/ui/core/Core','sap/ui/integration/util/CustomElements'],function(d,e){C=e;d.boot();c=d;d.attachInit(function(){a();});});}function r(l){var L=c.getLoadedLibraries()[l];var p=s.getAttribute("prefix")||L.defaultCustomElementsPrefix,t=Object.keys(L.customElements),T=s.getAttribute("tags");if(T){t=T.split(",");}w.sap.ui.require(t.map(function(o,i){return L.customElements[t[i]];}),function(){var d=arguments;t.forEach(function(o,i){C.registerTag(p+"-"+t[i],d[i]);});});}function a(){c.loadLibraries(["sap/ui/integration"],{async:true}).then(function(){r("sap.ui.integration");});}b();})(window);
