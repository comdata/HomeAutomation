var imageFullScreen = false;


jQuery.sap.require("sap.ui.core.UIComponent");
jQuery.sap.require("sap.m.Dialog");

function resize(element) {

    if (element != null) {

        // if (!imageFullScreen) {
        // imageFullScreen=true;

        var targetWidth = (window.innerWidth - 30);
        var targetHeight = targetWidth * (0.5625);

        element.style.height = targetHeight + "px";

        element.style.width = targetWidth + "px";
        /*
		 * } else { imageFullScreen=false; element.style.height="324px";
		 * element.style.width="576px";
		 *  }
		 */

    }
}

function guid() {
	function s4() {
		return Math.floor((1 + Math.random()) * 0x10000)
			.toString(16)
			.substring(1);
	}
	return s4() + s4() + '-' + s4() + '-' + s4() + '-' +s4() + '-' + s4() + s4() + s4();
}

sap.ui.define([
    'jquery.sap.global',
    'sap/ui/core/mvc/Controller',
    'sap/ui/model/json/JSONModel',
    'cm/webservice/RESTService',
    'cm/homeautomation/Validator',
], function (jQuery, Controller, JSONModel, RESTService, Validator) {
    "use strict";

    return Controller.extend("cm.homeautomation.Main", {

        selectedRoom: "",
        currentRoomModel: null,
        messageToast: null,
        _networkDevicesList:[],
        _wsOverviewUri: "ws://" + location.host + "/HomeAutomation/overview",
        _wsEventBusUri: "ws://" + location.host + "/HomeAutomation/eventbus",
        _webEventBusSocket: null,
        _webOverviewSocket: null,
        _wsGuid: guid(),
        loadDataInProgress: false,
        initWebSocket: function (uri, callback, socket) {

            try {
                if (socket != null) {
                    socket.close();
                }
            } catch (e) {

            }

            socket = new WebSocket(uri+"/"+this._wsGuid);
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
        wsClose: function(socket ) {
        	if (socket!=null && socket.readyState<3) {
        		socket.close();
        	}
        },
        wsOnClose: function (evt, uri, callback, socket) {
        	console.log("socket "+uri+" closed");
        	this.wsClose(socket);
            var that=this;
            window.setTimeout(function () {
                that.initWebSocket(uri, callback, socket);
            }, 2000);
        },
        handleSwitchEvent: function (data) {
            var eventModel = new JSONModel();

            eventModel.setData(JSON.parse(evt.data));

            var switchId = data.switchId;
            var status = data.status;

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
        
        wsEventBusOnMessage: function (evt) {
            var newData = JSON.parse(evt.data);
            if (newData.clazz=="TransmissionStatusData") {
            	this.handleTransmissionStatus(newData.data);
            }
            
            if (newData.clazz=="DistanceSensorData") {
            	
            	this.handleDistanceSensor(newData.data);
            }
            
            if (newData.clazz=="NetworkScannerHostFoundMessage") {
            	
            	this.handleNetworkMonitor(newData.data.host);
            }
            
            if  (newData.clazz=="SwitchEvent") {
            	
            	this.handleSwitchEvent(newData.data.host);
            }
            
            console.log(evt.data);
        },
        wsOverviewOnMessage: function (evt) {
            var newData = JSON.parse(evt.data);

            if (this.overviewData != null) {


                var tileNo = null;

                $.each(this.overviewData.overviewTiles, function (i, tile) {
                    if (tile!=null && tile.roomId!=null && tile.roomId == newData.roomId) {
                        tileNo = i;
                    }

                });

                if (tileNo != null) {
                    var tile = this.getView().getModel().getData().overviewTiles[tileNo];

                    tile.icon = newData.icon;
                    tile.number = newData.number;
                    tile.numberUnit = newData.numberUnit;
                    tile.info = newData.info;
                    tile.infoState = newData.infoState;
                }
                // this.getView().getModel().setData(this.overviewData);
                // newTiles.setData(tiles);
                this.getView().getModel().refresh(false);

                // this.getView().setModel(newTiles);
                $(".sapMStdTileIconDiv > img[src='/HomeAutomation/cameraproxy']").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
            }
        },
        wsOnError: function (evt, uri, callback, socket) {
        	console.log("socket "+uri+" errored");
        	this.wsClose(socket);
            var that=this;
            window.setTimeout(function () {
            that.initWebSocket(uri, callback, socket);
            }, 2000);
        },
        handleDistanceSensor: function (data) {
        	if (this.distanceTile!=null) {
        		console.log("distance:"+data.distance);
        		this.distanceTile.number=Number(parseFloat(data.distance)).toFixed(2);
      
        		this.getView().getModel().refresh(false);
        	}
        },
        handleTransmissionStatus: function (data) {
        	if (this.transmissionTile!=null) {
        		this.transmissionTile.number=data.numberOfDoneTorrents+" / "+data.numberOfTorrents;
        		this.transmissionTile.info=Math.round(data.downloadSpeed/1024*100)/100+" / "+Math.round(data.uploadSpeed/1024*100)/100 + " kb";
        	}
        },
        
    	handleNetworkMonitor: function(data) {
    		// create array if required
    		
    		var element = {
    				ipAddress: data.ip,
    				hostName: data.hostname,
    				mac: data.mac
    		};
    		
    		var foundElement = this._findInNetworkDeviceList(element);
    		
    		if (foundElement!=null) {
    			foundElement.ipAddress=element.ipAddress;
    			foundElement.hostName=element.hostName;
    			foundElement.mac=element.mac;
    		} else {
    			this._networkDevicesList.push(element);
    		}

    		this._networkDevicesTile.number=this._networkDevicesList.length;
    		
    		this.getView().getModel().refresh(false);
    		
    	},
    	_findInNetworkDeviceList: function (object) {
    		this._networkDevicesList.forEach(function (element, index, array) {
    			if (object.ipAddress==element.ipAddress) {
    				return element;
    			}
    		});
    		return null;
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

            /*
			 * window.setInterval(function () { subject.loadData.apply(subject) },
			 * 30000);
			 */

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
            
            this.initWebSocket(this._wsEventBusUri, this.wsEventBusOnMessage, this._webEventBusSocket);
            this.initWebSocket(this._wsOverviewUri, this.wsOverviewOnMessage, this._webOverviewSocket);
        },

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
        _initNetworkTile: function () {
			this._networkDevicesTile={
                    tileType: "networkDevices",
                    roomId: "networkDevices",
                    title: "Devices",
                    numberUnit: "Anzahl",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://laptop"	
			};
			
			this.getView().getModel().getData().overviewTiles.push(this._networkDevicesTile);
            
        },
        _initCameraTiles: function () {
            var cameras = [
                       	{
                       		window:null,
                       		tile: {
                                   tileType: "camera",
                                   roomId: "camera",
                                   title: "Küche",
                                   info: "Kamera - Küche",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/newCameraProxy?_ip=57&_port=8080&_action=snapshot",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=57&_port=8080&_action=stream"
                               }
                       	},
                       	{
                       		window:null,
                       		tile: {
                                   tileType: "camera",
                                   roomId: "camera2",
                                   title: "Keller",
                                   info: "Kamera - Keller",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/newCameraProxy?_ip=34&_port=8080&_action=snapshot",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=34&_port=8080&_action=stream"
                               }
                       	},
                       	{
                       		window:null,
                       		tile: {
                                   tileType: "camera",
                                   roomId: "camera3",
                                   title: "Wohnzimmer",
                                   info: "Kamera - Wohnzimmer",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/newCameraProxy?_ip=76&_port=8081&_action=snapshot",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=76&_port=8081&_action=stream"
                               }
                       	}
                       	
                       	/*,
                       	{
                       		window:null,
                       		tile: {
                                   tileType: "camera",
                                   roomId: "camera4",
                                   title: "Spielzimmer",
                                   info: "Kamera - Spielzimmer",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/newCameraProxy?_ip=33&_port=8090&_action=snapshot",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=336&_port=8090&_action=stream"
                               }
                       	}*/
                       	
                       	
                       ];

            var camerasDisabled=jQuery.sap.getUriParameters().get("disableCamera");
            
            camerasDisabled=(camerasDisabled=="true") ? true: false;
            
            if (!camerasDisabled) {
	            $.each(cameras, function (i, camera) {
	            	camera.tile.info=moment().format('DD.MM.YYYY HH:mm:ss');
	            	camera.tile.icon=camera.tile.icon+"&random=" + Math.random();
	            	subject.getView().getModel().getData().overviewTiles.push(camera.tile);
	            });
	            
	            this.getView().getModel().refresh(false);
	            
	            $.each(cameras, function (i, camera) {
	            		$(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	            	});
	            //
	            $(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	            $(".sapMStdTileIconDiv > img").parent().parent().parent().children().find('div[class="sapMStdTileTitle"]').css("position", "relative").css("top", "-120px");
	            $(".sapMStdTileInfoNone").css("position", "relative").css("top", "-30px");            
	            
	            /**
	             * 
	             * var canvas = document.createElement('canvas');
	             * var context = canvas.getContext('2d');
	             * var img = document.getElementById('myimg');
	             * context.drawImage(img, 0, 0 );
	             * var myData = context.getImageData(0, 0, img.width, img.height);
	             * 
	             */
	            
	            $.each($(".sapMStdTileIconDiv > img"), function(i, image) {
	            	console.log("img:"+image.src);
	            	image.onload=function () {
	            		$(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	            	 	$(".sapMStdTileIconDiv > img").parent().parent().parent().children().find('div[class="sapMStdTileTitle"]').css("position", "relative").css("top", "-120px");
	                 	$(".sapMStdTileInfoNone").css("position", "relative").css("top", "-30px");
	            	};
	            });
	            
	            this.cameraTimer = window.setInterval(function () {
	            	$.each(cameras, function (i, camera) {
	            		 var parts=camera.tile.icon.split("&");
	            		 var newUrl="";
	            		 
	            		 for (var i=0; i<parts.length;i++) {
	            			 if (parts[i].indexOf("random=")==-1) {
	            				 newUrl+=parts[i]+"&";
	            			 }
	            		 }
	            		 var imageURL = newUrl+"random=" + Math.random();
	            		 
	            		 var image = $(".sapMStdTileIconDiv > img[src*='"+newUrl+"']")[0];
	            		 console.log("old image src: "+image.src);
	            		 var downloadingImage = new Image();
	            		 downloadingImage.onload = function(){
	            			 console.log("new loaded image src: "+downloadingImage.src);
	            		     image.src = this.src;   
	            		     camera.tile.info=moment().format('DD.MM.YYYY HH:mm:ss');
	            		     
	            		     $(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	                         $(".sapMStdTileIconDiv > img").parent().parent().parent().children().find('div[class="sapMStdTileTitle"]').css("position", "relative").css("top", "-120px");
	                         $(".sapMStdTileInfoNone").css("position", "relative").css("top", "-30px");
	         		     
	            		 };
	            		 
	            		 
	            		 
	            		 downloadingImage.src=imageURL;
	            	});
	            	
	
	            }, 60000);
            }
            
        },
        _initPlanesTile: function () {
            var subject=this;
            
            this.planesTile = {
                    tileType: "planes",
                    roomId: "planes",
                    title: "Flugzeuge",
                    numberUnit: "Anzahl",
                    eventHandler: "showPlanes",
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://flight"
                };
            var planesTile = this.planesTile;
            this.planesTimer=null;
            
            if (this.planesTimer == null) {
                this.updatePlanesTile.apply(this, [subject, planesTile]);
                this.planesTimer = window.setInterval(function () {
                    subject.updatePlanesTile.apply(subject, [subject, planesTile]);
                }, 60000);

            }
            this.getView().getModel().getData().overviewTiles.push(this.planesTile);
        },
        _initTransmissionTile: function () {
            this.transmissionTile = {
                    tileType: "transmission",
                    roomId: "transmission",
                    title: "Downloads",
                    numberUnit: "Anzahl",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://download-from-cloud"
                };
            this.getView().getModel().getData().overviewTiles.push(this.transmissionTile);
        },
        _initDistanceTile: function () {
        	this.distanceTile = {
                    tileType: "distance",
                    roomId: "distance",
                    title: "Distance",
                    numberUnit: "cm",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://marketing-campaign"
                };
            this.getView().getModel().getData().overviewTiles.push(this.distanceTile);
        },
        /**
		 * handle successful data loading for overview tiles
		 * 
		 * @param event
		 * @param model
		 */
        handleDataLoaded: function (event, model, jsonModelData) {
            this.getView().setModel(model);
            this.overviewData = jsonModelData;
            
            this._initNetworkTile();
            this._initCameraTiles();
            this._initPlanesTile();
            this._initTransmissionTile();
            this._initDistanceTile();
 
        },
        updatePlanesTile: function (subject, planesTile) {
            $.getJSON("/HomeAutomation/planesproxy", function (result) {

                console.log("Anzahl " + result.length);
                planesTile.number = result.length;
                planesTile.info = $.format.date(new Date(), "dd.MM.yyyy HH:mm:ss");
                subject.getView().getModel().refresh(false);
            });
        },
        handleSwitchesLoaded: function (event, model) {

            var switchList = sap.ui.getCore().byId("switchList");
            var switchPanel = sap.ui.getCore().byId("switchPanel");
            if (model.getProperty("/switchStatuses").length > 0) {
                switchList.setProperty("visible", true);
                switchPanel.setProperty("expanded", true);
            } else {
                switchList.setProperty("visible", false);
                switchPanel.setProperty("expanded", false);
            }

            sap.ui.getCore().setModel(model, "switches");
        },
        handleWindowBlindsLoaded: function (event, model) {

            var windowBlindsList = sap.ui.getCore().byId("windowBlinds");
            var switchPanel = sap.ui.getCore().byId("switchPanel");
            if (model.getProperty("/windowBlinds").length > 0) {
                windowBlindsList.setProperty("visible", true);
                switchPanel.setProperty("expanded", true);
            } else {
                windowBlindsList.setProperty("visible", false);
            }

            sap.ui.getCore().setModel(model, "windowBlinds");
        },

        handleThermostatsLoaded: function (event, model) {

            var thermostatsList = sap.ui.getCore().byId("thermostats");
            var switchPanel = sap.ui.getCore().byId("switchPanel");
            if (model.getProperty("/switchStatuses").length > 0) {
                thermostatsList.setProperty("visible", true);
                switchPanel.setProperty("expanded", true);
            } else {
                thermostatsList.setProperty("visible", false);
            }

            sap.ui.getCore().setModel(model, "thermostats");
        },

        handleSwitchChange: function (event) {
            var singleSwitch = sap.ui.getCore().getModel("switches").getProperty(event.getSource().oPropagatedProperties.oBindingContexts.switches.sPath);

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
        	var windowBlindsModel = sap.ui.getCore().getModel("windowBlinds");
            var windowBlind = windowBlindsModel.getProperty(event.getSource().oPropagatedProperties.oBindingContexts.windowBlinds.sPath);

            var value = windowBlind.currentValue;
            console.log("new value: " + value);

            var oModel = new RESTService();
            var windowBlindId=( windowBlind.id==null) ? 0 : windowBlind.id;
            oModel.loadDataAsync("/HomeAutomation/services/windowBlinds/setDim/" + windowBlindId + "/"
                + value+"/"+windowBlind.type+"/"+windowBlind.room, "", "GET", this.handleSwitchChanged, null, this);
            
            /**
             * set value directly to all other window blinds
             * 
             */
            if (windowBlind.type=="ALL_AT_ONCE") {
            	
            	var windowBlinds=windowBlindsModel.getProperty("/");
            	
            	for(var i = 0; i < windowBlinds.windowBlinds.length; i++) {
            		
            		var singleWindowBlind=windowBlinds.windowBlinds[i];
            		
            		if (singleWindowBlind.type=="SINGLE") {
            			singleWindowBlind.currentValue=value;
            		}
            	}
            	windowBlindsModel.setData(windowBlinds);
            	sap.ui.getCore().SetModel(windowBlindsModel, "windowBlinds");
            	
            }
            
        },
        handleThermostatChange: function (event) {
            var thermostat = sap.ui.getCore().getModel("thermostats").getProperty(event.getSource().oPropagatedProperties.oBindingContexts.thermostats.sPath);

            var value = thermostat.currentValue;
            console.log("new value: " + value);

            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/thermostat/setValue/" + thermostat.id + "/"
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

            var thermostatModel = new RESTService();
            switchesModel.loadDataAsync("/HomeAutomation/services/actor/thermostat/forroom/" + subject.selectedRoom, "", "GET", subject.handleThermostatsLoaded, null, subject);


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
            // window.setTimeout(function () {
            if (oEvent.getParameter("expand") == true) {
                getHistoricalSensordata(this.selectedRoom);
            }
            // }, 5000);

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
            
            var currentRoomData={"roomName": roomName};
            
           
            
            var currentRoomModel = new sap.ui.model.json.JSONModel();

            currentRoomModel.setData(currentRoomData);
            
            sap.ui.getCore().setModel(currentRoomModel, "currentRoom");

            var roomId = this.selectedRoom;

            if (tileType == "camera") {
                if (!this.camera) {
                    this.camera = sap.ui.xmlfragment("cm.homeautomation.Camera", this);
                    var stream = selectedElement.stream;
                    jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this.camera);
                    this.camera.open();
                    this.camera.getContent()[0].setContent('<div align="center" width="100%" ><img onload="resize(this)" onclick="resize(this)" src="'+stream+'" width="576" height="324" /></div><br />');
                }
            }
            else if (tileType == "planes") {
                if (!this.planesView) {
                    this.planesView = sap.ui.xmlfragment("cm.homeautomation.Planes", this);

                }
                this.planesView.open();
                $(".sapMDialogScrollCont").css("height", "100%");
            } else {
                if (roomId != null && roomId) {

                    this.loadRoom();

                    if (!this._oDialog) {
                        this._oDialog = sap.ui.xmlfragment("cm.homeautomation.Switch", this);
                        this._oDialog.setModel(this.getView().getModel());
                        
                        
                    }

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
        },
        planesDialogClose: function () {
            this.planesView.close();
        },
        afterDialogClose: function () {
            this._oDialog.destroy();
            this._oDialog = null;
        },
        afterCameraDialogClose: function () {
            this.camera.destroy();
            this.camera = null;
        },

        afterPlanesDialogClose: function () {
            this.planesView.destroy();
            this.planesView = null;
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
            sLocale = "de";
            var time = Digital.toLocaleTimeString(sLocale);
            var date = Digital.toLocaleDateString(sLocale);
            if (this.byId("idMenuClock")) {
                this.byId("idMenuClock").setText(date + " - " + time.replace("MESZ", ""));
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
            } else if (oEvent.getParameter("item").sId == "openAdminDialog") {
                this._openAdminDialog();
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
        },
        _openAdminDialog: function () {
            if (!this.adminView) {
                this.adminView = sap.ui.xmlfragment("cm.homeautomation.Administration", this);

            }
            this.adminView.open();
            this.administrationDialogLoadRooms();

        },
        administrationDialogLoadRooms: function () {
            var subject = this;
            var roomModel = new RESTService();
            roomModel.loadDataAsync("/HomeAutomation/services/admin/room/getAll", "", "GET", subject.handleAdminRoomsLoaded, null, subject);

        },
        administrationDialogClose: function () {
            this.adminView.close();
        },
        afterAdministrationDialogClose: function () {
            this.adminView.destroy();
            this.adminView = null;
        },
        handleAdminRoomsLoaded: function (event, model) {
            sap.ui.getCore().setModel(model, "rooms");
        },
        handleAddRoomButtonPress: function (event) {
            var model = new JSONModel();


            this.roomAdminDialogShow("ADD", model);
        },
        handleEditRoomButtonPress: function (event) {
            console.log("selected room:" + this.administrationSelectedRoom.id);

            var model = new JSONModel();

            model.setData(this.administrationSelectedRoom);

            this.roomAdminDialogShow("EDIT", model);
        },
        roomAdminDialogShow: function (mode, model) {
            if (!this.roomAdminView) {
                this.roomAdminView = sap.ui.xmlfragment("cm.homeautomation.RoomAdmin", this);

            }
            this.roomAdminView.open();
            this.roomAdminMode = mode;

            sap.ui.getCore().setModel(model, "roomDetail");
        },
        roomAdminDialogOk: function (event) {

            var model = sap.ui.getCore().getModel("roomDetail");

            var roomName = model.getProperty("/roomName");
            var roomId = model.getProperty("/id");

            var url = "";

            if (this.roomAdminMode == "ADD") {
                url = "/HomeAutomation/services/admin/room/create/" + roomName;
            } else if (this.roomAdminMode == "EDIT") {
                url = "/HomeAutomation/services/admin/room/update/" + roomId + "/" + roomName;
            }

            var roomUpdate = new RESTService();
            roomUpdate.loadDataAsync(url, "", "GET", this.handleRoomUpdated, null, this);

            this.roomAdminView.close();
        },
        roomAdminDialogCancel: function (event) {
            this.roomAdminView.close();
        },
        handleRoomUpdated: function (event, model) {
            this.administrationDialogLoadRooms();
        },
        handleRoomSelected: function (item, items, selected) {
            var selectedRoom = sap.ui.getCore().getModel("rooms").getProperty(sap.ui.getCore().byId("rooms").getSelectedItem().oBindingContexts.rooms.sPath);
        },
        administrationRoomPressed: function (oEvent) {
        	this.administrationSelectedRoomPath=oEvent.getParameter("listItem").oBindingContexts.rooms.sPath;
            this.administrationSelectedRoom=sap.ui.getCore().getModel("rooms").getProperty(this.administrationSelectedRoomPath);
            var roomId=oEvent.getParameter("listItem").getCustomData()[0].getValue();

            this._administrationShowRoomDetails(this.administrationSelectedRoom);
        },
        _administrationShowRoomDetails:function (room) {
            var roomDetailModel=new JSONModel();
            roomDetailModel.setData(room);

            sap.ui.getCore().setModel(roomDetailModel, "administrationRoomDetail");

        },
        administrationReloadRoom: function () {
        	var subject = this;
            var roomModel = new RESTService();
           
            roomModel.loadDataAsync("/HomeAutomation/services/admin/room/getAll", "", "GET", function (event, model) { 
            		subject.handleAdminRoomsLoaded(event, model);
            		
            		subject.administrationSelectedRoom=sap.ui.getCore().getModel("rooms").getProperty(subject.administrationSelectedRoomPath);
                    subject._administrationShowRoomDetails(this.administrationSelectedRoom);

            		}, null, subject);
        },
        handleAddSensorButtonPress: function (event) {
        	var model = new JSONModel();
        	
        	console.log("pressed add sensor");

            this.sensorAdminDialogShow("ADD", model);
        },
        administrationDevicePressed: function(oEvent) {
        	console.log("device pressed");
        	
        	var device=sap.ui.getCore().getModel("administrationRoomDetail").getProperty(oEvent.getParameter("listItem").oBindingContexts.administrationRoomDetail.sPath);
        
        	this.adminDeviceForEditMac=device.mac;
        	console.log("device mac:" +device.mac);
        	
        	 var model = new JSONModel();
        	 device.mode="EDIT";
        	 model.setData(device);
        	 
             if (!this.deviceAdminView) {
                 this.deviceAdminView = sap.ui.xmlfragment("cm.homeautomation.DeviceAdmin", this);
             }
             this.deviceAdminView.open();
             this.deviceAdminMode="EDIT";

             sap.ui.getCore().setModel(model, "deviceAdminDetail");
        },
        
        handleAddDeviceButtonPress: function (event) {
            var model = new JSONModel();
    	 
         if (!this.deviceAdminView) {
             this.deviceAdminView = sap.ui.xmlfragment("cm.homeautomation.DeviceAdmin", this);
         }
         this.deviceAdminView.open();
         this.deviceAdminMode="ADD";

         sap.ui.getCore().setModel(model, "deviceAdminDetail");
         
            console.log("pressed add device");

        },
        
        deviceAdminValidate: function () {

            var validator = new Validator();
            
            // Validate input fields against root page with id 'somePage'
            if (validator.validate(sap.ui.getCore().byId("DeviceAdminForm"))) {
                return true;
            }
            return false;
        },
        
        deviceAdminDialogOk: function (event) {
        	
        	if (this.deviceAdminValidate()) {
	            var model = sap.ui.getCore().getModel("deviceAdminDetail");
	
	            var name = model.getProperty("/name");
	            var mac = model.getProperty("/mac");
	            var roomId=this.administrationSelectedRoom.id;
	
	            var url = "";
	
	            if (this.deviceAdminMode == "ADD") {
	                url = "/HomeAutomation/services/admin/device/create/"+roomId+ "/" + name+ "/" +mac;
	            } else if (this.deviceAdminMode == "EDIT") {
	            	var oldMac=this.adminDeviceForEditMac
	                url = "/HomeAutomation/services/admin/device/update/"+roomId+"/" + name + "/"+oldMac+"/"+mac;
	            }
	
	            var deviceUpdate = new RESTService();
	            var subject=this;
	            deviceUpdate.loadDataAsync(url, "", "GET", function() {subject.administrationReloadRoom(); this.deviceAdminView.close();}, null, this);
        	}
        },
        deviceAdminDialogDelete: function (event) {
        	
            var model = sap.ui.getCore().getModel("deviceAdminDetail");

            var mac = model.getProperty("/mac");
            var url = "/HomeAutomation/services/admin/device/delete/" +mac;
           
            var deviceUpdate = new RESTService();
            var subject=this;
            deviceUpdate.loadDataAsync(url, "", "GET", function() {subject.administrationReloadRoom(); this.deviceAdminView.close(); }, null, this);
        	
        },
        
        deviceAdminDialogCancel: function (event) {
            this.deviceAdminView.close();
        },
        
        sensorAdminDialogShow: function (mode, model) {
            if (!this.sensorAdminView) {
                this.sensorAdminView = sap.ui.xmlfragment("cm.homeautomation.SensorAdmin", this);

            }
            this.sensorAdminView.open();
            this.sensorAdminMode = mode;

            sap.ui.getCore().setModel(model, "sensorAdminDetail");
        },
        handleAddSwitchButtonPress: function (event) {
            console.log("pressed add switch");

            var model = new JSONModel();

            console.log("pressed add sensor");

            this.switchAdminDialogShow("ADD", model);
        },
        switchAdminDialogShow: function (mode, model) {
            if (!this.switchAdminView) {
                this.switchAdminView = sap.ui.xmlfragment("cm.homeautomation.SwitchAdmin", this);

            }
            this.switchAdminView.open();
            this.switchAdminMode = mode;

            sap.ui.getCore().setModel(model, "switchAdminDetail");
        },
        sensorAdminDialogOk: function (event) {

            var model = sap.ui.getCore().getModel("sensorAdminDetail");

            var name = model.getProperty("/name");
            var sensorId = model.getProperty("/id");

            var url = "";

            if (this.sensorAdminMode == "ADD") {
                url = "/HomeAutomation/services/admin/sensor/create/" + name;
            } else if (this.switchAdminMode == "EDIT") {
                url = "/HomeAutomation/services/admin/sensor/update/" + sensorId + "/" + name;
            }

            var sensorUpdate = new RESTService();
            sensorUpdate.loadDataAsync(url, "", "GET", this.handleSensorUpdated, null, this);

            this.sensorAdminView.close();
        },
        switchAdminDialogOk: function (event) {

            var model = sap.ui.getCore().getModel("switchAdminDetail");

            var name = model.getProperty("/name");
            var sensorId = model.getProperty("/id");

            var url = "";

            if (this.switchAdminMode == "ADD") {
                url = "/HomeAutomation/services/admin/switch/create/" + name;
            } else if (this.switchAdminMode == "EDIT") {
                url = "/HomeAutomation/services/admin/switch/update/" + sensorId + "/" + name;
            }

            var switchUpdate = new RESTService();
            switchUpdate.loadDataAsync(url, "", "GET", this.handleSwitchUpdated, null, this);

            this.switchAdminView.close();
        },
        sensorAdminDialogCancel: function (event) {
            this.sensorAdminView.close();
        },
        switchAdminDialogCancel: function (event) {
            this.switchAdminView.close();
        }
    });

});
