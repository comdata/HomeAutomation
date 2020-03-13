/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([
	"sap/ui/fl/registry/Settings"
], function(
	Settings
) {
	"use strict";

	/**
	 * Provides an API to determine which features are available for flexibility.
	 *
	 * @namespace sap.ui.fl.write.api.FeaturesAPI
	 * @experimental Since 1.70
	 * @since 1.70
	 * @public
	 *
	 */

	var FeaturesAPI = /** @lends sap.ui.fl.write.api.FeaturesAPI */ {

		/**
		 * Determines if the current runtime can publish flexibility content in the connected backend.
		 *
		 * @returns {Promise<boolean>} Promise resolving with a flag if publishing is available
		 *
		 * @private
	 	 * @ui5-restricted
		 */
		isPublishAvailable: function () {
			return Settings.getInstance().then(function (oSettings) {
				return !oSettings.isProductiveSystem();
			});
		},

		/**
		 * Checks if key user rights are available for the current user.
		 * Application developers can use this API to decide if the key user adaptation
		 * feature should be visible to the current user. This only applies if key user adaptation
		 * should be handled standalone without an SAP Fiori launchpad.
		 *
		 * @returns {Promise<boolean>} Resolves to a boolean indicating if key user is available
		 * @public
		 */
		isKeyUser: function () {
			return Settings.getInstance()
				.then(function (oSettings) {
					return oSettings.isKeyUser();
				});
		}
	};

	return FeaturesAPI;
});
