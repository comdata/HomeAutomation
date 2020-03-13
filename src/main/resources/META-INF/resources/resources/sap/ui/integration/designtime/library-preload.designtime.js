//@ui5-bundle sap/ui/integration/designtime/library-preload.designtime.js
/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.predefine('sap/ui/integration/designtime/controls/BaseEditor',["sap/ui/core/Control","sap/ui/model/resource/ResourceModel","sap/base/util/ObjectPath","sap/base/util/merge","sap/base/util/deepClone","sap/ui/model/json/JSONModel","sap/base/i18n/ResourceBundle"],function(C,R,O,m,d,J,a){"use strict";
var B=C.extend("sap.ui.integration.designtime.controls.BaseEditor",{
metadata:{properties:{"config":{type:"object"},"json":{type:"object"},"_defaultConfig":{type:"object",visibility:"hidden",defaultValue:{}}},aggregations:{"_propertyEditors":{type:"sap.ui.core.Control",visibility:"hidden"}},events:{jsonChanged:{parameters:{json:{type:"object"}}},propertyEditorsReady:{parameters:{propertyEditors:{type:"array"}}}}},
renderer:function(r,e){r.openStart("div",e);r.openEnd();e.getPropertyEditors().forEach(function(p){r.renderControl(p);});r.close("div");},
exit:function(){this._cleanup();},
setJson:function(j){var o;if(typeof j==="string"){o=JSON.parse(j);}else{o=d(j);}var r=this.setProperty("json",o,false);this._initialize();return r;},
addDefaultConfig:function(c){this.setProperty("_defaultConfig",this._mergeConfig(this.getProperty("_defaultConfig"),c));this.setConfig(this._oUnmergedConfig||{});return this;},
setConfig:function(c){this._oUnmergedConfig=c;return this._setConfig(this._mergeConfig(this.getProperty("_defaultConfig"),c));},
getPropertyEditor:function(p){return this._mPropertyEditors[p];},
getPropertyEditors:function(t){var h=function(p,T){return p.getConfig().tags&&(p.getConfig().tags.indexOf(T)!==-1);};if(!t){return this.getAggregation("_propertyEditors")||[];}else if(typeof t==="string"){return this.getPropertyEditors().filter(function(p){return h(p,t);});}else if(Array.isArray(t)){return this.getPropertyEditors().filter(function(p){return t.every(function(T){return h(p,T);});});}else{return[];}}
});
B.prototype._mergeConfig=function(t,s){var r=m({},t,s);r.i18n=[].concat(t.i18n||[],s.i18n||[]);return r;};
B.prototype._setConfig=function(c){var r=this.setProperty("config",c,false);this._initialize();return r;};
B.prototype._cleanup=function(c){if(this._oI18nModel){this._oI18nModel.destroy();delete this._oI18nModel;}if(this._oContextModel){this._oContextModel.destroy();delete this._oContextModel;}delete this._mEditorClasses;this._mPropertyEditors={};this.destroyAggregation("_propertyEditors");};
B.prototype._initialize=function(){this._cleanup();var j=this.getJson();var c=this.getConfig();if(j&&c&&c.properties){var o=j;if(c.context){o=O.get(c.context.split("/"),this.getJson());}this._oContextModel=new J(o);this._oContextModel.setDefaultBindingMode("OneWay");this._createI18nModel();this._createEditors();}};
B.prototype._createI18nModel=function(){var c=this.getConfig();c.i18n.forEach(function(i){a.create({url:sap.ui.require.toUrl(i),async:true}).then(function(b){if(!this._oI18nModel){this._oI18nModel=new R({bundle:b});this.setModel(this._oI18nModel,"i18n");this._oI18nModel.setDefaultBindingMode("OneWay");}else{this._oI18nModel.enhance(b);}}.bind(this));}.bind(this));};
B.prototype._createEditors=function(){var c=this.getConfig();var t=Object.keys(c.propertyEditors);var M=t.map(function(T){return c.propertyEditors[T];});this._mEditorClasses={};this._iCreateEditorsCallCount=(this._iCreateEditorsCallCount||0)+1;var i=this._iCreateEditorsCallCount;sap.ui.require(M,function(){if(this._iCreateEditorsCallCount===i){Array.from(arguments).forEach(function(E,I){this._mEditorClasses[t[I]]=E;}.bind(this));Object.keys(c.properties).forEach(function(p){var P=this.getConfig().properties[p];var e=this.createPropertyEditor(P);if(e){this._mPropertyEditors[p]=e;this.addAggregation("_propertyEditors",this._mPropertyEditors[p]);}}.bind(this));this.firePropertyEditorsReady({propertyEditors:this.getPropertyEditors()});}}.bind(this));};
B.prototype.createPropertyEditor=function(p){var E=this._mEditorClasses[p.type];if(E){var P=new E({editor:this});P.setModel(this._oContextModel,"_context");P.setConfig(d(p));P.attachPropertyChanged(this._onPropertyChanged.bind(this));P.addStyleClass("sapUiTinyMargin");return P;}};
B.prototype._onPropertyChanged=function(e){var p=e.getParameter("path");var P=p.split("/");this._oContext=this._oContextModel.getData();O.set(P,e.getParameter("value"),this._oContext);this._oContextModel.checkUpdate();this.fireJsonChanged({json:d(this.getJson())});};
return B;});
sap.ui.predefine('sap/ui/integration/designtime/controls/CardEditor',["sap/ui/integration/designtime/controls/BaseEditor","./DefaultCardConfig"],function(B,d){"use strict";
var C=B.extend("sap.ui.integration.designtime.controls.CardEditor",{
constructor:function(){B.prototype.constructor.apply(this,arguments);this.addDefaultConfig(d);},
renderer:B.getMetadata().getRenderer()
});
return C;});
sap.ui.predefine('sap/ui/integration/designtime/controls/propertyEditors/ArrayEditor',["sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor","sap/base/util/deepClone","sap/m/VBox","sap/m/Bar","sap/m/Label","sap/m/Button"],function(B,d,V,a,L,b){"use strict";
var A=B.extend("sap.ui.integration.designtime.controls.propertyEditors.ArrayEditor",{
constructor:function(){B.prototype.constructor.apply(this,arguments);var c=new V();this.addContent(c);c.bindAggregation("items","items",function(i,I){var o=I.getObject();var e=this.getConfig().items.indexOf(o);var g=new V({items:new a({contentLeft:[new L({text:this.getConfig().itemLabel||"{i18n>CARD_EDITOR.ARRAY.ITEM_LABEL}"})],contentRight:[new b({icon:"sap-icon://less",tooltip:"{i18n>CARD_EDITOR.ARRAY.REMOVE}",press:function(e){var v=this.getConfig().value;v.splice(e,1);this.firePropertyChanged(v);}.bind(this,e)})]})});Object.keys(o).forEach(function(s){var f=o[s];var S=this.getEditor().createPropertyEditor(f);S.getLabel().setDesign("Standard");g.addItem(S);}.bind(this));return g;}.bind(this));this.addContent(new a({contentRight:[new b({icon:"sap-icon://add",tooltip:"{i18n>CARD_EDITOR.ARRAY.ADD}",enabled:"{= ${items} ? ${items}.length < ${maxItems} : false}",press:function(){var v=this.getConfig().value;v.push({});this.firePropertyChanged(v);}.bind(this)})]}));},
onValueChange:function(){var r=B.prototype.onValueChange.apply(this,arguments);var c=this.getConfig();if(c.value&&c.template){c.items=[];c.value.forEach(function(v,i){var I=d(c.template);Object.keys(I).forEach(function(k){var o=I[k];if(o.path){o.path=o.path.replace(":index",i);}});c.items.push(I);});this.getModel().checkUpdate();}return r;},
renderer:B.getMetadata().getRenderer().render
});
return A;});
sap.ui.predefine('sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor',["sap/ui/core/Control","./../utils/ObjectBinding","sap/ui/model/json/JSONModel","sap/base/util/ObjectPath","sap/m/Label"],function(C,O,J,a,L){"use strict";
var B=C.extend("sap.ui.integration.designtime.controls.propertyEditors.BasePropertyEditor",{
metadata:{properties:{"renderLabel":{type:"boolean",defaultValue:true},"config":{type:"any"}},aggregations:{"_label":{type:"sap.m.Label",visibility:"hidden",multiple:false},"content":{type:"sap.ui.core.Control"}},associations:{"editor":{type:"sap.ui.integration.designtime.BaseEditor",multiple:false}},events:{propertyChanged:{parameters:{path:{type:"string"},value:{type:"any"}}}}},
constructor:function(){C.prototype.constructor.apply(this,arguments);this._oConfigModel=new J(this.getConfig());this._oConfigModel.setDefaultBindingMode("OneWay");this.setModel(this._oConfigModel);this.setBindingContext(this._oConfigModel.getContext("/"));},
clone:function(){this.destroyContent();return C.prototype.clone.apply(this,arguments);},
exit:function(){this._oConfigModel.destroy();if(this._oConfigBinding){this._oConfigBinding.destroy();}},
setConfig:function(c){var r=this.setProperty("config",c);this._initialize();return r;},
setModel:function(m,n){var r=C.prototype.setModel.apply(this,arguments);this._initialize();return r;},
onValueChange:function(v){var c=this.getConfig();if(typeof c.value==="undefined"&&c.defaultValue){c.value=c.defaultValue;this._oConfigModel.checkUpdate();}},
_initialize:function(){var c=this.getConfig();var j=this.getModel("_context");if(j&&c){if(c.path&&!c.value){c.value="{context>"+c.path+"}";}this._oConfigBinding=new O();this._oConfigBinding.setModel(j,"context");this._oConfigBinding.setBindingContext(j.getContext("/"),"context");this._oConfigBinding.setObject(c);this._oConfigModel.setData(c);this._oConfigModel.checkUpdate();this.onValueChange(c.value);this.bindProperty("visible","visible");this._oConfigBinding.attachChange(function(e){this._oConfigModel.checkUpdate();if(e.getParameter("path")==="value"){this.onValueChange(e.getParameter("value"));}}.bind(this));}},
getEditor:function(){return sap.ui.getCore().byId(this.getAssociation("editor"));},
getI18nProperty:function(n){return this.getModel("i18n").getProperty(n);},
getLabel:function(){var l=this.getAggregation("_label");if(!l){l=new L({text:this.getConfig().label,design:"Bold"});this.setAggregation("_label",l);}return l;},
renderer:function(r,p){r.openStart("div",p);r.openEnd();if(p.getRenderLabel()&&p.getLabel()){r.renderControl(p.getLabel());}p.getContent().forEach(function(c){r.renderControl(c);});r.close("div");},
firePropertyChanged:function(v){this.fireEvent("propertyChanged",{path:this.getConfig().path,value:v});}
});
return B;});
sap.ui.predefine('sap/ui/integration/designtime/controls/propertyEditors/EnumStringEditor',["sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor","sap/ui/core/Item","sap/ui/base/BindingParser"],function(B,I,a){"use strict";
var E=B.extend("sap.ui.integration.designtime.controls.propertyEditors.EnumStringEditor",{
constructor:function(){B.prototype.constructor.apply(this,arguments);this._oCombo=new sap.m.ComboBox({selectedKey:"{value}",value:"{value}",width:"100%"});this._oCombo.bindAggregation("items","enum",function(i,c){return new I({key:c.getObject(),text:c.getObject()});});this._oCombo.attachChange(function(){if(this._validate()){this.firePropertyChanged(this._oCombo.getSelectedKey()||this._oCombo.getValue());}}.bind(this));this.addContent(this._oCombo);},
_validate:function(){var s=this._oCombo.getSelectedKey();var v=this._oCombo.getValue();if(!s&&v){var p;try{p=a.complexParser(v);}finally{if(!p){this._oCombo.setValueState("Error");this._oCombo.setValueStateText(this.getI18nProperty("CARD_EDITOR.ENUM.INVALID_SELECTION_OR_BINDING"));return false;}else{this._oCombo.setValueState("None");return true;}}}else{this._oCombo.setValueState("None");return true;}},
renderer:B.getMetadata().getRenderer().render
});
return E;});
sap.ui.predefine('sap/ui/integration/designtime/controls/propertyEditors/IconEditor',["sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor","sap/ui/core/Fragment","sap/ui/model/json/JSONModel","sap/ui/model/Filter","sap/ui/model/FilterOperator","sap/ui/core/IconPool"],function(B,F,J,a,b,I){"use strict";
var c=B.extend("sap.ui.integration.designtime.controls.propertyEditors.IconEditor",{
constructor:function(){B.prototype.constructor.apply(this,arguments);this._oIconModel=new J(I.getIconNames().map(function(n){return{name:n,path:"sap-icon://"+n};}));this._oInput=new sap.m.Input({value:"{value}",showSuggestion:true,showValueHelp:true,valueHelpRequest:this._handleValueHelp.bind(this)});this._oInput.setModel(this._oIconModel,"icons");this._oInput.bindAggregation("suggestionItems","icons>/",new sap.ui.core.ListItem({text:"{icons>path}",additionalText:"{icons>name}"}));this._oInput.attachLiveChange(function(e){this.firePropertyChanged(e.getParameter("value"));}.bind(this));this._oInput.attachSuggestionItemSelected(function(e){this.firePropertyChanged(e.getParameter("selectedItem").getText());}.bind(this));this.addContent(this._oInput);},
renderer:B.getMetadata().getRenderer().render
});
c.prototype._handleValueHelp=function(e){var v=e.getSource().getValue();if(!this._oDialog){F.load({name:"sap.ui.integration.designtime.controls.propertyEditors.IconSelection",controller:this}).then(function(d){this._oDialog=d;this.addDependent(this._oDialog);this._oDialog.setModel(this._oIconModel);this._filter(v);this._oDialog.open(v);}.bind(this));}else{this._filter(v);this._oDialog.open(v);}};
c.prototype.handleSearch=function(e){var v=e.getParameter("value");this._filter(v);};
c.prototype._filter=function(v){var f=new a("path",b.Contains,v);var o=this._oDialog.getBinding("items");o.filter([f]);};
c.prototype.handleClose=function(e){var s=e.getParameter("selectedItem");if(s){this.firePropertyChanged(s.getIcon());}e.getSource().getBinding("items").filter([]);};
return c;});
sap.ui.predefine('sap/ui/integration/designtime/controls/propertyEditors/ParametersEditor',["sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor","sap/ui/core/Fragment","sap/ui/model/json/JSONModel","sap/base/util/deepClone"],function(B,F,J,d){"use strict";
var P=B.extend("sap.ui.integration.designtime.controls.propertyEditors.ParametersEditor",{
constructor:function(){B.prototype.constructor.apply(this,arguments);this._oTableModel=new J([]);F.load({name:"sap.ui.integration.designtime.controls.propertyEditors.ParametersTable",controller:this}).then(function(t){t.setModel(this._oTableModel);if(this.getRenderLabel()){t.getHeaderToolbar().insertContent(this.getLabel(),0);}this.addContent(t);}.bind(this));},
renderer:function(r,p){r.openStart("div",p);r.openEnd();p.getContent().forEach(function(c){r.openStart("div");r.style("max-heigth","500px");r.openEnd();r.renderControl(c);r.close("div");});r.close("div");},
onValueChange:function(){var r=B.prototype.onValueChange.apply(this,arguments);var p=this.getConfig().value||{};var a=Object.keys(p).map(function(k){var o=p[k];o._key=k;return o;});this._oTableModel.setData(a);return r;},
_syncParameters:function(){this._oTableModel.checkUpdate();var p={};this._oTableModel.getData().forEach(function(o){p[o._key]=d(o);delete p[o._key]._key;});this.firePropertyChanged(p);},
_addParameter:function(){var p=this.getConfig().value||{};var k="key";var i=0;while(p[k]){k="key"+ ++i;}var a=this._oTableModel.getData();a.push({_key:k,value:""});this._syncParameters(a);},
_removeParameter:function(e){var p=e.getSource().getBindingContext().getObject();var a=this._oTableModel.getData();a.splice(a.indexOf(p),1);this._syncParameters(a);},
_onKeyChange:function(e){var p=this.getConfig().value;var i=e.getSource();var n=e.getParameter("value");var o=i.getBindingContext().getObject();var O=o._key;if(!p[n]||n===O){i.setValueState("None");o._key=n;this._syncParameters();}else{i.setValueState("Error");i.setValueStateText(this.getI18nProperty("CARD_EDITOR.PARAMETERS.DUPLICATE_KEY"));}},
_onValueChange:function(e){var v=e.getParameter("value");var p=e.getSource().getBindingContext().getObject();p.value=v;this._syncParameters();}
});
return P;});
sap.ui.predefine('sap/ui/integration/designtime/controls/propertyEditors/StringEditor',["sap/ui/integration/designtime/controls/propertyEditors/BasePropertyEditor","sap/ui/base/BindingParser"],function(B,a){"use strict";
var S=B.extend("sap.ui.integration.designtime.controls.propertyEditors.StringEditor",{
constructor:function(){B.prototype.constructor.apply(this,arguments);this._oInput=new sap.m.Input({value:"{value}"});this._oInput.attachLiveChange(function(e){if(this._validate()){this.firePropertyChanged(this._oInput.getValue());}}.bind(this));this.addContent(this._oInput);},
_validate:function(p){var v=this._oInput.getValue();var i=false;try{a.complexParser(v);}catch(e){i=true;}finally{if(i){this._oInput.setValueState("Error");this._oInput.setValueStateText(this.getI18nProperty("CARD_EDITOR.STRING.INVALID_BINDING"));return false;}else{this._oInput.setValueState("None");return true;}}},
renderer:B.getMetadata().getRenderer().render
});
return S;});
sap.ui.predefine('sap/ui/integration/designtime/controls/utils/ObjectBinding',["sap/ui/base/ManagedObject","sap/base/util/deepClone","sap/base/util/ObjectPath","sap/ui/base/BindingParser"],function(M,d,O,B){"use strict";return M.extend("sap.ui.integration.designtime.controls.utils.ObjectBinding",{metadata:{properties:{object:{type:"object"},_value:{type:"any",hidden:true}},events:{change:{parameters:{path:{type:"string"},value:{type:"any"}}}}},exit:function(){this._cleanup();},setObject:function(o){var r=this.setProperty("object",o);this._originalObject=d(o);this._init();return r;},setModel:function(){var r=M.prototype.setModel.apply(this,arguments);this._init();return r;},setBindingContext:function(){var r=M.prototype.setBindingContext.apply(this,arguments);this._init();return r;},_init:function(){this._cleanup();var o=this.getObject();if(o){Object.keys(o).forEach(function(k){o[k]=d(this._originalObject[k]);}.bind(this));this._createPropertyBindings(o);}},_cleanup:function(){if(this._mSimpleBindings){Object.keys(this._mSimpleBindings).forEach(function(k){var b=this._mSimpleBindings[k];b.getModel().removeBinding(b);b.destroy();}.bind(this));}this._mSimpleBindings={};},_createPropertyBindings:function(o,p){Object.keys(o).forEach(function(k){var c=p?p+"/"+k:k;if(typeof o[k]==="string"){var b=B.complexParser(o[k]);if(b){if(b.parts){if(!b.parts.find(function(P){return!this.getModel(P.model);}.bind(this))){b.parts.forEach(function(P){this._createSimpleBinding(P,c,b);}.bind(this));}else{return;}}else if(this.getModel(b.model)){this._createSimpleBinding(b,c,b);}else{return;}this._updateValue(c,b);}}else if(o[k]&&typeof o[k]==="object"){this._createPropertyBindings(o[k],c);}}.bind(this));},_updateValue:function(p,b){var o=this.getObject();var P=p.split("/");var k=P.pop();if(P.length){o=O.get(P,o);}this.bindProperty("_value",b);var v=d(this.getProperty("_value"));this.unbindProperty("_value");if(v!==o[k]){o[k]=v;this.fireChange({path:p,value:v});}},_createSimpleBinding:function(s,c,b){var C=this.getBindingContext(s.model);var h=s.model+">"+s.path;var o=this._mSimpleBindings[h];if(!o){o=this.getModel(s.model).bindProperty(s.path,C);this._mSimpleBindings[h]=o;}o.attachChange(function(e){this._updateValue(c,b);}.bind(this));return o;}});});
/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 *
 * @constructor
 * @private
 * @experimental
 */
