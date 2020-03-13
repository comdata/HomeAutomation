/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/base/Log","sap/ui/test/matchers/_Visitor"],function(L,_){"use strict";var l=L.getLogger("sap.ui.test.matchers.Descendant");var v=new _();return function(d,D){return function(c){if(!d){l.debug("No descendant was defined so no controls will be filtered.");return true;}var o;if(typeof d==="string"){o=sap.ui.getCore().byId(d);}else{o=d;}var r=v.isMatching(o,function(a){return c===a;},D);if(!r){l.debug("Control '"+c+"' does not have "+(D?"direct ":"")+"descendant '"+o);}return r;};};},true);
