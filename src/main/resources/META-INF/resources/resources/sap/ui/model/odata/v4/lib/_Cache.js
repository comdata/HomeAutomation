/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["./_GroupLock","./_Helper","./_Requestor","sap/base/Log","sap/ui/base/SyncPromise","sap/ui/thirdparty/jquery"],function(_,a,b,L,S,q){"use strict";var r=/\(\$uid=[-\w]+\)$/,m="@com.sap.vocabularies.Common.v1.Messages",c=/^-?\d+$/,d=/^([^(]*)(\(.*\))$/;function e(i,p,j,D){if(j.$count!==undefined){s(i,p,j,j.$count+D);}}function f(R,p){return p===""||R===p||R.indexOf(p+"/")===0;}function s(i,p,j,v){if(typeof v==="string"){v=parseInt(v);}a.updateExisting(i,p,j,{$count:v});}function C(R,i,Q,j,G){this.bActive=true;this.mChangeListeners={};this.fnGetOriginalResourcePath=G;this.mLateQueryOptions=null;this.sMetaPath=a.getMetaPath("/"+i);this.mPatchRequests={};this.oPendingRequestsPromise=null;this.mPostRequests={};this.mPropertyRequestByPath={};this.oRequestor=R;this.sResourcePath=i;this.bSortExpandSelect=j;this.bSentReadRequest=false;this.oTypePromise=undefined;this.setQueryOptions(Q);}C.prototype._delete=function(G,E,p,o,i){var j=p.split("/"),D=j.pop(),k=j.join("/"),t=this;this.addPendingRequest();return this.fetchValue(_.$cached,k).then(function(v){var l=D?v[C.from$skip(D,v)]:v,H,K=a.getPrivateAnnotation(l,"predicate"),n=a.buildPath(k,Array.isArray(v)?K:D),T=a.getPrivateAnnotation(l,"transient");if(T===true){throw new Error("No 'delete' allowed while waiting for server response");}if(T){G.unlock();t.oRequestor.removePost(T,l);return undefined;}if(l["$ui5.deleting"]){throw new Error("Must not delete twice: "+E);}l["$ui5.deleting"]=true;H={"If-Match":o||l};E+=t.oRequestor.buildQueryString(t.sMetaPath,t.mQueryOptions,true);return t.oRequestor.request("DELETE",E,G,H,undefined,undefined,undefined,undefined,a.buildPath(t.getOriginalResourcePath(l),n)).catch(function(u){if(u.status!==404){delete l["$ui5.deleting"];throw u;}}).then(function(){if(Array.isArray(v)){i(t.removeElement(v,Number(D),K,k),v);}else{if(D){a.updateExisting(t.mChangeListeners,k,v,C.makeUpdateData([D],null));}else{l["$ui5.deleted"]=true;}i();}t.oRequestor.getModelInterface().reportBoundMessages(t.sResourcePath,[],[n]);});}).finally(function(){t.removePendingRequest();});};C.prototype.addPendingRequest=function(){var R;if(!this.oPendingRequestsPromise){this.oPendingRequestsPromise=new S(function(i){R=i;});this.oPendingRequestsPromise.$count=0;this.oPendingRequestsPromise.$resolve=R;}this.oPendingRequestsPromise.$count+=1;};C.prototype.calculateKeyPredicate=function(i,t,M){var p,T=t[M];if(T&&T.$Key){p=a.getKeyPredicate(i,M,t);if(p){a.setPrivateAnnotation(i,"predicate",p);}}return p;};C.prototype.checkActive=function(){var E;if(!this.bActive){E=new Error("Response discarded: cache is inactive");E.canceled=true;throw E;}};C.prototype.create=function(G,p,i,t,E,j,k,l){var n,K=E&&E["@$ui5.keepTransientPath"],o=this;function u(){a.removeByPath(o.mPostRequests,i,E);n.splice(n.indexOf(E),1);n.$created-=1;e(o.mChangeListeners,i,n,-1);delete n.$byPredicate[t];if(!i){o.adjustReadRequests(0,-1);}j();}function v(){o.addPendingRequest();a.setPrivateAnnotation(E,"transient",true);l();}function w(x,y){var z=y.getGroupId();a.setPrivateAnnotation(E,"transient",z);a.addByPath(o.mPostRequests,i,E);return S.all([o.oRequestor.request("POST",x,y,null,E,v,u,undefined,a.buildPath(o.sResourcePath,i,t)),o.fetchTypes()]).then(function(R){var A=R[0],B;a.deletePrivateAnnotation(E,"transient");E["@$ui5.context.isTransient"]=false;a.removeByPath(o.mPostRequests,i,E);o.visitResponse(A,R[1],a.getMetaPath(a.buildPath(o.sMetaPath,i)),i+t,K);if(!K){B=a.getPrivateAnnotation(A,"predicate");if(B){n.$byPredicate[B]=E;a.updateTransientPaths(o.mChangeListeners,t,B);}}a.updateSelected(o.mChangeListeners,a.buildPath(i,B||t),E,A,a.getQueryOptionsForPath(o.mQueryOptions,i).$select);o.removePendingRequest();return E;},function(A){if(A.canceled){throw A;}o.removePendingRequest();k(A);return w(x,o.oRequestor.lockGroup(o.oRequestor.getGroupSubmitMode(z)==="API"?z:"$parked."+z,o,true,true));});}E=q.extend(true,{},E);E=b.cleanPayload(E);a.setPrivateAnnotation(E,"transientPredicate",t);E["@$ui5.context.isTransient"]=true;n=this.getValue(i);if(!Array.isArray(n)){throw new Error("Create is only supported for collections; '"+i+"' does not reference a collection");}n.unshift(E);n.$created+=1;e(this.mChangeListeners,i,n,1);n.$byPredicate=n.$byPredicate||{};n.$byPredicate[t]=E;if(!i){o.adjustReadRequests(0,1);}return p.then(function(x){x+=o.oRequestor.buildQueryString(o.sMetaPath,o.mQueryOptions,true);return w(x,G);});};C.prototype.deregisterChange=function(p,l){a.removeByPath(this.mChangeListeners,p,l);};C.prototype.drillDown=function(D,p,G){var o=S.resolve(D),E,j,k,t=false,l=this;function n(i){L.error("Failed to drill-down into "+p+", invalid segment: "+i,l.toString(),"sap.ui.model.odata.v4.lib._Cache");return undefined;}function u(v,i,w){var x="",R,y;if(p[0]!=='('){x+="/";}x+=p.split("/").slice(0,w).join("/");return l.oRequestor.getModelInterface().fetchMetadata(l.sMetaPath+a.getMetaPath(x)).then(function(z){if(!z){return n(i);}if(z.$Type==="Edm.Stream"){R=v[i+"@odata.mediaReadLink"];y=l.oRequestor.getServiceUrl();return R||y+l.sResourcePath+x;}if(!t){if(G&&E&&z.$kind==="Property"){return l.fetchLateProperty(G,E,k.slice(0,j).join("/"),k.slice(j).join("/"),k.slice(j,w).join("/"));}return n(i);}if(z.$kind==="NavigationProperty"){return null;}if(!z.$Type.startsWith("Edm.")){return{};}if("$DefaultValue"in z){return z.$Type==="Edm.String"?z.$DefaultValue:a.parseLiteral(z.$DefaultValue,z.$Type,x);}return null;});}if(!p){return o;}k=p.split("/");return k.reduce(function(v,w,i){return v.then(function(V){var M,x;if(w==="$count"){return Array.isArray(V)?V.$count:n(w);}if(V===undefined||V===null){return undefined;}if(typeof V!=="object"||w==="@$ui5._"){return n(w);}if(a.getPrivateAnnotation(V,"predicate")){E=V;j=i;}x=V;t=t||a.getPrivateAnnotation(V,"transient");M=d.exec(w);if(M){if(M[1]){V=V[M[1]];}if(V){V=V.$byPredicate[M[2]];}}else{V=V[C.from$skip(w,V)];}return V===undefined&&w[0]!=="#"&&w[0]!=="@"?u(x,w,i+1):V;});},o);};C.prototype.fetchLateProperty=function(G,E,i,R,M){var p,Q=Object.assign({},this.mQueryOptions),j,t=this;this.mLateQueryOptions=this.mLateQueryOptions||a.merge({},this.mQueryOptions);if(this.mLateQueryOptions.$select.indexOf(R)<0){this.mLateQueryOptions.$select.push(R);}delete Q.$apply;delete Q.$count;delete Q.$expand;delete Q.$filter;delete Q.$orderby;delete Q.$search;Q.$select=R;j=a.buildPath(this.sResourcePath,i)+this.oRequestor.buildQueryString(this.sMetaPath,Q);p=this.mPropertyRequestByPath[j];if(!p){p=this.oRequestor.request("GET",j,G).then(function(D){if(D["@odata.etag"]!==E["@odata.etag"]){throw new Error("GET "+j+": ETag changed");}a.updateSelected(t.mChangeListeners,i,E,D);return a.drillDown(E,M.split("/"));}).finally(function(){delete t.mPropertyRequestByPath[j];});this.mPropertyRequestByPath[j]=p;}return p;};C.prototype.fetchTypes=function(){var p,t,i=this;function j(B,Q){if(Q&&Q.$expand){Object.keys(Q.$expand).forEach(function(n){var M=B;n.split("/").forEach(function(l){M+="/"+l;k(M);});j(M,Q.$expand[n]);});}}function k(M){p.push(i.oRequestor.fetchTypeForPath(M).then(function(T){var o=i.oRequestor.getModelInterface().fetchMetadata(M+"/"+m).getResult();if(o){T=Object.create(T);T[m]=o;}t[M]=T;if(T&&T.$Key){T.$Key.forEach(function(K){var I,l;if(typeof K!=="string"){l=K[Object.keys(K)[0]];I=l.lastIndexOf("/");if(I>=0){k(M+"/"+l.slice(0,I));}}});}}));}if(!this.oTypePromise){p=[];t={};k(this.sMetaPath);if(this.bFetchOperationReturnType){k(this.sMetaPath+"/$Type");}j(this.sMetaPath,this.mQueryOptions);this.oTypePromise=S.all(p).then(function(){return t;});}return this.oTypePromise;};C.prototype.getMeasureRangePromise=function(){return undefined;};C.prototype.getValue=function(p){throw new Error("Unsupported operation");};C.prototype.getOriginalResourcePath=function(E){return this.fnGetOriginalResourcePath&&this.fnGetOriginalResourcePath(E)||this.sResourcePath;};C.prototype.hasPendingChangesForPath=function(p){return Object.keys(this.mPatchRequests).some(function(R){return f(R,p);})||Object.keys(this.mPostRequests).some(function(R){return f(R,p);});};C.prototype.patch=function(p,D){var t=this;return this.fetchValue(_.$cached,p).then(function(o){a.updateExisting(t.mChangeListeners,p,o,D);return o;});};C.prototype.refreshSingle=function(G,p,i,D){var t=this;return this.fetchValue(_.$cached,p).then(function(E){var j=a.getPrivateAnnotation(E[i],"predicate"),R=a.buildPath(t.sResourcePath,p,j),Q=Object.assign({},a.getQueryOptionsForPath(t.mQueryOptions,p));delete Q["$apply"];delete Q["$count"];delete Q["$filter"];delete Q["$orderby"];delete Q["$search"];R+=t.oRequestor.buildQueryString(t.sMetaPath,Q,false,t.bSortExpandSelect);t.bSentReadRequest=true;return S.all([t.oRequestor.request("GET",R,G,undefined,undefined,D),t.fetchTypes()]).then(function(k){var o=k[0];t.replaceElement(E,i,j,o,k[1],p);return o;});});};C.prototype.refreshSingleWithRemove=function(G,p,i,D,o){var t=this;return S.all([this.fetchValue(_.$cached,p),this.fetchTypes()]).then(function(R){var E=R[0],j=E[i],k=a.getPrivateAnnotation(j,"predicate"),Q=Object.assign({},a.getQueryOptionsForPath(t.mQueryOptions,p)),F=Q["$filter"],l=a.buildPath(t.sResourcePath,p),T=R[1];delete Q["$count"];delete Q["$orderby"];Q["$filter"]=(F?"("+F+") and ":"")+a.getKeyFilter(j,t.sMetaPath,T);l+=t.oRequestor.buildQueryString(t.sMetaPath,Q,false,t.bSortExpandSelect);t.bSentReadRequest=true;return t.oRequestor.request("GET",l,G,undefined,undefined,D).then(function(n){if(n.value.length>1){throw new Error("Unexpected server response, more than one entity returned.");}else if(n.value.length===0){t.removeElement(E,i,k,p);t.oRequestor.getModelInterface().reportBoundMessages(t.sResourcePath,[],[p+k]);o();}else{t.replaceElement(E,i,k,n.value[0],T,p);}});});};C.prototype.registerChange=function(p,l){a.addByPath(this.mChangeListeners,p,l);};C.prototype.removeElement=function(E,i,p,j){var o,t;i=C.getElementIndex(E,p,i);o=E[i];E.splice(i,1);delete E.$byPredicate[p];t=a.getPrivateAnnotation(o,"transientPredicate");if(t){E.$created-=1;delete E.$byPredicate[t];}else if(!j){this.iLimit-=1;this.adjustReadRequests(i,-1);}e(this.mChangeListeners,j,E,-1);return i;};C.prototype.removePendingRequest=function(){this.oPendingRequestsPromise.$count-=1;if(!this.oPendingRequestsPromise.$count){this.oPendingRequestsPromise.$resolve();this.oPendingRequestsPromise=null;}};C.prototype.replaceElement=function(E,i,p,o,t,j){var O,T;i=C.getElementIndex(E,p,i);O=E[i];E[i]=E.$byPredicate[p]=o;T=a.getPrivateAnnotation(O,"transientPredicate");if(T){o["@$ui5.context.isTransient"]=false;E.$byPredicate[T]=o;a.setPrivateAnnotation(o,"transientPredicate",T);}this.visitResponse(o,t,a.getMetaPath(a.buildPath(this.sMetaPath,j)),j+p);};C.prototype.resetChangesForPath=function(p){var t=this;Object.keys(this.mPatchRequests).forEach(function(R){var i,j;if(f(R,p)){j=t.mPatchRequests[R];for(i=j.length-1;i>=0;i-=1){t.oRequestor.removePatch(j[i]);}delete t.mPatchRequests[R];}});Object.keys(this.mPostRequests).forEach(function(R){var E,i,T;if(f(R,p)){E=t.mPostRequests[R];for(i=E.length-1;i>=0;i-=1){T=a.getPrivateAnnotation(E[i],"transient");t.oRequestor.removePost(T,E[i]);}delete t.mPostRequests[R];}});};C.prototype.setActive=function(A){this.bActive=A;if(!A){this.mChangeListeners={};}};C.prototype.setQueryOptions=function(Q){if(this.bSentReadRequest){throw new Error("Cannot set query options: Cache has already sent a read request");}this.mQueryOptions=Q;this.sQueryString=this.oRequestor.buildQueryString(this.sMetaPath,Q,false,this.bSortExpandSelect);};C.prototype.toString=function(){return this.oRequestor.getServiceUrl()+this.sResourcePath+this.sQueryString;};C.prototype.update=function(G,p,v,E,i,j,u,k,l){var o,n=p.split("/"),U,t=this;try{o=this.fetchValue(_.$cached,j);}catch(w){if(!w.$cached){throw w;}o=S.resolve({"@odata.etag":"*"});}return o.then(function(x){var F=a.buildPath(j,p),y=G.getGroupId(),O,z,A,T,B,D=C.makeUpdateData(n,v);function H(){a.removeByPath(t.mPatchRequests,F,z);a.updateExisting(t.mChangeListeners,j,x,C.makeUpdateData(n,O));}function I(J,K){var R;function M(){R=t.oRequestor.lockGroup(y,t,true);if(l){l();}}z=t.oRequestor.request("PATCH",i,J,{"If-Match":x},D,M,H,undefined,a.buildPath(t.getOriginalResourcePath(x),j),K);a.addByPath(t.mPatchRequests,F,z);return S.all([z,t.fetchTypes()]).then(function(N){var Q=N[0];a.removeByPath(t.mPatchRequests,F,z);if(!k){t.visitResponse(Q,N[1],a.getMetaPath(a.buildPath(t.sMetaPath,j)),j);}a.updateExisting(t.mChangeListeners,j,x,k?{"@odata.etag":Q["@odata.etag"]}:Q);},function(w){var N=y;a.removeByPath(t.mPatchRequests,F,z);if(!E||w.canceled){throw w;}E(w);switch(t.oRequestor.getGroupSubmitMode(y)){case"API":break;case"Auto":if(!t.oRequestor.hasChanges(y,x)){N="$parked."+y;}break;default:throw w;}R.unlock();R=undefined;return I(t.oRequestor.lockGroup(N,t,true,true),true);}).finally(function(){if(R){R.unlock();}});}if(!x){throw new Error("Cannot update '"+p+"': '"+j+"' does not exist");}T=a.getPrivateAnnotation(x,"transient");if(T){if(T===true){throw new Error("No 'update' allowed while waiting for server response");}if(T.indexOf("$parked.")===0){A=T;T=T.slice(8);}if(T!==y){throw new Error("The entity will be created via group '"+T+"'. Cannot patch via group '"+y+"'");}}O=a.drillDown(x,n);a.updateSelected(t.mChangeListeners,j,x,D);if(u){U=u.split("/");u=a.buildPath(j,u);B=t.getValue(u);if(B===undefined){L.debug("Missing value for unit of measure "+u+" when updating "+F,t.toString(),"sap.ui.model.odata.v4.lib._Cache");}else{q.extend(true,T?x:D,C.makeUpdateData(U,B));}}if(T){if(A){a.setPrivateAnnotation(x,"transient",T);t.oRequestor.relocate(A,x,T);}G.unlock();return Promise.resolve();}t.oRequestor.relocateAll("$parked."+y,y,x);i+=t.oRequestor.buildQueryString(t.sMetaPath,t.mQueryOptions,true);return I(G);});};C.prototype.visitResponse=function(R,t,j,k,K,l){var n,H=false,p={},o=this.oRequestor.getServiceUrl()+this.sResourcePath,u=this;function v(M,i,z){H=true;if(M&&M.length){p[i]=M;M.forEach(function(A){if(A.longtextUrl){A.longtextUrl=a.makeAbsolute(A.longtextUrl,z);}});}}function w(B,i){return i?a.makeAbsolute(i,B):B;}function x(I,M,z,A){var B={},i,D,E,F;for(i=0;i<I.length;i+=1){E=I[i];D=z===""?l+i:i;if(E&&typeof E==="object"){y(E,M,z,A,D);F=a.getPrivateAnnotation(E,"predicate");if(!z){n.push(F||D.toString());}if(F){B[F]=E;I.$byPredicate=B;}}}}function y(i,M,I,z,A){var B,D,T=t[M],E=T&&T[m]&&T[m].$Path,F;z=w(z,i["@odata.context"]);D=u.calculateKeyPredicate(i,t,M);if(A!==undefined){I=a.buildPath(I,D||A);}else if(!K&&D){B=r.exec(I);if(B){I=I.slice(0,-B[0].length)+D;}}if(k&&!n){n=[I];}if(E){F=a.drillDown(i,E.split("/"));if(F!==undefined){v(F,I,z);}}Object.keys(i).forEach(function(G){var J,N=M+"/"+G,O=i[G],Q=a.buildPath(I,G);if(G.endsWith("@odata.mediaReadLink")){i[G]=a.makeAbsolute(O,z);}if(G.includes("@")){return;}if(Array.isArray(O)){O.$created=0;O.$count=undefined;J=i[G+"@odata.count"];if(J){s({},"",O,J);}else if(!i[G+"@odata.nextLink"]){s({},"",O,O.length);}x(O,N,Q,w(z,i[G+"@odata.context"]));}else if(O&&typeof O==="object"){y(O,N,Q,z);}});}if(l!==undefined){n=[];x(R.value,j||this.sMetaPath,"",w(o,R["@odata.context"]));}else if(R&&typeof R==="object"){y(R,j||this.sMetaPath,k||"",o);}if(H){this.oRequestor.getModelInterface().reportBoundMessages(this.getOriginalResourcePath(R),p,n);}};function g(R,i,Q,j,D){C.call(this,R,i,Q,j,function(){return D;});this.sContext=undefined;this.aElements=[];this.aElements.$byPredicate={};this.aElements.$count=undefined;this.aElements.$created=0;this.aElements.$tail=undefined;this.iLimit=Infinity;this.aReadRequests=[];this.oSyncPromiseAll=undefined;}g.prototype=Object.create(C.prototype);g.prototype.adjustReadRequests=function(i,o){this.aReadRequests.forEach(function(R){if(R.iStart>=i){R.iStart+=o;R.iEnd+=o;}});};g.prototype.fetchValue=function(G,p,D,l,F){var E,t=this;G.unlock();if(!this.oSyncPromiseAll){E=this.aElements.$tail?this.aElements.concat(this.aElements.$tail):this.aElements;this.oSyncPromiseAll=S.all(E);}return this.oSyncPromiseAll.then(function(){t.checkActive();t.registerChange(p,l);return t.drillDown(t.aElements,p,F?G.getUnlockedCopy():undefined);});};g.prototype.fill=function(p,j,E){var i,n=Math.max(this.aElements.length,1024);if(E>n){if(this.aElements.$tail&&p){throw new Error("Cannot fill from "+j+" to "+E+", $tail already in use, # of elements is "+this.aElements.length);}this.aElements.$tail=p;E=this.aElements.length;}for(i=j;i<E;i+=1){this.aElements[i]=p;}this.oSyncPromiseAll=undefined;};g.prototype.getQueryString=function(){var Q=Object.assign({},this.mQueryOptions),E,j,F=Q["$filter"],i,k,K=[],l=this.sQueryString,t;for(i=0;i<this.aElements.$created;i+=1){E=this.aElements[i];if(!E["@$ui5.context.isTransient"]){t=t||this.fetchTypes().getResult();k=a.getKeyFilter(E,this.sMetaPath,t);if(k){K.push(k);}}}if(K.length){j="not ("+K.join(" or ")+")";if(F){Q["$filter"]="("+F+") and "+j;l=this.oRequestor.buildQueryString(this.sMetaPath,Q,false,this.bSortExpandSelect);}else{l+=(l?"&":"?")+"$filter="+a.encode(j,false);}}return l;};g.prototype.getReadRange=function(j,l,p){var E=this.aElements;function k(j,n){var i;for(i=j;i<n;i+=1){if(E[i]===undefined){return true;}}return false;}if(k(j+l,j+l+p/2)){l+=p;}if(k(Math.max(j-p/2,0),j)){l+=p;j-=p;if(j<0){l+=j;if(isNaN(l)){l=Infinity;}j=0;}}return{length:l,start:j};};g.prototype.getResourcePath=function(i,E){var j=this.aElements.$created,Q=this.getQueryString(),D=Q?"&":"?",k=E-i,R=this.sResourcePath+Q;if(i<j){throw new Error("Must not request created element");}i-=j;if(i>0||k<Infinity){R+=D+"$skip="+i;}if(k<Infinity){R+="&$top="+k;}return R;};g.prototype.getValue=function(p){var o=this.drillDown(this.aElements,p);if(o.isFulfilled()){return o.getResult();}};g.prototype.handleResponse=function(j,E,R,t){var k=-1,l,n=this.aElements.$created,o,i,O=this.aElements.$count,p,u=R.value.length;this.sContext=R["@odata.context"];this.visitResponse(R,t,undefined,undefined,undefined,j);for(i=0;i<u;i+=1){o=R.value[i];this.aElements[j+i]=o;p=a.getPrivateAnnotation(o,"predicate");if(p){this.aElements.$byPredicate[p]=o;}}l=R["@odata.count"];if(l){this.iLimit=k=parseInt(l);}if(u<E-j){if(k===-1){k=O&&O-n;}k=Math.min(k!==undefined?k:Infinity,j-n+u);this.aElements.length=n+k;this.iLimit=k;if(!l&&k>0&&!this.aElements[k-1]){k=undefined;}}if(k!==-1){s(this.mChangeListeners,"",this.aElements,k!==undefined?k+n:undefined);}};g.prototype.read=function(I,l,p,G,D){var i,n,E,j,k=-1,o=this.oPendingRequestsPromise||this.aElements.$tail,R,t=this;if(I<0){throw new Error("Illegal index "+I+", must be >= 0");}if(l<0){throw new Error("Illegal length "+l+", must be >= 0");}if(o){return o.then(function(){return t.read(I,l,p,G,D);});}R=this.getReadRange(I,l,p);j=Math.min(R.start+R.length,this.aElements.$created+this.iLimit);n=Math.min(j,Math.max(R.start,this.aElements.length)+1);for(i=R.start;i<n;i+=1){if(this.aElements[i]!==undefined){if(k>=0){this.requestElements(k,i,G.getUnlockedCopy(),D);D=undefined;k=-1;}}else if(k<0){k=i;}}if(k>=0){this.requestElements(k,j,G.getUnlockedCopy(),D);}G.unlock();E=this.aElements.slice(I,j);if(this.aElements.$tail){E.push(this.aElements.$tail);}return S.all(E).then(function(){var u;t.checkActive();u={"@odata.context":t.sContext,value:t.aElements.slice(I,j)};u.value.$count=t.aElements.$count;return u;});};g.prototype.requestElements=function(i,E,G,D){var p,R={iEnd:E,iStart:i},t=this;this.aReadRequests.push(R);p=S.all([this.oRequestor.request("GET",this.getResourcePath(i,E),G,undefined,undefined,D),this.fetchTypes()]).then(function(j){if(t.aElements.$tail===p){t.aElements.$tail=undefined;}t.handleResponse(R.iStart,R.iEnd,j[0],j[1]);}).catch(function(o){t.fill(undefined,R.iStart,R.iEnd);throw o;}).finally(function(){t.aReadRequests.splice(t.aReadRequests.indexOf(R),1);});this.bSentReadRequest=true;this.fill(p,i,E);};g.prototype.requestSideEffects=function(G,p,N,j,l){var E,F=[],Q,R,t=this.fetchTypes().getResult(),k=this,i;function o(n){var u=a.getKeyFilter(n,k.sMetaPath,t);F.push(u);return u;}if(this.oPendingRequestsPromise){return this.oPendingRequestsPromise.then(function(){return k.requestSideEffects(G,p,N,j,l);});}Q=a.intersectQueryOptions(this.mLateQueryOptions||this.mQueryOptions,p,this.oRequestor.getModelInterface().fetchMetadata,this.sMetaPath,N);if(!Q){return S.resolve();}if(l===undefined){if(!o(this.aElements[j])){return null;}}else{for(i=0;i<this.aElements.length;i+=1){E=this.aElements[i];if(!E||a.hasPrivateAnnotation(E,"transient")){continue;}if((i<j||i>=j+l)&&!a.hasPrivateAnnotation(E,"transientPredicate")){delete this.aElements.$byPredicate[a.getPrivateAnnotation(E,"predicate")];delete this.aElements[i];continue;}if(!o(E)){return null;}}this.aElements.length=l?Math.min(j+l,this.aElements.length):this.aElements.$created;if(!F.length){return S.resolve();}}Q.$filter=F.join(" or ");a.selectKeyProperties(Q,t[this.sMetaPath]);delete Q.$count;delete Q.$orderby;delete Q.$search;R=this.sResourcePath+this.oRequestor.buildQueryString(this.sMetaPath,Q,false,true);return this.oRequestor.request("GET",R,G).then(function(u){var E,v,i,n;if(u.value.length!==F.length){throw new Error("Expected "+F.length+" row(s), but instead saw "+u.value.length);}k.visitResponse(u,t,undefined,"",false,NaN);for(i=0,n=u.value.length;i<n;i+=1){E=u.value[i];v=a.getPrivateAnnotation(E,"predicate");a.updateSelected(k.mChangeListeners,v,k.aElements.$byPredicate[v],E);}});};function P(R,i,Q){C.call(this,R,i,Q);this.oPromise=null;}P.prototype=Object.create(C.prototype);P.prototype._delete=function(){throw new Error("Unsupported");};P.prototype.create=function(){throw new Error("Unsupported");};P.prototype.fetchValue=function(G,p,D,l){var t=this;t.registerChange("",l);if(this.oPromise){G.unlock();}else{this.oPromise=S.resolve(this.oRequestor.request("GET",this.sResourcePath+this.sQueryString,G,undefined,undefined,D,undefined,this.sMetaPath));this.bSentReadRequest=true;}return this.oPromise.then(function(R){t.checkActive();return R.value;});};P.prototype.update=function(){throw new Error("Unsupported");};function h(R,i,Q,j,G,p,M,F){C.apply(this,arguments);this.bFetchOperationReturnType=F;this.sMetaPath=M||this.sMetaPath;this.bPost=p;this.bPosting=false;this.oPromise=null;}h.prototype=Object.create(C.prototype);h.prototype.fetchValue=function(G,p,D,l,F){var R=this.sResourcePath+this.sQueryString,t=this;this.registerChange(p,l);if(this.oPromise){G.unlock();}else{if(this.bPost){throw new Error("Cannot fetch a value before the POST request");}this.oPromise=S.all([this.oRequestor.request("GET",R,G,undefined,undefined,D,undefined,this.sMetaPath),this.fetchTypes()]).then(function(i){t.visitResponse(i[0],i[1],t.bFetchOperationReturnType?t.sMetaPath+"/$Type":undefined);return i[0];});this.bSentReadRequest=true;}return this.oPromise.then(function(o){t.checkActive();if(o["$ui5.deleted"]){throw new Error("Cannot read a deleted entity");}return t.drillDown(o,p,F?G.getUnlockedCopy():undefined);});};h.prototype.getValue=function(p){var o;if(this.oPromise&&this.oPromise.isFulfilled()){o=this.drillDown(this.oPromise.getResult(),p);if(o.isFulfilled()){return o.getResult();}}};h.prototype.post=function(G,D,E){var i,H="POST",t=this;if(!this.bPost){throw new Error("POST request not allowed");}if(this.bPosting){throw new Error("Parallel POST requests not allowed");}if(E){i=G.getGroupId();this.oRequestor.relocateAll("$parked."+i,i,E);}if(D){H=D["X-HTTP-Method"]||H;delete D["X-HTTP-Method"];if(this.oRequestor.isActionBodyOptional()&&!Object.keys(D).length){D=undefined;}}this.oPromise=S.all([this.oRequestor.request(H,this.sResourcePath+this.sQueryString,G,{"If-Match":E},D),this.fetchTypes()]).then(function(R){t.bPosting=false;t.visitResponse(R[0],R[1],t.bFetchOperationReturnType?t.sMetaPath+"/$Type":undefined);return R[0];},function(o){t.bPosting=false;throw o;});this.bPosting=true;return this.oPromise;};h.prototype.requestSideEffects=function(G,p,n,R){var o=this.fetchValue(_.$cached,""),Q=a.intersectQueryOptions(this.mLateQueryOptions||this.mQueryOptions,p,this.oRequestor.getModelInterface().fetchMetadata,this.sMetaPath+"/$Type",n),i,t=this;if(!Q){return o;}R=(R||this.sResourcePath)+this.oRequestor.buildQueryString(this.sMetaPath,Q,false,true);i=S.all([this.oRequestor.request("GET",R,G),this.fetchTypes(),o]).then(function(j){var N=j[0],O=j[2];t.visitResponse(N,j[1]);a.updateSelected(t.mChangeListeners,"",O,N);return O;});this.oPromise=i.catch(function(){return o;});return i;};C.create=function(R,i,Q,j,D){return new g(R,i,Q,j,D);};C.createProperty=function(R,i,Q){return new P(R,i,Q);};C.createSingle=function(R,i,Q,j,G,p,M,F){return new h(R,i,Q,j,G,p,M,F);};C.from$skip=function(i,j){return c.test(i)?j.$created+Number(i):i;};C.getElementIndex=function(E,k,i){var o=E[i];if(!o||a.getPrivateAnnotation(o,"predicate")!==k){i=E.indexOf(E.$byPredicate[k]);}return i;};C.makeUpdateData=function(p,v){return p.reduceRight(function(V,i){var R={};R[i]=V;return R;},v);};return C;},false);
