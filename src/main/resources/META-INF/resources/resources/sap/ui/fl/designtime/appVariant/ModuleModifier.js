/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/fl/designtime/appVariant/ChangeModifier","sap/ui/fl/designtime/appVariant/ModifierUtils"],function(C,M){"use strict";var a={};a.CODE_EXT_PATTERN={NAMESPACE:"/changes/coding/",FILETYPE:".js"};a.FRAGMENT_PATTERN={NAMESPACE:"/changes/fragments/",FILETYPE:".xml"};a.modify=function(n,f){var o=a._extractOldReference(f);if(o){return f.map(function(F){if(M.fileNameMatchesPattern(F.fileName,a.CODE_EXT_PATTERN)||M.fileNameMatchesPattern(F.fileName,a.FRAGMENT_PATTERN)){F.content=a._modifyModuleFile(F.content,o,n);}return F;});}return f;};a._extractOldReference=function(f){var o=null;var c;f.some(function(F){if(M.fileNameMatchesPattern(F.fileName,C.CHANGE_PATTERN)){if(F.content){c=JSON.parse(F.content);o=c.reference;if(o.endsWith(".Component")){o=o.replace(".Component","");}return true;}}});return o;};a._modifyModuleFile=function(m,o,n){var O=o.replace(/\./g,'\/');var s=m.replace(new RegExp(o,'g'),n);s=s.replace(new RegExp(O,'g'),n);return s;};return a;},false);
