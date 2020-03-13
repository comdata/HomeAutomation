/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([
	"sap/ui/thirdparty/jquery",
	"sap/ui/core/Core",
	"sap/ui/core/Control",
	"sap/ui/integration/util/Manifest",
	"sap/base/Log",
	'sap/ui/model/json/JSONModel',
	"sap/ui/model/resource/ResourceModel",
	"sap/ui/integration/WidgetRenderer",
	"sap/ui/integration/library",
	"sap/base/util/LoaderExtensions",
	"sap/ui/core/ComponentContainer"
], function (
	jQuery,
	Core,
	Control,
	WidgetManifest,
	Log,
	JSONModel,
	ResourceModel,
	WidgetRenderer,
	library,
	LoaderExtensions,
	ComponentContainer
) {
	"use strict";

	var MANIFEST_PATHS = {
		APP_TYPE: "/sap.app/type",
		PARAMS: "/sap.widget/configuration/parameters"
	};

	/**
	 * Constructor for a new <code>Widget</code>.
	 *
	 * @param {string} [sId] ID for the new control, generated automatically if no ID is given
	 * @param {object} [mSettings] Initial settings for the new control
	 *
	 * <h3>Overview</h3>
	 * Widgets is a wrapper for @see{sap.ui.core.ComponentContainer}. They are configured via an app descriptor manifest.json file.
	 * @extends sap.ui.core.Control
	 *
	 * @author SAP SE
	 * @version 1.71.1
	 * @constructor
	 * @since 1.70
	 * @alias sap.ui.integration.Widget
	 *
	 * @experimental since 1.70
	 *
	 */
	var Widget = Control.extend("sap.ui.integration.Widget", /** @lends sap.ui.integration.Widget.prototype */ {
		metadata: {
			library: "sap.ui.integration",
			properties: {

				/**
				 * The URL of the manifest or an object.
				 */
				manifest: {
					type: "any",
					defaultValue: ""
				},

				/**
				 * The parameters used in the manifest.
				 */
				parameters: {
					type: "object",
					defaultValue: null
				},

				/**
				 * The configuration used in the manifest within the sap.widget section
				 * This data will be merged on to of an already given manifest
				 */
				configuration: {
					type: "object"
				},

				/**
				 * Base URL
				 */
				baseUrl: {
					type: "string",
					defaultValue: ""
				}
			},
			aggregations: {

				/**
				 * Defines the content of the Widget.
				 */
				_content: {
					type: "sap.ui.core.Control",
					multiple: false,
					visibility : "hidden"
				}
			},
			events: {

				/**
				 * Fired when an action is triggered on the Widget.
				 */
				action: {
					parameters: {

						/**
						 * The action source.
						 */
						actionSource: {
							type: "sap.ui.core.Control"
						},

						/**
						 * The manifest parameters related to the triggered action.
						*/
						manifestParameters: {
							type: "object"
						}
					}
				}
			}
		},
		renderer: WidgetRenderer
	});

	/**
	 * Initialization hook.
	 * @private
	 */
	Widget.prototype.init = function () {
		this.setBusyIndicatorDelay(0);
	};

	/**
	 * Called on before rendering of the control.
	 * @private
	 */
	Widget.prototype.onBeforeRendering = function () {
		if (this._bApplyManifest) {
			this._bApplyManifest = false;
			var vManifest = this.getManifest();

			if (!vManifest) {
				// Destroy the manifest when null/undefined/empty string are passed
				this.destroyManifest();
			} else {
				this.createManifest(vManifest, this.getBaseUrl());
			}
		}
	};

	/**
	 * Called on destroying the control
	 * @private
	 */
	Widget.prototype.exit = function () {
		this.destroyManifest();
	};

	/**
	 * Destroys everything configured by the manifest.
	 */
	Widget.prototype.destroyManifest = function () {
		if (this._oWidgetManifest) {
			this._oWidgetManifest.destroy();
			this._oWidgetManifest = null;
		}

		this.destroyAggregation("_content");
	};


	Widget.prototype.setManifest = function (vValue) {
		this.setProperty("manifest", vValue);
		this._bApplyManifest = true;
		return this;
	};

	Widget.prototype.setParameters = function (vValue) {
		this.setProperty("parameters", vValue);
		this._bApplyManifest = true;
		return this;
	};

	/**
	 * Overwrites getter for Widget manifest.
	 *
	 * @public
	 * @returns {string|Object} Cloned of the parameters.
	 */
	Widget.prototype.getManifest = function () {
		var vValue = this.getProperty("manifest");
		if (vValue && typeof vValue === "object") {
			return jQuery.extend(true, {}, vValue);
		}
		return vValue;
	};

	/**
	 * Instantiates a Widget Manifest and applies it.
	 *
	 * @private
	 * @param {Object|string} vManifest The manifest URL or the manifest JSON.
	 * @param {string} sBaseUrl The base URL of the manifest.
	 * @returns {Promise} A promise resolved when the manifest is created and applied.
	 */
	Widget.prototype.createManifest = function (vManifest, sBaseUrl) {
		var mOptions = {};
		if (typeof vManifest === "string") {
			mOptions.manifestUrl = vManifest;
			vManifest = null;
		}

		// Let the Component loading trigger translation loading and processing.
		mOptions.processI18n = false;

		this.setBusy(true);
		this._oWidgetManifest = new WidgetManifest("sap.widget", vManifest, sBaseUrl);

		return this._oWidgetManifest
			.load(mOptions)
			.then(this._applyManifest.bind(this))
			.catch(this._applyManifest.bind(this));
	};

	/**
	 * Overwrites setter for Widget settings.
	 *
	 * @public
	 * @param {Object} vValue Settings to set in the Widget trough parameters property.
	 * @returns {sap.ui.integration.widgets.Widget} Pointer to the control instance to allow method chaining.
	 */
	Widget.prototype.setConfiguration = function (vValue) {
		this._bApplyManifest = true;
		this.setBusy(true);
		this.setProperty("configuration", vValue);
		return this;
	};

	/**
	 * Overwrites getter for Widget parameters.
	 *
	 * @public
	 * @returns {Object} A Clone of the parameters.
	 */
	Widget.prototype.getParameters = function () {
		var vValue = this.getProperty("parameters");
		if (vValue && typeof vValue === "object") {
			return jQuery.extend(true, {}, vValue);
		}
		return vValue;
	};

	/**
	 * Prepares the manifest and applies all settings.
	 *
	 * @returns {Promise} A promise resolved when the manifest is applied.
	 */
	Widget.prototype._applyManifest = function () {

		var oParameters = this.getParameters(),
			sAppType = this._oWidgetManifest.get(MANIFEST_PATHS.APP_TYPE),
			//in case the manifest is passed as url we need to register the module path
			oConfiguration = this.getConfiguration();

		if (sAppType && sAppType !== "widget") {
			Log.error("sap.app/type entry in manifest is not 'widget'");
		}

		this._oWidgetManifest._mergeConfiguration(oConfiguration);
		this._registerManifestModulePath();
		this._oWidgetManifest.processParameters(oParameters);
		return this._createComponent(this._oWidgetManifest.getJson(), this.getBaseUrl());
	};

	/**
	 * Registers the manifest ID as a module path.
	 */
	Widget.prototype._registerManifestModulePath = function () {
		if (!this._oWidgetManifest) {
			return;
		}

		var sAppId = this._oWidgetManifest.get("/sap.app/id");
		if (sAppId) {
			LoaderExtensions.registerResourcePath(sAppId.replace(/\./g, "/"), this._oWidgetManifest.getUrl());
		} else {
			Log.error("Widget sap.app/id entry in the manifest is mandatory");
		}
	};

	/**
	 * Creates component container for Widget.
	 *
	 * @param {Object} oManifest Manifest that is needed to create the component.
	 * @param {string} sBaseUrl The base URL to be used for component URL and manifest URL.
	 * @returns {Promise} A promise resolved when the component is loaded.
	 * @private
	 */
	Widget.prototype._createComponent = function (oManifest, sBaseUrl) {
		var mOptions = {
			manifest: oManifest
		};

		if (sBaseUrl) {
			mOptions.url = sBaseUrl;
			mOptions.altManifestUrl = sBaseUrl;
		}

		return sap.ui.core.Component.load(mOptions)
			.then(function(oComponent) {
				var oContainer = new ComponentContainer({
					component: oComponent().getId()
				});
				oContainer.attachEvent("action", function (oEvent) {
					this.fireEvent("action", {
						actionSource: oEvent.getParameter("actionSource"),
						manifestParameters: oEvent.getParameter("manifestParameters")
					});
				}.bind(this));
				this.setAggregation("_content", oContainer);
				this.setBusy(false);
				this.fireEvent("_ready");
			}.bind(this));
	};

	/**
	 * Loads the module designtime/Widget.designtime or the module given in
	 * "sap.widget": {
	 *    "designtime": "designtime/Own.designtime"
	 * }
	 * This file should contain the designtime configuration for the widget.
	 *
	 * Returns a promise that resolves with an object
	 * {
	 *    configuration: the current configuration
	 *    designtime: the designtime modules response
	 *    manifest: the complete manifest json
	 * }
	 * The promise is rejected if the module cannot be loaded with an object:
	 * {
	 *     error: "Widget.designtime not found"
	 * }
	 *
	 * @returns {Promise} Promise resolves after the designtime configuration is loaded.
	 */
	Widget.prototype.loadDesigntime = function() {
		if (!this._oWidgetManifest) {
			return Promise.reject("Manifest not yet available");
		}
		var sAppId = this._oWidgetManifest.get("/sap.app/id");
		if (!sAppId) {
			return Promise.reject("App id not maintained");
		}
		var sModulePath = sAppId.replace(/\./g,"/");
		return new Promise(function(resolve, reject) {
			//build the module path to load as part of the widgets module path
			var sModule = sModulePath + "/" + (this._oWidgetManifest.get("/sap.widget/designtime") || "designtime/Widget.designtime");
			if (sModule) {
				sap.ui.require([sModule, "sap/base/util/deepClone"], function(oDesigntime, deepClone) {
					//successfully loaded
					resolve({
						configuration: deepClone(this._oWidgetManifest.get("/sap.widget"), 30),
						designtime: oDesigntime,
						manifest: deepClone(this._oWidgetManifest.oJson, 30)
					});
				}.bind(this), function () {
					//error
					reject({
						error: sModule + " not found"
					});
				});
			} else {
				reject();
			}
		}.bind(this));
	};


	return Widget;
});
