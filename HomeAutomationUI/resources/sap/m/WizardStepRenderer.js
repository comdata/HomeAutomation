/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(function(){"use strict";var W={};W.render=function(r,s){r.write("<div");r.writeControlData(s);r.addClass("sapMWizardStep");r.writeClasses();r.write(">");r.write("<h3 class='sapMWizardStepTitle'>");r.writeEscaped(s.getTitle());r.renderControl(s.getAggregation("_editButton"));r.write("</h3>");this.renderContent(r,s.getContent());r.write("</div>");};W.renderContent=function(r,c){c.forEach(r.renderControl);};return W;},true);
