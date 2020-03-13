/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([
	"sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor",
	"sap/ui/core/Fragment",
	"sap/ui/model/json/JSONModel",
	"sap/ui/model/Filter",
	"sap/ui/model/FilterOperator",
	"sap/ui/core/IconPool"
], function (
	BasePropertyEditor,
	Fragment,
	JSONModel,
	Filter,
	FilterOperator,
	IconPool
) {
	"use strict";

	/**
	 * @constructor
	 * @private
	 * @experimental
	 */
	var IconEditor = BasePropertyEditor.extend("sap.ui.integration.designtime.controls.propertyEditors.IconEditor", {
		constructor: function() {
			BasePropertyEditor.prototype.constructor.apply(this, arguments);
			this._oIconModel = new JSONModel(IconPool.getIconNames().map(function(sName) {
				return {
					name: sName,
					path: "sap-icon://" + sName
				};
			}));
			this._oInput = new sap.m.Input({
				value: "{value}",
				showSuggestion: true,
				showValueHelp: true,
				valueHelpRequest: this._handleValueHelp.bind(this)
			});
			this._oInput.setModel(this._oIconModel, "icons");
			this._oInput.bindAggregation("suggestionItems", "icons>/", new sap.ui.core.ListItem({
				text: "{icons>path}",
				additionalText: "{icons>name}"
			}));
			this._oInput.attachLiveChange(function(oEvent) {
				this.firePropertyChanged(oEvent.getParameter("value"));
			}.bind(this));
			this._oInput.attachSuggestionItemSelected(function(oEvent) {
				this.firePropertyChanged(oEvent.getParameter("selectedItem").getText());
			}.bind(this));
			this.addContent(this._oInput);
		},
		renderer: BasePropertyEditor.getMetadata().getRenderer().render
	});

	IconEditor.prototype._handleValueHelp = function (oEvent) {
		var sValue = oEvent.getSource().getValue();

		if (!this._oDialog) {
			Fragment.load({
				name: "sap.ui.integration.designtime.controls.propertyEditors.IconSelection",
				controller: this
			}).then(function(oDialog){
				this._oDialog = oDialog;
				this.addDependent(this._oDialog);
				this._oDialog.setModel(this._oIconModel);
				this._filter(sValue);
				this._oDialog.open(sValue);
			}.bind(this));
		} else {
			this._filter(sValue);
			this._oDialog.open(sValue);
		}
	};

	IconEditor.prototype.handleSearch = function(oEvent) {
		var sValue = oEvent.getParameter("value");
		this._filter(sValue);
	};

	IconEditor.prototype._filter = function(sValue) {
		var oFilter = new Filter("path", FilterOperator.Contains, sValue);
		var oBinding = this._oDialog.getBinding("items");
		oBinding.filter([oFilter]);
	};

	IconEditor.prototype.handleClose = function(oEvent) {
		var oSelectedItem = oEvent.getParameter("selectedItem");
		if (oSelectedItem) {
			this.firePropertyChanged(oSelectedItem.getIcon());
		}
		oEvent.getSource().getBinding("items").filter([]);
	};

	return IconEditor;
});
