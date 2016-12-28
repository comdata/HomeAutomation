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
		 * element.style.width="576px"; }
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
        initWebSocket: function (uri, callback, socket, state) {


            this.wsClose(socket, state);

            state="CONNECTING";
            socket = new WebSocket(uri+"/"+this._wsGuid);
            var controller = this;
            socket.onopen = function (evt, state) {
                controller.wsOnOpen.apply(controller, [evt, state])
            };
            socket.onclose = function (evt) {
                controller.wsOnClose.apply(controller, [evt, uri, callback, socket, state])
            };
            socket.onmessage = function (evt) {
                callback.apply(controller, [evt])
            };
            socket.onerror = function (evt) {
                controller.wsOnClose.apply(controller, [evt, uri, callback, socket, state])

            };
        },
        wsOnOpen: function (evt, state) {
        	state="CONNECTED";
        },
        wsClose: function(socket, state) {
        	 try {
	        	state="DISCONNECTED";
	        	if (socket!=null && socket.readyState<3) {
	        		socket.close();
	        	}
        	 } catch (e) {

        	 }

        },
        wsOnClose: function (evt, uri, callback, socket, state) {
        	console.log("socket "+uri+" closed");
        	this.wsClose(socket, state);
            var that=this;

            if (state=="DISCONNECTED") {
            	window.setTimeout(function () {

            		that.initWebSocket(uri, callback, socket, state);
            	}, 30000);
            }
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
            if  (newData.clazz=="CameraImageUpdateEvent") {

            	this.handleCameraEvent(newData.data);
            }

            if (newData.clazz=="MailData") {
            	this.handleMailEvent(newData.data);
            }

            if (newData.clazz=="PowerMeterIntervalData") {
            	this.handlePowerEvent(newData.data);
            }

            if (newData.clazz=="WindowStateData") {
              this.handleWindowStateEvent(newData.data);
            }

            console.log(evt.data);
        },
        wsOverviewOnMessage: function (evt) {
            var newData = JSON.parse(evt.data);

            if (this.overviewData != null) {


                var tileNo = null;

                $.each(this.overviewData.overviewTiles, function (i, tile) {
                    if (tile!=null && tile.roomId!=null && tile.roomId == newData.roomId && tile.tileType=="room") {
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
        handleWindowStateEvent: function (data) {
          var state=data.state;
          var roomName=data.room.roomName;
          var roomId=data.room.id;
          var translatedState;

          if (state==0) {
            translatedState="CLOSED";
          } else {
            translatedState="OPEN";
          }

          this.messageToast.show("Fenster/Tuer in "+roomName+" "+((translatedState=="CLOSED")?"geschlossen":"geoeffnet"), {
              duration: 10000                  // default
          }
          );

          // find existing entry for update
          var found=false;

          this._openWindows.forEach(function (element, index, array) {
            if (element.mac==data.mac) {
              element.state=translatedState;
              found=true;
            }
          }, this );

          // add new entry
          if (found==false) {
            var newOpenWindow={"mac":data.mac, "state": translatedState, "roomName":roomName, "roomId": roomId};
            this._openWindows.push(newOpenWindow);
          }


          // do an update on the tile
          var numberOpen=0;

          this._openWindows.forEach(function (element, index, array) {
            if (element.state=="OPEN") {
              numberOpen++;
            }
          }, this );

          this.windowStateTile.number=numberOpen;
          this.windowStateTile.info=(numberOpen>0)?"Offen":"alle geschlossen";


        	//this.powerMeterTile.info="1 / 5 / 60 minutes";
          this.getView().getModel().refresh(false);
        },

        handlePowerEvent: function (data) {
        		this.powerMeterTileOneMinute.number=data.oneMinute;
        		this.powerMeterTileFiveMinute.number=data.fiveMinute;
        		this.powerMeterTileSixtyMinute.number=data.sixtyMinute;
        		this.powerMeterTileSixtyMinute.number=data.sixtyMinute;
            this.powerMeterTileToday.number=data.today;
        		this.powerMeterTileYesterday.number=data.yesterday;
        		this.powerMeterTileLastSevenDays.number=data.lastSevenDays;

        		//this.powerMeterTile.info="1 / 5 / 60 minutes";
        		 this.getView().getModel().refresh(false);
        },
        handleMailEvent: function (data) {
        	$.each(this._mailTiles, function (index, value) {
        		if (value.title==data.account) {
        			value.info=data.unreadMessages+" - "+data.newMessages;
        		}
        	});

        },
        handleCameraEvent: function (data) {
        	console.log("got camera event: "+data.camera + " - camera id: "+data.camera.id);
        	var that=this;

        	$.each(this.cameras, function (i, camera) {
        		if (camera.id==data.camera.id) {

    				var parts=camera.tile.icon.split("?");
    				var newUrl="";

        				for (var i=0; i<parts.length;i++) {
        					if (parts[i].indexOf("random=")==-1) {
    						newUrl+=parts[i]+"?";
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
    					that.resizeCameraPictures();
    					window.setTimeout(function() {that.resizeCameraPictures();}, 1000);
    					window.setTimeout(function() {that.resizeCameraPictures();}, 5000);
    					window.setTimeout(function() {that.resizeCameraPictures();}, 10000);

    				};

    				downloadingImage.src=imageURL;
        		}
        	});

        },
        resizeCameraPictures: function () {
        	$(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
			$(".sapMStdTileIconDiv > img").parent().parent().parent().children().find('div[class="sapMStdTileTitle"]').css("position", "relative").css("top", "-120px");
			$(".sapMStdTileInfoNone").css("position", "relative").css("top", "-30px");
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
    		console.log("data.ip: "+data.ip+" element.ipAddress: "+element.ipAddress);
    		var foundElement = this._findInNetworkDeviceList(element);
    		console.log("foundElement:"+foundElement);

    		if (foundElement!=null) {
    			console.log("updating element")
    			foundElement.hostName=element.hostName;
    			foundElement.mac=element.mac;
    		} else {
    			console.log("pushing new element");
    			this._networkDevicesList.push(element);
    		}

    		this._networkDevicesTile.number=this._networkDevicesList.length;

    		this.getView().getModel().refresh(false);

    	},
    	_findInNetworkDeviceList: function (object) {
    		var foundElement=null;
    		this._networkDevicesList.forEach(function (element, index, array) {
    			if (object.ipAddress==array[index].ipAddress) {
    				foundElement=array[index];
    				return;
    			}
    		});
    		return foundElement;
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

            this.initializeAllWebsockets();
        },
        /**
		 * initialize the web sockets
		 */
        initializeAllWebsockets: function () {
            this.initWebSocket(this._wsEventBusUri, this.wsEventBusOnMessage, this._webEventBusSocket, this._webEventBusState);
            this.initWebSocket(this._wsOverviewUri, this.wsOverviewOnMessage, this._webOverviewSocket, this._webOverviewState);
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
			this.getView().getModel().refresh(false);
        },
        _initCameraTiles: function () {
        	var subject=this;
            this.cameras = [
                       	{
                       		window:null,
                       		id: 1,
                       		tile: {
                                   tileType: "camera",
                                   roomId: 1,
                                   title: "Küche",
                                   info: "Kamera - Küche",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/services/camera/getSnapshot/1",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=57&_port=8080&_action=stream"
                               }
                       	},
                       	{
                       		window:null,
                       		id: 2,
                       		tile: {
                                   tileType: "camera",
                                   roomId: 2,
                                   title: "Auffahrt",
                                   info: "Kamera - Auffahrt",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/services/camera/getSnapshot/2",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=34&_port=8080&_action=stream"
                               }
                       	},
                       	{
                       		window:null,
                       		id: 3,
                       		tile: {
                                   tileType: "camera",
                                   roomId: 3,
                                   title: "Wohnzimmer",
                                   info: "Kamera - Wohnzimmer",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/services/camera/getSnapshot/3",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=76&_port=8081&_action=stream"
                               }
                       	},
                       	{
                       		window:null,
                       		id: 4,
                       		tile: {
                                   tileType: "camera",
                                   roomId: 6,
                                   title: "Spielzimmer",
                                   info: "Kamera - Spielzimmer",
                                   eventHandler: "showCamera",
                                   icon: "/HomeAutomation/services/camera/getSnapshot/4",
                                   stream: "/HomeAutomation/newCameraProxy?_ip=77&_port=8090&_action=stream"
                               }
                       	}




                       ];

            var camerasDisabled=jQuery.sap.getUriParameters().get("disableCamera");

            camerasDisabled=(camerasDisabled=="true") ? true: false;

            if (!camerasDisabled) {
	            $.each(this.cameras, function (i, camera) {
	            	camera.tile.info=moment().format('DD.MM.YYYY HH:mm:ss');
	            	camera.tile.icon=camera.tile.icon+"?random=" + Math.random();
	            	subject.getView().getModel().getData().overviewTiles.push(camera.tile);
	            });

	            this.getView().getModel().refresh(false);

	            $.each(this.cameras, function (i, camera) {
	            		$(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	            	});
	            //
	            $(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	            $(".sapMStdTileIconDiv > img").parent().parent().parent().children().find('div[class="sapMStdTileTitle"]').css("position", "relative").css("top", "-120px");
	            $(".sapMStdTileInfoNone").css("position", "relative").css("top", "-30px");

	            $.each($(".sapMStdTileIconDiv > img"), function(i, image) {
	            	console.log("img:"+image.src);
	            	image.onload=function () {
	            		$(".sapMStdTileIconDiv > img").css("width", "200px").css("height", "112px").css("position", "relative").css("left", "-20px").css("top", "30px");
	            	 	$(".sapMStdTileIconDiv > img").parent().parent().parent().children().find('div[class="sapMStdTileTitle"]').css("position", "relative").css("top", "-120px");
	                 	$(".sapMStdTileInfoNone").css("position", "relative").css("top", "-30px");
	            	};
	            });

            }
            this.getView().getModel().refresh(false);
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
            this.getView().getModel().refresh(false);
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
            this.getView().getModel().refresh(false);
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
            // this.getView().getModel().getData().overviewTiles.push(this.distanceTile);
            this.getView().getModel().refresh(false);
        },
        _initPowerMeterTile: function() {
        	this.powerMeterTileOneMinute = {
                    tileType: "powermeter",
                    roomId: "meter",
                    title: "1 Minute",
                    numberUnit: "kWh",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://energy-saving-lightbulb"
                };
            this.getView().getModel().getData().overviewTiles.push(this.powerMeterTileOneMinute);

        	this.powerMeterTileFiveMinute = {
                    tileType: "powermeter",
                    roomId: "meter",
                    title: "5 Minuten",
                    numberUnit: "kWh",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://energy-saving-lightbulb"
                };
            this.getView().getModel().getData().overviewTiles.push(this.powerMeterTileFiveMinute);
        	this.powerMeterTileSixtyMinute = {
                    tileType: "powermeter",
                    roomId: "meter",
                    title: "60 Minuten",
                    numberUnit: "kWh",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://energy-saving-lightbulb"
                };
        	this.getView().getModel().getData().overviewTiles.push(this.powerMeterTileSixtyMinute);

          this.powerMeterTileToday = {
                    tileType: "powermeter",
                    roomId: "meter",
                    title: "Heute Gesamt",
                    numberUnit: "kWh",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://energy-saving-lightbulb"
                };

            this.getView().getModel().getData().overviewTiles.push(this.powerMeterTileToday);


        	this.powerMeterTileYesterday = {
                    tileType: "powermeter",
                    roomId: "meter",
                    title: "Gestern Gesamt",
                    numberUnit: "kWh",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://energy-saving-lightbulb"
                };

            this.getView().getModel().getData().overviewTiles.push(this.powerMeterTileYesterday);


        	this.powerMeterTileLastSevenDays = {
                    tileType: "powermeter",
                    roomId: "meter",
                    title: "Letzte 7 Tage",
                    numberUnit: "kWh",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://energy-saving-lightbulb"
                };

            this.getView().getModel().getData().overviewTiles.push(this.powerMeterTileLastSevenDays);
            this.getView().getModel().refresh(false);
        },
        _initLegoTrainTile: function() {


        	  this.legoTrainTile = {
                    tileType: "legotrain",
                    roomId: "train",
                    title: "Lego Train",
                    numberUnit: "",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://passenger-train"
                };

            this.getView().getModel().getData().overviewTiles.push(this.legoTrainTile);
            this.getView().getModel().refresh(false);
        },
        _initWindowTile: function() {
            this._openWindows=[];

        	  this.windowStateTile = {
                    tileType: "windowState",
                    roomId: "windowsState",
                    title: "Fenster",
                    numberUnit: "",
                    eventHandler: null,
                    infoState: sap.ui.core.ValueState.Success,
                    icon: "sap-icon://windows-doors"
                };

            this.getView().getModel().getData().overviewTiles.push(this.windowStateTile);
            this.getView().getModel().refresh(false);

            var subject=this;
            $.ajax({

              type: "GET",
              url: "/HomeAutomation/services/window/readAll",
              contentType: "application/json",
              dataType: "json",

            success: function(response) {
              response.forEach(function (element, index, array) {
                if (element!=null && element.room!=null) {
			subject.handleWindowStateEvent(element);
		}
              }, subject);
              console.log(response);
            }});
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
            this._initWindowTile();
            this._initPowerMeterTile();
            this._initMailTile();
            this._initLegoTrainTile();
        },
        _initMailTile: function() {
        	var subject=this;

        	this._mailTiles=[];
            $.getJSON("/HomeAutomation/services/mail/get", function (result) {

            	$.each(result, function (index, mail) {
	                var singleMailTile = {
	                        tileType: "mail",
	                        roomId: "mail",
	                        title: mail.account,
	                        numberUnit: "Mails",
	                        eventHandler: "none",
	                        infoState: sap.ui.core.ValueState.Success,
	                        icon: "sap-icon://email"
	                    };
	                subject._mailTiles.push(singleMailTile);
	                subject.getView().getModel().getData().overviewTiles.push(singleMailTile);
	                singleMailTile.info=mail.unreadMessages+" - "+mail.newMessages;
	            	console.log(mail);

            	});
                subject.getView().getModel().refresh(false);
            });
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

        handleLightsLoaded: function (event, model) {

            var lightsList = sap.ui.getCore().byId("lights");
            var switchPanel = sap.ui.getCore().byId("switchPanel");
            if (model.getProperty("/").length > 0) {
            	lightsList.setProperty("visible", true);
                switchPanel.setProperty("expanded", true);
            } else {
                lightsList.setProperty("visible", false);
            }

            sap.ui.getCore().setModel(model, "lights");
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
            	sap.ui.getCore().setModel(windowBlindsModel, "windowBlinds");

            }

        },
        handleLightChange: function (event) {
        	var lightsModel = sap.ui.getCore().getModel("lights");
            var light = lightsModel.getProperty(event.getSource().oPropagatedProperties.oBindingContexts.lights.sPath);

            var value = light.brightnessLevel;
            console.log("new value: " + value);

            var oModel = new RESTService();
            var lightId=( light.id==null) ? 0 : light.id;
            oModel.loadDataAsync("/HomeAutomation/services/light/dim/" + lightId + "/"
                + value, "", "GET", this.handleSwitchChanged, null, this);


        },

        handleLightSwitchChange: function(event) {
        	var state=event.getSource().getProperty("state");

        	var lightsModel = sap.ui.getCore().getModel("lights");
            var light = lightsModel.getProperty(event.getSource().oPropagatedProperties.oBindingContexts.lights.sPath);

            if (state == true) {
            	light.brightnessLevel=99;
            } else {
            	light.brightnessLevel=0;
            }

            sap.ui.getCore().getModel("lights").refresh(false);

            var value = light.brightnessLevel;
            console.log("new value: " + value);

            var oModel = new RESTService();
            var lightId=( light.id==null) ? 0 : light.id;
            oModel.loadDataAsync("/HomeAutomation/services/light/dim/" + lightId + "/"
                + value, "", "GET", this.handleSwitchChanged, null, this);
        },

        handleThermostatChange: function (event) {
            var thermostat = sap.ui.getCore().getModel("thermostats").getProperty(event.getSource().oPropagatedProperties.oBindingContexts.thermostats.sPath);

            var value = thermostat.currentValue;
            console.log("new value: " + value);

            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/thermostat/setValue/" + thermostat.id + "/"
                + value, "", "GET", this.handleSwitchChanged, null, this);
        },
        handleColorPickerChange: function (oEvent) {
        	var colors = oEvent.getParameters();
        	var r=colors.r;
        	var g=colors.g;
        	var b=colors.b;

        	var roomId=0;
        	 var oModel = new RESTService();
             oModel.loadDataAsync("/HomeAutomation/services/led/set/" + roomId + "/"
                 + r+"/"+g+"/"+b, "", "GET", this.handleColorChanged, null, this);
        },
        handleColorChanged: function (event) {
            var subject = this;
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

            var lightModel = new RESTService();
            lightModel.loadDataAsync("/HomeAutomation/services/light/get/" + subject.selectedRoom, "", "GET", subject.handleLightsLoaded, null, subject);
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

        /**
		 * show historic data
		 */
        expandHistoricData: function (oEvent) {
            if (oEvent.getParameter("expand") == true) {
                getHistoricalSensordata(this.selectedRoom);
            }
        },

        handleTrainSpeedLightChange: function(event) {
          var train=this._trainModel.getProperty("train");
          var speed=this._trainModel.getProperty("speed");
          var light=this._trainModel.getProperty("light");

          if (speed==0) {
            speed=8; // brake
          } else if (speed<0)
            speed=(9+(8+speed))
          }

          var subject=this;
          var train = new RESTService();
          train.loadDataAsync("/HomeAutomation/services/lego/control/"+train+"/"+speed+"/"+light, "", "GET", null, null, subject);

        },
        trainEmergencyStop: function(oEvent) {
          var subject=this;
          var train = new RESTService();
          train.loadDataAsync("/HomeAutomation/services/lego/emergencyStop", "", "GET", null, null, subject);

        },
        handleTrainSelected: function(event) {
          this._trainModel.setProperty("train", event.getSource().getSelectedKey());
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

            if (tileType == "legotrain") {
                if (!this.trainDialog) {

                    this._trainModel = new sap.ui.model.json.JSONModel();
                    this._trainModel.setProperty("speed", 0);
                    this._trainModel.setProperty("light", 0);
                    sap.ui.getCore().setModel(this._trainModel, "train");
                    this.trainDialog = sap.ui.xmlfragment("cm.homeautomation.Train", this);
                    this.trainDialog.setModel(this.getView().getModel());
                    jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this.trainDialog);
                    this.trainDialog.open();

                }
            }

            else if (tileType == "camera") {
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
            } else if (tileType=="room") {
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
        trainDialogClose: function () {
            this.trainDialog.close();
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
        afterTrainDialogClose: function () {
            this.trainDialog.destroy();
            this.trainDialog = null;
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

        /**
		 * menu open pressed
		 *
		 */
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
        /**
		 * full screen toggle state
		 */
        _fullscreen: false,

        /**
		 * toggle full screen view on button press
		 */
        toggleFullscreen: function (oEvent) {
            var isFullscreen=this._fullscreen;

            try {
	            if (isFullscreen) {
	            	this._fullscreen=false;
	            	document.webkitExitFullscreen();
	            	document.exitFullscreen()
	            } else {
	            	this._fullscreen=true;
	            	document.getElementsByTagName("body")[0].webkitRequestFullscreen();
	            }
            } catch (e) {

            }

        },
        /**
		 * handle menu item press
		 */
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
        /**
		 * trigger a reload of the scheduler table
		 */
        _reloadScheduler: function () {
            var subject = this;
            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/scheduler/refresh", "", "GET", subject._handleSchedulerLoaded, null, subject);
        },
        /**
		 * show a response when the scheduler has been reloaded
		 */
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
