/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([], function() {
	"use strict";

	/**
	 * Base class for connectors.
	 *
	 * @namespace sap.ui.fl.apply.connectors.BaseConnector
	 * @experimental Since 1.67
	 * @since 1.67
	 * @version 1.71.1
	 * @public
	 */
	var BaseConnector = /** @lends sap.ui.fl.apply.api.connectors.BaseConnector */ {
		/**
		 * Interface called to get the flex data, including changes and variants.
		 *
		 * @param {object} mPropertyBag Properties needed by the connectors
		 * @param {string} mPropertyBag.flexReference Reference of the application
		 * @param {string} [mPropertyBag.appVersion] Version of the application
		 * @param {string} [mPropertyBag.url] Configured url for the connector
		 * @param {string} [mPropertyBag.cacheKey] Key which can be used to etag / cachebuster the request
		 * @returns {Promise<Object>} Promise resolving with an object containing a flex data response
		 * @private
		 * @ui5-restricted sap.ui.fl.apply._internal.Connector
		 */
		loadFlexData: function (/* mPropertyBag */) {
			return Promise.reject("loadFlexData is not implemented");
		}
	};

	return BaseConnector;
}, true);
