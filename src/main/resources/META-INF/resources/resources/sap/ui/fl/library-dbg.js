/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([
	"sap/ui/fl/RegistrationDelegator",
	"sap/ui/core/library", // library dependency
	"sap/m/library" // library dependency
], function(RegistrationDelegator) {
	"use strict";

	/**
	* Object containing information about a control if no instance is available.
	 *
	 * @typedef {object} sap.ui.fl.ElementSelector
	 * @property {string} elementId - Control ID
	 * @property {string} elementType - Control type
	 * @property {sap.ui.core.Component} appComponent - Instance of the app component in which the control is running
	 * @private
	 * @ui5-restricted
	 */

	/**
	 * Object containing information about a component if no instance is available.
	 *
	 * @typedef {object} sap.ui.fl.ComponentSelector
	 * @property {string} appId - Control object to be used as the selector for the change
	 * @property {string} appVersion - Control object to be used as the selector for the change
	 * @private
	 * @ui5-restricted
	 */

	/**
	 * The object a change is targeted at.
	 * This can be a {@link sap.ui.core.Element} or a {@link sap.ui.core.Component} instance or an object like {@link sap.ui.fl.ElementSelector} or {@link sap.ui.fl.ComponentSelector} containing information about the element or component.
	 *
	 * @typedef {sap.ui.core.Element | sap.ui.core.Component | sap.ui.fl.ElementSelector | sap.ui.fl.ComponentSelector} sap.ui.fl.Selector
	 * @since 1.69
	 * @private
	 * @ui5-restricted
	 */


	/**
	 * SAPUI5 Library for SAPUI5 Flexibility and Descriptor Changes, App Variants, Control Variants (Views) and Personalization.
	 * @namespace
	 * @name sap.ui.fl
	 * @author SAP SE
	 * @version 1.71.1
	 * @private
	 * @ui5-restricted UI5 controls and tools creating flexibility changes
	 */
	sap.ui.getCore().initLibrary({
		name: "sap.ui.fl",
		version: "1.71.1",
		controls: ["sap.ui.fl.variants.VariantManagement"],
		dependencies: [
			"sap.ui.core", "sap.m"
		],
		designtime: "sap/ui/fl/designtime/library.designtime",
		extensions: {
			"sap.ui.support": {
				diagnosticPlugins: [
					"sap/ui/fl/support/Flexibility"
				],
				//Configuration used for rule loading of Support Assistant
				publicRules: true
			}
		}
	});

	/**
	 * Available layers
	 *
	 * @enum {string}
	 */
	sap.ui.fl.Layer = {
		USER: "USER",
		CUSTOMER: "CUSTOMER",
		CUSTOMER_BASE: "CUSTOMER_BASE",
		PARTNER: "PARTNER",
		VENDOR: "VENDOR",
		BASE: "BASE"
	};

	/**
	 * Available scenarios
	 *
	 * @enum {string}
	 */
	sap.ui.fl.Scenario = {
		AppVariant: "APP_VARIANT",
		VersionedAppVariant: "VERSIONED_APP_VARIANT",
		AdaptationProject: "ADAPTATION_PROJECT",
		FioriElementsFromScratch: "FE_FROM_SCRATCH",
		UiAdaptation: "UI_ADAPTATION"
	};

	RegistrationDelegator.registerAll();

	return sap.ui.fl;
});
