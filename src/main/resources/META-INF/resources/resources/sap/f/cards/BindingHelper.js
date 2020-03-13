/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/base/ManagedObject"],function(M){"use strict";var B={};B.bindProperty=function(c,p,P,f){if(!P){return;}var b=M.bindingParser(P);if(b){if(!b.formatter&&f){b.formatter=f;}c.bindProperty(p,b);}else{var F=P;if(f){F=f.call(c,P);}c.setProperty(p,F);}};return B;});
