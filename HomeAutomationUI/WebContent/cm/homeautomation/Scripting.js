sap.ui.define([
	"sap/ui/core/mvc/Controller"
], function (Controller) {
	"use strict";

	return Controller.extend("cm.homeautomation.Scripting", {

		onInit: function () {
			
		},
		_dialog: null,
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