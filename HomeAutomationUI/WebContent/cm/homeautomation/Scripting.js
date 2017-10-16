sap.ui.define([
	'jquery.sap.global',
    'sap/ui/core/mvc/Controller',
    'sap/ui/model/json/JSONModel',
    'cm/webservice/RESTService',
    'cm/homeautomation/Validator',
    "sap/ui/model/resource/ResourceModel"
], function (jQuery, Controller, JSONModel, RESTService, Validator,ResourceModel) {
	"use strict";

	return Controller.extend("cm.homeautomation.Scripting", {
		_dialog: null,
		onBeforeRendering: function () {
			this.loadData();
		},

		setDialog: function(dialog) {
			this._dialog=dialog;
		},
		setMainController: function(mainController) {
			this._mainController=mainController;
		},
		
		loadData: function() {
	        var subject = this;
	        var scriptingModel = new RESTService();
	        scriptingModel.loadDataAsync("/HomeAutomation/services/admin/nashorn/getAll", "", "GET", function(event, model) {
	            sap.ui.getCore().setModel(model, "scriptingEntities");
	        }, null, subject);
		},
		scriptingDialogClose: function() {
	        this._mainController._dialogs["scripting"].close();
	        sap.ui.getCore().setModel(new JSONModel(), "scriptingEntities");
	    }, 
	    updateScriptingEntry: function(scriptingEntry) {
	    	
	    },
	    handleScriptingEditButtonPress: function (event) {
	    	
	    },
	    afterScriptingAdminDialogClose: function () {
			this._mainController._dialogs["scripting"].destroy();
			this._mainController._dialogs["scripting"] = null;
	    }
		
	});
});