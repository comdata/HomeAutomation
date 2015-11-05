/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define('sap/ui/test/TestUtils',['jquery.sap.global','sap/ui/core/Core'],function(q){"use strict";return{withNormalizedMessages:function(c){sinon.test(function(){var C=sap.ui.getCore(),g=C.getLibraryResourceBundle;this.stub(C,"getLibraryResourceBundle").returns({getText:function(k,a){var r=k,t=g.call(C).getText(k),i;for(i=0;i<10;i+=1){if(t.indexOf("{"+i+"}")>=0){r+=" "+(i>=a.length?"{"+i+"}":a[i]);}}return r;}});c.apply(this);}).apply({});}};},true);
