/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([],function(){"use strict";var M={};M.fileNameMatchesPattern=function(f,p){if(f.startsWith(p.NAMESPACE)&&f.endsWith(p.FILETYPE)){f=f.replace(new RegExp("^"+p.NAMESPACE),"");f=f.replace(new RegExp(p.FILETYPE+"$"),"");return f.indexOf("/")===-1;}return false;};return M;},false);
