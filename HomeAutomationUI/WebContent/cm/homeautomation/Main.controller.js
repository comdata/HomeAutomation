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
    "sap/ui/model/resource/ResourceModel"
], function (jQuery, Controller, JSONModel, RESTService, Validator,ResourceModel) {
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
        _dialogs: [],
        _cameraRefreshDisabled: false,
        cameraRefreshToggle: function(oEvent) {
        	this._cameraRefreshDisabled=!oEvent.getParameter("state");
        },
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

        	if (!this._cameraRefreshDisabled) {
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
        	}

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
            var i18nModel = new ResourceModel({
                bundleName: "cm.homeautomation.i18n.i18n"
             });
             this.getView().setModel(i18nModel, "i18n")
             sap.ui.getCore().setModel(i18nModel, "i18n");
        	
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
                                   stream: "/HomeAutomation/newCameraProxy?_ip=45&_port=8080&_action=stream"
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
                    tileType: "doorWindow",
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
            this._initMenu();
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
        handleNetworkDevicesLoaded: function(event, model) {
          sap.ui.getCore().setModel(model, "networkDevices");
        },
        handleDoorWindowLoaded: function(event, model) {
          sap.ui.getCore().setModel(model, "doorWindow");
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
        networkDeviceWakeUp: function (event) {
            var networkDevice = sap.ui.getCore().getModel("networkDevices").getProperty(event.getSource().oPropagatedProperties.oBindingContexts.networkDevices.sPath);
            var oModel = new RESTService();
            oModel.loadDataAsync("/HomeAutomation/services/networkdevices/wake/" + networkDevice.mac, "", "GET", null, null, this);
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
        handleTrainSpeedChange:function(event){
          this._trainModelData.speed= event.getSource().getValue();
          this.handleTrainSpeedLightChange("speed");
        },
        handleTrainLightChange:function(event){
          this._trainModelData.light= event.getSource().getValue();
          this.handleTrainSpeedLightChange("light");
        },
        handleTrainSpeedLightChange: function(mode) {
          var train=this._trainModelData.train;
          var speed=this._trainModelData.speed;
          var light=this._trainModelData.light;
          var value=0;

          if (speed==0) {
            speed=8; // brake
          } else if (speed<0) {
            speed=(9+(8+speed));
          }

          if (light==0) {
            light=8; // brake
          } else if (light<0) {
            light=(9+(8+light));
          }

          if (mode=="speed") {
            value=speed;
          } else if (mode=="light") {
            value=light;
          }

          var subject=this;
          var trainRESTModel = new RESTService();
          trainRESTModel.loadDataAsync("/HomeAutomation/services/lego/control/"+mode+"/"+train+"/"+value, "", "GET", null, null, subject);

        },
        trainStop: function(oEvent) {
            var train=this._trainModelData.train;
            var subject=this;
            var trainRESTModel = new RESTService();
            trainRESTModel.loadDataAsync("/HomeAutomation/services/lego/control/speed/"+train+"/8", "", "GET", null, null, subject);

            this._resetTrainModel("speed");
        },
        trainLightOff: function(oEvent) {
            var train=this._trainModelData.train;
            var subject=this;
            var trainRESTModel = new RESTService();
            trainRESTModel.loadDataAsync("/HomeAutomation/services/lego/control/light/"+train+"/8", "", "GET", null, null, subject);

            this._resetTrainModel("light");
        },
        trainEmergencyStop: function(oEvent) {

          var subject=this;
          var trainRESTModel = new RESTService();
          trainRESTModel.loadDataAsync("/HomeAutomation/services/lego/emergencyStop", "", "GET", null, null, subject);
          this._resetTrainModel();

        },
        handleTrainSelected: function(event) {
          this._trainModelData.train=event.getSource().getSelectedKey();
          this._resetTrainModel();
        },
        _resetTrainModel: function(mode) {
          if (mode==undefined) {
            mode=null;
          }

          if (this._trainModel==null) {
            this._trainModelData={train:0, speed:0, light:0};
            this._trainModel = new sap.ui.model.json.JSONModel();
          }
          if (mode==null || mode=="speed") {
            this._trainModelData.speed=0;
          }
          if (mode==null || mode=="light") {
            this._trainModelData.light=0;
          }
          this._trainModel.setData(this._trainModelData);
          sap.ui.getCore().setModel(this._trainModel, "train");
          sap.ui.getCore().getModel("train").refresh(false);
        },
        networkDevicesLoad: function() {
          var subject = this;
          var networkDeviceModel = new RESTService();
          networkDeviceModel.loadDataAsync("/HomeAutomation/services/networkdevices/getAll", "", "GET", subject.handleNetworkDevicesLoaded, null, subject);

        },
        doorWindowLoad: function() {
          var subject = this;
          var doorWindowModel = new RESTService();
          doorWindowModel.loadDataAsync("/HomeAutomation/services/window/readAll", "", "GET", subject.handleDoorWindowLoaded, null, subject);

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

                    this._resetTrainModel();
                    this.trainDialog = sap.ui.xmlfragment("cm.homeautomation.Train", this);
                    this.trainDialog.setModel(this.getView().getModel());
                    jQuery.sap.syncStyleClass("sapUiSizeCompact", this.getView(), this.trainDialog);
                    this.trainDialog.open();

                }
            }
            else if (tileType =="powermeter") {
                if (!this._dialogs["powermeter"]) {
                    this._dialogs["powermeter"] = sap.ui.xmlfragment("cm.homeautomation.PowerMeter", this);
                }
                this._dialogs["powermeter"].open();
                this.powerMeterLoad();
              }            
            else if (tileType =="networkDevices") {
              if (!this._dialogs["networkDevices"]) {
                  this._dialogs["networkDevices"] = sap.ui.xmlfragment("cm.homeautomation.NetworkDevices", this);
              }
              this._dialogs["networkDevices"].open();
              this.networkDevicesLoad();
              // TODO load data
            }
            else if (tileType =="doorWindow") {
              if (!this._dialogs["doorWindow"]) {
                  this._dialogs["doorWindow"] = sap.ui.xmlfragment("cm.homeautomation.DoorWindowDetails", this);
              }
              this._dialogs["doorWindow"].open();
              this.doorWindowLoad();
              // TODO load data
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
        _getRandomInt: function (min, max) {
        	  min = Math.ceil(min);
        	  max = Math.floor(max);
        	  return Math.floor(Math.random() * (max - min)) + min;
        	},
        powerMeterLoad: function () {
        	var c3jsData={
        			  "data1": [30, 200, 100, 400, 150, 250],
        			  "data2": [50, 20, 10, 40, 15, 25]
        			};

			var chartJSData={
        				 "barData": {
        				    
        				    "datasets": [{
        				      "label": "My First dataset",
        				      "fillColor": "rgba(220,220,220,0.5)",
        				      "strokeColor": "rgba(220,220,220,0.8)",
        				      "highlightFill": "rgba(220,220,220,0.75)",
        				      "highlightStroke": "rgba(220,220,220,1)",
        				      "data": [{"kwh":0.0010,"timeslice":1482175800000},{"kwh":0.0010,"timeslice":1482176700000},{"kwh":0.2620,"timeslice":1482177600000},{"kwh":0.1170,"timeslice":1482178500000},{"kwh":0.1210,"timeslice":1482179400000},{"kwh":0.1140,"timeslice":1482180300000},{"kwh":0.1130,"timeslice":1482181200000},{"kwh":0.1080,"timeslice":1482182100000},{"kwh":0.0570,"timeslice":1482183000000},{"kwh":0.0550,"timeslice":1482183900000},{"kwh":0.0480,"timeslice":1482184800000},{"kwh":0.0570,"timeslice":1482185700000},{"kwh":0.0660,"timeslice":1482186600000},{"kwh":0.0520,"timeslice":1482187500000},{"kwh":0.0450,"timeslice":1482188400000},{"kwh":0.0480,"timeslice":1482189300000},{"kwh":0.0640,"timeslice":1482190200000},{"kwh":0.0520,"timeslice":1482191100000},{"kwh":0.0450,"timeslice":1482192000000},{"kwh":0.0460,"timeslice":1482192900000},{"kwh":0.0650,"timeslice":1482193800000},{"kwh":0.0530,"timeslice":1482194700000},{"kwh":0.0450,"timeslice":1482195600000},{"kwh":0.0460,"timeslice":1482196500000},{"kwh":0.0630,"timeslice":1482197400000},{"kwh":0.0550,"timeslice":1482198300000},{"kwh":0.0450,"timeslice":1482199200000},{"kwh":0.0470,"timeslice":1482200100000},{"kwh":0.0610,"timeslice":1482201000000},{"kwh":0.0590,"timeslice":1482201900000},{"kwh":0.0470,"timeslice":1482202800000},{"kwh":0.0460,"timeslice":1482203700000},{"kwh":0.0570,"timeslice":1482204600000},{"kwh":0.0660,"timeslice":1482205500000},{"kwh":0.0590,"timeslice":1482206400000},{"kwh":0.0600,"timeslice":1482207300000},{"kwh":0.0660,"timeslice":1482208200000},{"kwh":0.1020,"timeslice":1482209100000},{"kwh":0.1680,"timeslice":1482210000000},{"kwh":0.4170,"timeslice":1482210900000},{"kwh":0.0920,"timeslice":1482211800000},{"kwh":0.1000,"timeslice":1482212700000},{"kwh":0.0910,"timeslice":1482213600000},{"kwh":0.0810,"timeslice":1482214500000},{"kwh":0.0720,"timeslice":1482215400000},{"kwh":0.0570,"timeslice":1482216300000},{"kwh":0.0700,"timeslice":1482217200000},{"kwh":0.0400,"timeslice":1482218100000},{"kwh":0.0690,"timeslice":1482219000000},{"kwh":0.0470,"timeslice":1482219900000},{"kwh":0.0650,"timeslice":1482220800000},{"kwh":0.0660,"timeslice":1482221700000},{"kwh":0.1540,"timeslice":1482222600000},{"kwh":0.0890,"timeslice":1482223500000},{"kwh":0.1510,"timeslice":1482224400000},{"kwh":0.1670,"timeslice":1482225300000},{"kwh":0.1560,"timeslice":1482226200000},{"kwh":0.1520,"timeslice":1482227100000},{"kwh":0.1470,"timeslice":1482228000000},{"kwh":0.1560,"timeslice":1482228900000},{"kwh":0.1650,"timeslice":1482229800000},{"kwh":0.1520,"timeslice":1482230700000},{"kwh":0.1470,"timeslice":1482231600000},{"kwh":0.1530,"timeslice":1482232500000},{"kwh":0.1680,"timeslice":1482233400000},{"kwh":0.1700,"timeslice":1482234300000},{"kwh":0.1800,"timeslice":1482235200000},{"kwh":0.0080,"timeslice":1482236100000},{"kwh":0.0020,"timeslice":1482237000000},{"kwh":0.0590,"timeslice":1482237900000},{"kwh":0.0400,"timeslice":1482238800000},{"kwh":0.0690,"timeslice":1482239700000},{"kwh":0.0860,"timeslice":1482240600000},{"kwh":0.0570,"timeslice":1482241500000},{"kwh":0.0500,"timeslice":1482242400000},{"kwh":0.0320,"timeslice":1482243300000},{"kwh":0.0380,"timeslice":1482244200000},{"kwh":0.1600,"timeslice":1482245100000},{"kwh":0.0980,"timeslice":1482246000000},{"kwh":0.1000,"timeslice":1482246900000},{"kwh":0.1310,"timeslice":1482247800000},{"kwh":0.0960,"timeslice":1482248700000},{"kwh":0.1030,"timeslice":1482249600000},{"kwh":0.0740,"timeslice":1482250500000},{"kwh":0.0700,"timeslice":1482251400000},{"kwh":0.1020,"timeslice":1482252300000},{"kwh":0.4830,"timeslice":1482253200000},{"kwh":0.5100,"timeslice":1482254100000},{"kwh":0.1890,"timeslice":1482255000000},{"kwh":0.0980,"timeslice":1482255900000},{"kwh":0.1310,"timeslice":1482256800000},{"kwh":0.1150,"timeslice":1482257700000},{"kwh":0.1250,"timeslice":1482258600000},{"kwh":0.0830,"timeslice":1482259500000},{"kwh":0.1080,"timeslice":1482260400000},{"kwh":0.1170,"timeslice":1482261300000},{"kwh":0.0690,"timeslice":1482262200000},{"kwh":0.0680,"timeslice":1482263100000},{"kwh":0.0670,"timeslice":1482264000000},{"kwh":0.0620,"timeslice":1482264900000},{"kwh":0.0470,"timeslice":1482265800000},{"kwh":0.0730,"timeslice":1482266700000},{"kwh":0.0670,"timeslice":1482267600000},{"kwh":0.0590,"timeslice":1482268500000},{"kwh":0.0480,"timeslice":1482269400000},{"kwh":0.0360,"timeslice":1482270300000},{"kwh":0.0570,"timeslice":1482271200000},{"kwh":0.0560,"timeslice":1482272100000},{"kwh":0.0610,"timeslice":1482273000000},{"kwh":0.0430,"timeslice":1482273900000},{"kwh":0.0590,"timeslice":1482274800000},{"kwh":0.0570,"timeslice":1482275700000},{"kwh":0.0470,"timeslice":1482276600000},{"kwh":0.0420,"timeslice":1482277500000},{"kwh":0.0510,"timeslice":1482278400000},{"kwh":0.0600,"timeslice":1482279300000},{"kwh":0.0550,"timeslice":1482280200000},{"kwh":0.0500,"timeslice":1482281100000},{"kwh":0.0560,"timeslice":1482282000000},{"kwh":0.0720,"timeslice":1482282900000},{"kwh":0.0590,"timeslice":1482283800000},{"kwh":0.0530,"timeslice":1482284700000},{"kwh":0.0530,"timeslice":1482285600000},{"kwh":0.0700,"timeslice":1482286500000},{"kwh":0.0620,"timeslice":1482287400000},{"kwh":0.0530,"timeslice":1482288300000},{"kwh":0.0530,"timeslice":1482289200000},{"kwh":0.0650,"timeslice":1482290100000},{"kwh":0.0690,"timeslice":1482291000000},{"kwh":0.0540,"timeslice":1482291900000},{"kwh":0.0620,"timeslice":1482292800000},{"kwh":0.0600,"timeslice":1482293700000},{"kwh":0.0730,"timeslice":1482294600000},{"kwh":0.0570,"timeslice":1482295500000},{"kwh":0.0540,"timeslice":1482296400000},{"kwh":0.0550,"timeslice":1482297300000},{"kwh":0.0670,"timeslice":1482298200000},{"kwh":0.0620,"timeslice":1482299100000},{"kwh":0.0590,"timeslice":1482300000000},{"kwh":0.5340,"timeslice":1482300900000},{"kwh":0.2520,"timeslice":1482301800000},{"kwh":0.4800,"timeslice":1482302700000},{"kwh":0.1540,"timeslice":1482303600000},{"kwh":0.2710,"timeslice":1482304500000},{"kwh":0.5330,"timeslice":1482305400000},{"kwh":0.1590,"timeslice":1482306300000},{"kwh":0.1480,"timeslice":1482307200000},{"kwh":0.1770,"timeslice":1482308100000},{"kwh":0.4350,"timeslice":1482309000000},{"kwh":0.4480,"timeslice":1482309900000},{"kwh":0.1240,"timeslice":1482310800000},{"kwh":0.0670,"timeslice":1482311700000},{"kwh":0.0650,"timeslice":1482312600000},{"kwh":0.0680,"timeslice":1482313500000},{"kwh":0.0810,"timeslice":1482314400000},{"kwh":0.0700,"timeslice":1482315300000},{"kwh":0.0610,"timeslice":1482316200000},{"kwh":0.0610,"timeslice":1482317100000},{"kwh":0.0780,"timeslice":1482318000000},{"kwh":0.0790,"timeslice":1482318900000},{"kwh":0.1520,"timeslice":1482319800000},{"kwh":0.1540,"timeslice":1482320700000},{"kwh":0.1600,"timeslice":1482321600000},{"kwh":0.1610,"timeslice":1482322500000},{"kwh":0.1550,"timeslice":1482323400000},{"kwh":0.1850,"timeslice":1482324300000},{"kwh":0.1740,"timeslice":1482325200000},{"kwh":0.6430,"timeslice":1482326100000},{"kwh":0.1760,"timeslice":1482327000000},{"kwh":0.1420,"timeslice":1482327900000},{"kwh":0.0830,"timeslice":1482328800000},{"kwh":0.1130,"timeslice":1482329700000},{"kwh":0.1820,"timeslice":1482330600000},{"kwh":0.1490,"timeslice":1482331500000},{"kwh":0.0860,"timeslice":1482332400000},{"kwh":0.1590,"timeslice":1482333300000},{"kwh":0.6790,"timeslice":1482334200000},{"kwh":0.2350,"timeslice":1482335100000},{"kwh":0.1170,"timeslice":1482336000000},{"kwh":0.1540,"timeslice":1482336900000},{"kwh":0.1160,"timeslice":1482337800000},{"kwh":0.1160,"timeslice":1482338700000},{"kwh":0.1240,"timeslice":1482339600000},{"kwh":0.6120,"timeslice":1482340500000},{"kwh":0.2230,"timeslice":1482341400000},{"kwh":0.1620,"timeslice":1482342300000},{"kwh":0.1370,"timeslice":1482343200000},{"kwh":0.1500,"timeslice":1482344100000},{"kwh":0.1790,"timeslice":1482345000000},{"kwh":0.1910,"timeslice":1482345900000},{"kwh":0.2070,"timeslice":1482346800000},{"kwh":0.1570,"timeslice":1482347700000},{"kwh":0.1400,"timeslice":1482348600000},{"kwh":0.1050,"timeslice":1482349500000},{"kwh":0.1170,"timeslice":1482350400000},{"kwh":0.1110,"timeslice":1482351300000},{"kwh":0.1000,"timeslice":1482352200000},{"kwh":0.0790,"timeslice":1482353100000},{"kwh":0.1200,"timeslice":1482354000000},{"kwh":0.1470,"timeslice":1482354900000},{"kwh":0.1250,"timeslice":1482355800000},{"kwh":0.1070,"timeslice":1482356700000},{"kwh":0.1220,"timeslice":1482357600000},{"kwh":0.0970,"timeslice":1482358500000},{"kwh":0.0680,"timeslice":1482359400000},{"kwh":0.0520,"timeslice":1482360300000},{"kwh":0.0440,"timeslice":1482361200000},{"kwh":0.0460,"timeslice":1482362100000},{"kwh":0.0480,"timeslice":1482363000000},{"kwh":0.0500,"timeslice":1482363900000},{"kwh":0.0430,"timeslice":1482364800000},{"kwh":0.0400,"timeslice":1482365700000},{"kwh":0.0520,"timeslice":1482366600000},{"kwh":0.0560,"timeslice":1482367500000},{"kwh":0.0580,"timeslice":1482368400000},{"kwh":0.0490,"timeslice":1482369300000},{"kwh":0.0530,"timeslice":1482370200000},{"kwh":0.0590,"timeslice":1482371100000},{"kwh":0.0500,"timeslice":1482372000000},{"kwh":0.0490,"timeslice":1482372900000},{"kwh":0.0510,"timeslice":1482373800000},{"kwh":0.0570,"timeslice":1482374700000},{"kwh":0.0600,"timeslice":1482375600000},{"kwh":0.0620,"timeslice":1482376500000},{"kwh":0.0510,"timeslice":1482377400000},{"kwh":0.0560,"timeslice":1482378300000},{"kwh":0.0680,"timeslice":1482379200000},{"kwh":0.0640,"timeslice":1482380100000},{"kwh":0.0500,"timeslice":1482381000000},{"kwh":0.0530,"timeslice":1482381900000},{"kwh":0.0600,"timeslice":1482382800000},{"kwh":0.0650,"timeslice":1482383700000},{"kwh":0.0530,"timeslice":1482384600000},{"kwh":0.0530,"timeslice":1482385500000},{"kwh":0.0650,"timeslice":1482386400000},{"kwh":0.0760,"timeslice":1482387300000},{"kwh":0.0770,"timeslice":1482388200000},{"kwh":0.1310,"timeslice":1482389100000},{"kwh":0.1180,"timeslice":1482390000000},{"kwh":0.1190,"timeslice":1482390900000},{"kwh":0.5430,"timeslice":1482391800000},{"kwh":0.2310,"timeslice":1482392700000},{"kwh":0.0710,"timeslice":1482393600000},{"kwh":0.0790,"timeslice":1482394500000},{"kwh":0.0920,"timeslice":1482395400000},{"kwh":0.0960,"timeslice":1482396300000},{"kwh":0.1270,"timeslice":1482397200000},{"kwh":0.0810,"timeslice":1482398100000},{"kwh":0.3740,"timeslice":1482399000000},{"kwh":0.3300,"timeslice":1482399900000},{"kwh":0.0830,"timeslice":1482400800000},{"kwh":0.0810,"timeslice":1482401700000},{"kwh":0.0920,"timeslice":1482402600000},{"kwh":0.1120,"timeslice":1482403500000},{"kwh":0.0670,"timeslice":1482404400000},{"kwh":0.0660,"timeslice":1482405300000},{"kwh":0.0630,"timeslice":1482406200000},{"kwh":0.0650,"timeslice":1482407100000},{"kwh":0.0580,"timeslice":1482408000000},{"kwh":0.0610,"timeslice":1482408900000},{"kwh":0.1190,"timeslice":1482409800000},{"kwh":0.1610,"timeslice":1482410700000},{"kwh":0.1490,"timeslice":1482411600000},{"kwh":0.2230,"timeslice":1482412500000},{"kwh":0.3660,"timeslice":1482413400000},{"kwh":0.2270,"timeslice":1482414300000},{"kwh":0.1890,"timeslice":1482415200000},{"kwh":0.1360,"timeslice":1482416100000},{"kwh":0.1230,"timeslice":1482417000000},{"kwh":0.1220,"timeslice":1482417900000},{"kwh":0.1930,"timeslice":1482418800000},{"kwh":0.6490,"timeslice":1482419700000},{"kwh":0.6540,"timeslice":1482420600000},{"kwh":0.2200,"timeslice":1482421500000},{"kwh":0.1370,"timeslice":1482422400000},{"kwh":0.1040,"timeslice":1482423300000},{"kwh":0.6070,"timeslice":1482424200000},{"kwh":0.6080,"timeslice":1482425100000},{"kwh":0.6080,"timeslice":1482426000000},{"kwh":0.6200,"timeslice":1482426900000},{"kwh":0.7140,"timeslice":1482427800000},{"kwh":0.8000,"timeslice":1482428700000},{"kwh":0.2380,"timeslice":1482429600000},{"kwh":0.0910,"timeslice":1482430500000},{"kwh":0.1500,"timeslice":1482431400000},{"kwh":0.5600,"timeslice":1482432300000},{"kwh":0.1540,"timeslice":1482433200000},{"kwh":0.1230,"timeslice":1482434100000},{"kwh":0.0960,"timeslice":1482435000000},{"kwh":0.0820,"timeslice":1482435900000},{"kwh":0.0890,"timeslice":1482436800000},{"kwh":0.1030,"timeslice":1482437700000},{"kwh":0.4990,"timeslice":1482438600000},{"kwh":0.2070,"timeslice":1482439500000},{"kwh":0.1110,"timeslice":1482440400000},{"kwh":0.0790,"timeslice":1482441300000},{"kwh":0.0830,"timeslice":1482442200000},{"kwh":0.0590,"timeslice":1482443100000},{"kwh":0.0630,"timeslice":1482444000000},{"kwh":0.0720,"timeslice":1482444900000},{"kwh":0.0880,"timeslice":1482445800000},{"kwh":0.0720,"timeslice":1482446700000},{"kwh":0.0620,"timeslice":1482447600000},{"kwh":0.0680,"timeslice":1482448500000},{"kwh":0.0710,"timeslice":1482449400000},{"kwh":0.0500,"timeslice":1482450300000},{"kwh":0.0430,"timeslice":1482451200000},{"kwh":0.0470,"timeslice":1482452100000},{"kwh":0.0620,"timeslice":1482453000000},{"kwh":0.0480,"timeslice":1482453900000},{"kwh":0.0410,"timeslice":1482454800000},{"kwh":0.0420,"timeslice":1482455700000},{"kwh":0.0580,"timeslice":1482456600000},{"kwh":0.0540,"timeslice":1482457500000},{"kwh":0.0420,"timeslice":1482458400000},{"kwh":0.0420,"timeslice":1482459300000},{"kwh":0.0500,"timeslice":1482460200000},{"kwh":0.0590,"timeslice":1482461100000},{"kwh":0.0430,"timeslice":1482462000000},{"kwh":0.0430,"timeslice":1482462900000},{"kwh":0.0480,"timeslice":1482463800000},{"kwh":0.0620,"timeslice":1482464700000},{"kwh":0.0580,"timeslice":1482465600000},{"kwh":0.0550,"timeslice":1482466500000},{"kwh":0.0570,"timeslice":1482467400000},{"kwh":0.0640,"timeslice":1482468300000},{"kwh":0.0600,"timeslice":1482469200000},{"kwh":0.0510,"timeslice":1482470100000},{"kwh":0.0560,"timeslice":1482471000000},{"kwh":0.0600,"timeslice":1482471900000},{"kwh":0.0640,"timeslice":1482472800000},{"kwh":0.0540,"timeslice":1482473700000},{"kwh":0.0530,"timeslice":1482474600000},{"kwh":0.0670,"timeslice":1482475500000},{"kwh":0.0650,"timeslice":1482476400000},{"kwh":0.0700,"timeslice":1482477300000},{"kwh":0.5170,"timeslice":1482478200000},{"kwh":0.1880,"timeslice":1482479100000},{"kwh":0.0670,"timeslice":1482480000000},{"kwh":0.0750,"timeslice":1482480900000},{"kwh":0.0590,"timeslice":1482481800000},{"kwh":0.0660,"timeslice":1482482700000},{"kwh":0.0660,"timeslice":1482483600000},{"kwh":0.0950,"timeslice":1482484500000},{"kwh":0.0600,"timeslice":1482485400000},{"kwh":0.0620,"timeslice":1482486300000},{"kwh":0.0570,"timeslice":1482487200000},{"kwh":0.0550,"timeslice":1482488100000},{"kwh":0.0640,"timeslice":1482489000000},{"kwh":0.0630,"timeslice":1482489900000},{"kwh":0.0610,"timeslice":1482490800000},{"kwh":0.0550,"timeslice":1482491700000},{"kwh":0.0670,"timeslice":1482492600000},{"kwh":0.0650,"timeslice":1482493500000},{"kwh":0.0650,"timeslice":1482494400000},{"kwh":0.0590,"timeslice":1482495300000},{"kwh":0.1320,"timeslice":1482496200000},{"kwh":0.1440,"timeslice":1482497100000},{"kwh":0.1460,"timeslice":1482498000000},{"kwh":0.1410,"timeslice":1482498900000},{"kwh":0.1820,"timeslice":1482499800000},{"kwh":0.6790,"timeslice":1482500700000},{"kwh":0.7070,"timeslice":1482501600000},{"kwh":0.2380,"timeslice":1482502500000},{"kwh":0.2180,"timeslice":1482503400000},{"kwh":0.1790,"timeslice":1482504300000},{"kwh":0.1480,"timeslice":1482505200000},{"kwh":0.3670,"timeslice":1482506100000},{"kwh":0.7790,"timeslice":1482507000000},{"kwh":0.7520,"timeslice":1482507900000},{"kwh":0.7230,"timeslice":1482508800000},{"kwh":0.7190,"timeslice":1482509700000},{"kwh":0.4420,"timeslice":1482510600000},{"kwh":0.2900,"timeslice":1482511500000},{"kwh":0.4510,"timeslice":1482512400000},{"kwh":0.6510,"timeslice":1482513300000},{"kwh":0.5450,"timeslice":1482514200000},{"kwh":0.1120,"timeslice":1482515100000},{"kwh":0.0930,"timeslice":1482516000000},{"kwh":0.1550,"timeslice":1482516900000},{"kwh":0.1960,"timeslice":1482517800000},{"kwh":0.1210,"timeslice":1482518700000},{"kwh":0.1070,"timeslice":1482519600000},{"kwh":0.1220,"timeslice":1482520500000},{"kwh":0.1330,"timeslice":1482521400000},{"kwh":0.1180,"timeslice":1482522300000},{"kwh":0.0900,"timeslice":1482523200000},{"kwh":0.0830,"timeslice":1482524100000},{"kwh":0.1030,"timeslice":1482525000000},{"kwh":0.0680,"timeslice":1482525900000},{"kwh":0.0580,"timeslice":1482526800000},{"kwh":0.0580,"timeslice":1482527700000},{"kwh":0.0640,"timeslice":1482528600000},{"kwh":0.0890,"timeslice":1482529500000},{"kwh":0.0700,"timeslice":1482530400000},{"kwh":0.0680,"timeslice":1482531300000},{"kwh":0.0630,"timeslice":1482532200000},{"kwh":0.0550,"timeslice":1482533100000},{"kwh":0.0560,"timeslice":1482534000000},{"kwh":0.0460,"timeslice":1482534900000},{"kwh":0.0460,"timeslice":1482535800000},{"kwh":0.0520,"timeslice":1482536700000},{"kwh":0.0600,"timeslice":1482537600000},{"kwh":0.0440,"timeslice":1482538500000},{"kwh":0.0510,"timeslice":1482539400000},{"kwh":0.0510,"timeslice":1482540300000},{"kwh":0.0520,"timeslice":1482541200000},{"kwh":0.0550,"timeslice":1482542100000},{"kwh":0.0510,"timeslice":1482543000000},{"kwh":0.0460,"timeslice":1482543900000},{"kwh":0.0540,"timeslice":1482544800000},{"kwh":0.0610,"timeslice":1482545700000},{"kwh":0.0460,"timeslice":1482546600000},{"kwh":0.0500,"timeslice":1482547500000},{"kwh":0.0540,"timeslice":1482548400000},{"kwh":0.0550,"timeslice":1482549300000},{"kwh":0.0510,"timeslice":1482550200000},{"kwh":0.0540,"timeslice":1482551100000},{"kwh":0.0470,"timeslice":1482552000000},{"kwh":0.0520,"timeslice":1482552900000},{"kwh":0.0630,"timeslice":1482553800000},{"kwh":0.0670,"timeslice":1482554700000},{"kwh":0.0790,"timeslice":1482555600000},{"kwh":0.0600,"timeslice":1482556500000},{"kwh":0.0850,"timeslice":1482557400000},{"kwh":0.0750,"timeslice":1482558300000},{"kwh":0.0610,"timeslice":1482559200000},{"kwh":0.0550,"timeslice":1482560100000},{"kwh":0.0580,"timeslice":1482561000000},{"kwh":0.2960,"timeslice":1482561900000},{"kwh":0.5350,"timeslice":1482562800000},{"kwh":0.0710,"timeslice":1482563700000},{"kwh":0.0800,"timeslice":1482564600000},{"kwh":0.1110,"timeslice":1482565500000},{"kwh":0.4380,"timeslice":1482566400000},{"kwh":0.1330,"timeslice":1482567300000},{"kwh":0.1280,"timeslice":1482568200000},{"kwh":0.2620,"timeslice":1482569100000},{"kwh":0.1240,"timeslice":1482570000000},{"kwh":0.0790,"timeslice":1482570900000},{"kwh":0.0850,"timeslice":1482571800000},{"kwh":0.4960,"timeslice":1482572700000},{"kwh":0.1000,"timeslice":1482573600000},{"kwh":0.0550,"timeslice":1482574500000},{"kwh":0.0760,"timeslice":1482575400000},{"kwh":0.0630,"timeslice":1482576300000},{"kwh":0.0540,"timeslice":1482577200000},{"kwh":0.0540,"timeslice":1482578100000},{"kwh":0.0660,"timeslice":1482579000000},{"kwh":0.0670,"timeslice":1482579900000},{"kwh":0.0510,"timeslice":1482580800000},{"kwh":0.0510,"timeslice":1482581700000},{"kwh":0.0590,"timeslice":1482582600000},{"kwh":0.0700,"timeslice":1482583500000},{"kwh":0.0520,"timeslice":1482584400000},{"kwh":0.0520,"timeslice":1482585300000},{"kwh":0.0590,"timeslice":1482586200000},{"kwh":0.0640,"timeslice":1482587100000},{"kwh":0.0620,"timeslice":1482588000000},{"kwh":0.0510,"timeslice":1482588900000},{"kwh":0.0590,"timeslice":1482589800000},{"kwh":0.0590,"timeslice":1482590700000},{"kwh":0.0640,"timeslice":1482591600000},{"kwh":0.0540,"timeslice":1482592500000},{"kwh":0.0560,"timeslice":1482593400000},{"kwh":0.0610,"timeslice":1482594300000},{"kwh":0.0670,"timeslice":1482595200000},{"kwh":0.0660,"timeslice":1482596100000},{"kwh":0.0540,"timeslice":1482597000000},{"kwh":0.0590,"timeslice":1482597900000},{"kwh":0.0570,"timeslice":1482598800000},{"kwh":0.0630,"timeslice":1482599700000},{"kwh":0.0550,"timeslice":1482600600000},{"kwh":0.0620,"timeslice":1482601500000},{"kwh":0.0640,"timeslice":1482602400000},{"kwh":0.0550,"timeslice":1482603300000},{"kwh":0.0660,"timeslice":1482604200000},{"kwh":0.0550,"timeslice":1482605100000},{"kwh":0.0600,"timeslice":1482606000000},{"kwh":0.0540,"timeslice":1482606900000},{"kwh":0.0600,"timeslice":1482607800000},{"kwh":0.0560,"timeslice":1482608700000},{"kwh":0.0600,"timeslice":1482609600000},{"kwh":0.0590,"timeslice":1482610500000},{"kwh":0.0540,"timeslice":1482611400000},{"kwh":0.0620,"timeslice":1482612300000},{"kwh":0.0710,"timeslice":1482613200000},{"kwh":0.0620,"timeslice":1482614100000},{"kwh":0.0560,"timeslice":1482615000000},{"kwh":0.0560,"timeslice":1482615900000},{"kwh":0.0600,"timeslice":1482616800000},{"kwh":0.0600,"timeslice":1482617700000},{"kwh":0.0500,"timeslice":1482618600000},{"kwh":0.0830,"timeslice":1482619500000},{"kwh":0.0650,"timeslice":1482620400000},{"kwh":0.0560,"timeslice":1482621300000},{"kwh":0.0590,"timeslice":1482622200000},{"kwh":0.0570,"timeslice":1482623100000},{"kwh":0.0470,"timeslice":1482624000000},{"kwh":0.0490,"timeslice":1482624900000},{"kwh":0.0430,"timeslice":1482625800000},{"kwh":0.0520,"timeslice":1482626700000},{"kwh":0.0510,"timeslice":1482627600000},{"kwh":0.0430,"timeslice":1482628500000},{"kwh":0.0460,"timeslice":1482629400000},{"kwh":0.0470,"timeslice":1482630300000},{"kwh":0.0660,"timeslice":1482631200000},{"kwh":0.0520,"timeslice":1482632100000},{"kwh":0.0440,"timeslice":1482633000000},{"kwh":0.0460,"timeslice":1482633900000},{"kwh":0.0600,"timeslice":1482634800000},{"kwh":0.0580,"timeslice":1482635700000},{"kwh":0.0460,"timeslice":1482636600000},{"kwh":0.0460,"timeslice":1482637500000},{"kwh":0.0510,"timeslice":1482638400000},{"kwh":0.0650,"timeslice":1482639300000},{"kwh":0.0470,"timeslice":1482640200000},{"kwh":0.0470,"timeslice":1482641100000},{"kwh":0.0500,"timeslice":1482642000000},{"kwh":0.0690,"timeslice":1482642900000},{"kwh":0.0720,"timeslice":1482643800000},{"kwh":0.0570,"timeslice":1482644700000},{"kwh":0.0560,"timeslice":1482645600000},{"kwh":0.0610,"timeslice":1482646500000},{"kwh":0.0710,"timeslice":1482647400000},{"kwh":0.0550,"timeslice":1482648300000},{"kwh":0.0530,"timeslice":1482649200000},{"kwh":0.0570,"timeslice":1482650100000},{"kwh":0.0660,"timeslice":1482651000000},{"kwh":0.0650,"timeslice":1482651900000},{"kwh":0.0510,"timeslice":1482652800000},{"kwh":0.0550,"timeslice":1482653700000},{"kwh":0.0610,"timeslice":1482654600000},{"kwh":0.0660,"timeslice":1482655500000},{"kwh":0.0560,"timeslice":1482656400000},{"kwh":0.0550,"timeslice":1482657300000},{"kwh":0.0580,"timeslice":1482658200000},{"kwh":0.0630,"timeslice":1482659100000},{"kwh":0.0640,"timeslice":1482660000000},{"kwh":0.0580,"timeslice":1482660900000},{"kwh":0.0580,"timeslice":1482661800000},{"kwh":0.0620,"timeslice":1482662700000},{"kwh":0.0630,"timeslice":1482663600000},{"kwh":0.0600,"timeslice":1482664500000},{"kwh":0.0530,"timeslice":1482665400000},{"kwh":0.0600,"timeslice":1482666300000},{"kwh":0.0600,"timeslice":1482667200000},{"kwh":0.0650,"timeslice":1482668100000},{"kwh":0.0540,"timeslice":1482669000000},{"kwh":0.0570,"timeslice":1482669900000},{"kwh":0.0630,"timeslice":1482670800000},{"kwh":0.0960,"timeslice":1482671700000},{"kwh":0.0780,"timeslice":1482672600000},{"kwh":0.0610,"timeslice":1482673500000},{"kwh":0.0640,"timeslice":1482674400000},{"kwh":0.1060,"timeslice":1482675300000},{"kwh":0.1550,"timeslice":1482676200000},{"kwh":0.1130,"timeslice":1482677100000},{"kwh":0.0730,"timeslice":1482678000000},{"kwh":0.0770,"timeslice":1482678900000},{"kwh":0.0760,"timeslice":1482679800000},{"kwh":0.0720,"timeslice":1482680700000},{"kwh":0.0890,"timeslice":1482681600000},{"kwh":0.0940,"timeslice":1482682500000},{"kwh":0.0790,"timeslice":1482683400000},{"kwh":0.0790,"timeslice":1482684300000},{"kwh":0.0720,"timeslice":1482685200000},{"kwh":0.0750,"timeslice":1482686100000},{"kwh":0.1020,"timeslice":1482687000000},{"kwh":0.3540,"timeslice":1482687900000},{"kwh":0.3980,"timeslice":1482688800000},{"kwh":0.2010,"timeslice":1482689700000},{"kwh":0.1230,"timeslice":1482690600000},{"kwh":0.1270,"timeslice":1482691500000},{"kwh":0.1140,"timeslice":1482692400000},{"kwh":0.1050,"timeslice":1482693300000},{"kwh":0.1050,"timeslice":1482694200000},{"kwh":0.0980,"timeslice":1482695100000},{"kwh":0.1160,"timeslice":1482696000000},{"kwh":0.0700,"timeslice":1482696900000},{"kwh":0.1360,"timeslice":1482697800000},{"kwh":0.1540,"timeslice":1482698700000},{"kwh":0.1330,"timeslice":1482699600000},{"kwh":0.1090,"timeslice":1482700500000},{"kwh":0.1060,"timeslice":1482701400000},{"kwh":0.0760,"timeslice":1482702300000},{"kwh":0.0630,"timeslice":1482703200000},{"kwh":0.0530,"timeslice":1482704100000},{"kwh":0.0470,"timeslice":1482705000000},{"kwh":0.0510,"timeslice":1482705900000},{"kwh":0.0560,"timeslice":1482706800000},{"kwh":0.0500,"timeslice":1482707700000},{"kwh":0.0420,"timeslice":1482708600000},{"kwh":0.0490,"timeslice":1482709500000},{"kwh":0.0590,"timeslice":1482710400000},{"kwh":0.0500,"timeslice":1482711300000},{"kwh":0.0450,"timeslice":1482712200000},{"kwh":0.0430,"timeslice":1482713100000},{"kwh":0.0570,"timeslice":1482714000000},{"kwh":0.0530,"timeslice":1482714900000},{"kwh":0.0490,"timeslice":1482715800000},{"kwh":0.0440,"timeslice":1482716700000},{"kwh":0.0510,"timeslice":1482717600000},{"kwh":0.0590,"timeslice":1482718500000},{"kwh":0.0510,"timeslice":1482719400000},{"kwh":0.0460,"timeslice":1482720300000},{"kwh":0.0450,"timeslice":1482721200000},{"kwh":0.0600,"timeslice":1482722100000},{"kwh":0.0540,"timeslice":1482723000000},{"kwh":0.0480,"timeslice":1482723900000},{"kwh":0.0580,"timeslice":1482724800000},{"kwh":0.0630,"timeslice":1482725700000},{"kwh":0.0710,"timeslice":1482726600000},{"kwh":0.0570,"timeslice":1482727500000},{"kwh":0.0540,"timeslice":1482728400000},{"kwh":0.0530,"timeslice":1482729300000},{"kwh":0.0710,"timeslice":1482730200000},{"kwh":0.0620,"timeslice":1482731100000},{"kwh":0.0540,"timeslice":1482732000000},{"kwh":0.0540,"timeslice":1482732900000},{"kwh":0.0620,"timeslice":1482733800000},{"kwh":0.0700,"timeslice":1482734700000},{"kwh":0.0650,"timeslice":1482735600000},{"kwh":0.2600,"timeslice":1482736500000},{"kwh":0.3510,"timeslice":1482737400000},{"kwh":0.3370,"timeslice":1482738300000},{"kwh":0.1590,"timeslice":1482739200000},{"kwh":0.0810,"timeslice":1482740100000},{"kwh":0.1490,"timeslice":1482741000000},{"kwh":0.1820,"timeslice":1482741900000},{"kwh":0.1710,"timeslice":1482742800000},{"kwh":0.1420,"timeslice":1482743700000},{"kwh":0.0920,"timeslice":1482744600000},{"kwh":0.1110,"timeslice":1482745500000},{"kwh":0.2880,"timeslice":1482746400000},{"kwh":0.3100,"timeslice":1482747300000},{"kwh":0.3040,"timeslice":1482748200000},{"kwh":0.2940,"timeslice":1482749100000},{"kwh":0.2930,"timeslice":1482750000000},{"kwh":0.2930,"timeslice":1482750900000},{"kwh":0.2930,"timeslice":1482751800000},{"kwh":0.2870,"timeslice":1482752700000},{"kwh":0.2870,"timeslice":1482753600000},{"kwh":0.2910,"timeslice":1482754500000},{"kwh":0.2140,"timeslice":1482755400000},{"kwh":0.1680,"timeslice":1482756300000},{"kwh":0.1600,"timeslice":1482757200000},{"kwh":0.2530,"timeslice":1482758100000},{"kwh":0.2900,"timeslice":1482759000000},{"kwh":0.2890,"timeslice":1482759900000},{"kwh":0.2880,"timeslice":1482760800000},{"kwh":0.2900,"timeslice":1482761700000},{"kwh":0.2890,"timeslice":1482762600000},{"kwh":0.1730,"timeslice":1482763500000},{"kwh":0.1720,"timeslice":1482764400000},{"kwh":0.2910,"timeslice":1482765300000},{"kwh":0.2900,"timeslice":1482766200000},{"kwh":0.2310,"timeslice":1482767100000},{"kwh":0.1560,"timeslice":1482768000000},{"kwh":0.1520,"timeslice":1482768900000},{"kwh":0.0960,"timeslice":1482769800000},{"kwh":0.1040,"timeslice":1482770700000},{"kwh":0.2230,"timeslice":1482771600000},{"kwh":0.2210,"timeslice":1482772500000},{"kwh":0.2430,"timeslice":1482773400000},{"kwh":0.1790,"timeslice":1482774300000},{"kwh":0.2970,"timeslice":1482775200000},{"kwh":0.2940,"timeslice":1482776100000},{"kwh":0.2940,"timeslice":1482777000000},{"kwh":0.2980,"timeslice":1482777900000},{"kwh":0.2990,"timeslice":1482778800000},{"kwh":0.1950,"timeslice":1482779700000},{"kwh":0.1500,"timeslice":1482780600000},{"kwh":0.1290,"timeslice":1482781500000},{"kwh":0.2070,"timeslice":1482782400000},{"kwh":0.1370,"timeslice":1482783300000},{"kwh":0.1050,"timeslice":1482784200000},{"kwh":0.0980,"timeslice":1482785100000},{"kwh":0.1060,"timeslice":1482786000000},{"kwh":0.1130,"timeslice":1482786900000},{"kwh":0.1120,"timeslice":1482787800000},{"kwh":0.0790,"timeslice":1482788700000},{"kwh":0.0870,"timeslice":1482789600000},{"kwh":0.0970,"timeslice":1482790500000},{"kwh":0.0940,"timeslice":1482791400000},{"kwh":0.0390,"timeslice":1482792300000},{"kwh":0.0400,"timeslice":1482793200000},{"kwh":0.0460,"timeslice":1482794100000},{"kwh":0.0610,"timeslice":1482795000000},{"kwh":0.0480,"timeslice":1482795900000},{"kwh":0.0400,"timeslice":1482796800000},{"kwh":0.0430,"timeslice":1482797700000},{"kwh":0.0530,"timeslice":1482798600000},{"kwh":0.0550,"timeslice":1482799500000},{"kwh":0.0400,"timeslice":1482800400000},{"kwh":0.0420,"timeslice":1482801300000},{"kwh":0.0480,"timeslice":1482802200000},{"kwh":0.0620,"timeslice":1482803100000},{"kwh":0.0440,"timeslice":1482804000000},{"kwh":0.0450,"timeslice":1482804900000},{"kwh":0.0570,"timeslice":1482805800000},{"kwh":0.0550,"timeslice":1482806700000},{"kwh":0.0480,"timeslice":1482807600000},{"kwh":0.0420,"timeslice":1482808500000},{"kwh":0.0470,"timeslice":1482809400000},{"kwh":0.0480,"timeslice":1482810300000},{"kwh":0.0710,"timeslice":1482811200000},{"kwh":0.0560,"timeslice":1482812100000},{"kwh":0.0520,"timeslice":1482813000000},{"kwh":0.0580,"timeslice":1482813900000},{"kwh":0.0580,"timeslice":1482814800000},{"kwh":0.0580,"timeslice":1482815700000},{"kwh":0.0540,"timeslice":1482816600000},{"kwh":0.0590,"timeslice":1482817500000},{"kwh":0.0540,"timeslice":1482818400000},{"kwh":0.0510,"timeslice":1482819300000},{"kwh":0.0480,"timeslice":1482820200000},{"kwh":0.0570,"timeslice":1482821100000},{"kwh":0.0560,"timeslice":1482822000000},{"kwh":0.0690,"timeslice":1482822900000},{"kwh":0.2550,"timeslice":1482823800000},{"kwh":0.2550,"timeslice":1482824700000},{"kwh":0.2490,"timeslice":1482825600000},{"kwh":0.1600,"timeslice":1482826500000},{"kwh":0.2490,"timeslice":1482827400000},{"kwh":0.2480,"timeslice":1482828300000},{"kwh":0.2430,"timeslice":1482829200000},{"kwh":0.2320,"timeslice":1482830100000},{"kwh":0.2320,"timeslice":1482831000000},{"kwh":0.2360,"timeslice":1482831900000},{"kwh":0.2270,"timeslice":1482832800000},{"kwh":0.2210,"timeslice":1482833700000},{"kwh":0.2190,"timeslice":1482834600000},{"kwh":0.2160,"timeslice":1482835500000},{"kwh":0.2200,"timeslice":1482836400000},{"kwh":0.2200,"timeslice":1482837300000},{"kwh":0.2160,"timeslice":1482838200000},{"kwh":0.2260,"timeslice":1482839100000},{"kwh":0.2300,"timeslice":1482840000000},{"kwh":0.2350,"timeslice":1482840900000},{"kwh":0.1210,"timeslice":1482841800000},{"kwh":0.0700,"timeslice":1482842700000},{"kwh":0.0900,"timeslice":1482843600000},{"kwh":0.0780,"timeslice":1482844500000},{"kwh":0.0550,"timeslice":1482845400000},{"kwh":0.0510,"timeslice":1482846300000},{"kwh":0.0680,"timeslice":1482847200000},{"kwh":0.0600,"timeslice":1482848100000},{"kwh":0.0500,"timeslice":1482849000000},{"kwh":0.0500,"timeslice":1482849900000},{"kwh":0.0620,"timeslice":1482850800000},{"kwh":0.0640,"timeslice":1482851700000},{"kwh":0.0520,"timeslice":1482852600000},{"kwh":0.0490,"timeslice":1482853500000},{"kwh":0.0630,"timeslice":1482854400000},{"kwh":0.1680,"timeslice":1482855300000},{"kwh":0.2240,"timeslice":1482856200000},{"kwh":0.2190,"timeslice":1482857100000},{"kwh":0.2150,"timeslice":1482858000000},{"kwh":0.2080,"timeslice":1482858900000},{"kwh":0.2000,"timeslice":1482859800000},{"kwh":0.2000,"timeslice":1482860700000},{"kwh":0.2200,"timeslice":1482861600000},{"kwh":0.2420,"timeslice":1482862500000},{"kwh":0.2470,"timeslice":1482863400000},{"kwh":0.2450,"timeslice":1482864300000},{"kwh":0.2380,"timeslice":1482865200000},{"kwh":0.1520,"timeslice":1482866100000},{"kwh":0.2980,"timeslice":1482867000000},{"kwh":0.1770,"timeslice":1482867900000},{"kwh":0.1030,"timeslice":1482868800000},{"kwh":0.0950,"timeslice":1482869700000},{"kwh":0.1070,"timeslice":1482870600000},{"kwh":0.1150,"timeslice":1482871500000},{"kwh":0.1300,"timeslice":1482872400000},{"kwh":0.0830,"timeslice":1482873300000},{"kwh":0.1000,"timeslice":1482874200000},{"kwh":0.0880,"timeslice":1482875100000},{"kwh":0.0820,"timeslice":1482876000000},{"kwh":0.0720,"timeslice":1482876900000},{"kwh":0.0570,"timeslice":1482877800000},{"kwh":0.0540,"timeslice":1482878700000},{"kwh":0.0430,"timeslice":1482879600000},{"kwh":0.0440,"timeslice":1482880500000},{"kwh":0.0490,"timeslice":1482881400000},{"kwh":0.0480,"timeslice":1482882300000},{"kwh":0.0430,"timeslice":1482883200000},{"kwh":0.0400,"timeslice":1482884100000},{"kwh":0.0460,"timeslice":1482885000000},{"kwh":0.0480,"timeslice":1482885900000},{"kwh":0.0590,"timeslice":1482886800000},{"kwh":0.0470,"timeslice":1482887700000},{"kwh":0.0430,"timeslice":1482888600000},{"kwh":0.0490,"timeslice":1482889500000},{"kwh":0.0590,"timeslice":1482890400000},{"kwh":0.0430,"timeslice":1482891300000},{"kwh":0.0410,"timeslice":1482892200000},{"kwh":0.0490,"timeslice":1482893100000},{"kwh":0.0610,"timeslice":1482894000000},{"kwh":0.0480,"timeslice":1482894900000},{"kwh":0.0420,"timeslice":1482895800000},{"kwh":0.0480,"timeslice":1482896700000},{"kwh":0.0690,"timeslice":1482897600000},{"kwh":0.0620,"timeslice":1482898500000},{"kwh":0.0490,"timeslice":1482899400000},{"kwh":0.0550,"timeslice":1482900300000},{"kwh":0.0630,"timeslice":1482901200000},{"kwh":0.0650,"timeslice":1482902100000},{"kwh":0.0500,"timeslice":1482903000000},{"kwh":0.0520,"timeslice":1482903900000},{"kwh":0.0570,"timeslice":1482904800000},{"kwh":0.0690,"timeslice":1482905700000},{"kwh":0.0490,"timeslice":1482906600000},{"kwh":0.0490,"timeslice":1482907500000},{"kwh":0.0570,"timeslice":1482908400000},{"kwh":0.0700,"timeslice":1482909300000},{"kwh":0.5120,"timeslice":1482910200000},{"kwh":0.3310,"timeslice":1482911100000},{"kwh":0.1020,"timeslice":1482912000000},{"kwh":0.0690,"timeslice":1482912900000},{"kwh":0.0830,"timeslice":1482913800000},{"kwh":0.0860,"timeslice":1482914700000},{"kwh":0.0720,"timeslice":1482915600000},{"kwh":0.0600,"timeslice":1482916500000},{"kwh":0.0670,"timeslice":1482917400000},{"kwh":0.0590,"timeslice":1482918300000},{"kwh":0.0630,"timeslice":1482919200000},{"kwh":0.0620,"timeslice":1482920100000},{"kwh":0.0650,"timeslice":1482921000000},{"kwh":0.0410,"timeslice":1482921900000},{"kwh":0.2380,"timeslice":1482922800000},{"kwh":0.5390,"timeslice":1482923700000},{"kwh":0.1070,"timeslice":1482924600000},{"kwh":0.0560,"timeslice":1482925500000},{"kwh":0.1470,"timeslice":1482926400000},{"kwh":0.1640,"timeslice":1482927300000},{"kwh":0.1660,"timeslice":1482928200000},{"kwh":0.1560,"timeslice":1482929100000},{"kwh":0.1220,"timeslice":1482930000000},{"kwh":0.1290,"timeslice":1482930900000},{"kwh":0.1220,"timeslice":1482931800000},{"kwh":0.1160,"timeslice":1482932700000},{"kwh":0.1890,"timeslice":1482933600000},{"kwh":0.1150,"timeslice":1482934500000},{"kwh":0.1360,"timeslice":1482935400000},{"kwh":0.1400,"timeslice":1482936300000},{"kwh":0.1160,"timeslice":1482937200000},{"kwh":0.0820,"timeslice":1482938100000},{"kwh":0.0610,"timeslice":1482939000000},{"kwh":0.0580,"timeslice":1482939900000},{"kwh":0.0740,"timeslice":1482940800000},{"kwh":0.1070,"timeslice":1482941700000},{"kwh":0.1630,"timeslice":1482942600000},{"kwh":0.2080,"timeslice":1482943500000},{"kwh":0.1950,"timeslice":1482944400000},{"kwh":0.4230,"timeslice":1482945300000},{"kwh":0.5040,"timeslice":1482946200000},{"kwh":0.4910,"timeslice":1482947100000},{"kwh":0.2010,"timeslice":1482948000000},{"kwh":0.1170,"timeslice":1482948900000},{"kwh":0.4500,"timeslice":1482949800000},{"kwh":0.0850,"timeslice":1482950700000},{"kwh":0.1210,"timeslice":1482951600000},{"kwh":0.1080,"timeslice":1482952500000},{"kwh":0.0970,"timeslice":1482953400000},{"kwh":0.0890,"timeslice":1482954300000},{"kwh":0.0820,"timeslice":1482955200000},{"kwh":0.3590,"timeslice":1482956100000},{"kwh":0.4260,"timeslice":1482957000000},{"kwh":0.1310,"timeslice":1482957900000},{"kwh":0.1330,"timeslice":1482958800000},{"kwh":0.1190,"timeslice":1482959700000},{"kwh":0.1170,"timeslice":1482960600000},{"kwh":0.1050,"timeslice":1482961500000},{"kwh":0.1080,"timeslice":1482962400000},{"kwh":0.0790,"timeslice":1482963300000},{"kwh":0.0460,"timeslice":1482964200000},{"kwh":0.0590,"timeslice":1482965100000},{"kwh":0.0520,"timeslice":1482966000000},{"kwh":0.0520,"timeslice":1482966900000},{"kwh":0.0430,"timeslice":1482967800000},{"kwh":0.0560,"timeslice":1482968700000},{"kwh":0.0560,"timeslice":1482969600000},{"kwh":0.0500,"timeslice":1482970500000},{"kwh":0.0430,"timeslice":1482971400000},{"kwh":0.0470,"timeslice":1482972300000},{"kwh":0.0590,"timeslice":1482973200000},{"kwh":0.0520,"timeslice":1482974100000},{"kwh":0.0450,"timeslice":1482975000000},{"kwh":0.0440,"timeslice":1482975900000},{"kwh":0.0600,"timeslice":1482976800000},{"kwh":0.0530,"timeslice":1482977700000},{"kwh":0.0480,"timeslice":1482978600000},{"kwh":0.0440,"timeslice":1482979500000},{"kwh":0.0550,"timeslice":1482980400000},{"kwh":0.0620,"timeslice":1482981300000},{"kwh":0.0510,"timeslice":1482982200000},{"kwh":0.0440,"timeslice":1482983100000},{"kwh":0.0590,"timeslice":1482984000000},{"kwh":0.0770,"timeslice":1482984900000},{"kwh":0.0610,"timeslice":1482985800000},{"kwh":0.0530,"timeslice":1482986700000},{"kwh":0.0530,"timeslice":1482987600000},{"kwh":0.0630,"timeslice":1482988500000},{"kwh":0.0690,"timeslice":1482989400000},{"kwh":0.0540,"timeslice":1482990300000},{"kwh":0.0530,"timeslice":1482991200000},{"kwh":0.0570,"timeslice":1482992100000},{"kwh":0.0690,"timeslice":1482993000000},{"kwh":0.0760,"timeslice":1482993900000},{"kwh":0.3260,"timeslice":1482994800000},{"kwh":0.3080,"timeslice":1482995700000},{"kwh":0.0790,"timeslice":1482996600000},{"kwh":0.1030,"timeslice":1482997500000},{"kwh":0.0740,"timeslice":1482998400000},{"kwh":0.0730,"timeslice":1482999300000},{"kwh":0.0930,"timeslice":1483000200000},{"kwh":0.0920,"timeslice":1483001100000},{"kwh":0.0630,"timeslice":1483002000000},{"kwh":0.0620,"timeslice":1483002900000},{"kwh":0.0610,"timeslice":1483003800000},{"kwh":0.0620,"timeslice":1483004700000},{"kwh":0.0800,"timeslice":1483005600000},{"kwh":0.0870,"timeslice":1483006500000},{"kwh":0.0580,"timeslice":1483007400000},{"kwh":0.0680,"timeslice":1483008300000},{"kwh":0.0660,"timeslice":1483009200000},{"kwh":0.0610,"timeslice":1483010100000},{"kwh":0.0560,"timeslice":1483011000000},{"kwh":0.1220,"timeslice":1483011900000},{"kwh":0.1420,"timeslice":1483012800000},{"kwh":0.1390,"timeslice":1483013700000},{"kwh":0.1470,"timeslice":1483014600000},{"kwh":0.1410,"timeslice":1483015500000},{"kwh":0.0870,"timeslice":1483016400000},{"kwh":0.0580,"timeslice":1483017300000},{"kwh":0.0580,"timeslice":1483018200000},{"kwh":0.0580,"timeslice":1483019100000},{"kwh":0.0510,"timeslice":1483020000000},{"kwh":0.0570,"timeslice":1483020900000},{"kwh":0.0600,"timeslice":1483021800000},{"kwh":0.0630,"timeslice":1483022700000},{"kwh":0.0520,"timeslice":1483023600000},{"kwh":0.0550,"timeslice":1483024500000},{"kwh":0.0580,"timeslice":1483025400000},{"kwh":0.0590,"timeslice":1483026300000},{"kwh":0.0640,"timeslice":1483027200000},{"kwh":0.1680,"timeslice":1483028100000},{"kwh":0.3830,"timeslice":1483029000000},{"kwh":0.2640,"timeslice":1483029900000},{"kwh":0.0050,"timeslice":1483034400000},{"kwh":0.0770,"timeslice":1483035300000},{"kwh":0.1330,"timeslice":1483036200000},{"kwh":0.1620,"timeslice":1483037100000},{"kwh":0.4700,"timeslice":1483038000000},{"kwh":0.4620,"timeslice":1483038900000},{"kwh":0.4410,"timeslice":1483039800000},{"kwh":0.2070,"timeslice":1483040700000},{"kwh":0.2020,"timeslice":1483041600000},{"kwh":0.2040,"timeslice":1483042500000},{"kwh":0.2070,"timeslice":1483043400000},{"kwh":0.1220,"timeslice":1483044300000},{"kwh":0.1210,"timeslice":1483045200000},{"kwh":0.1220,"timeslice":1483046100000},{"kwh":0.1060,"timeslice":1483047000000},{"kwh":0.0930,"timeslice":1483047900000},{"kwh":0.0560,"timeslice":1483048800000},{"kwh":0.0540,"timeslice":1483049700000},{"kwh":0.0630,"timeslice":1483050600000},{"kwh":0.0410,"timeslice":1483051500000},{"kwh":0.0450,"timeslice":1483052400000},{"kwh":0.0390,"timeslice":1483053300000},{"kwh":0.0380,"timeslice":1483054200000},{"kwh":0.0400,"timeslice":1483055100000},{"kwh":0.0420,"timeslice":1483056000000},{"kwh":0.0360,"timeslice":1483056900000},{"kwh":0.0450,"timeslice":1483057800000},{"kwh":0.0540,"timeslice":1483058700000},{"kwh":0.0510,"timeslice":1483059600000},{"kwh":0.0520,"timeslice":1483060500000},{"kwh":0.0640,"timeslice":1483061400000},{"kwh":0.0840,"timeslice":1483062300000},{"kwh":0.0510,"timeslice":1483063200000},{"kwh":0.0590,"timeslice":1483064100000},{"kwh":0.0580,"timeslice":1483065000000},{"kwh":0.0580,"timeslice":1483065900000},{"kwh":0.0530,"timeslice":1483066800000},{"kwh":0.0460,"timeslice":1483067700000},{"kwh":0.0530,"timeslice":1483068600000},{"kwh":0.0520,"timeslice":1483069500000},{"kwh":0.0670,"timeslice":1483070400000},{"kwh":0.0480,"timeslice":1483071300000},{"kwh":0.0480,"timeslice":1483072200000},{"kwh":0.0520,"timeslice":1483073100000},{"kwh":0.0500,"timeslice":1483074000000},{"kwh":0.0440,"timeslice":1483074900000},{"kwh":0.0460,"timeslice":1483075800000},{"kwh":0.0520,"timeslice":1483076700000},{"kwh":0.0590,"timeslice":1483077600000},{"kwh":0.0440,"timeslice":1483078500000},{"kwh":0.0500,"timeslice":1483079400000},{"kwh":0.0620,"timeslice":1483080300000},{"kwh":0.0500,"timeslice":1483081200000},{"kwh":0.0450,"timeslice":1483082100000},{"kwh":0.0450,"timeslice":1483083000000},{"kwh":0.3200,"timeslice":1483083900000},{"kwh":0.1840,"timeslice":1483084800000},{"kwh":0.0610,"timeslice":1483085700000},{"kwh":0.3940,"timeslice":1483086600000},{"kwh":0.4370,"timeslice":1483087500000},{"kwh":0.1380,"timeslice":1483088400000},{"kwh":0.0980,"timeslice":1483089300000},{"kwh":0.1130,"timeslice":1483090200000},{"kwh":0.0890,"timeslice":1483091100000},{"kwh":0.1570,"timeslice":1483092000000},{"kwh":0.3520,"timeslice":1483092900000},{"kwh":0.4240,"timeslice":1483093800000},{"kwh":0.4170,"timeslice":1483094700000},{"kwh":0.4170,"timeslice":1483095600000},{"kwh":0.1340,"timeslice":1483096500000},{"kwh":0.0860,"timeslice":1483097400000},{"kwh":0.0880,"timeslice":1483098300000},{"kwh":0.1030,"timeslice":1483099200000},{"kwh":0.0540,"timeslice":1483100100000},{"kwh":0.0560,"timeslice":1483101000000},{"kwh":0.0560,"timeslice":1483101900000},{"kwh":0.0550,"timeslice":1483102800000},{"kwh":0.0460,"timeslice":1483103700000},{"kwh":0.0520,"timeslice":1483104600000},{"kwh":0.0510,"timeslice":1483105500000},{"kwh":0.0560,"timeslice":1483106400000},{"kwh":0.0430,"timeslice":1483107300000},{"kwh":0.1200,"timeslice":1483108200000},{"kwh":0.1350,"timeslice":1483109100000},{"kwh":0.1560,"timeslice":1483110000000},{"kwh":0.2580,"timeslice":1483110900000},{"kwh":0.0590,"timeslice":1483111800000},{"kwh":0.0690,"timeslice":1483112700000},{"kwh":0.0790,"timeslice":1483113600000},{"kwh":0.1240,"timeslice":1483114500000},{"kwh":0.1200,"timeslice":1483115400000},{"kwh":0.1040,"timeslice":1483116300000},{"kwh":0.0930,"timeslice":1483117200000},{"kwh":0.2780,"timeslice":1483118100000},{"kwh":0.1550,"timeslice":1483119000000},{"kwh":0.1360,"timeslice":1483119900000},{"kwh":0.1790,"timeslice":1483120800000},{"kwh":0.0910,"timeslice":1483121700000},{"kwh":0.0970,"timeslice":1483122600000},{"kwh":0.0970,"timeslice":1483123500000},{"kwh":0.0910,"timeslice":1483124400000},{"kwh":0.0930,"timeslice":1483125300000},{"kwh":0.1120,"timeslice":1483126200000},{"kwh":0.1370,"timeslice":1483127100000},{"kwh":0.1000,"timeslice":1483128000000},{"kwh":0.1200,"timeslice":1483128900000},{"kwh":0.1070,"timeslice":1483129800000},{"kwh":0.0990,"timeslice":1483130700000},{"kwh":0.1140,"timeslice":1483131600000},{"kwh":0.1020,"timeslice":1483132500000},{"kwh":0.0850,"timeslice":1483133400000},{"kwh":0.0420,"timeslice":1483134300000},{"kwh":0.0430,"timeslice":1483135200000},{"kwh":0.0360,"timeslice":1483136100000},{"kwh":0.0400,"timeslice":1483137000000},{"kwh":0.0430,"timeslice":1483137900000},{"kwh":0.0410,"timeslice":1483138800000},{"kwh":0.0410,"timeslice":1483139700000},{"kwh":0.0360,"timeslice":1483140600000},{"kwh":0.0340,"timeslice":1483141500000},{"kwh":0.0460,"timeslice":1483142400000},{"kwh":0.0510,"timeslice":1483143300000},{"kwh":0.0440,"timeslice":1483144200000},{"kwh":0.0420,"timeslice":1483145100000},{"kwh":0.0490,"timeslice":1483146000000},{"kwh":0.0540,"timeslice":1483146900000},{"kwh":0.0510,"timeslice":1483147800000},{"kwh":0.0470,"timeslice":1483148700000},{"kwh":0.0480,"timeslice":1483149600000},{"kwh":0.0550,"timeslice":1483150500000},{"kwh":0.0490,"timeslice":1483151400000},{"kwh":0.0570,"timeslice":1483152300000},{"kwh":0.0460,"timeslice":1483153200000},{"kwh":0.0580,"timeslice":1483154100000},{"kwh":0.0460,"timeslice":1483155000000},{"kwh":0.0510,"timeslice":1483155900000},{"kwh":0.0490,"timeslice":1483156800000},{"kwh":0.0520,"timeslice":1483157700000},{"kwh":0.0490,"timeslice":1483158600000},{"kwh":0.0470,"timeslice":1483159500000},{"kwh":0.0520,"timeslice":1483160400000},{"kwh":0.0500,"timeslice":1483161300000},{"kwh":0.0630,"timeslice":1483162200000},{"kwh":0.0430,"timeslice":1483163100000},{"kwh":0.0520,"timeslice":1483164000000},{"kwh":0.0500,"timeslice":1483164900000},{"kwh":0.0520,"timeslice":1483165800000},{"kwh":0.0480,"timeslice":1483166700000},{"kwh":0.0450,"timeslice":1483167600000},{"kwh":0.0510,"timeslice":1483168500000},{"kwh":0.0490,"timeslice":1483169400000},{"kwh":0.2710,"timeslice":1483170300000},{"kwh":0.4230,"timeslice":1483171200000},{"kwh":0.2680,"timeslice":1483172100000},{"kwh":0.0720,"timeslice":1483173000000},{"kwh":0.0780,"timeslice":1483173900000},{"kwh":0.0950,"timeslice":1483174800000},{"kwh":0.1100,"timeslice":1483175700000},{"kwh":0.3150,"timeslice":1483176600000},{"kwh":0.3140,"timeslice":1483177500000},{"kwh":0.2670,"timeslice":1483178400000},{"kwh":0.3620,"timeslice":1483179300000},{"kwh":0.4190,"timeslice":1483180200000},{"kwh":0.4210,"timeslice":1483181100000},{"kwh":0.4130,"timeslice":1483182000000},{"kwh":0.4140,"timeslice":1483182900000},{"kwh":0.4080,"timeslice":1483183800000},{"kwh":0.4040,"timeslice":1483184700000},{"kwh":0.3980,"timeslice":1483185600000},{"kwh":0.3990,"timeslice":1483186500000},{"kwh":0.1810,"timeslice":1483187400000},{"kwh":0.1270,"timeslice":1483188300000},{"kwh":0.1050,"timeslice":1483189200000},{"kwh":0.0670,"timeslice":1483190100000},{"kwh":0.3920,"timeslice":1483191000000},{"kwh":0.2180,"timeslice":1483191900000},{"kwh":0.0430,"timeslice":1483192800000},{"kwh":0.0420,"timeslice":1483193700000},{"kwh":0.0600,"timeslice":1483194600000},{"kwh":0.0510,"timeslice":1483195500000},{"kwh":0.0440,"timeslice":1483196400000},{"kwh":0.0490,"timeslice":1483197300000},{"kwh":0.0520,"timeslice":1483198200000},{"kwh":0.0580,"timeslice":1483199100000},{"kwh":0.0510,"timeslice":1483200000000},{"kwh":0.0430,"timeslice":1483200900000},{"kwh":0.0430,"timeslice":1483201800000},{"kwh":0.0470,"timeslice":1483202700000},{"kwh":0.0540,"timeslice":1483203600000},{"kwh":0.2020,"timeslice":1483204500000},{"kwh":0.3820,"timeslice":1483205400000},{"kwh":0.3770,"timeslice":1483206300000},{"kwh":0.3780,"timeslice":1483207200000},{"kwh":0.3100,"timeslice":1483208100000},{"kwh":0.0750,"timeslice":1483209000000},{"kwh":0.1160,"timeslice":1483209900000},{"kwh":0.1120,"timeslice":1483210800000},{"kwh":0.1210,"timeslice":1483211700000},{"kwh":0.1630,"timeslice":1483212600000},{"kwh":0.0630,"timeslice":1483213500000},{"kwh":0.0490,"timeslice":1483214400000},{"kwh":0.1160,"timeslice":1483215300000},{"kwh":0.1240,"timeslice":1483216200000},{"kwh":0.1850,"timeslice":1483217100000},{"kwh":0.1710,"timeslice":1483218000000},{"kwh":0.1120,"timeslice":1483218900000},{"kwh":0.1240,"timeslice":1483219800000},{"kwh":0.1230,"timeslice":1483220700000},{"kwh":0.1080,"timeslice":1483221600000},{"kwh":0.0900,"timeslice":1483222500000},{"kwh":0.1050,"timeslice":1483223400000},{"kwh":0.1020,"timeslice":1483224300000},{"kwh":0.0970,"timeslice":1483225200000},{"kwh":0.0760,"timeslice":1483226100000},{"kwh":0.0570,"timeslice":1483227000000},{"kwh":0.0400,"timeslice":1483227900000},{"kwh":0.0500,"timeslice":1483228800000},{"kwh":0.0540,"timeslice":1483229700000},{"kwh":0.0370,"timeslice":1483230600000},{"kwh":0.0350,"timeslice":1483231500000},{"kwh":0.0470,"timeslice":1483232400000},{"kwh":0.0410,"timeslice":1483233300000},{"kwh":0.0540,"timeslice":1483234200000},{"kwh":0.0450,"timeslice":1483235100000},{"kwh":0.0500,"timeslice":1483236000000},{"kwh":0.0510,"timeslice":1483236900000},{"kwh":0.0500,"timeslice":1483237800000},{"kwh":0.0500,"timeslice":1483238700000},{"kwh":0.0680,"timeslice":1483239600000},{"kwh":0.0800,"timeslice":1483240500000},{"kwh":0.0500,"timeslice":1483241400000},{"kwh":0.0580,"timeslice":1483242300000},{"kwh":0.0590,"timeslice":1483243200000},{"kwh":0.0560,"timeslice":1483244100000},{"kwh":0.0510,"timeslice":1483245000000},{"kwh":0.0460,"timeslice":1483245900000},{"kwh":0.0510,"timeslice":1483246800000},{"kwh":0.0530,"timeslice":1483247700000},{"kwh":0.0670,"timeslice":1483248600000},{"kwh":0.0490,"timeslice":1483249500000},{"kwh":0.0460,"timeslice":1483250400000},{"kwh":0.0510,"timeslice":1483251300000},{"kwh":0.0760,"timeslice":1483252200000},{"kwh":0.0520,"timeslice":1483253100000},{"kwh":0.0480,"timeslice":1483254000000},{"kwh":0.0460,"timeslice":1483254900000},{"kwh":0.1770,"timeslice":1483255800000},{"kwh":0.3400,"timeslice":1483256700000},{"kwh":0.1380,"timeslice":1483257600000},{"kwh":0.0590,"timeslice":1483258500000},{"kwh":0.0560,"timeslice":1483259400000},{"kwh":0.0700,"timeslice":1483260300000},{"kwh":0.0620,"timeslice":1483261200000},{"kwh":0.0520,"timeslice":1483262100000},{"kwh":0.0950,"timeslice":1483263000000},{"kwh":0.1000,"timeslice":1483263900000},{"kwh":0.1020,"timeslice":1483264800000},{"kwh":0.0550,"timeslice":1483265700000},{"kwh":0.0570,"timeslice":1483266600000},{"kwh":0.1900,"timeslice":1483267500000},{"kwh":0.3320,"timeslice":1483268400000},{"kwh":0.2140,"timeslice":1483269300000},{"kwh":0.3290,"timeslice":1483270200000},{"kwh":0.3280,"timeslice":1483271100000},{"kwh":0.2550,"timeslice":1483272000000},{"kwh":0.3280,"timeslice":1483272900000},{"kwh":0.2220,"timeslice":1483273800000},{"kwh":0.0870,"timeslice":1483274700000},{"kwh":0.0940,"timeslice":1483275600000},{"kwh":0.3060,"timeslice":1483276500000},{"kwh":0.3220,"timeslice":1483277400000},{"kwh":0.2900,"timeslice":1483278300000},{"kwh":0.1520,"timeslice":1483279200000},{"kwh":0.1570,"timeslice":1483280100000},{"kwh":0.1320,"timeslice":1483281000000},{"kwh":0.0770,"timeslice":1483281900000},{"kwh":0.1020,"timeslice":1483282800000},{"kwh":0.1130,"timeslice":1483283700000},{"kwh":0.1090,"timeslice":1483284600000},{"kwh":0.0870,"timeslice":1483285500000},{"kwh":0.0890,"timeslice":1483286400000},{"kwh":0.2000,"timeslice":1483287300000},{"kwh":0.2370,"timeslice":1483288200000},{"kwh":0.1630,"timeslice":1483289100000},{"kwh":0.1780,"timeslice":1483290000000},{"kwh":0.2000,"timeslice":1483290900000},{"kwh":0.2920,"timeslice":1483291800000},{"kwh":0.2980,"timeslice":1483292700000},{"kwh":0.2950,"timeslice":1483293600000},{"kwh":0.2980,"timeslice":1483294500000},{"kwh":0.2960,"timeslice":1483295400000},{"kwh":0.2990,"timeslice":1483296300000},{"kwh":0.2030,"timeslice":1483297200000},{"kwh":0.0940,"timeslice":1483298100000},{"kwh":0.0740,"timeslice":1483299000000},{"kwh":0.0970,"timeslice":1483299900000},{"kwh":0.0840,"timeslice":1483300800000},{"kwh":0.0990,"timeslice":1483301700000},{"kwh":0.1140,"timeslice":1483302600000},{"kwh":0.0970,"timeslice":1483303500000},{"kwh":0.1060,"timeslice":1483304400000},{"kwh":0.0940,"timeslice":1483305300000},{"kwh":0.0490,"timeslice":1483306200000},{"kwh":0.0620,"timeslice":1483307100000},{"kwh":0.0630,"timeslice":1483308000000},{"kwh":0.0490,"timeslice":1483308900000},{"kwh":0.0340,"timeslice":1483309800000},{"kwh":0.0340,"timeslice":1483310700000},{"kwh":0.0400,"timeslice":1483311600000},{"kwh":0.0530,"timeslice":1483312500000},{"kwh":0.0370,"timeslice":1483313400000},{"kwh":0.0350,"timeslice":1483314300000},{"kwh":0.0360,"timeslice":1483315200000},{"kwh":0.0490,"timeslice":1483316100000},{"kwh":0.0470,"timeslice":1483317000000},{"kwh":0.0350,"timeslice":1483317900000},{"kwh":0.0360,"timeslice":1483318800000},{"kwh":0.0380,"timeslice":1483319700000},{"kwh":0.0590,"timeslice":1483320600000},{"kwh":0.0490,"timeslice":1483321500000},{"kwh":0.0450,"timeslice":1483322400000},{"kwh":0.0450,"timeslice":1483323300000},{"kwh":0.0540,"timeslice":1483324200000},{"kwh":0.0600,"timeslice":1483325100000},{"kwh":0.0440,"timeslice":1483326000000},{"kwh":0.0460,"timeslice":1483326900000},{"kwh":0.0450,"timeslice":1483327800000},{"kwh":0.0510,"timeslice":1483328700000},{"kwh":0.0580,"timeslice":1483329600000},{"kwh":0.0470,"timeslice":1483330500000},{"kwh":0.0480,"timeslice":1483331400000},{"kwh":0.0930,"timeslice":1483332300000},{"kwh":0.1250,"timeslice":1483333200000},{"kwh":0.2860,"timeslice":1483334100000},{"kwh":0.2570,"timeslice":1483335000000},{"kwh":0.1090,"timeslice":1483335900000},{"kwh":0.0780,"timeslice":1483336800000},{"kwh":0.0510,"timeslice":1483337700000},{"kwh":0.0450,"timeslice":1483338600000},{"kwh":0.0420,"timeslice":1483339500000},{"kwh":0.0540,"timeslice":1483340400000},{"kwh":0.0480,"timeslice":1483341300000},{"kwh":0.0490,"timeslice":1483342200000},{"kwh":0.0420,"timeslice":1483343100000},{"kwh":0.0520,"timeslice":1483344000000},{"kwh":0.0460,"timeslice":1483344900000},{"kwh":0.0470,"timeslice":1483345800000},{"kwh":0.0490,"timeslice":1483346700000},{"kwh":0.0390,"timeslice":1483347600000},{"kwh":0.0550,"timeslice":1483348500000},{"kwh":0.0470,"timeslice":1483349400000},{"kwh":0.0520,"timeslice":1483350300000},{"kwh":0.0490,"timeslice":1483351200000},{"kwh":0.0580,"timeslice":1483352100000},{"kwh":0.0480,"timeslice":1483353000000},{"kwh":0.0480,"timeslice":1483353900000},{"kwh":0.0500,"timeslice":1483354800000},{"kwh":0.0490,"timeslice":1483355700000},{"kwh":0.0480,"timeslice":1483356600000},{"kwh":0.0410,"timeslice":1483357500000},{"kwh":0.0490,"timeslice":1483358400000},{"kwh":0.0480,"timeslice":1483359300000},{"kwh":0.0550,"timeslice":1483360200000},{"kwh":0.0410,"timeslice":1483361100000},{"kwh":0.0430,"timeslice":1483362000000},{"kwh":0.0420,"timeslice":1483362900000},{"kwh":0.0550,"timeslice":1483363800000},{"kwh":0.0470,"timeslice":1483364700000},{"kwh":0.0410,"timeslice":1483365600000},{"kwh":0.0460,"timeslice":1483366500000},{"kwh":0.0460,"timeslice":1483367400000},{"kwh":0.0550,"timeslice":1483368300000},{"kwh":0.0430,"timeslice":1483369200000},{"kwh":0.1190,"timeslice":1483370100000},{"kwh":0.0750,"timeslice":1483371000000},{"kwh":0.0970,"timeslice":1483371900000},{"kwh":0.1190,"timeslice":1483372800000},{"kwh":0.1100,"timeslice":1483373700000},{"kwh":0.2420,"timeslice":1483374600000},{"kwh":0.3510,"timeslice":1483375500000},{"kwh":0.3470,"timeslice":1483376400000},{"kwh":0.3450,"timeslice":1483377300000},{"kwh":0.3450,"timeslice":1483378200000},{"kwh":0.3420,"timeslice":1483379100000},{"kwh":0.3380,"timeslice":1483380000000},{"kwh":0.2270,"timeslice":1483380900000},{"kwh":0.1380,"timeslice":1483381800000},{"kwh":0.0960,"timeslice":1483382700000},{"kwh":0.1770,"timeslice":1483383600000},{"kwh":0.3340,"timeslice":1483384500000},{"kwh":0.3070,"timeslice":1483385400000},{"kwh":0.0870,"timeslice":1483386300000},{"kwh":0.0760,"timeslice":1483387200000},{"kwh":0.0890,"timeslice":1483388100000},{"kwh":0.0800,"timeslice":1483389000000},{"kwh":0.0600,"timeslice":1483389900000},{"kwh":0.0470,"timeslice":1483390800000},{"kwh":0.0500,"timeslice":1483391700000},{"kwh":0.0530,"timeslice":1483392600000},{"kwh":0.0500,"timeslice":1483393500000},{"kwh":0.0430,"timeslice":1483394400000},{"kwh":0.0320,"timeslice":1483395300000},{"kwh":0.0430,"timeslice":1483396200000},{"kwh":0.0470,"timeslice":1483397100000},{"kwh":0.0510,"timeslice":1483398000000},{"kwh":0.0390,"timeslice":1483398900000},{"kwh":0.0330,"timeslice":1483399800000},{"kwh":0.0400,"timeslice":1483400700000},{"kwh":0.0510,"timeslice":1483401600000},{"kwh":0.0410,"timeslice":1483402500000},{"kwh":0.0390,"timeslice":1483403400000},{"kwh":0.0430,"timeslice":1483404300000},{"kwh":0.0540,"timeslice":1483405200000},{"kwh":0.0580,"timeslice":1483406100000},{"kwh":0.0400,"timeslice":1483407000000},{"kwh":0.0430,"timeslice":1483407900000},{"kwh":0.0460,"timeslice":1483408800000},{"kwh":0.0610,"timeslice":1483409700000},{"kwh":0.0460,"timeslice":1483410600000},{"kwh":0.0420,"timeslice":1483411500000},{"kwh":0.0420,"timeslice":1483412400000},{"kwh":0.0570,"timeslice":1483413300000},{"kwh":0.0540,"timeslice":1483414200000},{"kwh":0.0440,"timeslice":1483415100000},{"kwh":0.0520,"timeslice":1483416000000},{"kwh":0.0500,"timeslice":1483416900000},{"kwh":0.0620,"timeslice":1483417800000},{"kwh":0.0870,"timeslice":1483418700000},{"kwh":0.1130,"timeslice":1483419600000},{"kwh":0.3340,"timeslice":1483420500000},{"kwh":0.1510,"timeslice":1483421400000},{"kwh":0.1230,"timeslice":1483422300000},{"kwh":0.1070,"timeslice":1483423200000},{"kwh":0.0910,"timeslice":1483424100000},{"kwh":0.0480,"timeslice":1483425000000},{"kwh":0.0490,"timeslice":1483425900000},{"kwh":0.0500,"timeslice":1483426800000},{"kwh":0.0470,"timeslice":1483427700000},{"kwh":0.0370,"timeslice":1483428600000},{"kwh":0.0560,"timeslice":1483429500000},{"kwh":0.0430,"timeslice":1483430400000},{"kwh":0.0510,"timeslice":1483431300000},{"kwh":0.0470,"timeslice":1483432200000},{"kwh":0.0520,"timeslice":1483433100000},{"kwh":0.0480,"timeslice":1483434000000},{"kwh":0.0470,"timeslice":1483434900000},{"kwh":0.0510,"timeslice":1483435800000},{"kwh":0.0500,"timeslice":1483436700000},{"kwh":0.0840,"timeslice":1483437600000},{"kwh":0.0390,"timeslice":1483438500000},{"kwh":0.0130,"timeslice":1483439400000},{"kwh":0.0260,"timeslice":1483440300000},{"kwh":0.0540,"timeslice":1483441200000},{"kwh":0.0460,"timeslice":1483442100000},{"kwh":0.0430,"timeslice":1483443000000},{"kwh":0.0430,"timeslice":1483443900000},{"kwh":0.0510,"timeslice":1483444800000},{"kwh":0.0610,"timeslice":1483445700000},{"kwh":0.0470,"timeslice":1483446600000},{"kwh":0.0420,"timeslice":1483447500000},{"kwh":0.0350,"timeslice":1483448400000},{"kwh":0.0550,"timeslice":1483449300000},{"kwh":0.0580,"timeslice":1483450200000},{"kwh":0.0390,"timeslice":1483451100000},{"kwh":0.0440,"timeslice":1483452000000},{"kwh":0.0490,"timeslice":1483452900000},{"kwh":0.0620,"timeslice":1483453800000},{"kwh":0.0480,"timeslice":1483454700000},{"kwh":0.0450,"timeslice":1483455600000},{"kwh":0.0610,"timeslice":1483456500000},{"kwh":0.1790,"timeslice":1483457400000},{"kwh":0.2020,"timeslice":1483458300000},{"kwh":0.2280,"timeslice":1483459200000},{"kwh":0.2330,"timeslice":1483460100000},{"kwh":0.2220,"timeslice":1483461000000},{"kwh":0.2210,"timeslice":1483461900000},{"kwh":0.2180,"timeslice":1483462800000},{"kwh":0.2140,"timeslice":1483463700000},{"kwh":0.2140,"timeslice":1483464600000},{"kwh":0.2170,"timeslice":1483465500000},{"kwh":0.2130,"timeslice":1483466400000},{"kwh":0.2110,"timeslice":1483467300000},{"kwh":0.2090,"timeslice":1483468200000},{"kwh":0.2060,"timeslice":1483469100000},{"kwh":0.2080,"timeslice":1483470000000},{"kwh":0.1250,"timeslice":1483470900000},{"kwh":0.0950,"timeslice":1483471800000},{"kwh":0.0820,"timeslice":1483472700000},{"kwh":0.2270,"timeslice":1483473600000},{"kwh":0.0700,"timeslice":1483474500000},{"kwh":0.0680,"timeslice":1483475400000},{"kwh":0.0560,"timeslice":1483476300000},{"kwh":0.0410,"timeslice":1483477200000},{"kwh":0.0390,"timeslice":1483478100000},{"kwh":0.0370,"timeslice":1483479000000},{"kwh":0.0560,"timeslice":1483479900000},{"kwh":0.0640,"timeslice":1483480800000},{"kwh":0.0400,"timeslice":1483481700000},{"kwh":0.0410,"timeslice":1483482600000},{"kwh":0.0470,"timeslice":1483483500000},{"kwh":0.0510,"timeslice":1483484400000},{"kwh":0.0400,"timeslice":1483485300000},{"kwh":0.0370,"timeslice":1483486200000},{"kwh":0.0390,"timeslice":1483487100000},{"kwh":0.0530,"timeslice":1483488000000},{"kwh":0.0450,"timeslice":1483488900000},{"kwh":0.0380,"timeslice":1483489800000},{"kwh":0.0360,"timeslice":1483490700000},{"kwh":0.0470,"timeslice":1483491600000},{"kwh":0.0490,"timeslice":1483492500000},{"kwh":0.0450,"timeslice":1483493400000},{"kwh":0.0380,"timeslice":1483494300000},{"kwh":0.0430,"timeslice":1483495200000},{"kwh":0.0470,"timeslice":1483496100000},{"kwh":0.0460,"timeslice":1483497000000},{"kwh":0.0430,"timeslice":1483497900000},{"kwh":0.0380,"timeslice":1483498800000},{"kwh":0.0510,"timeslice":1483499700000},{"kwh":0.0450,"timeslice":1483500600000},{"kwh":0.0450,"timeslice":1483501500000},{"kwh":0.0540,"timeslice":1483502400000},{"kwh":0.0570,"timeslice":1483503300000},{"kwh":0.0580,"timeslice":1483504200000},{"kwh":0.0910,"timeslice":1483505100000},{"kwh":0.1760,"timeslice":1483506000000},{"kwh":0.2290,"timeslice":1483506900000},{"kwh":0.2310,"timeslice":1483507800000},{"kwh":0.0920,"timeslice":1483508700000},{"kwh":0.0730,"timeslice":1483509600000},{"kwh":0.0620,"timeslice":1483510500000},{"kwh":0.0660,"timeslice":1483511400000},{"kwh":0.0770,"timeslice":1483512300000},{"kwh":0.1560,"timeslice":1483513200000},{"kwh":0.1110,"timeslice":1483514100000},{"kwh":0.0540,"timeslice":1483515000000},{"kwh":0.0610,"timeslice":1483515900000},{"kwh":0.0480,"timeslice":1483516800000},{"kwh":0.0440,"timeslice":1483517700000},{"kwh":0.0470,"timeslice":1483518600000},{"kwh":0.0490,"timeslice":1483519500000},{"kwh":0.0490,"timeslice":1483520400000},{"kwh":0.0440,"timeslice":1483521300000},{"kwh":0.0440,"timeslice":1483522200000},{"kwh":0.0540,"timeslice":1483523100000},{"kwh":0.0620,"timeslice":1483524000000},{"kwh":0.0450,"timeslice":1483524900000},{"kwh":0.0450,"timeslice":1483525800000},{"kwh":0.0450,"timeslice":1483526700000},{"kwh":0.0650,"timeslice":1483527600000},{"kwh":0.0550,"timeslice":1483528500000},{"kwh":0.0480,"timeslice":1483529400000},{"kwh":0.0470,"timeslice":1483530300000},{"kwh":0.0550,"timeslice":1483531200000},{"kwh":0.0630,"timeslice":1483532100000},{"kwh":0.0500,"timeslice":1483533000000},{"kwh":0.0480,"timeslice":1483533900000},{"kwh":0.0480,"timeslice":1483534800000},{"kwh":0.0630,"timeslice":1483535700000},{"kwh":0.0550,"timeslice":1483536600000},{"kwh":0.0470,"timeslice":1483537500000},{"kwh":0.0440,"timeslice":1483538400000},{"kwh":0.0510,"timeslice":1483539300000},{"kwh":0.0570,"timeslice":1483540200000},{"kwh":0.0550,"timeslice":1483541100000},{"kwh":0.0460,"timeslice":1483542000000},{"kwh":0.0860,"timeslice":1483542900000},{"kwh":0.1290,"timeslice":1483543800000},{"kwh":0.1830,"timeslice":1483544700000},{"kwh":0.1680,"timeslice":1483545600000},{"kwh":0.0780,"timeslice":1483546500000},{"kwh":0.0510,"timeslice":1483547400000},{"kwh":0.0600,"timeslice":1483548300000},{"kwh":0.0570,"timeslice":1483549200000},{"kwh":0.0490,"timeslice":1483550100000},{"kwh":0.0460,"timeslice":1483551000000},{"kwh":0.0570,"timeslice":1483551900000},{"kwh":0.0620,"timeslice":1483552800000},{"kwh":0.0510,"timeslice":1483553700000},{"kwh":0.0460,"timeslice":1483554600000},{"kwh":0.0470,"timeslice":1483555500000},{"kwh":0.0610,"timeslice":1483556400000},{"kwh":0.0510,"timeslice":1483557300000},{"kwh":0.0470,"timeslice":1483558200000},{"kwh":0.0900,"timeslice":1483559100000},{"kwh":0.1180,"timeslice":1483560000000},{"kwh":0.2200,"timeslice":1483560900000},{"kwh":0.1550,"timeslice":1483561800000},{"kwh":0.1190,"timeslice":1483562700000},{"kwh":0.1200,"timeslice":1483563600000},{"kwh":0.1260,"timeslice":1483564500000},{"kwh":0.1120,"timeslice":1483565400000},{"kwh":0.0940,"timeslice":1483566300000},{"kwh":0.0570,"timeslice":1483567200000},{"kwh":0.0680,"timeslice":1483568100000},{"kwh":0.0540,"timeslice":1483569000000},{"kwh":0.0450,"timeslice":1483569900000},{"kwh":0.0350,"timeslice":1483570800000},{"kwh":0.0350,"timeslice":1483571700000},{"kwh":0.0460,"timeslice":1483572600000},{"kwh":0.0470,"timeslice":1483573500000},{"kwh":0.0380,"timeslice":1483574400000},{"kwh":0.0340,"timeslice":1483575300000},{"kwh":0.0390,"timeslice":1483576200000},{"kwh":0.0600,"timeslice":1483577100000},{"kwh":0.0520,"timeslice":1483578000000},{"kwh":0.0440,"timeslice":1483578900000},{"kwh":0.0440,"timeslice":1483579800000},{"kwh":0.0450,"timeslice":1483580700000},{"kwh":0.0530,"timeslice":1483581600000},{"kwh":0.0480,"timeslice":1483582500000},{"kwh":0.0770,"timeslice":1483583400000},{"kwh":0.0610,"timeslice":1483584300000},{"kwh":0.0640,"timeslice":1483585200000},{"kwh":0.0630,"timeslice":1483586100000},{"kwh":0.0590,"timeslice":1483587000000},{"kwh":0.0500,"timeslice":1483587900000},{"kwh":0.0590,"timeslice":1483588800000},{"kwh":0.0560,"timeslice":1483589700000},{"kwh":0.0550,"timeslice":1483590600000},{"kwh":0.0560,"timeslice":1483591500000},{"kwh":0.1300,"timeslice":1483592400000},{"kwh":0.2160,"timeslice":1483593300000},{"kwh":0.2140,"timeslice":1483594200000},{"kwh":0.1250,"timeslice":1483595100000},{"kwh":0.0670,"timeslice":1483596000000},{"kwh":0.0820,"timeslice":1483596900000},{"kwh":0.0480,"timeslice":1483597800000},{"kwh":0.0480,"timeslice":1483598700000},{"kwh":0.0490,"timeslice":1483599600000},{"kwh":0.0280,"timeslice":1483600500000},{"kwh":0.0010,"timeslice":1483601400000},{"kwh":0.0420,"timeslice":1483602300000},{"kwh":0.0520,"timeslice":1483603200000},{"kwh":0.0500,"timeslice":1483604100000},{"kwh":0.0530,"timeslice":1483605000000},{"kwh":0.0470,"timeslice":1483605900000},{"kwh":0.0500,"timeslice":1483606800000},{"kwh":0.0520,"timeslice":1483607700000},{"kwh":0.0510,"timeslice":1483608600000},{"kwh":0.0520,"timeslice":1483609500000},{"kwh":0.0420,"timeslice":1483610400000},{"kwh":0.0520,"timeslice":1483611300000},{"kwh":0.0510,"timeslice":1483612200000},{"kwh":0.0500,"timeslice":1483613100000},{"kwh":0.0400,"timeslice":1483614000000},{"kwh":0.0470,"timeslice":1483614900000},{"kwh":0.0510,"timeslice":1483615800000},{"kwh":0.0530,"timeslice":1483616700000},{"kwh":0.0490,"timeslice":1483617600000},{"kwh":0.0410,"timeslice":1483618500000},{"kwh":0.0510,"timeslice":1483619400000},{"kwh":0.0520,"timeslice":1483620300000},{"kwh":0.0540,"timeslice":1483621200000},{"kwh":0.0410,"timeslice":1483622100000},{"kwh":0.0470,"timeslice":1483623000000},{"kwh":0.0500,"timeslice":1483623900000},{"kwh":0.0540,"timeslice":1483624800000},{"kwh":0.0460,"timeslice":1483625700000},{"kwh":0.0440,"timeslice":1483626600000},{"kwh":0.0540,"timeslice":1483627500000},{"kwh":0.0530,"timeslice":1483628400000},{"kwh":0.0550,"timeslice":1483629300000},{"kwh":0.1090,"timeslice":1483630200000},{"kwh":0.1810,"timeslice":1483631100000},{"kwh":0.1470,"timeslice":1483632000000},{"kwh":0.1390,"timeslice":1483632900000},{"kwh":0.0920,"timeslice":1483633800000},{"kwh":0.0790,"timeslice":1483634700000},{"kwh":0.1150,"timeslice":1483635600000},{"kwh":0.2010,"timeslice":1483636500000},{"kwh":0.3300,"timeslice":1483637400000},{"kwh":0.3700,"timeslice":1483638300000},{"kwh":0.3690,"timeslice":1483639200000},{"kwh":0.1620,"timeslice":1483640100000},{"kwh":0.1090,"timeslice":1483641000000},{"kwh":0.0980,"timeslice":1483641900000},{"kwh":0.0650,"timeslice":1483642800000},{"kwh":0.0720,"timeslice":1483643700000},{"kwh":0.0690,"timeslice":1483644600000},{"kwh":0.0780,"timeslice":1483645500000},{"kwh":0.0650,"timeslice":1483646400000},{"kwh":0.0600,"timeslice":1483647300000},{"kwh":0.0520,"timeslice":1483648200000},{"kwh":0.0640,"timeslice":1483649100000},{"kwh":0.0700,"timeslice":1483650000000},{"kwh":0.0370,"timeslice":1483650900000}]
        				    }]
        				  }
        				};
        	
            var chartJSModel = new JSONModel();
            var c3jsModel = new JSONModel();
            chartJSModel.setData(chartJSData);
            c3jsModel.setData(c3jsData);
            sap.ui.getCore().setModel(chartJSModel, "chartjsData");
            sap.ui.getCore().setModel(c3jsModel, "c3jsData");
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
        networkDialogClose: function() {
            this._dialogs["networkDevices"].close();
            sap.ui.getCore().setModel(new JSONModel(), "networkDevices");
        },
        powerMeterDialogClose: function() {
            this._dialogs["powermeter"].close();
            
        },        
        doorWindowDialogClose: function() {
            this._dialogs["doorWindow"].close();
            sap.ui.getCore().setModel(new JSONModel(), "doorWindow");
        },
        afterDoorWindowDialogClose: function () {
            this._oDialog.destroy();
            this._oDialog = null;
        },
        afterCameraDialogClose: function () {
            this.camera.destroy();
            this.camera = null;
        },
        afterNetworkDialogClose: function () {
          // TODO add cleanup
        },
        afterPowerMeterDialogClose: function () {
        	sap.ui.getCore().setModel(new JSONModel(), "powermeter");
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
        _initMenu: function () {
        	 // create menu only once
            if (!this._menu) {
                this._menu = sap.ui.xmlfragment(
                    "cm.homeautomation.Menu",
                    this
                );
                this.getView().addDependent(this._menu);
            }
        },
        /**
		 * menu open pressed
		 *
		 */
        handlePressOpenMenu: function (oEvent) {
            var oButton = oEvent.getSource();

            if (this._menu!=null) {
	            var eDock = sap.ui.core.Popup.Dock;
	            this._menu.open(this._bKeyboard, oButton, eDock.BeginTop, eDock.BeginBottom, oButton);
            } else {
            	console.log("menu not initialized");
            }
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
