/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([],function(){"use strict";var W={};W.render=function(r,w){r.write("<div");r.writeControlData(w);r.addClass("sapMWizard");r.writeClasses();r.addStyle("width",w.getWidth());r.addStyle("height",w.getHeight());r.writeStyles();r.write(">");r.renderControl(w.getAggregation("_page"));r.renderControl(w.getAggregation("_nextButton"));r.write("</div>");};return W;},true);
