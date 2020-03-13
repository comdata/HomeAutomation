/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([], function () {
	"use strict";

	/**
	 * Util class for Connector implementations (apply and write)
	 *
	 * @namespace sap.ui.fl.apply._internal.connectors.Utils
	 * @experimental Since 1.70
	 * @since 1.70
	 * @version 1.71.1
	 * @private
	 * @ui5-restricted sap.ui.fl.apply._internal, sap.ui.fl.write._internal
	 */

	var FL_CHANGE_KEY = "sap.ui.fl.change";
	var FL_VARIANT_KEY = "sap.ui.fl.variant";

	return {
		/**
		 * The iterator for the fl changes in the given Storage. Filters the changes if a reference or layer is passed
		 * @public
		 * @param {object} mPropertyBag object with necessary information
		 * @param {Storage} mPropertyBag.storage browser storage, can be either session or local storage
		 * @param {string} [mPropertyBag.reference] reference of the application
		 * @param {string} [mPropertyBag.layer] current layer
		 * @param {function} fnPredicate The function to apply for each change
		 */
		forEachChangeInStorage: function(mPropertyBag, fnPredicate) {
			// getItems() is used in the internal keys of the JsObjectStorage
			var oRealStorage = mPropertyBag.storage.getItems && mPropertyBag.storage.getItems() || mPropertyBag.storage;

			var aKeys = Object.keys(oRealStorage);
			aKeys.forEach(function(sKey) {
				var bIsFlexObject = sKey.includes(FL_CHANGE_KEY) || sKey.includes(FL_VARIANT_KEY);

				if (!bIsFlexObject) {
					return;
				}

				var oFlexObject = JSON.parse(oRealStorage[sKey]);
				var bSameReference = true;
				if (mPropertyBag.reference) {
					bSameReference = oFlexObject.reference === mPropertyBag.reference || oFlexObject.reference + ".Component" === mPropertyBag.reference;
				}

				var bSameLayer = true;
				if (mPropertyBag.layer) {
					bSameLayer = oFlexObject.layer === mPropertyBag.layer;
				}

				if (!bSameReference || !bSameLayer) {
					return;
				}

				fnPredicate({
					changeDefinition: oFlexObject,
					key: sKey
				});
			});
		},

		/**
		 * Returns an array with all the flex objects in the storage
		 *
		 * @param {object} mPropertyBag - object with the necessary information
		 * @returns {array} Returns an array with maps with two keys: 'key' and 'changeDefinition'
		 */
		getAllFlexObjects: function(mPropertyBag) {
			var aFlexObjects = [];
			this.forEachChangeInStorage(mPropertyBag, function(mFlexObject) {
				aFlexObjects.push(mFlexObject);
			});
			return aFlexObjects;
		},

		/**
		 * Creates the fl change key
		 * @param  {string} sId The change id
		 * @returns {string} the prefixed id
		 */
		createChangeKey: function(sId) {
			if (sId) {
				return FL_CHANGE_KEY + "." + sId;
			}
		},

		/**
		 * Creates the fl variant key
		 * @param  {string} sId The variant id
		 * @returns {string} the prefixed id
		 */
		createVariantKey: function(sId) {
			if (sId) {
				return FL_VARIANT_KEY + "." + sId;
			}
		},

		/**
		 * Returns the key depending on the fileType
		 *
		 * @param {object} oFlexObject The definition of the flex Object
		 * @returns {string} Returns the prefixed ID
		 */
		createFlexObjectKey: function(oFlexObject) {
			if (oFlexObject.fileType === "ctrl_variant" && oFlexObject.variantManagementReference) {
				return this.createVariantKey(oFlexObject.fileName);
			}
			return this.createChangeKey(oFlexObject.fileName);
		},

		sortGroupedFlexObjects: function(mResult) {
			function byCreation(oChangeA, oChangeB) {
				return new Date(oChangeA.creation) - new Date(oChangeB.creation);
			}

			[
				"changes",
				"variantChanges",
				"variants",
				"variantDependentControlChanges",
				"variantManagementChanges"
			].forEach(function (sSectionName) {
				mResult[sSectionName] = mResult[sSectionName].sort(byCreation);
			});

			return mResult;
		}
	};
});
