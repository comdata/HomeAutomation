/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/fl/ControlPersonalizationAPI","sap/ui/fl/Utils","sap/ui/fl/apply/_internal/ChangesController"],function(O,U,C){"use strict";var F={isPersonalized:function(p){return O.isPersonalized(p.selectors,p.changeTypes);},waitForChanges:function(p){return C.getFlexControllerInstance(p.element).waitForChangesToBeApplied(p.element);},isFlexSupported:function(p){return!!U.getAppComponentForControl(p.element);},hasVariantManagement:function(p){return O.hasVariantManagement(p.element);}};return F;},true);
