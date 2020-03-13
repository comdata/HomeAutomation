/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['sap/ui/core/Renderer','./ToolbarRenderer',"sap/m/BarInPageEnabler"],function(R,T,B){"use strict";var O=R.extend(T);O.renderBarContent=function(r,t){t._getVisibleContent().forEach(function(c){B.addChildClassTo(c,t);r.renderControl(c);});if(t._getOverflowButtonNeeded()){O.renderOverflowButton(r,t);}};O.renderOverflowButton=function(r,t){var o=t._getOverflowButton();B.addChildClassTo(o,t);r.renderControl(o);};return O;},true);
