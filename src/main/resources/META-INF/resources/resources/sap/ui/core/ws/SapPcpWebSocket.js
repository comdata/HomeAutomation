/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['./WebSocket',"sap/base/Log"],function(W,L){"use strict";var S=W.extend("sap.ui.core.ws.SapPcpWebSocket",{constructor:function(u,p){W.apply(this,arguments);}});S.SUPPORTED_PROTOCOLS={v10:"v10.pcp.sap.com"};S._deserializeRegexp=/((?:[^:\\]|(?:\\.))+):((?:[^:\\\n]|(?:\\.))*)/;S._SEPARATOR="\n\n";S._MESSAGE="MESSAGE";S.prototype._onopen=function(){var s=false;if(this.getProtocol()===""){s=true;}else{for(var p in S.SUPPORTED_PROTOCOLS){if(S.SUPPORTED_PROTOCOLS.hasOwnProperty(p)){if(S.SUPPORTED_PROTOCOLS[p]===this.getProtocol()){s=true;break;}}}}if(s){this.fireOpen();}else{L.error("Unsupported protocol '"+this.getProtocol()+"' selected by the server. "+"Connection will be closed.");this.close("Unsupported protocol selected by the server");}};S.prototype._onmessage=function(m){var s=-1,e={};if(typeof m.data==="string"){s=m.data.indexOf(S._SEPARATOR);}if(s!==-1){e.pcpFields=this._extractPcpFields(m.data.substring(0,s));e.data=m.data.substr(s+S._SEPARATOR.length);}else{L.warning("Invalid PCP message received: "+m.data);e.pcpFields={};e.data=m.data;}this.fireMessage(e);};S.prototype._extractPcpFields=function(h){var f=h.split("\n"),l=[],p={};for(var i=0;i<f.length;i++){l=f[i].match(S._deserializeRegexp);if(l&&l.length===3){p[this._unescape(l[1])]=this._unescape(l[2]);}}return p;};S.prototype._unescape=function(e){var p=e.split("\u0008"),u="";for(var i=0;i<p.length;i++){p[i]=p[i].replace(/\\\\/g,"\u0008").replace(/\\:/g,':').replace(/\\n/g,'\n').replace(/\u0008/g,"\\");}u=p.join("\u0008");return u;};S.prototype._serializePcpFields=function(p,m,P){var s="",f="",a="";if(m==='string'){a='text';}else if(m==='blob'||m==='arraybuffer'){a='binary';}if(p&&typeof p==='object'){for(f in p){if(p.hasOwnProperty(f)&&f.indexOf('pcp-')!==0){s+=this._escape(f)+":"+this._escape(String(p[f]))+"\n";}}}return"pcp-action:"+P+"\npcp-body-type:"+a+"\n"+s+"\n";};S.prototype._escape=function(u){return u.replace(/\\/g,'\\\\').replace(/:/g,'\\:').replace(/\n/g,'\\n');};S.prototype.send=function(m,p){var M=typeof m,P="";P=this._serializePcpFields(p,M,S._MESSAGE);W.prototype.send.call(this,P+m);return this;};return S;});
