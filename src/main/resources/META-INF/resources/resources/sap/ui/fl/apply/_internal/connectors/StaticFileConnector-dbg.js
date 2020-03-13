/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([
	"sap/base/util/merge",
	"sap/ui/fl/apply/connectors/BaseConnector",
	"sap/ui/fl/apply/_internal/connectors/Utils",
	"sap/base/Log",
	"sap/base/util/LoaderExtensions"
], function(
	merge,
	BaseConnector,
	Utils,
	Log,
	LoaderExtensions
) {
	"use strict";

	/**
	 * Connector for requesting data from an LRep based back end.
	 *
	 * @namespace sap.ui.fl.apply._internal.connectors.StaticFileConnector
	 * @since 1.67
	 * @private
	 * @ui5-restricted sap.ui.fl.apply._internal.Connector
	 */
	var StaticFileConnector = merge({}, BaseConnector, /** sap.ui.fl.apply._internal.connectors.StaticFileConnector */ {
		/**
		 * Provides the flex data stored in the build changes-bundle JSON file.
		 *
		 * @param {object} mPropertyBag Properties needed by the connector
		 * @param {string} mPropertyBag.reference Reference of the application
		 * @returns {Promise<Object>} Resolving with an object containing a data contained in the changes-bundle
		 */
		loadFlexData: function (mPropertyBag) {
			var sReference = mPropertyBag.reference;
			var sResourcePath = sReference.replace(/\./g, "/") + "/changes/changes-bundle.json";
			var bChangesBundleLoaded = !!sap.ui.loader._.getModuleState(sResourcePath);
			var oConfiguration = sap.ui.getCore().getConfiguration();
			if (bChangesBundleLoaded || oConfiguration.getDebug() || oConfiguration.isFlexBundleRequestForced()) {
				try {
					var oResponse = {
						changes: LoaderExtensions.loadResource(sResourcePath)
					};
					return Promise.resolve(oResponse);
				} catch (e) {
					Log.warning("flexibility did not find a changes-bundle.json for the application: " + sReference);
				}
			}

			return Promise.resolve(Utils.getEmptyFlexDataResponse());
		}
	});

	return StaticFileConnector;
}, true);
