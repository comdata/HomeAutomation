/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

/**
 * Initialization Code and shared classes of library sap.ui.dt.
 */
sap.ui.define([
	"sap/ui/base/ManagedObjectMetadata",
	"sap/ui/dt/SelectionMode",
	"sap/ui/core/library"
],
function (
	ManagedObjectMetadata
) {
	"use strict";

	/**
	 * DesignTime library.
	 *
	 * @namespace
	 * @name sap.ui.dt
	 * @author SAP SE
	 * @version 1.71.1
	 * @experimental This class is experimental and provides only limited functionality. Also the API might be changed in future.
	 * @private
	 */

	// delegate further initialization of this library to the Core
	sap.ui.getCore().initLibrary({
		name : "sap.ui.dt",
		version: "1.71.1",
		dependencies : ["sap.ui.core"],
		types: [
			"sap.ui.dt.SelectionMode"
		],
		interfaces: [],
		controls: [],
		elements: []
	});

	ManagedObjectMetadata.setDesignTimeDefaultMapping({
		"not-adaptable": "sap/ui/dt/designtime/notAdaptable.designtime",
		"not-removable": "sap/ui/dt/designtime/notRemovable.designtime"
	});

	return sap.ui.dt;
}, /* bExport= */ true);
