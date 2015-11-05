/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define(["./library", "sap/ui/core/Control", "./Button", "./MessageBox"],
	function (library, Control, Button, MessageBox) {

	"use strict";

	/**
	 * Constructor for a new WizardStep.
	 *
	 * @param {string} [sId] id for the new control, generated automatically if no id is given
	 * @param {object} [mSettings] initial settings for the new control
	 *
	 * @class
	 * The WizardStep control is an aggregation of the Wizard control.
	 * This Control is supposed to be used only inside the Wizard, and should not be used as standalone.
	 * It aggregates the contents, and gives the developer the ability to validate, or invalidate the step.
	 * @extends sap.ui.core.Control
	 *
	 * @author SAP SE
	 * @version 1.30.10
	 *
	 * @constructor
	 * @public
	 * @since 1.30
	 * @alias sap.m.WizardStep
	 * @ui5-metamodel This control/element also will be described in the UI5 (legacy) designtime metamodel
	 */
	var WizardStep = Control.extend("sap.m.WizardStep", /** @lends sap.m.Wizard.prototype */ {
		metadata: {
			properties: {
				/**
				 * The title to be shown for the WizardStep control
				 */
				title: {type: "string", group: "appearance", defaultValue: ""},
				/**
				 * Indicates whether or not the step is valid
				 */
				validated: {type: "boolean", group: "Behavior", defaultValue: true}
			},
			events: {
				/**
				 * This event is fired after the user presses the Next button in the Wizard,
				 * or on nextStep() method call from the app developer
				 */
				complete: {
					parameters: {}
				},
				/**
				 * This event is fired on next step activation from the Wizard
				 */
				activate: {
					parameters: {}
				}
			},
			defaultAggregation: "content",
			aggregations: {
				/**
				 * The content of the Wizard Step
				 */
				content: {type: "sap.ui.core.Control", multiple: true, singularName: "content"},
				/**
				 * The edit button of the WizardStep
				 * when autoLocking is set to true
				 */
				_editButton: {type: "sap.m.Button", multiple: false, visibility: "hidden"}
			}
		}
	});

	WizardStep.prototype.init = function () {
		this._enabled = true;
		this._initEditButton();
	};

	WizardStep.prototype.getEnabled = function () {
		return this._enabled;
	};

	WizardStep.prototype.setValidated = function (validated) {
		this.setProperty("validated", validated, true);

		var parent = this._getWizardParent();
		if (parent === null) {
			return this;
		}

		if (validated) {
			parent.validateStep(this);
		} else {
			parent.invalidateStep(this);
		}

		return this;
	};

	WizardStep.prototype._getWizardParent = function () {
		var parent = this.getParent();

		while (!(parent instanceof sap.m.Wizard)) {
			if (parent === null) {
				return null;
			}
			parent = parent.getParent();
		}

		return parent;
	};

	//Could be used in the future
	WizardStep.prototype._initEditButton = function () {
		var that = this;
		var editButton = new Button({
			visible : false,
			icon : "sap-icon://edit",
			press : function () {
				MessageBox.confirm("Are you sure you want to edit this step and discard the progress?", {
					actions: [MessageBox.Action.YES, MessageBox.Action.NO],
					onClose: function (oAction) {
						if (oAction === MessageBox.Action.YES) {
							var wizParent = that._getWizardParent();
							wizParent.discardProgress(that);
						}
					}
				});

			}
		});

		editButton.getEnabled = function() {
			return true;
		};

		this.setAggregation("_editButton", editButton);
	};

	WizardStep.prototype._markAsLast = function () {
		this.addStyleClass("sapMWizardLastActivatedStep");
	};

	WizardStep.prototype._unMarkAsLast = function () {
		this.removeStyleClass("sapMWizardLastActivatedStep");
	};

	WizardStep.prototype._activate = function () {
		if (this.hasStyleClass("sapMWizardStepActivated")) {
			return;
		}

		this._markAsLast();
		this.addStyleClass("sapMWizardStepActivated");
		this.fireActivate();
	};

	WizardStep.prototype._deactivate = function (unlock) {
		this.removeStyleClass("sapMWizardStepActivated");
		if (unlock) {
			this._unlockContent();
		}
	};

	WizardStep.prototype._complete = function (lock) {
		this._unMarkAsLast();
		this.fireComplete();
		if (lock) {
			this._lockContent();
		}
	};

	//Could be used in the future
	WizardStep.prototype._lockContent = function () {
		this._enabled = false;
		this._getEditButton().setVisible(true);
		this.invalidate();
	};

	//Could be used in the future
	WizardStep.prototype._unlockContent = function () {
		this._enabled = true;
		this._getEditButton().setVisible(false);
		this.invalidate();
	};

	WizardStep.prototype._getEditButton = function ()  {
		return this.getAggregation("_editButton");
	};

	return WizardStep;

}, /* bExport= */ true);
