/*!
 * UI development toolkit for HTML5 (OpenUI5)
 * (c) Copyright 2009-2015 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
/*global QUnit */
/**
 * QUnit test adapter for opa.js has the same signature as a test of QUnit.
 * Suggested usage:
 * <code>
 * sap.ui.require(["sap/ui/test/Opa5", "sap/ui/test/opaQunit"], function (Opa5, opaTest) {
 *
 *    Opa5.extendConfig({
 *        assertions: new Opa5({
 *            checkIfSomethingIsOk : function () {
 *                this.waitFor({
 *                    success: function () {
 *                        Opa5.assert.ok(true, "Everything is fine");
 *                    }
 *                });
 *            }
 *        })
 *    });
 *
 *    opaTest("Should test something", function (Given, When, Then) {
 *       // Implementation of the test
 *       Then.checkIfSomethingIsOk();
 *    });
 *
 * });
 * </code>
 *
 * When you require this file, it will also introduce a global variable: opaTest
 * @public
 * @returns {asncTest} the async qunit test wrapped by opa
 * @experimental
 */
/////////////////////
//// OPA - One Page Acceptance testing the qUnit adapter
//// Currently this is distributed with UI5 but it does not have dependencies to it.
//// The only dependency is jQuery. As i plan to get this into a separate repository, i did not use the UI5 naming conventions
/////////////////////

sap.ui.define(['./Opa', './Opa5'], function (Opa, Opa5) {
	"use strict";
	var opaTest = function (testName, expected, callback, async) {
		var config = Opa.config;
		//Increase qunit's timeout to 90 seconds to match default OPA timeouts
		if (!QUnit.config.testTimeout) {
			QUnit.config.testTimeout  = 90000;
		}

		if (arguments.length === 2) {
			callback = expected;
			expected = null;
		}

		var testBody = function(assert) {
			var fnStart = assert.async();
			config.testName = testName;
			
			// provide current "assert" object to the tests
			Opa.assert = assert;
			Opa5.assert = assert;
			
			callback.call(this, config.arrangements, config.actions, config.assertions);

			var promise = Opa.emptyQueue();
			promise.done(function() {
				Opa.assert = undefined;
				Opa5.assert = undefined;
				fnStart();
			});

			promise.fail(function (oOptions) {
				QUnit.ok(false, oOptions.errorMessage);
				Opa.assert = undefined;
				Opa5.assert = undefined;
				fnStart();
			});
		};

		return QUnit.test(testName, expected, testBody, async);
	};
	// Export to global namespace to be backwards compatible
	window.opaTest = opaTest;

	return opaTest;
});

