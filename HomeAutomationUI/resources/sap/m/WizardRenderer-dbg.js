/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([], function () {
	"use strict";

	var WizardRenderer = {};

	WizardRenderer.render = function (oRm, oWizard) {
		oRm.write("<div");
		oRm.writeControlData(oWizard);
		oRm.addClass("sapMWizard");
		oRm.writeClasses();
		oRm.addStyle("width", oWizard.getWidth());
		oRm.addStyle("height", oWizard.getHeight());
		oRm.writeStyles();
		oRm.write(">");

		oRm.renderControl(oWizard.getAggregation("_page"));
		oRm.renderControl(oWizard.getAggregation("_nextButton"));

		oRm.write("</div>");
	};

	return WizardRenderer;

}, /* bExport= */ true);
