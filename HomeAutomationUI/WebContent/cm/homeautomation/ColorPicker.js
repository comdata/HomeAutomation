sap.ui.define([
	'jquery.sap.global',
    'sap/ui/core/mvc/Controller',
    'sap/ui/model/json/JSONModel',
    'cm/webservice/RESTService',
    'cm/homeautomation/Validator',
    "sap/ui/model/resource/ResourceModel"
], function (jQuery, Controller, JSONModel, RESTService, Validator,ResourceModel) {
	"use strict";

	return Controller.extend("cm.homeautomation.ColorPicker", {
		_dialog: null,
		_light: null,
		onBeforeRendering: function () {
			this.loadData();
		},
		setLight: function (light) {
			this._light=light;
		},

		setDialog: function(dialog) {
			this._dialog=dialog;
		},
		setMainController: function(mainController) {
			this._mainController=mainController;
		},
		
		loadData: function() {
	       
		},
		dialogClose: function() {
	        this._mainController._dialogs["colorPicker"].close();
	       
	    }, 
	    afterDialogClose: function () {
			this._mainController._dialogs["colorPicker"].destroy();
			this._mainController._dialogs["colorPicker"] = null;
	    }
		
	});
});