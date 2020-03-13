/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
/*global Error */

sap.ui.define([
	"sap/ui/fl/LrepConnector",
	"sap/ui/fl/Cache",
	"sap/ui/fl/Utils",
	"sap/base/util/UriParameters",
	"sap/base/Log"
], function(
	LrepConnector,
	Cache,
	Utils,
	UriParameters,
	Log
) {
	"use strict";

	/**
	 * FlexSettings access
	 *
	 * @param {object} oSettings settings as JSON object
	 * @constructor
	 * @alias sap.ui.fl.registry.Settings
	 * @experimental Since 1.27.0
	 * @private
	 */
	var Settings = function(oSettings) {
		if (!oSettings) {
			throw new Error("no flex settings provided");
		}
		// Defaults layers used for standard changes, such as 'move' or 'add'
		if (!oSettings.defaultLayerPermissions) {
			oSettings.defaultLayerPermissions = {
				VENDOR: true,
				CUSTOMER_BASE: true,
				CUSTOMER: true,
				USER: false
			};
		}

		// These are the permissions for the Developer Mode Changes, e.g. 'propertyChange', 'propertyBindingChange'
		if (!oSettings.developerModeLayerPermissions) {
			oSettings.developerModeLayerPermissions = {
				VENDOR: true,
				CUSTOMER_BASE: true,
				CUSTOMER: false,
				USER: false
			};
		}

		// By default, variant sharing is enabled
		if (!(Settings._IS_VARIANT_SHARING_ENABLED in oSettings)) {
			oSettings.isVariantSharingEnabled = true;
		}

		this._oSettings = oSettings;
	};

	/**
	 * Sets the initial value of the settings instance. If the current system is a trial system it will create an instance with default parameters.
	 * Otherwise the instance remains undefined.
	 */
	Settings._initInstance = function () {
		var oSettings;
		if (Utils.isTrialSystem()) {
			oSettings = new Settings({
				isKeyUser: true,
				isVariantSharingEnabled: false
			});
		}
		Settings._instance = oSettings;
	};

	Settings._initInstance();
	Settings._IS_VARIANT_SHARING_ENABLED = "isVariantSharingEnabled";


	/**
	 * attaches a callback to an event on the event provider of Settings
	 *
	 * @param {string} sEventId name of the event
	 * @param {function} oCallback
	 *
	 * @public
	 */
	Settings.attachEvent = function(sEventId, oCallback) {
		Settings._oEventProvider.attachEvent(sEventId, oCallback);
	};

	/**
	 * detaches a callback to an event on the event provider of Settings
	 *
	 * @param {string} sEventId name of the event
	 * @param {function} oCallback
	 *
	 * @public
	 */
	Settings.detachEvent = function(sEventId, oCallback) {
		Settings._oEventProvider.detachEvent(sEventId, oCallback);
	};

	/**
	 * Returns a settings instance after reading the settings from the back end if not already done. There is only one instance of settings during a
	 * session.
	 *
	 * @returns {Promise} with parameter <code>oInstance</code> of type {sap.ui.fl.registry.Settings}
	 * @public
	 */
	Settings.getInstance = function() {
		if (Settings._instance) {
			return Promise.resolve(Settings._instance);
		}
		var oPromise = Cache.getFlexDataPromise();
		if (oPromise) {
			return oPromise.then(
				function (oFileContent) {
					var oSettings = {};
					if (oFileContent.changes && oFileContent.changes.settings) {
						oSettings = oFileContent.changes.settings;
						return Settings._storeInstance(oSettings);
					}
					return Settings._loadSettings();
				},
				function () {
					// In case /flex/data request failed, send /flex/settings as a fallback
					return Settings._loadSettings();
				});
		}
		return Settings._loadSettings();
	};

	/**
	 * Sends request to the back end for settings content. Stores content into internal setting instance and returns the instance.
	 *
	 * @returns {Promise} With parameter <code>oInstance</code> of type {sap.ui.fl.registry.Settings}
	 * @private
	 */
	Settings._loadSettings = function() {
		return LrepConnector.createConnector().loadSettings().then(function (oSettings) {
			if (!oSettings) {
				Log.error("The request for flexibility settings failed; A default response is generated and returned to consuming APIs");
				// in case the back end cannot respond resolve with a default response
				oSettings = {
					isKeyUser: false,
					isVariantSharingEnabled: false,
					isAtoAvailable: false,
					isAtoEnabled: false,
					isProductiveSystem: true,
					_bFlexChangeMode: false,
					_bFlexibilityAdaptationButtonAllowed: false
				};
			}

			return Settings._storeInstance(oSettings);
		});
	};

	/**
	 * Writes the data received from the back end or cache into an internal instance and then returns the settings object within a Promise.
	 *
	 * @param oSettings - Data received from the back end or cache
	 * @returns {Promise} with parameter <code>oInstance</code> of type {sap.ui.fl.registry.Settings}
	 * @protected
	 *
	 */
	Settings._storeInstance = function(oSettings) {
		if (!Settings._instance) {
			Settings._instance = new Settings(oSettings);
		}
		return Settings._instance;
	};

	/**
	 * Returns a settings instance from the local instance cache. There is only one instance of settings during a session. If no instance has been
	 * created before, undefined will be returned.
	 *
	 * @returns {sap.ui.fl.registry.Settings} instance or undefined if no instance has been created so far.
	 * @public
	 */
	Settings.getInstanceOrUndef = function() {
		var oSettings;
		if (Settings._instance) {
			oSettings = Settings._instance;
		}
		return oSettings;
	};

	/**
	 * Reads boolean property of settings.
	 *
	 * @param {string} sPropertyName name of property
	 * @returns {boolean} true if the property exists and is true.
	 * @public
	 */
	Settings.prototype._getBooleanProperty = function(sPropertyName) {
		var bValue = false;
		if (this._oSettings[sPropertyName]) {
			bValue = this._oSettings[sPropertyName];
		}
		return bValue;
	};

	/**
	 * Returns the key user status of the current user.
	 *
	 * @returns {boolean} true if the user is a flexibility key user, false if not supported.
	 * @public
	 */
	Settings.prototype.isKeyUser = function() {
		return this._getBooleanProperty("isKeyUser");
	};

	/**
	 * Returns true if back end is ModelS back end.
	 *
	 * @returns {boolean} true if ATO coding exists in back end.
	 * @public
	 */
	Settings.prototype.isModelS = function() {
		return this._getBooleanProperty("isAtoAvailable");
	};

	/**
	 * Returns true if ATO is enabled in the back end.
	 *
	 * @returns {boolean} true if ATO is enabled.
	 * @public
	 */
	Settings.prototype.isAtoEnabled = function() {
		return this._getBooleanProperty("isAtoEnabled");
	};

	/**
	 * Returns true if ATO is available in the back end.
	 *
	 * @returns {boolean} true if ATO is available.
	 * @public
	 */
	Settings.prototype.isAtoAvailable = function() {
		return this._getBooleanProperty("isAtoAvailable");
	};

	/**
	 * Checks whether the current system is defined as a productive system.
	 *
	 * @public
	 * @returns {boolean} true if system is productive system
	 */
	Settings.prototype.isProductiveSystem = function() {
		return this._getBooleanProperty("isProductiveSystem");
	};

	/**
	 * Checks whether sharing of variants is enabled.
	 *
	 * @public
	 * @returns {boolean} true if sharing of variants is enabled
	 */
	Settings.prototype.isVariantSharingEnabled = function() {
		return (this._oSettings.isVariantSharingEnabled === true);
	};

	/**
	 * Getter for the system Id of the connected backend.
	 * Taken from the property 'system' of the LRep flex settings file. Only filled for an ABAP backend.
	 *
	 * @public
	 * @returns {String} system Id of the connected backend or undefined (when property 'system' does not exist in the flex settings file)
	 */
	Settings.prototype.getSystem = function() {
		return this._oSettings.system;
	};

	/**
	 * Getter for the client of the connected backend.
	 * Taken from the property 'client' of the LRep flex settings file. Only filled for an ABAP backend.
	 *
	 * @public
	 * @returns {String} client of the connected backend or undefined (when property 'system' does not exist in the flex settings file)
	 */
	Settings.prototype.getClient = function() {
		return this._oSettings.client;
	};

	/**
	 * Getter for the default Layer-Permissions
	 */
	Settings.prototype.getDefaultLayerPermissions = function() {
		return this._oSettings.defaultLayerPermissions;
	};

	/**
	 * Getter for the Developer Mode Layer-Permissions
	 */
	Settings.prototype.getDeveloperModeLayerPermissions = function() {
		return this._oSettings.developerModeLayerPermissions;
	};

	return Settings;
}, /* bExport= */true);