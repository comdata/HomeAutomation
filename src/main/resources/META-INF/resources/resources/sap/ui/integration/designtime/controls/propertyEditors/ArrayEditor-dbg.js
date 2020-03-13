/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([
	"sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor",
	"sap/base/util/deepClone",
	"sap/m/VBox",
	"sap/m/Bar",
	"sap/m/Label",
	"sap/m/Button"
], function (
	BasePropertyEditor,
	deepClone,
	VBox,
	Bar,
	Label,
	Button
) {
	"use strict";

	/**
	 * @constructor
	 * @private
	 * @experimental
	 */
	var ArrayEditor = BasePropertyEditor.extend("sap.ui.integration.designtime.controls.propertyEditors.ArrayEditor", {
		constructor: function() {
			BasePropertyEditor.prototype.constructor.apply(this, arguments);
			var oContainer = new VBox();
			this.addContent(oContainer);

			oContainer.bindAggregation("items", "items", function(sId, oItemContext) {
				var oItem = oItemContext.getObject();
				var iIndex = this.getConfig().items.indexOf(oItem);

				var oGroup = new VBox({
					items: new Bar({
						contentLeft: [
							new Label({
								text: this.getConfig().itemLabel || "{i18n>CARD_EDITOR.ARRAY.ITEM_LABEL}"
							})
						],
						contentRight: [
							new Button({
								icon: "sap-icon://less",
								tooltip: "{i18n>CARD_EDITOR.ARRAY.REMOVE}",
								press: function(iIndex) {
									var aValue = this.getConfig().value;
									aValue.splice(iIndex, 1);
									this.firePropertyChanged(aValue);
								}.bind(this, iIndex)
							})
						]
					})
				});
				Object.keys(oItem).forEach(function(sItemProperty) {
					var oItemConfig = oItem[sItemProperty];
					var oSubEditor = this.getEditor().createPropertyEditor(oItemConfig);
					oSubEditor.getLabel().setDesign("Standard");

					oGroup.addItem(oSubEditor);
				}.bind(this));

				return oGroup;

			}.bind(this));

			this.addContent(new Bar({
				contentRight: [
					new Button({
						icon: "sap-icon://add",
						tooltip: "{i18n>CARD_EDITOR.ARRAY.ADD}",
						enabled: "{= ${items} ? ${items}.length < ${maxItems} : false}",
						press: function() {
							var aValue = this.getConfig().value;
							aValue.push({});
							this.firePropertyChanged(aValue);
						}.bind(this)
					})
				]
			}));
		},
		onValueChange: function() {
			var vReturn = BasePropertyEditor.prototype.onValueChange.apply(this, arguments);
			var oConfig = this.getConfig();
			if (oConfig.value && oConfig.template) {
				oConfig.items = [];
				oConfig.value.forEach(function(oValue, iIndex) {
					var mItem = deepClone(oConfig.template);
					Object.keys(mItem).forEach(function(sKey) {
						var oItemProperty = mItem[sKey];
						if (oItemProperty.path) {
							oItemProperty.path = oItemProperty.path.replace(":index", iIndex);
						}
					});
					oConfig.items.push(mItem);
				});
				this.getModel().checkUpdate();
			}
			return vReturn;
		},
		renderer: BasePropertyEditor.getMetadata().getRenderer().render
	});

	return ArrayEditor;
});