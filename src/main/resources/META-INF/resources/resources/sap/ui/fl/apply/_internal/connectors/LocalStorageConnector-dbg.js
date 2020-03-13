/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([
	"sap/base/util/merge",
	"sap/ui/fl/apply/_internal/connectors/BrowserStorageConnector"
], function(
	merge,
	BrowserStorageConnector
) {
	"use strict";

	/**
	 * Connector for requesting data from <code>window.localStorage</code>.
	 *
	 * @namespace sap.ui.fl.apply._internal.connectors.LocalStorageConnector
	 * @experimental Since 1.70
	 * @since 1.70
	 * @private
	 * @ui5-restricted sap.ui.fl.apply._internal.Connector, sap.ui.fl.write._internal.Connector
	 */
	var LocalStorageConnector = merge({}, BrowserStorageConnector, /** @lends sap.ui.fl.apply._internal.connectors.LocalStorageConnector */ {
		oStorage: window.localStorage
	});

	return LocalStorageConnector;
}, true);
