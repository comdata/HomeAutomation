/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['jquery.sap.global','sap/ui/core/Renderer','./CalendarRenderer'],function(q,R,C){"use strict";var a=R.extend(C);a.addAttributes=function(r,c){r.addClass("sapUiCalDateInt");var d=c._getDays();if(d>c._iDaysLarge){r.addClass("sapUiCalDateIntLarge");}if(d>c._iDaysMonthHead){r.addClass("sapUiCalDateIntMonthHead");}var w=c.getWidth();if(w&&w!=''){r.addStyle("width",w);r.writeStyles();}};return a;},true);
