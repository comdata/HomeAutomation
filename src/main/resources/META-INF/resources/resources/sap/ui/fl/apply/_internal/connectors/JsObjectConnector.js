/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(["sap/base/util/merge","sap/ui/fl/apply/_internal/connectors/BrowserStorageConnector"],function(m,B){"use strict";var M={_items:{},setItem:function(k,v){M._items[k]=v;},removeItem:function(k){delete M._items[k];},clear:function(){M._items={};},getItem:function(k){return M._items[k];},getItems:function(){return M._items;}};var J=m({},B,{oStorage:M});return J;},true);