sap.ui.predefine('sap/ui/integration/designtime/controls/DefaultCardConfig',[],function(){"use strict";var D={"context":"sap.card","properties":{"headerType":{"tags":["header"],"label":"{i18n>CARD_EDITOR.HEADERTYPE}","path":"header/type","type":"enum","enum":["Default",'Numeric'],"defaultValue":"Default"},"title":{"tags":["header"],"label":"{i18n>CARD_EDITOR.TITLE}","type":"string","path":"header/title"},"subTitle":{"tags":["header"],"label":"{i18n>CARD_EDITOR.SUBTITLE}","type":"string","path":"header/subTitle"},"icon":{"tags":["header defaultHeader"],"label":"{i18n>CARD_EDITOR.ICON}","type":"icon","path":"header/icon/src","visible":"{= ${context>header/type} !== 'Numeric' }"},"statusText":{"tags":["header defaultHeader"],"label":"{i18n>CARD_EDITOR.STATUS}","type":"string","path":"header/status/text","visible":"{= ${context>header/type} !== 'Numeric' }"},"unitOfMeasurement":{"tags":["header numericHeader"],"label":"{i18n>CARD_EDITOR.UOM}","type":"string","path":"header/unitOfMeasurement","visible":"{= ${context>header/type} === 'Numeric' }"},"mainIndicatorNumber":{"tags":["header numericHeader mainIndicator"],"label":"{i18n>CARD_EDITOR.MAIN_INDICATOR.NUMBER}","type":"string","path":"header/mainIndicator/number","visible":"{= ${context>header/type} === 'Numeric' }"},"mainIndicatorUnit":{"tags":["header numericHeader mainIndicator"],"label":"{i18n>CARD_EDITOR.MAIN_INDICATOR.UNIT}","type":"string","path":"header/mainIndicator/unit","visible":"{= ${context>header/type} === 'Numeric' }"},"mainIndicatorTrend":{"tags":["header numericHeader mainIndicator"],"label":"{i18n>CARD_EDITOR.MAIN_INDICATOR.TREND}","type":"enum","enum":["Down","None","Up"],"allowBinding":true,"path":"header/mainIndicator/trend","visible":"{= ${context>header/type} === 'Numeric' }"},"mainIndicatorState":{"tags":["header numericHeader mainIndicator"],"label":"{i18n>CARD_EDITOR.MAIN_INDICATOR.STATE}","type":"enum","enum":["Critical","Error","Good","Neutral"],"allowBinding":true,"path":"header/mainIndicator/state","visible":"{= ${context>header/type} === 'Numeric' }"},"details":{"tags":["header numericHeader"],"label":"{i18n>CARD_EDITOR.DETAILS}","type":"string","path":"header/details","visible":"{= ${context>header/type} === 'Numeric' }"},"sideIndicators":{"tags":["header numericHeader"],"label":"{i18n>CARD_EDITOR.SIDE_INDICATORS}","path":"header/sideIndicators","type":"array","itemLabel":"{i18n>CARD_EDITOR.SIDE_INDICATOR}","template":{"title":{"label":"{i18n>CARD_EDITOR.SIDE_INDICATOR.TITLE}","type":"string","path":"header/sideIndicators/:index/title"},"number":{"label":"{i18n>CARD_EDITOR.SIDE_INDICATOR.NUMBER}","type":"string","path":"header/sideIndicators/:index/number"},"unit":{"label":"{i18n>CARD_EDITOR.SIDE_INDICATOR.UNIT}","type":"string","path":"header/sideIndicators/:index/unit"}},"maxItems":2,"visible":"{= ${context>header/type} === 'Numeric' }"},"listItemTitle":{"tags":["content listItem"],"label":"{i18n>CARD_EDITOR.LIST_ITEM.TITLE}","type":"string","path":"content/item/title","visible":"{= ${context>type} === 'List' }"},"listItemDescription":{"tags":["content listItem"],"label":"{i18n>CARD_EDITOR.LIST_ITEM.DESCRIPTION}","type":"string","path":"content/item/description","visible":"{= ${context>type} === 'List' }"},"listItemHighlight":{"tags":["content listItem"],"label":"{i18n>CARD_EDITOR.LIST_ITEM.HIGHLIGHT}","type":"string","path":"content/item/highlight","visible":"{= ${context>type} === 'List' }"},"parameters":{"tags":["parameters"],"label":"{i18n>CARD_EDITOR.PARAMETERS}","path":"configuration/parameters","type":"parameters"}},"propertyEditors":{"enum":"sap/ui/integration/designtime/controls/propertyEditors/EnumStringEditor","string":"sap/ui/integration/designtime/controls/propertyEditors/StringEditor","icon":"sap/ui/integration/designtime/controls/propertyEditors/IconEditor","array":"sap/ui/integration/designtime/controls/propertyEditors/ArrayEditor","parameters":"sap/ui/integration/designtime/controls/propertyEditors/ParametersEditor"},"i18n":"sap/ui/integration/designtime/controls/i18n/i18n.properties"};return D;},true);
//# sourceMappingURL=library-preload.designtime.js.map