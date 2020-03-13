/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['./library','sap/ui/core/Control','sap/ui/Device','sap/ui/core/theming/Parameters',"sap/ui/thirdparty/jquery"],function(l,C,D,P,q){"use strict";var S=C.extend("sap.ui.unified.ShellHeader",{metadata:{properties:{logo:{type:"sap.ui.core.URI",defaultValue:""},searchVisible:{type:"boolean",defaultValue:true}},aggregations:{headItems:{type:"sap.ui.unified.ShellHeadItem",multiple:true},headEndItems:{type:"sap.ui.unified.ShellHeadItem",multiple:true},search:{type:"sap.ui.core.Control",multiple:false},user:{type:"sap.ui.unified.ShellHeadUserItem",multiple:false}}},renderer:{render:function(r,h){var i=h.getId();r.write("<div");r.writeControlData(h);r.writeAttribute("class","sapUiUfdShellHeader");if(sap.ui.getCore().getConfiguration().getAccessibility()){r.writeAttribute("role","toolbar");}r.write(">");r.write("<div id='",i,"-hdr-begin' class='sapUiUfdShellHeadBegin'>");this.renderHeaderItems(r,h,true);r.write("</div>");r.write("<div id='",i,"-hdr-center' class='sapUiUfdShellHeadCenter'>");this.renderSearch(r,h);r.write("</div>");r.write("<div id='",i,"-hdr-end' class='sapUiUfdShellHeadEnd'>");this.renderHeaderItems(r,h,false);r.write("</div>");r.write("</div>");},renderSearch:function(r,h){var s=h.getSearch();r.write("<div id='",h.getId(),"-hdr-search'");if(sap.ui.getCore().getConfiguration().getAccessibility()){r.writeAttribute("role","search");}r.writeAttribute("class","sapUiUfdShellSearch"+(h.getSearchVisible()?"":" sapUiUfdShellHidden"));r.write("><div>");if(s){r.renderControl(s);}r.write("</div></div>");},renderHeaderItems:function(r,h,b){r.write("<div class='sapUiUfdShellHeadContainer'>");var I=b?h.getHeadItems():h.getHeadEndItems();for(var i=0;i<I.length;i++){r.write("<div tabindex='0'");r.writeElementData(I[i]);r.addClass("sapUiUfdShellHeadItm");if(I[i].getStartsSection()){r.addClass("sapUiUfdShellHeadItmDelim");}if(I[i].getShowSeparator()){r.addClass("sapUiUfdShellHeadItmSep");}if(!I[i].getVisible()){r.addClass("sapUiUfdShellHidden");}if(I[i].getSelected()&&I[i].getToggleEnabled()){r.addClass("sapUiUfdShellHeadItmSel");}if(I[i].getShowMarker()){r.addClass("sapUiUfdShellHeadItmMark");}r.writeClasses();var t=I[i].getTooltip_AsString();if(t){r.writeAttributeEscaped("title",t);}if(sap.ui.getCore().getConfiguration().getAccessibility()){r.writeAccessibilityState(I[i],{role:"button",selected:null,pressed:I[i].getToggleEnabled()?I[i].getSelected():null});}r.write("><span></span><div class='sapUiUfdShellHeadItmMarker'><div></div></div></div>");}var u=h.getUser();if(!b&&u){r.write("<div tabindex='0'");r.writeElementData(u);r.addClass("sapUiUfdShellHeadUsrItm");if(!u.getShowPopupIndicator()){r.addClass("sapUiUfdShellHeadUsrItmWithoutPopup");}r.writeClasses();var t=u.getTooltip_AsString();if(t){r.writeAttributeEscaped("title",t);}if(sap.ui.getCore().getConfiguration().getAccessibility()){r.writeAccessibilityState(u,{role:"button"});if(u.getShowPopupIndicator()){r.writeAttribute("aria-haspopup","true");}}r.write("><span id='",u.getId(),"-img' aria-hidden='true' class='sapUiUfdShellHeadUsrItmImg'></span>");r.write("<span id='"+u.getId()+"-name' class='sapUiUfdShellHeadUsrItmName'");var U=u.getUsername()||"";r.writeAttributeEscaped("title",U);r.write(">");r.writeEscaped(U);r.write("</span><span class='sapUiUfdShellHeadUsrItmExp' aria-hidden='true'></span></div>");}r.write("</div>");if(b){this._renderLogo(r,h);}},_renderLogo:function(r,h){var a=sap.ui.getCore().getLibraryResourceBundle("sap.ui.unified"),L=a.getText("SHELL_LOGO_TOOLTIP"),i=h._getLogo();r.write("<div class='sapUiUfdShellIco'>");r.write("<img id='",h.getId(),"-icon'");r.writeAttributeEscaped("title",L);r.writeAttributeEscaped("alt",L);r.write("src='");r.writeEscaped(i);r.write("' style='",i?"":"display:none;","'/>");r.write("</div>");}}});S.prototype.init=function(){var t=this;this._rtl=sap.ui.getCore().getConfiguration().getRTL();this._handleMediaChange=function(p){if(!t.getDomRef()){return;}t._refresh();};D.media.attachHandler(this._handleMediaChange,this,D.media.RANGESETS.SAP_STANDARD);this._handleResizeChange=function(p){if(!t.getDomRef()||!t.getUser()){return;}var u=this.getUser();var c=u._checkAndAdaptWidth(!t.$("hdr-search").hasClass("sapUiUfdShellHidden")&&!!t.getSearch());if(c){t._refresh();}};D.resize.attachHandler(this._handleResizeChange,this);this.data("sap-ui-fastnavgroup","true",true);};S.prototype.exit=function(){D.media.detachHandler(this._handleMediaChange,this,D.media.RANGESETS.SAP_STANDARD);delete this._handleMediaChange;D.resize.detachHandler(this._handleResizeChange,this);delete this._handleResizeChange;};S.prototype.onAfterRendering=function(){this._refresh();this.$("hdr-center").toggleClass("sapUiUfdShellAnim",!this._noHeadCenterAnim);};S.prototype.onThemeChanged=function(){if(this.getDomRef()){this.invalidate();}};S.prototype._getLogo=function(){var i=this.getLogo();if(!i){i=P._getThemeImage(null,true);}return i;};S.prototype._refresh=function(){function u(I){for(var i=0;i<I.length;i++){I[i]._refreshIcon();}}u(this.getHeadItems());u(this.getHeadEndItems());var U=this.getUser(),a=q("html").hasClass("sapUiMedia-Std-Phone"),s=!this.$("hdr-search").hasClass("sapUiUfdShellHidden"),$=this.$("icon");if(U){U._refreshImage();U._checkAndAdaptWidth(s&&!!this.getSearch());}$.parent().toggleClass("sapUiUfdShellHidden",a&&s&&!!this.getSearch());var w=this.$("hdr-end").outerWidth(),b=this.$("hdr-begin").outerWidth(),c=Math.max(w,b),d=(a&&s?b:c)+"px",e=(a&&s?w:c)+"px";this.$("hdr-center").css({"left":this._rtl?e:d,"right":this._rtl?d:e});};return S;});
