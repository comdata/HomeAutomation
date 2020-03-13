/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['sap/ui/core/Locale','sap/ui/core/LocaleData',"sap/base/Log","sap/ui/thirdparty/jquery","sap/base/util/isEmptyObject"],function(L,a,b,q,c){"use strict";var d=function(){throw new Error();};d.oDefaultListFormat={type:"standard",style:"wide"};d.getInstance=function(f,l){return this.createInstance(f,l);};d.createInstance=function(f,l){var F=Object.create(this.prototype);if(f instanceof L){l=f;f=undefined;}if(!l){l=sap.ui.getCore().getConfiguration().getFormatSettings().getFormatLocale();}F.oLocale=l;F.oLocaleData=a.getInstance(l);F.oOriginalFormatOptions=q.extend({},this.oDefaultListFormat,f);return F;};d.prototype.format=function(l){if(!Array.isArray(l)){b.error("ListFormat can only format with an array given.");return"";}var o=this.oOriginalFormatOptions,m,p,v,s,M,e,V=[].concat(l),S,f;m=this.oLocaleData.getListFormat(o.type,o.style);if(c(m)){b.error("No list pattern exists for the provided format options (type, style).");return"";}function r(V,p){var R=V[0];for(var i=1;i<V.length;i++){R=p.replace("{0}",R);R=R.replace("{1}",V[i]);}return R;}if(m[V.length]){p=m[V.length];for(var i=0;i<V.length;i++){p=p.replace('{'+i+'}',V[i]);}v=p;}else if(V.length<2){v=V.toString();}else{S=V.shift();e=V.pop();f=V;s=m.start.replace("{0}",S);e=m.end.replace("{1}",e);M=r(f,m.middle);v=s.replace("{1}",e.replace("{0}",M));}return v;};d.prototype.parse=function(v){if(typeof v!=='string'){b.error("ListFormat can only parse a String.");return[];}var r=[],s=[],m=[],e=[],E=[],o=this.oOriginalFormatOptions,l,f=/\{[01]\}/g,g,S,h,i,j;if(!o){o=d.oDefaultListFormat;}l=this.oLocaleData.getListFormat(o.type,o.style);if(c(l)){b.error("No list pattern exists for the provided format options (type, style).");return[];}h=l.start.replace(f,"");i=l.middle.replace(f,"");j=l.end.replace(f,"");s=v.split(h);r=r.concat(s.shift());e=s.join(h).split(j);g=e.pop();m=e.join(j).split(i);r=r.concat(m);r.push(g);if(s.length<1||m.length<1||e.length<1){S=l["2"].replace(f,"");E=v.split(S);if(E.length===2){return E;}if(v){return[v];}else{return[];}}return r;};return d;});
