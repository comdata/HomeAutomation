/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([],function(){"use strict";var O={};O.render=function(r,c){r.write("<span ");r.writeControlData(c);r.addClass("sapMObjectMarker");r.writeClasses();r.write(">");r.renderControl(c._getInnerControl());r.write("</span>");};return O;},true);
