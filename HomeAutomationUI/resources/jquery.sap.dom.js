/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['jquery.sap.global','sap/ui/Device'],function(q,D){"use strict";var d=function(I,w){if(!w){w=window;}if(!I||I==""){return null;}var o=w.document.getElementById(I);if(o&&o.id==I){return o;}var R=w.document.getElementsByName(I);for(var i=0;i<R.length;i++){o=R[i];if(o&&o.id==I){return o;}}return null;};q.sap.domById=!!D.browser.internet_explorer&&D.browser.version<8?d:function domById(i,w){return i?(w||window).document.getElementById(i):null;};q.sap.byId=function byId(i,c){var e="";if(i){e="#"+i.replace(/(:|\.)/g,'\\$1');}return q(e,c);};q.sap.focus=function focus(o){if(!o){return;}try{o.focus();}catch(e){var i=(o&&o.id)?" (ID: '"+o.id+"')":"";q.sap.log.warning("Error when trying to focus a DOM element"+i+": "+e.message);return false;}return true;};q.fn.cursorPos=function cursorPos(p){var l=arguments.length,t,L,T,s;T=this.prop("tagName");s=this.prop("type");if(this.length===1&&((T=="INPUT"&&(s=="text"||s=="password"||s=="search"))||T=="TEXTAREA")){var o=this.get(0);if(l>0){if(typeof(o.selectionStart)=="number"){o.focus();o.selectionStart=p;o.selectionEnd=p;}else if(o.createTextRange){t=o.createTextRange();var m=o.value.length;if(p<0||p>m){p=m;}if(t){t.collapse();t.moveEnd("character",p);t.moveStart("character",p);t.select();}}return this;}else{if(typeof(o.selectionStart)=="number"){return o.selectionStart;}else if(o.createTextRange){t=window.document.selection.createRange();var c=t.duplicate();if(o.tagName=="TEXTAREA"){c.moveToElementText(o);var C=c.duplicate();L=c.text.length;C.moveStart("character",L);var S=0;if(C.inRange(t)){S=L;}else{var i=L;while(L>1){i=Math.round(L/2);S=S+i;C=c.duplicate();C.moveStart("character",S);if(C.inRange(t)){L=L-i;}else{S=S-i;L=i;}}}return S;}else if(c.parentElement()===o){c.collapse();var L=o.value.length;c.moveStart('character',-L);return c.text.length;}}return-1;}}else{return this;}};q.fn.selectText=function selectText(s,E){var o=this.get(0);try{if(typeof(o.selectionStart)==="number"){o.setSelectionRange(s,E);}else if(o.createTextRange){var t=o.createTextRange();t.collapse();t.moveStart('character',s);t.moveEnd('character',E-s);t.select();}}catch(e){}return this;};q.fn.getSelectedText=function(){var o=this.get(0);try{if(typeof o.selectionStart==="number"){return o.value.substring(o.selectionStart,o.selectionEnd);}if(document.selection){return document.selection.createRange().text;}}catch(e){}return"";};q.fn.outerHTML=function outerHTML(){var o=this.get(0);if(o&&o.outerHTML){return q.trim(o.outerHTML);}else{var c=this[0]?this[0].ownerDocument:document;var e=c.createElement("div");e.appendChild(o.cloneNode(true));return e.innerHTML;}};q.sap.containsOrEquals=function containsOrEquals(o,c){if(c&&o&&c!=document&&c!=window){return(o===c)||q.contains(o,c);}return false;};q.fn.rect=function rect(){var o=this.get(0);if(o){if(o.getBoundingClientRect){var c=o.getBoundingClientRect();var R={top:c.top,left:c.left,width:c.right-c.left,height:c.bottom-c.top};var w=q.sap.ownerWindow(o);R.left+=q(w).scrollLeft();R.top+=q(w).scrollTop();return R;}else{return{top:10,left:10,width:o.offsetWidth,height:o.offsetWidth};}}return null;};q.fn.rectContains=function rectContains(p,P){var R=this.rect();if(R){return p>=R.left&&p<=R.left+R.width&&P>=R.top&&P<=R.top+R.height;}return false;};q.fn.hasTabIndex=function hasTabIndex(){var t=this.prop("tabIndex");if(this.attr("disabled")&&!this.attr("tabindex")){t=-1;}return!isNaN(t)&&t>=0;};q.fn.firstFocusableDomRef=function firstFocusableDomRef(){var c=this.get(0);var e=function(i){return q(this).css("visibility")=="hidden";};if(!c||q(c).is(':hidden')||q(c).filter(e).length==1){return null;}var C=c.firstChild,o=null;while(C){if(C.nodeType==1&&q(C).is(':visible')){if(q(C).hasTabIndex()){return C;}if(C.childNodes){o=q(C).firstFocusableDomRef();if(o){return o;}}}C=C.nextSibling;}return null;};q.fn.lastFocusableDomRef=function lastFocusableDomRef(){var c=this.get(0);var e=function(i){return q(this).css("visibility")=="hidden";};if(!c||q(c).is(':hidden')||q(c).filter(e).length==1){return null;}var C=c.lastChild,o=null;while(C){if(C.nodeType==1&&q(C).is(':visible')){if(C.childNodes){o=q(C).lastFocusableDomRef();if(o){return o;}}if(q(C).hasTabIndex()){return C;}}C=C.previousSibling;}return null;};q.fn.scrollLeftRTL=function scrollLeftRTL(p){var o=this.get(0);if(o){if(p===undefined){if(!!D.browser.internet_explorer||!!D.browser.edge){return o.scrollWidth-o.scrollLeft-o.clientWidth;}else if(!!D.browser.webkit){return o.scrollLeft;}else if(!!D.browser.firefox){return o.scrollWidth+o.scrollLeft-o.clientWidth;}else{return o.scrollLeft;}}else{o.scrollLeft=q.sap.denormalizeScrollLeftRTL(p,o);return this;}}};q.fn.scrollRightRTL=function scrollRightRTL(){var o=this.get(0);if(o){if(!!D.browser.internet_explorer){return o.scrollLeft;}else if(!!D.browser.webkit){return o.scrollWidth-o.scrollLeft-o.clientWidth;}else if(!!D.browser.firefox){return(-o.scrollLeft);}else{return o.scrollLeft;}}};q.sap.denormalizeScrollLeftRTL=function(n,o){if(o){if(!!D.browser.internet_explorer){return o.scrollWidth-o.clientWidth-n;}else if(!!D.browser.webkit){return n;}else if(!!D.browser.firefox){return o.clientWidth+n-o.scrollWidth;}else{return n;}}};q.sap.denormalizeScrollBeginRTL=function(n,o){if(o){if(!!D.browser.internet_explorer){return n;}else if(!!D.browser.webkit){return o.scrollWidth-o.clientWidth-n;}else if(!!D.browser.firefox){return-n;}else{return n;}}};
/*
	 * The following methods are taken from jQuery UI core but modified.
	 *
	 * jQuery UI Core
	 * http://jqueryui.com
	 *
	 * Copyright 2014 jQuery Foundation and other contributors
	 * Released under the MIT license.
	 * http://jquery.org/license
	 *
	 * http://api.jqueryui.com/category/ui-core/
	 */
q.support.selectstart="onselectstart"in document.createElement("div");q.fn.extend({disableSelection:function(){return this.on((q.support.selectstart?"selectstart":"mousedown")+".ui-disableSelection",function(e){e.preventDefault();});},enableSelection:function(){return this.off(".ui-disableSelection");}});
/*!
	 * The following functions are taken from jQuery UI 1.8.17 but modified
	 *
	 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
	 * Dual licensed under the MIT or GPL Version 2 licenses.
	 * http://jquery.org/license
	 *
	 * http://docs.jquery.com/UI
	 */
function v(e){var o=q(e).offsetParent();var O=false;var $=q(e).parents().filter(function(){if(this===o){O=true;}return O;});return!q(e).add($).filter(function(){return q.css(this,"visibility")==="hidden"||q.expr.filters.hidden(this);}).length;}function f(e,i){var n=e.nodeName.toLowerCase();if(n==="area"){var m=e.parentNode,c=m.name,h;if(!e.href||!c||m.nodeName.toLowerCase()!=="map"){return false;}h=q("img[usemap=#"+c+"]")[0];return!!h&&v(h);}return(/input|select|textarea|button|object/.test(n)?!e.disabled:n=="a"?e.href||i:i)&&v(e);}if(!q.expr[":"].focusable){
/*!
		 * The following function is taken from jQuery UI 1.8.17
		 *
		 * Copyright 2011, AUTHORS.txt (http://jqueryui.com/about)
		 * Dual licensed under the MIT or GPL Version 2 licenses.
		 * http://jquery.org/license
		 *
		 * http://docs.jquery.com/UI
		 *
		 * But since visible is modified, focusable is different too the jQuery UI version too.
		 */
q.extend(q.expr[":"],{focusable:function(e){return f(e,!isNaN(q.attr(e,"tabindex")));}});}if(!q.expr[":"].sapTabbable){
/*!
		 * The following function is taken from
		 * jQuery UI Core 1.11.1
		 * http://jqueryui.com
		 *
		 * Copyright 2014 jQuery Foundation and other contributors
		 * Released under the MIT license.
		 * http://jquery.org/license
		 *
		 * http://api.jqueryui.com/category/ui-core/
		 */
q.extend(q.expr[":"],{sapTabbable:function(e){var t=q.attr(e,"tabindex"),i=isNaN(t);return(i||t>=0)&&f(e,!i);}});}if(!q.expr[":"].sapFocusable){q.extend(q.expr[":"],{sapFocusable:function(e){return f(e,!isNaN(q.attr(e,"tabindex")));}});}if(!q.fn.zIndex){q.fn.zIndex=function(z){if(z!==undefined){return this.css("zIndex",z);}if(this.length){var e=q(this[0]),p,c;while(e.length&&e[0]!==document){p=e.css("position");if(p==="absolute"||p==="relative"||p==="fixed"){c=parseInt(e.css("zIndex"),10);if(!isNaN(c)&&c!==0){return c;}}e=e.parent();}}return 0;};}q.fn.parentByAttribute=function parentByAttribute(A,V){if(this.length>0){if(V){return this.first().parents("["+A+"='"+V+"']").get(0);}else{return this.first().parents("["+A+"]").get(0);}}};q.sap.ownerWindow=function ownerWindow(o){if(o.ownerDocument.parentWindow){return o.ownerDocument.parentWindow;}return o.ownerDocument.defaultView;};var _={};q.sap.scrollbarSize=function(c,F){if(typeof c==="boolean"){F=c;c=null;}var k=c||"#DEFAULT";if(F){if(c){delete _[c];}else{_={};}}if(_[k]){return _[k];}if(!document.body){return{width:0,height:0};}var A=q("<DIV/>").css("visibility","hidden").css("height","0").css("width","0").css("overflow","hidden");if(c){A.addClass(c);}A.prependTo(document.body);var $=q("<div style=\"visibility:visible;position:absolute;height:100px;width:100px;overflow:scroll;opacity:0;\"></div>");A.append($);var o=$.get(0);var w=o.offsetWidth-o.scrollWidth;var h=o.offsetHeight-o.scrollHeight;A.remove();if(w===0||h===0){return{width:w,height:h};}_[k]={width:w,height:h};return _[k];};var a;function g(){return a||(a=sap.ui.require('sap/ui/core/Control'));}q.sap.syncStyleClass=function(s,S,c){if(!s){return c;}var C=g();if(C&&S instanceof C){S=S.$();}else if(typeof S==="string"){S=q.sap.byId(S);}else if(!(S instanceof q)){return c;}var e=!!S.closest("."+s).length;if(c instanceof q){c.toggleClass(s,e);}else if(C&&c instanceof C){c.toggleStyleClass(s,e);}else{}return c;};function b(A,V){var s=this.attr(A);if(!s){return this.attr(A,V);}var c=s.split(" ");if(c.indexOf(V)==-1){c.push(V);this.attr(A,c.join(" "));}return this;}function r(A,V){var s=this.attr(A)||"",c=s.split(" "),i=c.indexOf(V);if(i==-1){return this;}c.splice(i,1);if(c.length){this.attr(A,c.join(" "));}else{this.removeAttr(A);}return this;}q.fn.addAriaLabelledBy=function(i){return b.call(this,"aria-labelledby",i);};q.fn.removeAriaLabelledBy=function(i){return r.call(this,"aria-labelledby",i);};q.fn.addAriaDescribedBy=function(i){return b.call(this,"aria-describedby",i);};q.fn.removeAriaDescribedBy=function(i){return r.call(this,"aria-describedby",i);};return q;});
