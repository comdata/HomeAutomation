/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/base/util/merge","sap/ui/fl/apply/connectors/BaseConnector","sap/ui/fl/apply/_internal/connectors/Utils","sap/base/util/restricted/_pick"],function(m,B,A,_){"use strict";var P="/flex/keyuser";var a="/v1";var R={DATA:P+a+"/data/"};var K=m({},B,{xsrfToken:undefined,loadFlexData:function(p){var b=_(p,["appVersion"]);var d=A.getUrl(R.DATA,p,b);return A.sendRequest(d,"GET",{xsrfToken:this.xsrfToken}).then(function(r){var o=r.response;if(r.xsrfToken){this.xsrfToken=r.xsrfToken;}o.changes=o.changes.concat(o.compVariants||[]);return o;}.bind(this));}});return K;},true);
