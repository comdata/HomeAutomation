var application = null;
	var dataQueryRunning = false;
	var dataQuery;

	sap.ui.loader.config({async: true});
	
	sap.ui.getCore().attachInit(function() {
		application = new sap.m.App({
			/*metadata : {
				rootView : "cm.homeautomation.App",
				"routing" : {
					"config" : {
						"routerClass" : "sap.m.routing.Router",
						"viewType" : "XML",
						"viewPath" : "cm.homeautomation",
						"controlId" : "app",
						"controlAggregation" : "pages",
						"transition" : "slide",
						"bypassed" : {
							"target" : "Main"
						}
					},
					"routes" : [ {
						"pattern" : "",
						"name" : "Main",
						"target" : "Main"
					}, {
						"pattern" : "roomDetail/{roomId}",
						"name" : "roomDetail",
						"target" : "roomDetail"
					} ],
					"targets" : {
						"Main" : {
							"viewName" : "Main",
							"viewLevel" : 1
						},
						"roomDetail" : {
							"viewPath" : "cm.homeautomation.RoomDetail",
							"viewName" : "RoomDetail",
							"viewLevel" : 2,
							transition : "show"
						}
					}
				}
			},*/

			/*init : function() {
				UIComponent.prototype.init.apply(this, arguments);

				// Parse the current url and display the targets of the route that matches the hash
				this.getRouter().initialize();
			},*/
			pages : [
				new sap.m.Page({
					showHeader : false,
					enableScrolling : false,
					content : [
						new sap.ui.xmlview({
							viewName : "cm.homeautomation.Main",
							width : "100%",
							height : "100%"
						}) ]
				})
			],
			id : "app"
		}).placeAt("content");
	});