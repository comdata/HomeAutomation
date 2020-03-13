/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/base/util/merge","sap/base/security/encodeURLParameters","sap/ui/fl/apply/connectors/BaseConnector","sap/ui/fl/apply/_internal/connectors/Utils","sap/ui/fl/Utils","sap/base/util/restricted/_pick"],function(m,e,B,A,F,_){"use strict";var R={DATA:"/flex/data/",MODULES:"/flex/modules/"};var L=m({},B,{xsrfToken:undefined,loadFlexData:function(p){var P=_(p,["appVersion"]);var c=F.getUrlParameter("sap-client");if(c){P["sap-client"]=c;}var d=A.getUrl(R.DATA,p,P);return A.sendRequest(d,"GET",{xsrfToken:this.xsrfToken}).then(function(r){var o=r.response;if(r.token){this.xsrfToken=r.token;}if(!o.loadModules){return o;}var M=A.getUrl(R.MODULES,p,P);return A.sendRequest(M,"GET").then(function(a){return a.response;});}.bind(this));}});return L;},true);
