var imageFullScreen = false;


jQuery.sap.require("sap.ui.core.UIComponent");
jQuery.sap.require("sap.m.Dialog");

function resize(element) {

    if (element != null) {

        //if (!imageFullScreen) {
        //	imageFullScreen=true;

        var targetWidth = (window.innerWidth - 30);
        var targetHeight = targetWidth * (0.5625);

        element.style.height = targetHeight + "px";

        element.style.width = targetWidth + "px";
        /*} else  {
         imageFullScreen=false;
         element.style.height="324px";
         element.style.width="576px";

         }*/

    }
}


sap.ui.define([
    'jquery.sap.global',
    'sap/ui/core/mvc/Controller',
    'sap/ui/model/json/JSONModel',
    'cm/webservice/RESTService'
], function (jQuery, Controller, JSONModel, RESTService) {
    "use strict";

    return Controller.extend("cm.homeautomation.Main", {

        selectedRoom: "",
        currentRoomModel: null,
        messageToast: null,
        _wsActorUri: "ws://" + location.host + "/HomeAutomation/actor",
        _wsOverviewUri: "ws://" + location.host + "/HomeAutomation/overview",
        _webActorSocket: null,
        _webOverviewSocket: null,
        initWebSocket: function (uri, callback, socket) {

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (e) {

            }

            socket = new WebSocket(uri);
            var controller = this;
            socket.onopen = function (evt) {
                controller.wsOnOpen.apply(controller, [evt])
            };
            socket.onclose = function (evt) {
                controller.wsOnClose.apply(controller, [evt, uri, callback, socket])
            };
            socket.onmessage = function (evt) {
                callback.apply(controller, [evt])
            };
            socket.onerror = function (evt) {
                controller.wsOnError.apply(controller, [evt, uri, callback, socket])
            };
        },
        wsOnOpen: function (evt) {

        },
        wsOnClose: function (evt, uri, callback, socket) {
            this.initWebSocket(uri, callback, socket);
        },
        wsActorOnMessage: function (evt) {
            var eventModel = new JSONModel();

            eventModel.setData(JSON.parse(evt.data));

            var switchId = eventModel.getProperty("/switchId");
            var status = eventModel.getProperty("/status");

            var switchModel = sap.ui.getCore().getModel("switches");
            if (switchModel != null) {

                var switches = switchModel.oData;

                switches.switchStatuses.forEach(function (singleSwitch) {
                    if (singleSwitch.id == switchId) {
                        singleSwitch.switchState = (status == "ON") ? true : false;
                    }
                });

                var switchModel = new JSONModel();

                switchModel.setData(switches);

                sap.ui.getCore().setModel(switchModel, "switches");
            }
        },
        wsOverviewOnMessage: function (evt) {
            var newData = JSON.parse(evt.data);
            var newTiles = new JSONModel();

            if (this.getView().getModel() != null) {
                var tiles = this.getView().getModel().oData;

                var tileNo = null;

                tiles.overviewTiles.forEach(function (tile, i) {
                    if (tile.roomId == newData.roomId) {
                        tileNo = i;
                    }

                });

                if (tileNo != null) {
                    tiles.overviewTiles[tileNo] = newData;
                }
                newTiles.setData(tiles);

                this.getView().setModel(newTiles);
                $(".sapMStdTileIconDiv > img[src='/HomeAutomation/cameraproxy']").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
            }
        },
        wsOnError: function (evt, uri, callback, socket) {
            this.initWebSocket(uri, callback, socket);
        },

        /**
         * initialize
         *
         * @param evt
         */
        onInit: function (evt) {
            this.loadData();
            var subject = this;

            this.currentRoomModel = new sap.ui.model.json.JSONModel();

            sap.ui.getCore().getModel(this.currentRoomModel, "currentRoom");

            /*window.setInterval(function () {
             subject.loadData.apply(subject)
             }, 30000);*/

            window.setInterval(
                function () {
                    subject.getCurrentTime();
                }, 1000
            );

            this.byId("openMenu").attachBrowserEvent("tab keyup", function (oEvent) {
                this._bKeyboard = oEvent.type == "keyup";
            }, this);

            jQuery.sap.require("sap.m.MessageToast");
            this.messageToast = sap.m.MessageToast;


            var imageModel = new sap.ui.model.json.JSONModel({
                tiles: [""]
            });

            sap.ui.getCore().setModel(imageModel, "imageTile");
            this.initWebSocket(this._wsActorUri, this.wsActorOnMessage, this._webActorSocket);
            this.initWebSocket(this._wsOverviewUri, this.wsOverviewOnMessage, this._webOverviewSocket);
        },
        loadDataInProgress: false,
        /**
         * perform data loading
         *
         */
        loadData: function () {

            if (this.loadDataInProgress == false) {
                this.loadDataInProgress = true;
                var oModel = new RESTService();
                oModel.loadDataAsync("/HomeAutomation/services/overview/get", "", "GET", this.handleDataLoaded, this._loadDataFailed, this);
            }
        },
        /**
         * handle successful data loading
         *
         * @param event
         * @param model
         */
        handleDataLoaded: function (event, model, jsonModelData) {
            this.getView().setModel(model);


            this.cameraTile = {
                tileType: "camera",
                roomId: "camera",
                tile: "Küche",
                info: "Kamera - Küche",
                eventHandler: "showCamera",
                icon: "/HomeAutomation/cameraproxy"
            };

            //this.byId(this.createId("container")).addTile(cameraTile);

            jsonModelData.overviewTiles.push(this.cameraTile);
            $(".sapMStdTileIconDiv > img[src='/HomeAutomation/cameraproxy']").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");

            this.loadDataInProgress = false;
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
        handleWindowBlindsLoaded: function (event, model) {

            var windowBlindsList = sap.ui.getCore().byId("windowBlinds");
            if (model.getProperty("/windowBlinds").length > 0) {
                windowBlindsList.setProperty("visible", true);
            } else {
                windowBlindsList.setProperty("visible", false);
            }

            sap.ui.getCore().setModel(model, "windowBlinds");

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
        handleBlindChange: function (event) {
            var windowBlind = sap.ui.getCore().getModel("windowBlinds").getProperty(event.getSource().oPropagatedProperties.oBindingContexts.windowBlinds.sPath);

            var value = windowBlind.currentValue;
            console.log("new value: " + value);

            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/windowBlinds/setDim/" + windowBlind.id + "/"
                + value, "", "GET", this.handleSwitchChanged, null, this);
        },
        handleSwitchChanged: function (event) {
            var subject = this;


        },
        /**
         * load a room
         */
        loadRoom: function () {

            var subject = this;
            var switchesModel = new RESTService();
            switchesModel.loadDataAsync("/HomeAutomation/services/actor/forroom/" + subject.selectedRoom, "", "GET", subject.handleSwitchesLoaded, null, subject);

            var windowBlindsModel = new RESTService();
            windowBlindsModel.loadDataAsync("/HomeAutomation/services/windowBlinds/forRoom/" + subject.selectedRoom, "", "GET", subject.handleWindowBlindsLoaded, null, subject);
        },

        /**
         * trigger a reload if something goes wrong
         *
         */
        _loadDataFailed: function (event) {
            this.loadDataInProgress = false;
            var subject = this;
            window.setTimeout(function () {
                if (subject != null) {
                    subject.loadData.apply(subject);
                }
            }, 2000);
        },

        expandHistoricData: function (oEvent) {
            //window.setTimeout(function () {
            if (oEvent.getParameter("expand") == true) {
                getHistoricalSensordata(this.selectedRoom);
            }
            //}, 5000);

        },

        /**
         * handle selection, triggering navigation
         *
         * @param event
         */
        handleSelect: function (event) {


            // set empty model
            var model = new sap.ui.model.json.JSONModel();
            sap.ui.getCore().setModel(model, "switches");

            var selectedElement = this.getView().getModel().getProperty(event.getSource().oBindingContexts["undefined"].sPath);

            this.selectedRoom = selectedElement.roomId;
            var roomName = selectedElement.roomName;
            var tileType = selectedElement.tileType;
            this.currentRoomModel.setProperty("/roomName", roomName);

            var roomId = this.selectedRoom;

            if (tileType == "camera") {
                if (!this.camera) {
                    this.camera = sap.ui.xmlfragment("cm.homeautomation.Camera", this);
                    jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this.camera);
                    this.camera.open();
                    this.cameraTile.icon=null;
                }
            } else {
                if (roomId != null && roomId) {

                    this.loadRoom();

                    if (!this._oDialog) {
                        this._oDialog = sap.ui.xmlfragment("cm.homeautomation.Switch", this);
                        this._oDialog.setModel(this.getView().getModel());
                    }


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
            }

        },
        dialogClose: function () {
            this._oDialog.close();
        },
        cameraDialogClose: function () {
            this.camera.close();
            this.cameraTile.icon='/HomeAutomation/cameraproxy';
        },
        afterDialogClose: function () {
            this._oDialog.destroy();
            this._oDialog = null;
        },
        afterCameraDialogClose: function () {
            this.camera.destroy();
            this.camera = null;
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
                this.byId("idMenuClock").setText(date + " - " + time);
                // this.byId("idMenuClock").setInfo(date);
            }
        },

        handlePressOpenMenu: function (oEvent) {
            var oButton = oEvent.getSource();

            // create menu only once
            if (!this._menu) {
                this._menu = sap.ui.xmlfragment(
                    "cm.homeautomation.Menu",
                    this
                );
                this.getView().addDependent(this._menu);
            }

            var eDock = sap.ui.core.Popup.Dock;
            this._menu.open(this._bKeyboard, oButton, eDock.BeginTop, eDock.BeginBottom, oButton);
        },

        handleMenuItemPress: function (oEvent) {
            if (oEvent.getParameter("item").getSubmenu()) {
                return;
            }

            if (oEvent.getParameter("item").sId == "reloadScheduler") {
                this._reloadScheduler();
            } else {
                var msg = "";
                msg = "'" + oEvent.getParameter("item").getText() + "' pressed";

                this.messageToast.show(msg);
            }
        },
        _reloadScheduler: function () {
            var subject = this;
            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/scheduler/refresh", "", "GET", subject._handleSchedulerLoaded, null, subject);
        },
        _handleSchedulerLoaded: function (event) {
            this.messageToast.show("Scheduler reloaded");
        }


    });

});
