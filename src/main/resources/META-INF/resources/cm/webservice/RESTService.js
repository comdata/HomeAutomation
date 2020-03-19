/**
 * Created by mertins on 28.10.15.
 */

jQuery.sap.declare("cm.webservice.RESTService");

sap.ui.base.Object
    .extend(
    "cm.webservice.RESTService",
    {
        messageToast: null,
        /**
         * Constructor to create THWebservice object instance
         *
         * @memberof UI5Framework.businessLogic.THWebservices
         */
        constructor: function () {
            jQuery.sap.require("sap.m.MessageToast");
            this.messageToast = sap.m.MessageToast;
        },
        loadDataAsync: function (url, inModel, method, successCallback, errorCallBack, subject) {
            jQuery.sap.require("sap.ui.model.json.JSONModel");
            var model = new sap.ui.model.json.JSONModel();

            model.attachRequestCompleted(function (event) {
                var resultModel = new sap.ui.model.json.JSONModel();
                resultModel.setData(event.getSource().oData);

                if (successCallback != null) {

                    successCallback.apply(subject, [event, resultModel, event.getSource().oData]);
                }
            });

            if (!errorCallBack) {
                errorCallBack = function (event) {


                	subject.messageToast.show("Loading failed");
                }

            }
            model.attachRequestFailed(function (event) {
                errorCallBack(event);
            })


            model.loadData(url, inModel, true, method, false, false);
        }
    });
