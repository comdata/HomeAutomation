sap.ui.define([
    'jquery.sap.global',
    'sap/ui/core/mvc/Controller',
    'sap/ui/model/json/JSONModel',
    'cm/webservice/RESTService'
], function (jQuery, Controller, JSONModel, RESTService) {
    "use strict";

    return Controller.extend("cm.homeautomation.Main", {

        selectedRoom: "",
        /**
         * initialize
         *
         * @param evt
         */
        onInit: function (evt) {
            this.loadData();
            var subject = this;
            window.setInterval(function () {
                subject.loadData.apply(subject)
            }, 30000);

            window.setInterval(
                function () {
                    subject.getCurrentTime();
                }, 1000
            );

        },
        /**
         * perform data loading
         *
         */
        loadData: function () {
            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/overview/get", "", "GET", this.handleDataLoaded, null, this);

        },
        /**
         * handle successful data loading
         *
         * @param event
         * @param model
         */
        handleDataLoaded: function (event, model) {
            this.getView().setModel(model);
        },
        handleSwitchesLoaded: function (event, model) {

            var switchList = sap.ui.getCore().byId("switchList");
            if (model.getProperty("/switchStatuses").length > 0) {
                switchList.setProperty("visible", true);
            } else {
                switchList.setProperty("visible", false);
            }

            sap.ui.getCore().setModel(model, "switches");

            //alert(sap.ui.getCore().getModel("switches"));
        },
        handleSwitchChange: function (event) {
            var singleSwitch = sap.ui.getCore().getModel("switches").getProperty(event.getSource().oPropagatedProperties.oBindingContexts.switches.sPath);

            //alert(singleSwitch.switchState);

            var newState = "";

            if (singleSwitch.switchState == true) {
                newState = "ON";
            } else {
                newState = "OFF";
            }

            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/actor/press/" + singleSwitch.id + "/"
                + newState, "", "GET", this.handleSwitchChanged, null, this);
        },
        handleSwitchChanged: function (event) {
            var subject = this;

            window.setTimeout(function () {
                subject.loadRoom.apply(subject);
                subject.loadData.apply(subject);
            }, 5000);

            window.setTimeout(function () {
                subject.loadRoom.apply(subject);
                subject.loadData.apply(subject);
            }, 15000);

            window.setTimeout(function () {
                subject.loadRoom.apply(subject);
                subject.loadData.apply(subject);
            }, 30000);
        },
        loadRoom: function () {

            var subject = this;


            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/actor/forroom/" + subject.selectedRoom, "", "GET", subject.handleSwitchesLoaded, null, subject);

        },

        /**
         * handle selection, triggering navigation
         *
         * @param event
         */
        handleSelect: function (event) {

            jQuery.sap.require("sap.ui.core.UIComponent");
            jQuery.sap.require("sap.m.Dialog");

            // set empty model
            var model = new sap.ui.model.json.JSONModel();
            sap.ui.getCore().setModel(model, "switches");

            this.selectedRoom = this.getView().getModel().getProperty(event.getSource().oBindingContexts["undefined"].sPath).roomId;
            var roomId = this.selectedRoom;

            if (roomId!=null && roomId) {

                this.loadRoom();

                if (!this._oDialog) {
                    this._oDialog = sap.ui.xmlfragment("cm.homeautomation.Switch", this);
                    this._oDialog.setModel(this.getView().getModel());
                }


                window.setTimeout(function () {
                    getHistoricalSensordata(roomId);
                }, 1000);

                // Multi-select if required
                //var bMultiSelect = !!oEvent.getSource().data("multi");
                //this._oDialog.setMultiSelect(bMultiSelect);

                // Remember selections if required
                //var bRemember = !!oEvent.getSource().data("remember");
                //this._oDialog.setRememberSelections(bRemember);

                // clear the old search filter
                // this._oDialog.getBinding("items").filter([]);

                // toggle compact style
                jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this._oDialog);
                this._oDialog.open();
            }

        },
        dialogClose: function () {
            this._oDialog.close();
        },
        afterDialogClose: function () {
            this._oDialog.destroy();
            this._oDialog = null;
        },
        handleClose: function (oEvent) {

        },

        getCurrentTime: function () {
            var Digital = new Date();
            var hours = Digital.getHours();
            var minutes = Digital.getMinutes();
            var seconds = Digital.getSeconds();
            var dn = "AM";
            if (hours > 12) {
                dn = "PM";
                hours = hours - 12;
            }
            if (hours == 0)
                hours = 12;
            if (minutes <= 9)
                minutes = "0" + minutes;
            if (seconds <= 9)
                seconds = "0" + seconds;
            var sLocale = sap.ui.getCore().getConfiguration().getLanguage();
            sLocale = "DE";
            var time = Digital.toLocaleTimeString(sLocale);
            var date = Digital.toLocaleDateString(sLocale);
            if (this.byId("idMenuClock")) {
                this.byId("idMenuClock").setText(date+" - "+time);
               // this.byId("idMenuClock").setInfo(date);
            }
        }

    });

});