//@ui5-bundle sap/ui/fl/designtime/library-preload.designtime.js
/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.predefine('sap/ui/fl/designtime/appVariant/AppVariantModifier',["sap/ui/fl/descriptorRelated/api/DescriptorInlineChangeFactory","sap/ui/fl/designtime/appVariant/ModifierUtils","sap/base/util/includes"],function(D,M,i){"use strict";var A={};A.DESCRIPTOR_CHANGE_PATTERN={NAMESPACE:"/descriptorChanges/",FILETYPE:".change"};var _=D.getCondensableDescriptorChangeTypes();var a="/manifest.appdescr_variant";
function b(c){return i(_,c);}
function s(c,C){if(c.creation===C.creation){return 0;}return c.creation>C.creation?1:-1;}
A.modify=function(n,f){if(f.length!==0){var S=A._separateDescriptorAndInlineChangesFromOtherFiles(f);var r=S.inlineChanges.concat(S.descriptorChanges.sort(s)).concat(n.content);n.content=A._condenseDescriptorChanges(r);f=S.otherFiles;}f.push({fileName:a,content:JSON.stringify(n)});return f;};
A._separateDescriptorAndInlineChangesFromOtherFiles=function(f){var d=[];var I=[];var o=[];f.forEach(function(F){if(M.fileNameMatchesPattern(F.fileName,A.DESCRIPTOR_CHANGE_PATTERN)){var c=JSON.parse(F.content);d.push(c);}else if(F.fileName===a){var O=JSON.parse(F.content);I=O.content;}else{o.push(F);}});return{descriptorChanges:d,inlineChanges:I,otherFiles:o};};
A._condenseDescriptorChanges=function(d){var c=[];var C=[];d.reverse().forEach(function(o){var e=o.changeType;if(!i(c,e)){C.push(o);if(b(e)){c.push(e);}}});return C.reverse();};
return A;},false);
sap.ui.predefine('sap/ui/fl/designtime/appVariant/AppVariantUtils',["sap/ui/fl/designtime/appVariant/ChangeModifier","sap/ui/fl/designtime/appVariant/AppVariantModifier","sap/ui/fl/designtime/appVariant/ModuleModifier"],function(C,A,M){"use strict";var a={};
a.prepareContent=function(f,n,N,s,S){S=S||sap.ui.fl.Scenario.VersionedAppVariant;return new Promise(function(r,b){if(!f||!n||!N||!s){b("Not all parameters were passed!");}r(f);}).then(M.modify.bind(M,N)).then(C.modify.bind(C,N,s,S)).then(A.modify.bind(A,n));};
return a;},false);
sap.ui.predefine('sap/ui/fl/designtime/appVariant/ChangeModifier',["sap/ui/fl/Utils","sap/ui/fl/designtime/appVariant/ModifierUtils"],function(U,M){"use strict";var C={};C.CHANGE_PATTERN={NAMESPACE:"/changes/",FILETYPE:".change"};
C.modify=function(n,N,s,f){return f.map(function(F){if(M.fileNameMatchesPattern(F.fileName,C.CHANGE_PATTERN)){F.content=C._modifyChangeFile(F.content,n,N,s);}return F;});};
var _=new RegExp("(apps/[^/]*/).*/","g");
C._modifyChangeFile=function(c,n,N,s){var o=JSON.parse(c);o.reference=n;o.validAppVersions=U.getValidAppVersions({appVersion:N,developerMode:true,scenario:s});o.support.generator="appVariant.UiChangeModifier";o.support.user="";o.projectId=n;o.packageName="";o.namespace=C._adjustFileName(o.namespace,n);return JSON.stringify(o);};
C._adjustFileName=function(n,N){return n.replace(_,"$1appVariants/"+N+"/changes/");};
return C;},false);
sap.ui.predefine('sap/ui/fl/designtime/appVariant/ModifierUtils',[],function(){"use strict";var M={};
M.fileNameMatchesPattern=function(f,p){if(f.startsWith(p.NAMESPACE)&&f.endsWith(p.FILETYPE)){f=f.replace(new RegExp("^"+p.NAMESPACE),"");f=f.replace(new RegExp(p.FILETYPE+"$"),"");return f.indexOf("/")===-1;}return false;};
return M;},false);
sap.ui.predefine('sap/ui/fl/designtime/appVariant/ModuleModifier',["sap/ui/fl/designtime/appVariant/ChangeModifier","sap/ui/fl/designtime/appVariant/ModifierUtils"],function(C,M){"use strict";var a={};a.CODE_EXT_PATTERN={NAMESPACE:"/changes/coding/",FILETYPE:".js"};a.FRAGMENT_PATTERN={NAMESPACE:"/changes/fragments/",FILETYPE:".xml"};
a.modify=function(n,f){var o=a._extractOldReference(f);if(o){return f.map(function(F){if(M.fileNameMatchesPattern(F.fileName,a.CODE_EXT_PATTERN)||M.fileNameMatchesPattern(F.fileName,a.FRAGMENT_PATTERN)){F.content=a._modifyModuleFile(F.content,o,n);}return F;});}return f;};
a._extractOldReference=function(f){var o=null;var c;f.some(function(F){if(M.fileNameMatchesPattern(F.fileName,C.CHANGE_PATTERN)){if(F.content){c=JSON.parse(F.content);o=c.reference;if(o.endsWith(".Component")){o=o.replace(".Component","");}return true;}}});return o;};
a._modifyModuleFile=function(m,o,n){var O=o.replace(/\./g,'\/');var s=m.replace(new RegExp(o,'g'),n);s=s.replace(new RegExp(O,'g'),n);return s;};
return a;},false);
sap.ui.predefine('sap/ui/fl/designtime/variants/VariantManagement.designtime',["sap/ui/fl/Utils"],function(f){"use strict";var s=function(v,d){var a=f.getAppComponentForControl(v);var c=v.getId();var m=a.getModel(f.VARIANT_MODEL_NAME);var V=a.getLocalId(c)||c;if(!m){return;}m.setModelPropertiesForControl(V,d,v);m.checkUpdate(true);};return{annotations:{},properties:{showExecuteOnSelection:{ignore:false},showSetAsDefault:{ignore:false},manualVariantKey:{ignore:false},inErrorState:{ignore:false},editable:{ignore:false},modelName:{ignore:false},updateVariantInURL:{ignore:false}},variantRenameDomRef:function(v){return v.getTitle().getDomRef("inner");},customData:{},tool:{start:function(v){var d=true;s(v,d);},stop:function(v){var d=false;s(v,d);}}};},false);
/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.predefine('sap/ui/fl/designtime/library.designtime',[],function(){"use strict";return{};});
//# sourceMappingURL=library-preload.designtime.js.map