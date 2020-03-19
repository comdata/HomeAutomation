sap.ui.define([
	"sap/ui/core/mvc/XMLView"
], function (XMLView) {
	"use strict";

	XMLView.create({
		viewName: "cm.homeautomation.Main",
        width : "100%",
		height : "100%"
	}).then(function (oView) {
		oView.placeAt("content");
	});

});