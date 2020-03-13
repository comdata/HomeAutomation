/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/ui/core/Control","sap/f/CardRenderer","sap/f/library"],function(C,a,l){"use strict";var H=l.cards.HeaderPosition;var b=C.extend("sap.f.Card",{metadata:{library:"sap.f",interfaces:["sap.f.ICard"],properties:{width:{type:"sap.ui.core.CSSSize",group:"Appearance",defaultValue:"100%"},height:{type:"sap.ui.core.CSSSize",group:"Appearance",defaultValue:"auto"},headerPosition:{type:"sap.f.cards.HeaderPosition",group:"Appearance",defaultValue:H.Top}},aggregations:{header:{type:"sap.f.cards.IHeader",multiple:false},content:{type:"sap.ui.core.Control",multiple:false}}},renderer:a});b.prototype.getCardHeader=function(){return this.getHeader();};b.prototype.getCardHeaderPosition=function(){return this.getHeaderPosition();};b.prototype.getCardContent=function(){return this.getContent();};return b;});
