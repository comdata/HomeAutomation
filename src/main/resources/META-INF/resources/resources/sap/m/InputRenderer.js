/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define(['sap/ui/core/InvisibleText','sap/ui/core/Renderer','./InputBaseRenderer','sap/m/library'],function(I,R,a,l){"use strict";var b=l.InputType;var c=R.extend(a);c.apiVersion=2;c.addOuterClasses=function(r,C){r.class("sapMInput");if(C.getDescription()){r.class("sapMInputWithDescription");}};c.writeInnerAttributes=function(r,C){r.attr("type",C.getType().toLowerCase());if(C.getType()==b.Number){r.attr("step","any");}if(C.getType()==b.Number&&sap.ui.getCore().getConfiguration().getRTL()){r.attr("dir","ltr");r.style("text-align","right");}if(C.getShowSuggestion()||C.getShowValueStateMessage()){r.attr("autocomplete","off");}if((!C.getEnabled()&&C.getType()=="Password")||(C.getShowSuggestion()&&C._bUseDialog)||(C.getValueHelpOnly()&&C.getEnabled()&&C.getEditable()&&C.getShowValueHelp())){r.attr("readonly","readonly");}};c.addInnerClasses=function(r,C){};c.writeDescription=function(r,C){r.openStart("div");r.class("sapMInputDescriptionWrapper");r.style("width","calc(100% - "+C.getFieldWidth()+")");r.openEnd();r.openStart("span",C.getId()+"-descr");r.class("sapMInputDescriptionText");r.openEnd();r.text(C.getDescription());r.close("span");r.close("div");};c.writeDecorations=function(r,C){if(C.getDescription()){this.writeDescription(r,C);}if(sap.ui.getCore().getConfiguration().getAccessibility()){if(C.getShowSuggestion()&&C.getEnabled()&&C.getEditable()){r.openStart("span",C.getId()+"-SuggDescr").class("sapUiPseudoInvisibleText").attr("role","status").attr("aria-live","polite").openEnd().close("span");}}};c.addWrapperStyles=function(r,C){r.style("width",C.getDescription()?C.getFieldWidth():"100%");};c.getAriaLabelledBy=function(C){var d=a.getAriaLabelledBy.call(this,C)||"";if(C.getDescription()){d=d+" "+C.getId()+"-descr";}return d;};c.getAriaDescribedBy=function(C){var A=a.getAriaDescribedBy.apply(this,arguments);function d(s){A=A?A+" "+s:s;}if(C.getShowValueHelp()&&C.getEnabled()&&C.getEditable()){d(I.getStaticId("sap.m","INPUT_VALUEHELP"));if(C.getValueHelpOnly()){d(I.getStaticId("sap.m","INPUT_DISABLED"));}}if(C.getShowSuggestion()&&C.getEnabled()&&C.getEditable()){d(C.getId()+"-SuggDescr");}return A;};c.getAriaRole=function(C){return"";};c.getAccessibilityState=function(C){var A=a.getAccessibilityState.apply(this,arguments);if(C.getShowSuggestion()&&C.getEnabled()&&C.getEditable()){A.autocomplete="list";}return A;};return c;},true);
