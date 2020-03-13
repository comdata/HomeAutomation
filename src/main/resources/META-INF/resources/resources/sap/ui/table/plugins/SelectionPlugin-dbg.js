/*
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([
	'sap/ui/core/Element',
	'../library'
], function(
	Element,
	library
) {

	"use strict";

	var SelectionMode = library.SelectionMode;

	/**
	 * Constructs an instance of sap.ui.table.plugins.SelectionPlugin
	 *
	 * The following restrictions apply:
	 * <ul>
	 *  <li>Do not create subclasses of the SelectionPlugin. The API is subject to change.</li>
	 * </ul>
	 *
	 * @abstract
	 * @class Implements the selection methods for a table.
	 * @extends sap.ui.core.Element
	 * @author SAP SE
	 * @version 1.71.1
	 * @public
	 * @since 1.64
	 * @experimental As of version 1.64
	 * @alias sap.ui.table.plugins.SelectionPlugin
	 * @ui5-metamodel This control/element also will be described in the UI5 (legacy) designtime metamodel
	 */
	var SelectionPlugin = Element.extend("sap.ui.table.plugins.SelectionPlugin", {metadata: {
		"abstract": true,
		library: "sap.ui.table",
		properties: {
			/**
			 * Defines whether single or multiple items can be selected.
			 * @private
			 */
			selectionMode: {type: "sap.ui.table.SelectionMode", defaultValue: SelectionMode.MultiToggle, visibility: "hidden"}
		},
		events: {
			/**
			 * This event is fired when the selection is changed.
			 */
			selectionChange: {
				parameters: {
				}
			}
		}
    }});

	/**
	 * Sets up the initial values.
	 */
	SelectionPlugin.prototype.init = function() {
		this._bSuspended = false;
	};

	/**
	 * Terminates the plugin
	 *
	 * @private
	 */
	SelectionPlugin.prototype.exit = function() {
		this._oBinding = null;
	};

	SelectionPlugin.prototype.getRenderConfig = function() {
		return {
			headerSelector: {
				type: "none"
			}
		};
	};

	/**
	 * Adds the given selection interval to the selection.
	 *
	 * @param {int} iIndexFrom Index from which the selection starts
	 * @param {int} iIndexTo Index up to which to select
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.addSelectionInterval = function(iIndexFrom, iIndexTo) {
	};

	/**
	 * Removes the complete selection.
	 *
	 * @private
	 * @abstract
 	 */
	SelectionPlugin.prototype.clearSelection = function() {
	};

	/**
	 * Retrieves the lead selection index.
	 *
	 * @returns {int}
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.getSelectedIndex = function() {
		return -1;
	};

	/**
	 * Zero-based indices of selected items, wrapped in an array. An empty array means nothing has been selected.
	 *
	 * @returns {int[]} An array containing all selected indices
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.getSelectedIndices = function() {
		return [];
    };

	/**
	 * Returns the number of items that can be selected.
	 *
	 * @returns {int} Number of items that can be selected
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.getSelectableCount = function() {
		return 0;
	};

	/**
	 * Returns the number of selected items.
	 *
	 * @returns {int} Number of selected items
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.getSelectedCount = function() {
		return 0;
	};

	/**
	 * Checks whether an index is selectable.
	 *
	 * @param {int} iIndex The index to be checked
	 * @returns {boolean} <code>true</code> if the index is selectable, <code>false</code> otherwise
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.isIndexSelectable = function(iIndex) {
		return false;
	};

	/**
	 * Returns the information whether the given index is selected.
	 *
	 * @param {int} iIndex The index for which the selection state is retrieved.
	 * @returns {boolean} <code>true</code> if the index is selected, <code>false</code> otherwise
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.isIndexSelected = function(iIndex) {
		return false;
	};

	/**
	 * Removes the given selection interval from the selection. In case of a single selection, only <code>iIndexTo</code> is removed from the selection.
	 *
	 * @param {int} iIndexFrom Index from which the deselection starts
	 * @param {int} iIndexTo Index up to which to deselect
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.removeSelectionInterval = function(iIndexFrom, iIndexTo) {
	};

	/**
	 * Selects all indices.
	 *
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.selectAll = function() {
	};

	/**
	 * Sets the selected index.
	 *
	 * @param {int} iIndex The index which is selected (if existing)
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.setSelectedIndex = function(iIndex) {
	};

	/**
	 * Sets the given selection interval as a selection. In case of a single selection, only <code>iIndexTo</code> is selected.
	 *
	 * @param {int} iIndexFrom Index from which the selection starts
	 * @param {int} iIndexTo Index up to which to select
	 * @private
	 * @abstract
	 */
	SelectionPlugin.prototype.setSelectionInterval = function(iIndexFrom, iIndexTo) {
	};

	SelectionPlugin.prototype.fireSelectionChange = function(mArguments) {
		if (!this._isSuspended()) {
			this.fireEvent("selectionChange", mArguments);
		}
	};

	SelectionPlugin.prototype._setSelectionMode = function(sSelectionMode) {
		this.setProperty("selectionMode", sSelectionMode);
	};

	SelectionPlugin.prototype._getSelectionMode = function() {
		return this.getProperty("selectionMode");
	};

	/**
	 * Gets the binding of the associated table.
	 *
	 * @returns {sap.ui.model.Binding|undefined}
	 * @private
	 */
	SelectionPlugin.prototype._getBinding = function() {
		return this._oBinding;
	};

	/**
	 * Sets the binding of the associated table.
	 *
	 * @param {sap.ui.model.Binding} oBinding
	 * @private
	 */
	SelectionPlugin.prototype._setBinding = function(oBinding) {
		this._oBinding = oBinding;
	};

	/**
	 * Suspends the selectionChange event
	 *
	 * When _bSuspended is true, the selectionChange event is not being fired.
	 *
	 * @private
	 */
	SelectionPlugin.prototype._suspend = function() {
		this._bSuspended = true;
	};

	/**
	 * Resumes the selectionChange event
	 *
	 * When _bSuspended is false, the selectionChange event is being fired
	 *
	 * @private
	 */
	SelectionPlugin.prototype._resume = function() {
		this._bSuspended = false;
	};

	/**
	 * Checks if the selectionChange event is suspended.
	 *
	 * @return {boolean}
	 * @private
	 */
	SelectionPlugin.prototype._isSuspended = function() {
		return this._bSuspended;
	};

	return SelectionPlugin;
});