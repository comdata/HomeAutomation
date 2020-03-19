;(function () {
    "use strict";

    jQuery.sap.declare('cm.homeautomation.D3Chart');

    sap.ui.core.Control.extend("cm.homeautomation.D3Chart", {

        metadata: {
            properties : {
                radius   : {type : "int", defaultValue: 50}
            },
            //defaultAggregation : "...",
            aggregations : { },
            associations : { },
            events       : { }
        },

        init : function(){ },

        onAfterRendering: function (oEvent){
            var jqContent, svg, radius;

            radius = this.getRadius();
            if (radius <10){
                radius = 10;
            }

            //HINT: jQuery(this.getDomRef()) and this.$() is equal - it gives you the jQuery object for this control's DOM element
            svg = d3.select(this.getDomRef()).append("svg")
                .attr({
                    "class" : "nabiD3CircleSvg",
                    "width" : 500,
                    "height": 500
                });
            svg.append("circle").attr({
                cx : 250,
                cy : 100,
                r  : radius
            });

        },

        renderer : {

            render : function(rm, oControl) {
                rm.write("<div");
                rm.writeControlData(oControl);
                rm.addClass("nabiD3Circle");
                rm.writeClasses();
                rm.write(">");
                rm.write("</div>");
            }
        }
    });

}());