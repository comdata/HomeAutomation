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

var formatter = {};
formatter.dateTimeFormatter=function(sDate) {
	return moment(new Date(sDate)).format('DD.MM.YYYY HH:mm:ss');
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
        				      "data": [{y:0.0010,x:1482175800000},{y:0.0010,x:1482176700000},{y:0.2620,x:1482177600000},{y:0.1170,x:1482178500000},{y:0.1210,x:1482179400000},{y:0.1140,x:1482180300000},{y:0.1130,x:1482181200000},{y:0.1080,x:1482182100000},{y:0.0570,x:1482183000000},{y:0.0550,x:1482183900000},{y:0.0480,x:1482184800000},{y:0.0570,x:1482185700000},{y:0.0660,x:1482186600000},{y:0.0520,x:1482187500000},{y:0.0450,x:1482188400000},{y:0.0480,x:1482189300000},{y:0.0640,x:1482190200000},{y:0.0520,x:1482191100000},{y:0.0450,x:1482192000000},{y:0.0460,x:1482192900000},{y:0.0650,x:1482193800000},{y:0.0530,x:1482194700000},{y:0.0450,x:1482195600000},{y:0.0460,x:1482196500000},{y:0.0630,x:1482197400000},{y:0.0550,x:1482198300000},{y:0.0450,x:1482199200000},{y:0.0470,x:1482200100000},{y:0.0610,x:1482201000000},{y:0.0590,x:1482201900000},{y:0.0470,x:1482202800000},{y:0.0460,x:1482203700000},{y:0.0570,x:1482204600000},{y:0.0660,x:1482205500000},{y:0.0590,x:1482206400000},{y:0.0600,x:1482207300000},{y:0.0660,x:1482208200000},{y:0.1020,x:1482209100000},{y:0.1680,x:1482210000000},{y:0.4170,x:1482210900000},{y:0.0920,x:1482211800000},{y:0.1000,x:1482212700000},{y:0.0910,x:1482213600000},{y:0.0810,x:1482214500000},{y:0.0720,x:1482215400000},{y:0.0570,x:1482216300000},{y:0.0700,x:1482217200000},{y:0.0400,x:1482218100000},{y:0.0690,x:1482219000000},{y:0.0470,x:1482219900000},{y:0.0650,x:1482220800000},{y:0.0660,x:1482221700000},{y:0.1540,x:1482222600000},{y:0.0890,x:1482223500000},{y:0.1510,x:1482224400000},{y:0.1670,x:1482225300000},{y:0.1560,x:1482226200000},{y:0.1520,x:1482227100000},{y:0.1470,x:1482228000000},{y:0.1560,x:1482228900000},{y:0.1650,x:1482229800000},{y:0.1520,x:1482230700000},{y:0.1470,x:1482231600000},{y:0.1530,x:1482232500000},{y:0.1680,x:1482233400000},{y:0.1700,x:1482234300000},{y:0.1800,x:1482235200000},{y:0.0080,x:1482236100000},{y:0.0020,x:1482237000000},{y:0.0590,x:1482237900000},{y:0.0400,x:1482238800000},{y:0.0690,x:1482239700000},{y:0.0860,x:1482240600000},{y:0.0570,x:1482241500000},{y:0.0500,x:1482242400000},{y:0.0320,x:1482243300000},{y:0.0380,x:1482244200000},{y:0.1600,x:1482245100000},{y:0.0980,x:1482246000000},{y:0.1000,x:1482246900000},{y:0.1310,x:1482247800000},{y:0.0960,x:1482248700000},{y:0.1030,x:1482249600000},{y:0.0740,x:1482250500000},{y:0.0700,x:1482251400000},{y:0.1020,x:1482252300000},{y:0.4830,x:1482253200000},{y:0.5100,x:1482254100000},{y:0.1890,x:1482255000000},{y:0.0980,x:1482255900000},{y:0.1310,x:1482256800000},{y:0.1150,x:1482257700000},{y:0.1250,x:1482258600000},{y:0.0830,x:1482259500000},{y:0.1080,x:1482260400000},{y:0.1170,x:1482261300000},{y:0.0690,x:1482262200000},{y:0.0680,x:1482263100000},{y:0.0670,x:1482264000000},{y:0.0620,x:1482264900000},{y:0.0470,x:1482265800000},{y:0.0730,x:1482266700000},{y:0.0670,x:1482267600000},{y:0.0590,x:1482268500000},{y:0.0480,x:1482269400000},{y:0.0360,x:1482270300000},{y:0.0570,x:1482271200000},{y:0.0560,x:1482272100000},{y:0.0610,x:1482273000000},{y:0.0430,x:1482273900000},{y:0.0590,x:1482274800000},{y:0.0570,x:1482275700000},{y:0.0470,x:1482276600000},{y:0.0420,x:1482277500000},{y:0.0510,x:1482278400000},{y:0.0600,x:1482279300000},{y:0.0550,x:1482280200000},{y:0.0500,x:1482281100000},{y:0.0560,x:1482282000000},{y:0.0720,x:1482282900000},{y:0.0590,x:1482283800000},{y:0.0530,x:1482284700000},{y:0.0530,x:1482285600000},{y:0.0700,x:1482286500000},{y:0.0620,x:1482287400000},{y:0.0530,x:1482288300000},{y:0.0530,x:1482289200000},{y:0.0650,x:1482290100000},{y:0.0690,x:1482291000000},{y:0.0540,x:1482291900000},{y:0.0620,x:1482292800000},{y:0.0600,x:1482293700000},{y:0.0730,x:1482294600000},{y:0.0570,x:1482295500000},{y:0.0540,x:1482296400000},{y:0.0550,x:1482297300000},{y:0.0670,x:1482298200000},{y:0.0620,x:1482299100000},{y:0.0590,x:1482300000000},{y:0.5340,x:1482300900000},{y:0.2520,x:1482301800000},{y:0.4800,x:1482302700000},{y:0.1540,x:1482303600000},{y:0.2710,x:1482304500000},{y:0.5330,x:1482305400000},{y:0.1590,x:1482306300000},{y:0.1480,x:1482307200000},{y:0.1770,x:1482308100000},{y:0.4350,x:1482309000000},{y:0.4480,x:1482309900000},{y:0.1240,x:1482310800000},{y:0.0670,x:1482311700000},{y:0.0650,x:1482312600000},{y:0.0680,x:1482313500000},{y:0.0810,x:1482314400000},{y:0.0700,x:1482315300000},{y:0.0610,x:1482316200000},{y:0.0610,x:1482317100000},{y:0.0780,x:1482318000000},{y:0.0790,x:1482318900000},{y:0.1520,x:1482319800000},{y:0.1540,x:1482320700000},{y:0.1600,x:1482321600000},{y:0.1610,x:1482322500000},{y:0.1550,x:1482323400000},{y:0.1850,x:1482324300000},{y:0.1740,x:1482325200000},{y:0.6430,x:1482326100000},{y:0.1760,x:1482327000000},{y:0.1420,x:1482327900000},{y:0.0830,x:1482328800000},{y:0.1130,x:1482329700000},{y:0.1820,x:1482330600000},{y:0.1490,x:1482331500000},{y:0.0860,x:1482332400000},{y:0.1590,x:1482333300000},{y:0.6790,x:1482334200000},{y:0.2350,x:1482335100000},{y:0.1170,x:1482336000000},{y:0.1540,x:1482336900000},{y:0.1160,x:1482337800000},{y:0.1160,x:1482338700000},{y:0.1240,x:1482339600000},{y:0.6120,x:1482340500000},{y:0.2230,x:1482341400000},{y:0.1620,x:1482342300000},{y:0.1370,x:1482343200000},{y:0.1500,x:1482344100000},{y:0.1790,x:1482345000000},{y:0.1910,x:1482345900000},{y:0.2070,x:1482346800000},{y:0.1570,x:1482347700000},{y:0.1400,x:1482348600000},{y:0.1050,x:1482349500000},{y:0.1170,x:1482350400000},{y:0.1110,x:1482351300000},{y:0.1000,x:1482352200000},{y:0.0790,x:1482353100000},{y:0.1200,x:1482354000000},{y:0.1470,x:1482354900000},{y:0.1250,x:1482355800000},{y:0.1070,x:1482356700000},{y:0.1220,x:1482357600000},{y:0.0970,x:1482358500000},{y:0.0680,x:1482359400000},{y:0.0520,x:1482360300000},{y:0.0440,x:1482361200000},{y:0.0460,x:1482362100000},{y:0.0480,x:1482363000000},{y:0.0500,x:1482363900000},{y:0.0430,x:1482364800000},{y:0.0400,x:1482365700000},{y:0.0520,x:1482366600000},{y:0.0560,x:1482367500000},{y:0.0580,x:1482368400000},{y:0.0490,x:1482369300000},{y:0.0530,x:1482370200000},{y:0.0590,x:1482371100000},{y:0.0500,x:1482372000000},{y:0.0490,x:1482372900000},{y:0.0510,x:1482373800000},{y:0.0570,x:1482374700000},{y:0.0600,x:1482375600000},{y:0.0620,x:1482376500000},{y:0.0510,x:1482377400000},{y:0.0560,x:1482378300000},{y:0.0680,x:1482379200000},{y:0.0640,x:1482380100000},{y:0.0500,x:1482381000000},{y:0.0530,x:1482381900000},{y:0.0600,x:1482382800000},{y:0.0650,x:1482383700000},{y:0.0530,x:1482384600000},{y:0.0530,x:1482385500000},{y:0.0650,x:1482386400000},{y:0.0760,x:1482387300000},{y:0.0770,x:1482388200000},{y:0.1310,x:1482389100000},{y:0.1180,x:1482390000000},{y:0.1190,x:1482390900000},{y:0.5430,x:1482391800000},{y:0.2310,x:1482392700000},{y:0.0710,x:1482393600000},{y:0.0790,x:1482394500000},{y:0.0920,x:1482395400000},{y:0.0960,x:1482396300000},{y:0.1270,x:1482397200000},{y:0.0810,x:1482398100000},{y:0.3740,x:1482399000000},{y:0.3300,x:1482399900000},{y:0.0830,x:1482400800000},{y:0.0810,x:1482401700000},{y:0.0920,x:1482402600000},{y:0.1120,x:1482403500000},{y:0.0670,x:1482404400000},{y:0.0660,x:1482405300000},{y:0.0630,x:1482406200000},{y:0.0650,x:1482407100000},{y:0.0580,x:1482408000000},{y:0.0610,x:1482408900000},{y:0.1190,x:1482409800000},{y:0.1610,x:1482410700000},{y:0.1490,x:1482411600000},{y:0.2230,x:1482412500000},{y:0.3660,x:1482413400000},{y:0.2270,x:1482414300000},{y:0.1890,x:1482415200000},{y:0.1360,x:1482416100000},{y:0.1230,x:1482417000000},{y:0.1220,x:1482417900000},{y:0.1930,x:1482418800000},{y:0.6490,x:1482419700000},{y:0.6540,x:1482420600000},{y:0.2200,x:1482421500000},{y:0.1370,x:1482422400000},{y:0.1040,x:1482423300000},{y:0.6070,x:1482424200000},{y:0.6080,x:1482425100000},{y:0.6080,x:1482426000000},{y:0.6200,x:1482426900000},{y:0.7140,x:1482427800000},{y:0.8000,x:1482428700000},{y:0.2380,x:1482429600000},{y:0.0910,x:1482430500000},{y:0.1500,x:1482431400000},{y:0.5600,x:1482432300000},{y:0.1540,x:1482433200000},{y:0.1230,x:1482434100000},{y:0.0960,x:1482435000000},{y:0.0820,x:1482435900000},{y:0.0890,x:1482436800000},{y:0.1030,x:1482437700000},{y:0.4990,x:1482438600000},{y:0.2070,x:1482439500000},{y:0.1110,x:1482440400000},{y:0.0790,x:1482441300000},{y:0.0830,x:1482442200000},{y:0.0590,x:1482443100000},{y:0.0630,x:1482444000000},{y:0.0720,x:1482444900000},{y:0.0880,x:1482445800000},{y:0.0720,x:1482446700000},{y:0.0620,x:1482447600000},{y:0.0680,x:1482448500000},{y:0.0710,x:1482449400000},{y:0.0500,x:1482450300000},{y:0.0430,x:1482451200000},{y:0.0470,x:1482452100000},{y:0.0620,x:1482453000000},{y:0.0480,x:1482453900000},{y:0.0410,x:1482454800000},{y:0.0420,x:1482455700000},{y:0.0580,x:1482456600000},{y:0.0540,x:1482457500000},{y:0.0420,x:1482458400000},{y:0.0420,x:1482459300000},{y:0.0500,x:1482460200000},{y:0.0590,x:1482461100000},{y:0.0430,x:1482462000000},{y:0.0430,x:1482462900000},{y:0.0480,x:1482463800000},{y:0.0620,x:1482464700000},{y:0.0580,x:1482465600000},{y:0.0550,x:1482466500000},{y:0.0570,x:1482467400000},{y:0.0640,x:1482468300000},{y:0.0600,x:1482469200000},{y:0.0510,x:1482470100000},{y:0.0560,x:1482471000000},{y:0.0600,x:1482471900000},{y:0.0640,x:1482472800000},{y:0.0540,x:1482473700000},{y:0.0530,x:1482474600000},{y:0.0670,x:1482475500000},{y:0.0650,x:1482476400000},{y:0.0700,x:1482477300000},{y:0.5170,x:1482478200000},{y:0.1880,x:1482479100000},{y:0.0670,x:1482480000000},{y:0.0750,x:1482480900000},{y:0.0590,x:1482481800000},{y:0.0660,x:1482482700000},{y:0.0660,x:1482483600000},{y:0.0950,x:1482484500000},{y:0.0600,x:1482485400000},{y:0.0620,x:1482486300000},{y:0.0570,x:1482487200000},{y:0.0550,x:1482488100000},{y:0.0640,x:1482489000000},{y:0.0630,x:1482489900000},{y:0.0610,x:1482490800000},{y:0.0550,x:1482491700000},{y:0.0670,x:1482492600000},{y:0.0650,x:1482493500000},{y:0.0650,x:1482494400000},{y:0.0590,x:1482495300000},{y:0.1320,x:1482496200000},{y:0.1440,x:1482497100000},{y:0.1460,x:1482498000000},{y:0.1410,x:1482498900000},{y:0.1820,x:1482499800000},{y:0.6790,x:1482500700000},{y:0.7070,x:1482501600000},{y:0.2380,x:1482502500000},{y:0.2180,x:1482503400000},{y:0.1790,x:1482504300000},{y:0.1480,x:1482505200000},{y:0.3670,x:1482506100000},{y:0.7790,x:1482507000000},{y:0.7520,x:1482507900000},{y:0.7230,x:1482508800000},{y:0.7190,x:1482509700000},{y:0.4420,x:1482510600000},{y:0.2900,x:1482511500000},{y:0.4510,x:1482512400000},{y:0.6510,x:1482513300000},{y:0.5450,x:1482514200000},{y:0.1120,x:1482515100000},{y:0.0930,x:1482516000000},{y:0.1550,x:1482516900000},{y:0.1960,x:1482517800000},{y:0.1210,x:1482518700000},{y:0.1070,x:1482519600000},{y:0.1220,x:1482520500000},{y:0.1330,x:1482521400000},{y:0.1180,x:1482522300000},{y:0.0900,x:1482523200000},{y:0.0830,x:1482524100000},{y:0.1030,x:1482525000000},{y:0.0680,x:1482525900000},{y:0.0580,x:1482526800000},{y:0.0580,x:1482527700000},{y:0.0640,x:1482528600000},{y:0.0890,x:1482529500000},{y:0.0700,x:1482530400000},{y:0.0680,x:1482531300000},{y:0.0630,x:1482532200000},{y:0.0550,x:1482533100000},{y:0.0560,x:1482534000000},{y:0.0460,x:1482534900000},{y:0.0460,x:1482535800000},{y:0.0520,x:1482536700000},{y:0.0600,x:1482537600000},{y:0.0440,x:1482538500000},{y:0.0510,x:1482539400000},{y:0.0510,x:1482540300000},{y:0.0520,x:1482541200000},{y:0.0550,x:1482542100000},{y:0.0510,x:1482543000000},{y:0.0460,x:1482543900000},{y:0.0540,x:1482544800000},{y:0.0610,x:1482545700000},{y:0.0460,x:1482546600000},{y:0.0500,x:1482547500000},{y:0.0540,x:1482548400000},{y:0.0550,x:1482549300000},{y:0.0510,x:1482550200000},{y:0.0540,x:1482551100000},{y:0.0470,x:1482552000000},{y:0.0520,x:1482552900000},{y:0.0630,x:1482553800000},{y:0.0670,x:1482554700000},{y:0.0790,x:1482555600000},{y:0.0600,x:1482556500000},{y:0.0850,x:1482557400000},{y:0.0750,x:1482558300000},{y:0.0610,x:1482559200000},{y:0.0550,x:1482560100000},{y:0.0580,x:1482561000000},{y:0.2960,x:1482561900000},{y:0.5350,x:1482562800000},{y:0.0710,x:1482563700000},{y:0.0800,x:1482564600000},{y:0.1110,x:1482565500000},{y:0.4380,x:1482566400000},{y:0.1330,x:1482567300000},{y:0.1280,x:1482568200000},{y:0.2620,x:1482569100000},{y:0.1240,x:1482570000000},{y:0.0790,x:1482570900000},{y:0.0850,x:1482571800000},{y:0.4960,x:1482572700000},{y:0.1000,x:1482573600000},{y:0.0550,x:1482574500000},{y:0.0760,x:1482575400000},{y:0.0630,x:1482576300000},{y:0.0540,x:1482577200000},{y:0.0540,x:1482578100000},{y:0.0660,x:1482579000000},{y:0.0670,x:1482579900000},{y:0.0510,x:1482580800000},{y:0.0510,x:1482581700000},{y:0.0590,x:1482582600000},{y:0.0700,x:1482583500000},{y:0.0520,x:1482584400000},{y:0.0520,x:1482585300000},{y:0.0590,x:1482586200000},{y:0.0640,x:1482587100000},{y:0.0620,x:1482588000000},{y:0.0510,x:1482588900000},{y:0.0590,x:1482589800000},{y:0.0590,x:1482590700000},{y:0.0640,x:1482591600000},{y:0.0540,x:1482592500000},{y:0.0560,x:1482593400000},{y:0.0610,x:1482594300000},{y:0.0670,x:1482595200000},{y:0.0660,x:1482596100000},{y:0.0540,x:1482597000000},{y:0.0590,x:1482597900000},{y:0.0570,x:1482598800000},{y:0.0630,x:1482599700000},{y:0.0550,x:1482600600000},{y:0.0620,x:1482601500000},{y:0.0640,x:1482602400000},{y:0.0550,x:1482603300000},{y:0.0660,x:1482604200000},{y:0.0550,x:1482605100000},{y:0.0600,x:1482606000000},{y:0.0540,x:1482606900000},{y:0.0600,x:1482607800000},{y:0.0560,x:1482608700000},{y:0.0600,x:1482609600000},{y:0.0590,x:1482610500000},{y:0.0540,x:1482611400000},{y:0.0620,x:1482612300000},{y:0.0710,x:1482613200000},{y:0.0620,x:1482614100000},{y:0.0560,x:1482615000000},{y:0.0560,x:1482615900000},{y:0.0600,x:1482616800000},{y:0.0600,x:1482617700000},{y:0.0500,x:1482618600000},{y:0.0830,x:1482619500000},{y:0.0650,x:1482620400000},{y:0.0560,x:1482621300000},{y:0.0590,x:1482622200000},{y:0.0570,x:1482623100000},{y:0.0470,x:1482624000000},{y:0.0490,x:1482624900000},{y:0.0430,x:1482625800000},{y:0.0520,x:1482626700000},{y:0.0510,x:1482627600000},{y:0.0430,x:1482628500000},{y:0.0460,x:1482629400000},{y:0.0470,x:1482630300000},{y:0.0660,x:1482631200000},{y:0.0520,x:1482632100000},{y:0.0440,x:1482633000000},{y:0.0460,x:1482633900000},{y:0.0600,x:1482634800000},{y:0.0580,x:1482635700000},{y:0.0460,x:1482636600000},{y:0.0460,x:1482637500000},{y:0.0510,x:1482638400000},{y:0.0650,x:1482639300000},{y:0.0470,x:1482640200000},{y:0.0470,x:1482641100000},{y:0.0500,x:1482642000000},{y:0.0690,x:1482642900000},{y:0.0720,x:1482643800000},{y:0.0570,x:1482644700000},{y:0.0560,x:1482645600000},{y:0.0610,x:1482646500000},{y:0.0710,x:1482647400000},{y:0.0550,x:1482648300000},{y:0.0530,x:1482649200000},{y:0.0570,x:1482650100000},{y:0.0660,x:1482651000000},{y:0.0650,x:1482651900000},{y:0.0510,x:1482652800000},{y:0.0550,x:1482653700000},{y:0.0610,x:1482654600000},{y:0.0660,x:1482655500000},{y:0.0560,x:1482656400000},{y:0.0550,x:1482657300000},{y:0.0580,x:1482658200000},{y:0.0630,x:1482659100000},{y:0.0640,x:1482660000000},{y:0.0580,x:1482660900000},{y:0.0580,x:1482661800000},{y:0.0620,x:1482662700000},{y:0.0630,x:1482663600000},{y:0.0600,x:1482664500000},{y:0.0530,x:1482665400000},{y:0.0600,x:1482666300000},{y:0.0600,x:1482667200000},{y:0.0650,x:1482668100000},{y:0.0540,x:1482669000000},{y:0.0570,x:1482669900000},{y:0.0630,x:1482670800000},{y:0.0960,x:1482671700000},{y:0.0780,x:1482672600000},{y:0.0610,x:1482673500000},{y:0.0640,x:1482674400000},{y:0.1060,x:1482675300000},{y:0.1550,x:1482676200000},{y:0.1130,x:1482677100000},{y:0.0730,x:1482678000000},{y:0.0770,x:1482678900000},{y:0.0760,x:1482679800000},{y:0.0720,x:1482680700000},{y:0.0890,x:1482681600000},{y:0.0940,x:1482682500000},{y:0.0790,x:1482683400000},{y:0.0790,x:1482684300000},{y:0.0720,x:1482685200000},{y:0.0750,x:1482686100000},{y:0.1020,x:1482687000000},{y:0.3540,x:1482687900000},{y:0.3980,x:1482688800000},{y:0.2010,x:1482689700000},{y:0.1230,x:1482690600000},{y:0.1270,x:1482691500000},{y:0.1140,x:1482692400000},{y:0.1050,x:1482693300000},{y:0.1050,x:1482694200000},{y:0.0980,x:1482695100000},{y:0.1160,x:1482696000000},{y:0.0700,x:1482696900000},{y:0.1360,x:1482697800000},{y:0.1540,x:1482698700000},{y:0.1330,x:1482699600000},{y:0.1090,x:1482700500000},{y:0.1060,x:1482701400000},{y:0.0760,x:1482702300000},{y:0.0630,x:1482703200000},{y:0.0530,x:1482704100000},{y:0.0470,x:1482705000000},{y:0.0510,x:1482705900000},{y:0.0560,x:1482706800000},{y:0.0500,x:1482707700000},{y:0.0420,x:1482708600000},{y:0.0490,x:1482709500000},{y:0.0590,x:1482710400000},{y:0.0500,x:1482711300000},{y:0.0450,x:1482712200000},{y:0.0430,x:1482713100000},{y:0.0570,x:1482714000000},{y:0.0530,x:1482714900000},{y:0.0490,x:1482715800000},{y:0.0440,x:1482716700000},{y:0.0510,x:1482717600000},{y:0.0590,x:1482718500000},{y:0.0510,x:1482719400000},{y:0.0460,x:1482720300000},{y:0.0450,x:1482721200000},{y:0.0600,x:1482722100000},{y:0.0540,x:1482723000000},{y:0.0480,x:1482723900000},{y:0.0580,x:1482724800000},{y:0.0630,x:1482725700000},{y:0.0710,x:1482726600000},{y:0.0570,x:1482727500000},{y:0.0540,x:1482728400000},{y:0.0530,x:1482729300000},{y:0.0710,x:1482730200000},{y:0.0620,x:1482731100000},{y:0.0540,x:1482732000000},{y:0.0540,x:1482732900000},{y:0.0620,x:1482733800000},{y:0.0700,x:1482734700000},{y:0.0650,x:1482735600000},{y:0.2600,x:1482736500000},{y:0.3510,x:1482737400000},{y:0.3370,x:1482738300000},{y:0.1590,x:1482739200000},{y:0.0810,x:1482740100000},{y:0.1490,x:1482741000000},{y:0.1820,x:1482741900000},{y:0.1710,x:1482742800000},{y:0.1420,x:1482743700000},{y:0.0920,x:1482744600000},{y:0.1110,x:1482745500000},{y:0.2880,x:1482746400000},{y:0.3100,x:1482747300000},{y:0.3040,x:1482748200000},{y:0.2940,x:1482749100000},{y:0.2930,x:1482750000000},{y:0.2930,x:1482750900000},{y:0.2930,x:1482751800000},{y:0.2870,x:1482752700000},{y:0.2870,x:1482753600000},{y:0.2910,x:1482754500000},{y:0.2140,x:1482755400000},{y:0.1680,x:1482756300000},{y:0.1600,x:1482757200000},{y:0.2530,x:1482758100000},{y:0.2900,x:1482759000000},{y:0.2890,x:1482759900000},{y:0.2880,x:1482760800000},{y:0.2900,x:1482761700000},{y:0.2890,x:1482762600000},{y:0.1730,x:1482763500000},{y:0.1720,x:1482764400000},{y:0.2910,x:1482765300000},{y:0.2900,x:1482766200000},{y:0.2310,x:1482767100000},{y:0.1560,x:1482768000000},{y:0.1520,x:1482768900000},{y:0.0960,x:1482769800000},{y:0.1040,x:1482770700000},{y:0.2230,x:1482771600000},{y:0.2210,x:1482772500000},{y:0.2430,x:1482773400000},{y:0.1790,x:1482774300000},{y:0.2970,x:1482775200000},{y:0.2940,x:1482776100000},{y:0.2940,x:1482777000000},{y:0.2980,x:1482777900000},{y:0.2990,x:1482778800000},{y:0.1950,x:1482779700000},{y:0.1500,x:1482780600000},{y:0.1290,x:1482781500000},{y:0.2070,x:1482782400000},{y:0.1370,x:1482783300000},{y:0.1050,x:1482784200000},{y:0.0980,x:1482785100000},{y:0.1060,x:1482786000000},{y:0.1130,x:1482786900000},{y:0.1120,x:1482787800000},{y:0.0790,x:1482788700000},{y:0.0870,x:1482789600000},{y:0.0970,x:1482790500000},{y:0.0940,x:1482791400000},{y:0.0390,x:1482792300000},{y:0.0400,x:1482793200000},{y:0.0460,x:1482794100000},{y:0.0610,x:1482795000000},{y:0.0480,x:1482795900000},{y:0.0400,x:1482796800000},{y:0.0430,x:1482797700000},{y:0.0530,x:1482798600000},{y:0.0550,x:1482799500000},{y:0.0400,x:1482800400000},{y:0.0420,x:1482801300000},{y:0.0480,x:1482802200000},{y:0.0620,x:1482803100000},{y:0.0440,x:1482804000000},{y:0.0450,x:1482804900000},{y:0.0570,x:1482805800000},{y:0.0550,x:1482806700000},{y:0.0480,x:1482807600000},{y:0.0420,x:1482808500000},{y:0.0470,x:1482809400000},{y:0.0480,x:1482810300000},{y:0.0710,x:1482811200000},{y:0.0560,x:1482812100000},{y:0.0520,x:1482813000000},{y:0.0580,x:1482813900000},{y:0.0580,x:1482814800000},{y:0.0580,x:1482815700000},{y:0.0540,x:1482816600000},{y:0.0590,x:1482817500000},{y:0.0540,x:1482818400000},{y:0.0510,x:1482819300000},{y:0.0480,x:1482820200000},{y:0.0570,x:1482821100000},{y:0.0560,x:1482822000000},{y:0.0690,x:1482822900000},{y:0.2550,x:1482823800000},{y:0.2550,x:1482824700000},{y:0.2490,x:1482825600000},{y:0.1600,x:1482826500000},{y:0.2490,x:1482827400000},{y:0.2480,x:1482828300000},{y:0.2430,x:1482829200000},{y:0.2320,x:1482830100000},{y:0.2320,x:1482831000000},{y:0.2360,x:1482831900000},{y:0.2270,x:1482832800000},{y:0.2210,x:1482833700000},{y:0.2190,x:1482834600000},{y:0.2160,x:1482835500000},{y:0.2200,x:1482836400000},{y:0.2200,x:1482837300000},{y:0.2160,x:1482838200000},{y:0.2260,x:1482839100000},{y:0.2300,x:1482840000000},{y:0.2350,x:1482840900000},{y:0.1210,x:1482841800000},{y:0.0700,x:1482842700000},{y:0.0900,x:1482843600000},{y:0.0780,x:1482844500000},{y:0.0550,x:1482845400000},{y:0.0510,x:1482846300000},{y:0.0680,x:1482847200000},{y:0.0600,x:1482848100000},{y:0.0500,x:1482849000000},{y:0.0500,x:1482849900000},{y:0.0620,x:1482850800000},{y:0.0640,x:1482851700000},{y:0.0520,x:1482852600000},{y:0.0490,x:1482853500000},{y:0.0630,x:1482854400000},{y:0.1680,x:1482855300000},{y:0.2240,x:1482856200000},{y:0.2190,x:1482857100000},{y:0.2150,x:1482858000000},{y:0.2080,x:1482858900000},{y:0.2000,x:1482859800000},{y:0.2000,x:1482860700000},{y:0.2200,x:1482861600000},{y:0.2420,x:1482862500000},{y:0.2470,x:1482863400000},{y:0.2450,x:1482864300000},{y:0.2380,x:1482865200000},{y:0.1520,x:1482866100000},{y:0.2980,x:1482867000000},{y:0.1770,x:1482867900000},{y:0.1030,x:1482868800000},{y:0.0950,x:1482869700000},{y:0.1070,x:1482870600000},{y:0.1150,x:1482871500000},{y:0.1300,x:1482872400000},{y:0.0830,x:1482873300000},{y:0.1000,x:1482874200000},{y:0.0880,x:1482875100000},{y:0.0820,x:1482876000000},{y:0.0720,x:1482876900000},{y:0.0570,x:1482877800000},{y:0.0540,x:1482878700000},{y:0.0430,x:1482879600000},{y:0.0440,x:1482880500000},{y:0.0490,x:1482881400000},{y:0.0480,x:1482882300000},{y:0.0430,x:1482883200000},{y:0.0400,x:1482884100000},{y:0.0460,x:1482885000000},{y:0.0480,x:1482885900000},{y:0.0590,x:1482886800000},{y:0.0470,x:1482887700000},{y:0.0430,x:1482888600000},{y:0.0490,x:1482889500000},{y:0.0590,x:1482890400000},{y:0.0430,x:1482891300000},{y:0.0410,x:1482892200000},{y:0.0490,x:1482893100000},{y:0.0610,x:1482894000000},{y:0.0480,x:1482894900000},{y:0.0420,x:1482895800000},{y:0.0480,x:1482896700000},{y:0.0690,x:1482897600000},{y:0.0620,x:1482898500000},{y:0.0490,x:1482899400000},{y:0.0550,x:1482900300000},{y:0.0630,x:1482901200000},{y:0.0650,x:1482902100000},{y:0.0500,x:1482903000000},{y:0.0520,x:1482903900000},{y:0.0570,x:1482904800000},{y:0.0690,x:1482905700000},{y:0.0490,x:1482906600000},{y:0.0490,x:1482907500000},{y:0.0570,x:1482908400000},{y:0.0700,x:1482909300000},{y:0.5120,x:1482910200000},{y:0.3310,x:1482911100000},{y:0.1020,x:1482912000000},{y:0.0690,x:1482912900000},{y:0.0830,x:1482913800000},{y:0.0860,x:1482914700000},{y:0.0720,x:1482915600000},{y:0.0600,x:1482916500000},{y:0.0670,x:1482917400000},{y:0.0590,x:1482918300000},{y:0.0630,x:1482919200000},{y:0.0620,x:1482920100000},{y:0.0650,x:1482921000000},{y:0.0410,x:1482921900000},{y:0.2380,x:1482922800000},{y:0.5390,x:1482923700000},{y:0.1070,x:1482924600000},{y:0.0560,x:1482925500000},{y:0.1470,x:1482926400000},{y:0.1640,x:1482927300000},{y:0.1660,x:1482928200000},{y:0.1560,x:1482929100000},{y:0.1220,x:1482930000000},{y:0.1290,x:1482930900000},{y:0.1220,x:1482931800000},{y:0.1160,x:1482932700000},{y:0.1890,x:1482933600000},{y:0.1150,x:1482934500000},{y:0.1360,x:1482935400000},{y:0.1400,x:1482936300000},{y:0.1160,x:1482937200000},{y:0.0820,x:1482938100000},{y:0.0610,x:1482939000000},{y:0.0580,x:1482939900000},{y:0.0740,x:1482940800000},{y:0.1070,x:1482941700000},{y:0.1630,x:1482942600000},{y:0.2080,x:1482943500000},{y:0.1950,x:1482944400000},{y:0.4230,x:1482945300000},{y:0.5040,x:1482946200000},{y:0.4910,x:1482947100000},{y:0.2010,x:1482948000000},{y:0.1170,x:1482948900000},{y:0.4500,x:1482949800000},{y:0.0850,x:1482950700000},{y:0.1210,x:1482951600000},{y:0.1080,x:1482952500000},{y:0.0970,x:1482953400000},{y:0.0890,x:1482954300000},{y:0.0820,x:1482955200000},{y:0.3590,x:1482956100000},{y:0.4260,x:1482957000000},{y:0.1310,x:1482957900000},{y:0.1330,x:1482958800000},{y:0.1190,x:1482959700000},{y:0.1170,x:1482960600000},{y:0.1050,x:1482961500000},{y:0.1080,x:1482962400000},{y:0.0790,x:1482963300000},{y:0.0460,x:1482964200000},{y:0.0590,x:1482965100000},{y:0.0520,x:1482966000000},{y:0.0520,x:1482966900000},{y:0.0430,x:1482967800000},{y:0.0560,x:1482968700000},{y:0.0560,x:1482969600000},{y:0.0500,x:1482970500000},{y:0.0430,x:1482971400000},{y:0.0470,x:1482972300000},{y:0.0590,x:1482973200000},{y:0.0520,x:1482974100000},{y:0.0450,x:1482975000000},{y:0.0440,x:1482975900000},{y:0.0600,x:1482976800000},{y:0.0530,x:1482977700000},{y:0.0480,x:1482978600000},{y:0.0440,x:1482979500000},{y:0.0550,x:1482980400000},{y:0.0620,x:1482981300000},{y:0.0510,x:1482982200000},{y:0.0440,x:1482983100000},{y:0.0590,x:1482984000000},{y:0.0770,x:1482984900000},{y:0.0610,x:1482985800000},{y:0.0530,x:1482986700000},{y:0.0530,x:1482987600000},{y:0.0630,x:1482988500000},{y:0.0690,x:1482989400000},{y:0.0540,x:1482990300000},{y:0.0530,x:1482991200000},{y:0.0570,x:1482992100000},{y:0.0690,x:1482993000000},{y:0.0760,x:1482993900000},{y:0.3260,x:1482994800000},{y:0.3080,x:1482995700000},{y:0.0790,x:1482996600000},{y:0.1030,x:1482997500000},{y:0.0740,x:1482998400000},{y:0.0730,x:1482999300000},{y:0.0930,x:1483000200000},{y:0.0920,x:1483001100000},{y:0.0630,x:1483002000000},{y:0.0620,x:1483002900000},{y:0.0610,x:1483003800000},{y:0.0620,x:1483004700000},{y:0.0800,x:1483005600000},{y:0.0870,x:1483006500000},{y:0.0580,x:1483007400000},{y:0.0680,x:1483008300000},{y:0.0660,x:1483009200000},{y:0.0610,x:1483010100000},{y:0.0560,x:1483011000000},{y:0.1220,x:1483011900000},{y:0.1420,x:1483012800000},{y:0.1390,x:1483013700000},{y:0.1470,x:1483014600000},{y:0.1410,x:1483015500000},{y:0.0870,x:1483016400000},{y:0.0580,x:1483017300000},{y:0.0580,x:1483018200000},{y:0.0580,x:1483019100000},{y:0.0510,x:1483020000000},{y:0.0570,x:1483020900000},{y:0.0600,x:1483021800000},{y:0.0630,x:1483022700000},{y:0.0520,x:1483023600000},{y:0.0550,x:1483024500000},{y:0.0580,x:1483025400000},{y:0.0590,x:1483026300000},{y:0.0640,x:1483027200000},{y:0.1680,x:1483028100000},{y:0.3830,x:1483029000000},{y:0.2640,x:1483029900000},{y:0.0050,x:1483034400000},{y:0.0770,x:1483035300000},{y:0.1330,x:1483036200000},{y:0.1620,x:1483037100000},{y:0.4700,x:1483038000000},{y:0.4620,x:1483038900000},{y:0.4410,x:1483039800000},{y:0.2070,x:1483040700000},{y:0.2020,x:1483041600000},{y:0.2040,x:1483042500000},{y:0.2070,x:1483043400000},{y:0.1220,x:1483044300000},{y:0.1210,x:1483045200000},{y:0.1220,x:1483046100000},{y:0.1060,x:1483047000000},{y:0.0930,x:1483047900000},{y:0.0560,x:1483048800000},{y:0.0540,x:1483049700000},{y:0.0630,x:1483050600000},{y:0.0410,x:1483051500000},{y:0.0450,x:1483052400000},{y:0.0390,x:1483053300000},{y:0.0380,x:1483054200000},{y:0.0400,x:1483055100000},{y:0.0420,x:1483056000000},{y:0.0360,x:1483056900000},{y:0.0450,x:1483057800000},{y:0.0540,x:1483058700000},{y:0.0510,x:1483059600000},{y:0.0520,x:1483060500000},{y:0.0640,x:1483061400000},{y:0.0840,x:1483062300000},{y:0.0510,x:1483063200000},{y:0.0590,x:1483064100000},{y:0.0580,x:1483065000000},{y:0.0580,x:1483065900000},{y:0.0530,x:1483066800000},{y:0.0460,x:1483067700000},{y:0.0530,x:1483068600000},{y:0.0520,x:1483069500000},{y:0.0670,x:1483070400000},{y:0.0480,x:1483071300000},{y:0.0480,x:1483072200000},{y:0.0520,x:1483073100000},{y:0.0500,x:1483074000000},{y:0.0440,x:1483074900000},{y:0.0460,x:1483075800000},{y:0.0520,x:1483076700000},{y:0.0590,x:1483077600000},{y:0.0440,x:1483078500000},{y:0.0500,x:1483079400000},{y:0.0620,x:1483080300000},{y:0.0500,x:1483081200000},{y:0.0450,x:1483082100000},{y:0.0450,x:1483083000000},{y:0.3200,x:1483083900000},{y:0.1840,x:1483084800000},{y:0.0610,x:1483085700000},{y:0.3940,x:1483086600000},{y:0.4370,x:1483087500000},{y:0.1380,x:1483088400000},{y:0.0980,x:1483089300000},{y:0.1130,x:1483090200000},{y:0.0890,x:1483091100000},{y:0.1570,x:1483092000000},{y:0.3520,x:1483092900000},{y:0.4240,x:1483093800000},{y:0.4170,x:1483094700000},{y:0.4170,x:1483095600000},{y:0.1340,x:1483096500000},{y:0.0860,x:1483097400000},{y:0.0880,x:1483098300000},{y:0.1030,x:1483099200000},{y:0.0540,x:1483100100000},{y:0.0560,x:1483101000000},{y:0.0560,x:1483101900000},{y:0.0550,x:1483102800000},{y:0.0460,x:1483103700000},{y:0.0520,x:1483104600000},{y:0.0510,x:1483105500000},{y:0.0560,x:1483106400000},{y:0.0430,x:1483107300000},{y:0.1200,x:1483108200000},{y:0.1350,x:1483109100000},{y:0.1560,x:1483110000000},{y:0.2580,x:1483110900000},{y:0.0590,x:1483111800000},{y:0.0690,x:1483112700000},{y:0.0790,x:1483113600000},{y:0.1240,x:1483114500000},{y:0.1200,x:1483115400000},{y:0.1040,x:1483116300000},{y:0.0930,x:1483117200000},{y:0.2780,x:1483118100000},{y:0.1550,x:1483119000000},{y:0.1360,x:1483119900000},{y:0.1790,x:1483120800000},{y:0.0910,x:1483121700000},{y:0.0970,x:1483122600000},{y:0.0970,x:1483123500000},{y:0.0910,x:1483124400000},{y:0.0930,x:1483125300000},{y:0.1120,x:1483126200000},{y:0.1370,x:1483127100000},{y:0.1000,x:1483128000000},{y:0.1200,x:1483128900000},{y:0.1070,x:1483129800000},{y:0.0990,x:1483130700000},{y:0.1140,x:1483131600000},{y:0.1020,x:1483132500000},{y:0.0850,x:1483133400000},{y:0.0420,x:1483134300000},{y:0.0430,x:1483135200000},{y:0.0360,x:1483136100000},{y:0.0400,x:1483137000000},{y:0.0430,x:1483137900000},{y:0.0410,x:1483138800000},{y:0.0410,x:1483139700000},{y:0.0360,x:1483140600000},{y:0.0340,x:1483141500000},{y:0.0460,x:1483142400000},{y:0.0510,x:1483143300000},{y:0.0440,x:1483144200000},{y:0.0420,x:1483145100000},{y:0.0490,x:1483146000000},{y:0.0540,x:1483146900000},{y:0.0510,x:1483147800000},{y:0.0470,x:1483148700000},{y:0.0480,x:1483149600000},{y:0.0550,x:1483150500000},{y:0.0490,x:1483151400000},{y:0.0570,x:1483152300000},{y:0.0460,x:1483153200000},{y:0.0580,x:1483154100000},{y:0.0460,x:1483155000000},{y:0.0510,x:1483155900000},{y:0.0490,x:1483156800000},{y:0.0520,x:1483157700000},{y:0.0490,x:1483158600000},{y:0.0470,x:1483159500000},{y:0.0520,x:1483160400000},{y:0.0500,x:1483161300000},{y:0.0630,x:1483162200000},{y:0.0430,x:1483163100000},{y:0.0520,x:1483164000000},{y:0.0500,x:1483164900000},{y:0.0520,x:1483165800000},{y:0.0480,x:1483166700000},{y:0.0450,x:1483167600000},{y:0.0510,x:1483168500000},{y:0.0490,x:1483169400000},{y:0.2710,x:1483170300000},{y:0.4230,x:1483171200000},{y:0.2680,x:1483172100000},{y:0.0720,x:1483173000000},{y:0.0780,x:1483173900000},{y:0.0950,x:1483174800000},{y:0.1100,x:1483175700000},{y:0.3150,x:1483176600000},{y:0.3140,x:1483177500000},{y:0.2670,x:1483178400000},{y:0.3620,x:1483179300000},{y:0.4190,x:1483180200000},{y:0.4210,x:1483181100000},{y:0.4130,x:1483182000000},{y:0.4140,x:1483182900000},{y:0.4080,x:1483183800000},{y:0.4040,x:1483184700000},{y:0.3980,x:1483185600000},{y:0.3990,x:1483186500000},{y:0.1810,x:1483187400000},{y:0.1270,x:1483188300000},{y:0.1050,x:1483189200000},{y:0.0670,x:1483190100000},{y:0.3920,x:1483191000000},{y:0.2180,x:1483191900000},{y:0.0430,x:1483192800000},{y:0.0420,x:1483193700000},{y:0.0600,x:1483194600000},{y:0.0510,x:1483195500000},{y:0.0440,x:1483196400000},{y:0.0490,x:1483197300000},{y:0.0520,x:1483198200000},{y:0.0580,x:1483199100000},{y:0.0510,x:1483200000000},{y:0.0430,x:1483200900000},{y:0.0430,x:1483201800000},{y:0.0470,x:1483202700000},{y:0.0540,x:1483203600000},{y:0.2020,x:1483204500000},{y:0.3820,x:1483205400000},{y:0.3770,x:1483206300000},{y:0.3780,x:1483207200000},{y:0.3100,x:1483208100000},{y:0.0750,x:1483209000000},{y:0.1160,x:1483209900000},{y:0.1120,x:1483210800000},{y:0.1210,x:1483211700000},{y:0.1630,x:1483212600000},{y:0.0630,x:1483213500000},{y:0.0490,x:1483214400000},{y:0.1160,x:1483215300000},{y:0.1240,x:1483216200000},{y:0.1850,x:1483217100000},{y:0.1710,x:1483218000000},{y:0.1120,x:1483218900000},{y:0.1240,x:1483219800000},{y:0.1230,x:1483220700000},{y:0.1080,x:1483221600000},{y:0.0900,x:1483222500000},{y:0.1050,x:1483223400000},{y:0.1020,x:1483224300000},{y:0.0970,x:1483225200000},{y:0.0760,x:1483226100000},{y:0.0570,x:1483227000000},{y:0.0400,x:1483227900000},{y:0.0500,x:1483228800000},{y:0.0540,x:1483229700000},{y:0.0370,x:1483230600000},{y:0.0350,x:1483231500000},{y:0.0470,x:1483232400000},{y:0.0410,x:1483233300000},{y:0.0540,x:1483234200000},{y:0.0450,x:1483235100000},{y:0.0500,x:1483236000000},{y:0.0510,x:1483236900000},{y:0.0500,x:1483237800000},{y:0.0500,x:1483238700000},{y:0.0680,x:1483239600000},{y:0.0800,x:1483240500000},{y:0.0500,x:1483241400000},{y:0.0580,x:1483242300000},{y:0.0590,x:1483243200000},{y:0.0560,x:1483244100000},{y:0.0510,x:1483245000000},{y:0.0460,x:1483245900000},{y:0.0510,x:1483246800000},{y:0.0530,x:1483247700000},{y:0.0670,x:1483248600000},{y:0.0490,x:1483249500000},{y:0.0460,x:1483250400000},{y:0.0510,x:1483251300000},{y:0.0760,x:1483252200000},{y:0.0520,x:1483253100000},{y:0.0480,x:1483254000000},{y:0.0460,x:1483254900000},{y:0.1770,x:1483255800000},{y:0.3400,x:1483256700000},{y:0.1380,x:1483257600000},{y:0.0590,x:1483258500000},{y:0.0560,x:1483259400000},{y:0.0700,x:1483260300000},{y:0.0620,x:1483261200000},{y:0.0520,x:1483262100000},{y:0.0950,x:1483263000000},{y:0.1000,x:1483263900000},{y:0.1020,x:1483264800000},{y:0.0550,x:1483265700000},{y:0.0570,x:1483266600000},{y:0.1900,x:1483267500000},{y:0.3320,x:1483268400000},{y:0.2140,x:1483269300000},{y:0.3290,x:1483270200000},{y:0.3280,x:1483271100000},{y:0.2550,x:1483272000000},{y:0.3280,x:1483272900000},{y:0.2220,x:1483273800000},{y:0.0870,x:1483274700000},{y:0.0940,x:1483275600000},{y:0.3060,x:1483276500000},{y:0.3220,x:1483277400000},{y:0.2900,x:1483278300000},{y:0.1520,x:1483279200000},{y:0.1570,x:1483280100000},{y:0.1320,x:1483281000000},{y:0.0770,x:1483281900000},{y:0.1020,x:1483282800000},{y:0.1130,x:1483283700000},{y:0.1090,x:1483284600000},{y:0.0870,x:1483285500000},{y:0.0890,x:1483286400000},{y:0.2000,x:1483287300000},{y:0.2370,x:1483288200000},{y:0.1630,x:1483289100000},{y:0.1780,x:1483290000000},{y:0.2000,x:1483290900000},{y:0.2920,x:1483291800000},{y:0.2980,x:1483292700000},{y:0.2950,x:1483293600000},{y:0.2980,x:1483294500000},{y:0.2960,x:1483295400000},{y:0.2990,x:1483296300000},{y:0.2030,x:1483297200000},{y:0.0940,x:1483298100000},{y:0.0740,x:1483299000000},{y:0.0970,x:1483299900000},{y:0.0840,x:1483300800000},{y:0.0990,x:1483301700000},{y:0.1140,x:1483302600000},{y:0.0970,x:1483303500000},{y:0.1060,x:1483304400000},{y:0.0940,x:1483305300000},{y:0.0490,x:1483306200000},{y:0.0620,x:1483307100000},{y:0.0630,x:1483308000000},{y:0.0490,x:1483308900000},{y:0.0340,x:1483309800000},{y:0.0340,x:1483310700000},{y:0.0400,x:1483311600000},{y:0.0530,x:1483312500000},{y:0.0370,x:1483313400000},{y:0.0350,x:1483314300000},{y:0.0360,x:1483315200000},{y:0.0490,x:1483316100000},{y:0.0470,x:1483317000000},{y:0.0350,x:1483317900000},{y:0.0360,x:1483318800000},{y:0.0380,x:1483319700000},{y:0.0590,x:1483320600000},{y:0.0490,x:1483321500000},{y:0.0450,x:1483322400000},{y:0.0450,x:1483323300000},{y:0.0540,x:1483324200000},{y:0.0600,x:1483325100000},{y:0.0440,x:1483326000000},{y:0.0460,x:1483326900000},{y:0.0450,x:1483327800000},{y:0.0510,x:1483328700000},{y:0.0580,x:1483329600000},{y:0.0470,x:1483330500000},{y:0.0480,x:1483331400000},{y:0.0930,x:1483332300000},{y:0.1250,x:1483333200000},{y:0.2860,x:1483334100000},{y:0.2570,x:1483335000000},{y:0.1090,x:1483335900000},{y:0.0780,x:1483336800000},{y:0.0510,x:1483337700000},{y:0.0450,x:1483338600000},{y:0.0420,x:1483339500000},{y:0.0540,x:1483340400000},{y:0.0480,x:1483341300000},{y:0.0490,x:1483342200000},{y:0.0420,x:1483343100000},{y:0.0520,x:1483344000000},{y:0.0460,x:1483344900000},{y:0.0470,x:1483345800000},{y:0.0490,x:1483346700000},{y:0.0390,x:1483347600000},{y:0.0550,x:1483348500000},{y:0.0470,x:1483349400000},{y:0.0520,x:1483350300000},{y:0.0490,x:1483351200000},{y:0.0580,x:1483352100000},{y:0.0480,x:1483353000000},{y:0.0480,x:1483353900000},{y:0.0500,x:1483354800000},{y:0.0490,x:1483355700000},{y:0.0480,x:1483356600000},{y:0.0410,x:1483357500000},{y:0.0490,x:1483358400000},{y:0.0480,x:1483359300000},{y:0.0550,x:1483360200000},{y:0.0410,x:1483361100000},{y:0.0430,x:1483362000000},{y:0.0420,x:1483362900000},{y:0.0550,x:1483363800000},{y:0.0470,x:1483364700000},{y:0.0410,x:1483365600000},{y:0.0460,x:1483366500000},{y:0.0460,x:1483367400000},{y:0.0550,x:1483368300000},{y:0.0430,x:1483369200000},{y:0.1190,x:1483370100000},{y:0.0750,x:1483371000000},{y:0.0970,x:1483371900000},{y:0.1190,x:1483372800000},{y:0.1100,x:1483373700000},{y:0.2420,x:1483374600000},{y:0.3510,x:1483375500000},{y:0.3470,x:1483376400000},{y:0.3450,x:1483377300000},{y:0.3450,x:1483378200000},{y:0.3420,x:1483379100000},{y:0.3380,x:1483380000000},{y:0.2270,x:1483380900000},{y:0.1380,x:1483381800000},{y:0.0960,x:1483382700000},{y:0.1770,x:1483383600000},{y:0.3340,x:1483384500000},{y:0.3070,x:1483385400000},{y:0.0870,x:1483386300000},{y:0.0760,x:1483387200000},{y:0.0890,x:1483388100000},{y:0.0800,x:1483389000000},{y:0.0600,x:1483389900000},{y:0.0470,x:1483390800000},{y:0.0500,x:1483391700000},{y:0.0530,x:1483392600000},{y:0.0500,x:1483393500000},{y:0.0430,x:1483394400000},{y:0.0320,x:1483395300000},{y:0.0430,x:1483396200000},{y:0.0470,x:1483397100000},{y:0.0510,x:1483398000000},{y:0.0390,x:1483398900000},{y:0.0330,x:1483399800000},{y:0.0400,x:1483400700000},{y:0.0510,x:1483401600000},{y:0.0410,x:1483402500000},{y:0.0390,x:1483403400000},{y:0.0430,x:1483404300000},{y:0.0540,x:1483405200000},{y:0.0580,x:1483406100000},{y:0.0400,x:1483407000000},{y:0.0430,x:1483407900000},{y:0.0460,x:1483408800000},{y:0.0610,x:1483409700000},{y:0.0460,x:1483410600000},{y:0.0420,x:1483411500000},{y:0.0420,x:1483412400000},{y:0.0570,x:1483413300000},{y:0.0540,x:1483414200000},{y:0.0440,x:1483415100000},{y:0.0520,x:1483416000000},{y:0.0500,x:1483416900000},{y:0.0620,x:1483417800000},{y:0.0870,x:1483418700000},{y:0.1130,x:1483419600000},{y:0.3340,x:1483420500000},{y:0.1510,x:1483421400000},{y:0.1230,x:1483422300000},{y:0.1070,x:1483423200000},{y:0.0910,x:1483424100000},{y:0.0480,x:1483425000000},{y:0.0490,x:1483425900000},{y:0.0500,x:1483426800000},{y:0.0470,x:1483427700000},{y:0.0370,x:1483428600000},{y:0.0560,x:1483429500000},{y:0.0430,x:1483430400000},{y:0.0510,x:1483431300000},{y:0.0470,x:1483432200000},{y:0.0520,x:1483433100000},{y:0.0480,x:1483434000000},{y:0.0470,x:1483434900000},{y:0.0510,x:1483435800000},{y:0.0500,x:1483436700000},{y:0.0840,x:1483437600000},{y:0.0390,x:1483438500000},{y:0.0130,x:1483439400000},{y:0.0260,x:1483440300000},{y:0.0540,x:1483441200000},{y:0.0460,x:1483442100000},{y:0.0430,x:1483443000000},{y:0.0430,x:1483443900000},{y:0.0510,x:1483444800000},{y:0.0610,x:1483445700000},{y:0.0470,x:1483446600000},{y:0.0420,x:1483447500000},{y:0.0350,x:1483448400000},{y:0.0550,x:1483449300000},{y:0.0580,x:1483450200000},{y:0.0390,x:1483451100000},{y:0.0440,x:1483452000000},{y:0.0490,x:1483452900000},{y:0.0620,x:1483453800000},{y:0.0480,x:1483454700000},{y:0.0450,x:1483455600000},{y:0.0610,x:1483456500000},{y:0.1790,x:1483457400000},{y:0.2020,x:1483458300000},{y:0.2280,x:1483459200000},{y:0.2330,x:1483460100000},{y:0.2220,x:1483461000000},{y:0.2210,x:1483461900000},{y:0.2180,x:1483462800000},{y:0.2140,x:1483463700000},{y:0.2140,x:1483464600000},{y:0.2170,x:1483465500000},{y:0.2130,x:1483466400000},{y:0.2110,x:1483467300000},{y:0.2090,x:1483468200000},{y:0.2060,x:1483469100000},{y:0.2080,x:1483470000000},{y:0.1250,x:1483470900000},{y:0.0950,x:1483471800000},{y:0.0820,x:1483472700000},{y:0.2270,x:1483473600000},{y:0.0700,x:1483474500000},{y:0.0680,x:1483475400000},{y:0.0560,x:1483476300000},{y:0.0410,x:1483477200000},{y:0.0390,x:1483478100000},{y:0.0370,x:1483479000000},{y:0.0560,x:1483479900000},{y:0.0640,x:1483480800000},{y:0.0400,x:1483481700000},{y:0.0410,x:1483482600000},{y:0.0470,x:1483483500000},{y:0.0510,x:1483484400000},{y:0.0400,x:1483485300000},{y:0.0370,x:1483486200000},{y:0.0390,x:1483487100000},{y:0.0530,x:1483488000000},{y:0.0450,x:1483488900000},{y:0.0380,x:1483489800000},{y:0.0360,x:1483490700000},{y:0.0470,x:1483491600000},{y:0.0490,x:1483492500000},{y:0.0450,x:1483493400000},{y:0.0380,x:1483494300000},{y:0.0430,x:1483495200000},{y:0.0470,x:1483496100000},{y:0.0460,x:1483497000000},{y:0.0430,x:1483497900000},{y:0.0380,x:1483498800000},{y:0.0510,x:1483499700000},{y:0.0450,x:1483500600000},{y:0.0450,x:1483501500000},{y:0.0540,x:1483502400000},{y:0.0570,x:1483503300000},{y:0.0580,x:1483504200000},{y:0.0910,x:1483505100000},{y:0.1760,x:1483506000000},{y:0.2290,x:1483506900000},{y:0.2310,x:1483507800000},{y:0.0920,x:1483508700000},{y:0.0730,x:1483509600000},{y:0.0620,x:1483510500000},{y:0.0660,x:1483511400000},{y:0.0770,x:1483512300000},{y:0.1560,x:1483513200000},{y:0.1110,x:1483514100000},{y:0.0540,x:1483515000000},{y:0.0610,x:1483515900000},{y:0.0480,x:1483516800000},{y:0.0440,x:1483517700000},{y:0.0470,x:1483518600000},{y:0.0490,x:1483519500000},{y:0.0490,x:1483520400000},{y:0.0440,x:1483521300000},{y:0.0440,x:1483522200000},{y:0.0540,x:1483523100000},{y:0.0620,x:1483524000000},{y:0.0450,x:1483524900000},{y:0.0450,x:1483525800000},{y:0.0450,x:1483526700000},{y:0.0650,x:1483527600000},{y:0.0550,x:1483528500000},{y:0.0480,x:1483529400000},{y:0.0470,x:1483530300000},{y:0.0550,x:1483531200000},{y:0.0630,x:1483532100000},{y:0.0500,x:1483533000000},{y:0.0480,x:1483533900000},{y:0.0480,x:1483534800000},{y:0.0630,x:1483535700000},{y:0.0550,x:1483536600000},{y:0.0470,x:1483537500000},{y:0.0440,x:1483538400000},{y:0.0510,x:1483539300000},{y:0.0570,x:1483540200000},{y:0.0550,x:1483541100000},{y:0.0460,x:1483542000000},{y:0.0860,x:1483542900000},{y:0.1290,x:1483543800000},{y:0.1830,x:1483544700000},{y:0.1680,x:1483545600000},{y:0.0780,x:1483546500000},{y:0.0510,x:1483547400000},{y:0.0600,x:1483548300000},{y:0.0570,x:1483549200000},{y:0.0490,x:1483550100000},{y:0.0460,x:1483551000000},{y:0.0570,x:1483551900000},{y:0.0620,x:1483552800000},{y:0.0510,x:1483553700000},{y:0.0460,x:1483554600000},{y:0.0470,x:1483555500000},{y:0.0610,x:1483556400000},{y:0.0510,x:1483557300000},{y:0.0470,x:1483558200000},{y:0.0900,x:1483559100000},{y:0.1180,x:1483560000000},{y:0.2200,x:1483560900000},{y:0.1550,x:1483561800000},{y:0.1190,x:1483562700000},{y:0.1200,x:1483563600000},{y:0.1260,x:1483564500000},{y:0.1120,x:1483565400000},{y:0.0940,x:1483566300000},{y:0.0570,x:1483567200000},{y:0.0680,x:1483568100000},{y:0.0540,x:1483569000000},{y:0.0450,x:1483569900000},{y:0.0350,x:1483570800000},{y:0.0350,x:1483571700000},{y:0.0460,x:1483572600000},{y:0.0470,x:1483573500000},{y:0.0380,x:1483574400000},{y:0.0340,x:1483575300000},{y:0.0390,x:1483576200000},{y:0.0600,x:1483577100000},{y:0.0520,x:1483578000000},{y:0.0440,x:1483578900000},{y:0.0440,x:1483579800000},{y:0.0450,x:1483580700000},{y:0.0530,x:1483581600000},{y:0.0480,x:1483582500000},{y:0.0770,x:1483583400000},{y:0.0610,x:1483584300000},{y:0.0640,x:1483585200000},{y:0.0630,x:1483586100000},{y:0.0590,x:1483587000000},{y:0.0500,x:1483587900000},{y:0.0590,x:1483588800000},{y:0.0560,x:1483589700000},{y:0.0550,x:1483590600000},{y:0.0560,x:1483591500000},{y:0.1300,x:1483592400000},{y:0.2160,x:1483593300000},{y:0.2140,x:1483594200000},{y:0.1250,x:1483595100000},{y:0.0670,x:1483596000000},{y:0.0820,x:1483596900000},{y:0.0480,x:1483597800000},{y:0.0480,x:1483598700000},{y:0.0490,x:1483599600000},{y:0.0280,x:1483600500000},{y:0.0010,x:1483601400000},{y:0.0420,x:1483602300000},{y:0.0520,x:1483603200000},{y:0.0500,x:1483604100000},{y:0.0530,x:1483605000000},{y:0.0470,x:1483605900000},{y:0.0500,x:1483606800000},{y:0.0520,x:1483607700000},{y:0.0510,x:1483608600000},{y:0.0520,x:1483609500000},{y:0.0420,x:1483610400000},{y:0.0520,x:1483611300000},{y:0.0510,x:1483612200000},{y:0.0500,x:1483613100000},{y:0.0400,x:1483614000000},{y:0.0470,x:1483614900000},{y:0.0510,x:1483615800000},{y:0.0530,x:1483616700000},{y:0.0490,x:1483617600000},{y:0.0410,x:1483618500000},{y:0.0510,x:1483619400000},{y:0.0520,x:1483620300000},{y:0.0540,x:1483621200000},{y:0.0410,x:1483622100000},{y:0.0470,x:1483623000000},{y:0.0500,x:1483623900000},{y:0.0540,x:1483624800000},{y:0.0460,x:1483625700000},{y:0.0440,x:1483626600000},{y:0.0540,x:1483627500000},{y:0.0530,x:1483628400000},{y:0.0550,x:1483629300000},{y:0.1090,x:1483630200000},{y:0.1810,x:1483631100000},{y:0.1470,x:1483632000000},{y:0.1390,x:1483632900000},{y:0.0920,x:1483633800000},{y:0.0790,x:1483634700000},{y:0.1150,x:1483635600000},{y:0.2010,x:1483636500000},{y:0.3300,x:1483637400000},{y:0.3700,x:1483638300000},{y:0.3690,x:1483639200000},{y:0.1620,x:1483640100000},{y:0.1090,x:1483641000000},{y:0.0980,x:1483641900000},{y:0.0650,x:1483642800000},{y:0.0720,x:1483643700000},{y:0.0690,x:1483644600000},{y:0.0780,x:1483645500000},{y:0.0650,x:1483646400000},{y:0.0600,x:1483647300000},{y:0.0520,x:1483648200000},{y:0.0640,x:1483649100000},{y:0.0700,x:1483650000000},{y:0.0370,x:1483650900000}]
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
