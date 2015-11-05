/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define(function () {

	"use strict";

	var WizardStepRenderer = {};

	WizardStepRenderer.render = function (oRm, oStep) {
		oRm.write("<div");
		oRm.writeControlData(oStep);
		oRm.addClass("sapMWizardStep");
		oRm.writeClasses();
		oRm.write(">");
		oRm.write("<h3 class='sapMWizardStepTitle'>");
		oRm.writeEscaped(oStep.getTitle());
		oRm.renderControl(oStep.getAggregation("_editButton"));
		oRm.write("</h3>");

		this.renderContent(oRm, oStep.getContent());

		oRm.write("</div>");
	};

	WizardStepRenderer.renderContent = function (oRm, aChildren) {
		aChildren.forEach(oRm.renderControl);
	};

	return WizardStepRenderer;

}, /* bExport= */ true);
