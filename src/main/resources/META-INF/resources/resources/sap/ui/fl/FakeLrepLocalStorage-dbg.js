/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([
	"sap/ui/fl/FakeLrepStorage"
], function(
	FakeLrepStorage
) {
	/**
	 * Class handling the Fake Lrep storage for local storage
	 *
	 * @class
	 *
	 * @author SAP SE
	 * @version 1.71.1
	 *
	 * @private
	 * @static
	 * @since 1.48
	 * @alias sap.ui.fl.FakeLrepLocalStorage
	 */

	"use strict";

	return FakeLrepStorage(window.localStorage);
}, /* bExport= */ true);