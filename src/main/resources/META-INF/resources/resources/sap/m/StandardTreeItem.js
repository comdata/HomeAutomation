/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['./TreeItemBase','./library','sap/ui/core/IconPool','./Image','./StandardTreeItemRenderer'],function(T,l,I,a,S){"use strict";var b=T.extend("sap.m.StandardTreeItem",{metadata:{library:"sap.m",properties:{title:{type:"string",group:"Misc",defaultValue:""},icon:{type:"sap.ui.core.URI",group:"Misc",defaultValue:null}}}});b.prototype._getIconControl=function(){var u=this.getIcon();if(this._oIconControl){this._oIconControl.setSrc(u);return this._oIconControl;}this._oIconControl=I.createControlByURI({id:this.getId()+"-icon",src:u,useIconTooltip:false,noTabStop:true},a).setParent(this,null,true).addStyleClass("sapMSTIIcon");return this._oIconControl;};b.prototype.getContentAnnouncement=function(){return this.getTitle();};b.prototype.exit=function(){T.prototype.exit.apply(this,arguments);this.destroyControls(["Icon"]);};b.prototype.setIcon=function(i){var o=this.getIcon();this.setProperty("icon",i);if(this._oIconControl&&(!i||I.isIconURI(i)!=I.isIconURI(o))){this._oIconControl.destroy("KeepDom");this._oIconControl=undefined;}return this;};return b;});
