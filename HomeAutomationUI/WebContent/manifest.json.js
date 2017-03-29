/**
 * Created by mertins on 28.10.15.
 */
{   
	"_version": "1.0.0",
    "sap.app": {
        "_version": "1.0.0",
        "id": "cm.homeautomation",
        "type": "application",
        "i18n": "i18n/i18n.properties",
        "title": "{{appTitle}}",
        "description": "{{appDescription}}",
        "applicationVersion": {
            "version": "1.0.0"
        },
        "ach": "CA-UI5-DOC",
        "dataSources": {
            "employeeRemote": {
                "uri": "/here/goes/your/serviceUrl/",
                "type": "OData",
                "settings": {
                    "odataVersion": "2.0",
                    "localUri": "localService/metadata.xml"
                }
            }
        }
    },
    "sap.ui": {
        "_version": "1.1.0",
        "technology": "UI5",
        "deviceTypes": {
            "desktop": true,
            "tablet": true,
            "phone": true
        },
        "supportedThemes": ["sap_bluecrystal"]
    },
    "sap.ui5": {
        "_version": "1.1.0",
        "rootView": "cm.homeautomation.Main",
        "dependencies": {
            "minUI5Version": "1.30",
            "libs": {
                "sap.m": {}
            }
        },
        "models": {
            "i18n": {
                "type": "sap.ui.model.resource.ResourceModel",
                "settings": {
                    "bundleName": "cm.homeautomation.i18n"
                }
            },
            "": {
                "dataSource": ""
            }
        },
        "routing": {
            "config": {
                "routerClass": "sap.m.routing.Router",
                "viewType": "XML",
                "viewPath": "cm.homeautomation",
                "controlId": "app",
                "controlAggregation": "pages",
                "transition": "slide",
                "bypassed": {
                    "target": "Main"
                }
            },
            "routes": [{
                "pattern": "",
                "name": "Main",
                "target": "Main"
            }, {
                "pattern": "roomDetail/{roomId}",
                "name": "roomDetail",
                "target": "roomDetail"
            }],
            "targets": {
                "Main": {
                    "viewName": "Main",
                    "viewLevel": 1
                },
                "roomDetail": {
                    "viewPath": "cm.homeautomation.RoomDetail",
                    "viewName": "RoomDetail",
                    "viewLevel": 2,
                    "transition": "show"
                }
            }
        }
    }
}