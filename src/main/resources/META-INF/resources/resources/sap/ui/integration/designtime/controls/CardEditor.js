/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/integration/designtime/controls/BaseEditor","./DefaultCardConfig"],function(B,d){"use strict";var C=B.extend("sap.ui.integration.designtime.controls.CardEditor",{constructor:function(){B.prototype.constructor.apply(this,arguments);this.addDefaultConfig(d);},renderer:B.getMetadata().getRenderer()});return C;});
