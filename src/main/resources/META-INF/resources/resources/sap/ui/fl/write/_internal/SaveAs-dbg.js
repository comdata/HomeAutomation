/*
 * ! OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */

sap.ui.define([
	"sap/ui/fl/descriptorRelated/api/DescriptorVariantFactory",
	"sap/ui/fl/transport/TransportSelection",
	"sap/ui/fl/descriptorRelated/api/DescriptorInlineChangeFactory",
	"sap/ui/fl/apply/_internal/ChangesController",
	"sap/ui/fl/Utils",
	"sap/base/Log",
	"sap/base/util/includes",
	"sap/base/util/merge"
], function(
	DescriptorVariantFactory,
	TransportSelection,
	DescriptorInlineChangeFactory,
	ChangesController,
	Utils,
	Log,
	includes,
	merge
) {
	"use strict";

	function _prepareTransportInfo(oAppVariant, mPropertyBag) {
		var oSettings = oAppVariant.getSettings();

		// Smart business colleagues must pass package and transport information as a part of propertybag in case of onPrem systems
		if (
			mPropertyBag
			&& mPropertyBag.package
			&& mPropertyBag.transport
			&& !oSettings.isAtoEnabled()
			&& !oSettings.isAtoAvailable()
		) {
			return Promise.resolve({
				packageName: mPropertyBag.package,
				transport: mPropertyBag.transport
			});
		}

		// Save As scenario for onPrem systems
		return Promise.resolve({
			packageName: "$TMP",
			transport: ""
		});
	}

	function _setTransportInfoForAppVariant(oAppVariant, oTransportInfo) {
		// Sets the transport info for app variant
		if (oTransportInfo) {
			if (oTransportInfo.transport && oTransportInfo.packageName !== "$TMP") {
				return oAppVariant.setTransportRequest(oTransportInfo.transport)
					.then(oAppVariant.setPackage(oTransportInfo.packageName));
			}
			return Promise.resolve();
		}
		return Promise.reject();
	}


	function _getInlineChangesFromDescrChanges(aDescrChanges) {
		var aInlineChangesPromises = [];

		aDescrChanges.forEach(function(oChange) {
			var oChangeDefinition = oChange.getDefinition();
			// Change contains only descriptor change information so the descriptor inline change needs to be created again
			aInlineChangesPromises.push(DescriptorInlineChangeFactory.createNew(oChangeDefinition.changeType, oChangeDefinition.content, oChangeDefinition.texts));
		});
		return Promise.all(aInlineChangesPromises);
	}

	function _moveChangesToNewFlexReference(oChange, oAppVariant) {
		var oPropertyBag = {
			reference: oAppVariant.getId()
		};
		var sChangesNamespace = Utils.createNamespace(oPropertyBag, "changes");
		oChange.setNamespace(sChangesNamespace);
		oChange.setComponent(oAppVariant.getId());
		if (oAppVariant.getVersion()) {
			// Only needed for RTA tool, Smart business might not pass the version
			oChange.setValidAppVersions({
				creation: oAppVariant.getVersion(),
				from: oAppVariant.getVersion()
			});
		}
	}

	function _inlineDescriptorChanges(aAllInlineChanges, oAppVariant) {
		var aAllDescrChanges = [];
		aAllInlineChanges.forEach(function(oInlineChange) {
			//Replace the hosting key with the new reference
			oInlineChange.replaceHostingIdForTextKey(oAppVariant.getId(), oAppVariant.getReference(), oInlineChange.getContent(), oInlineChange.getTexts());
			aAllDescrChanges.push(oAppVariant.addDescriptorInlineChange(oInlineChange));
		});

		return Promise.all(aAllDescrChanges);
	}

	function _triggerTransportHandling(oAppVariant) {
		var oTransportSelection = new TransportSelection();
		return oTransportSelection.openTransportSelection(oAppVariant);
	}

	function _getTransportInfo(oAppVariant, mPropertyBag) {
		var oSettings = oAppVariant.getSettings();
		// Since smart business has its own transport handling, they must pass transport in the property bag
		if (
			mPropertyBag
			&& mPropertyBag.transport
			&& !oSettings.isAtoEnabled()
			&& !oSettings.isAtoAvailable()
		) {
			return Promise.resolve({
				packageName: oAppVariant.getPackage(),
				transport: mPropertyBag.transport
			});
		}

		return _triggerTransportHandling(oAppVariant);
	}

	function _getDirtyDescrChanges(vSelector) {
		var aDescrChanges = ChangesController.getDescriptorFlexControllerInstance(vSelector)
			._oChangePersistence.getDirtyChanges();
		aDescrChanges = aDescrChanges.slice();
		return aDescrChanges;
	}

	function _getDirtyUIChanges(vSelector) {
		var aUIChanges = ChangesController.getFlexControllerInstance(vSelector)
			._oChangePersistence.getDirtyChanges();
		aUIChanges = aUIChanges.slice();
		return aUIChanges;
	}

	function _arePersistenciesTheSame(vSelector) {
		var oFlexControllerPersistence = ChangesController.getFlexControllerInstance(vSelector)._oChangePersistence;
		var oDescriptorFlexControllerPersistence = ChangesController.getDescriptorFlexControllerInstance(vSelector)._oChangePersistence;
		// If the base application is already an app variant, the references and therefore both persistences are same
		return oFlexControllerPersistence === oDescriptorFlexControllerPersistence;
	}

	function _reactOnChangesBasedOnPersistences(vSelector, bArePersistencesEqual, oAppVariant) {
		var aDescrChanges = [];
		if (bArePersistencesEqual) {
			_getDirtyDescrChanges(vSelector).forEach(function(oChange) {
				// UI and Descriptor changes need to be separated here so as to perform different operations on changes
				if (includes(DescriptorInlineChangeFactory.getDescriptorChangeTypes(), oChange.getDefinition().changeType)) {
					aDescrChanges.push(oChange);
				} else {
					_moveChangesToNewFlexReference(oChange, oAppVariant);
				}
			});
		} else {
			_getDirtyUIChanges(vSelector).forEach(function(oChange) {
				_moveChangesToNewFlexReference(oChange, oAppVariant);
			});

			aDescrChanges = _getDirtyDescrChanges(vSelector);
		}

		return aDescrChanges;
	}

	function _deleteDescrChangesFromPersistence(vSelector) {
		// In case of app variant, both persistences hold descriptor changes and have to be removed from one of the persistences
		_getDirtyDescrChanges(vSelector).forEach(function(oChange) {
			if (includes(DescriptorInlineChangeFactory.getDescriptorChangeTypes(), oChange.getChangeType())) {
				// In case of app variant, both persistences hold descriptor changes and have to be removed.
				// In case there are UI changes, they will be sent to the backend in the last rpomise chain and will be removed from the persistence
				ChangesController.getDescriptorFlexControllerInstance(vSelector)._oChangePersistence.deleteChange(oChange);
			}
		});
	}

	var SaveAs = {
		saveAs: function(mPropertyBag) {
			var oAppVariantClosure;
			var oAppVariantResultClosure;
			var bArePersistencesEqual = false;

			return DescriptorVariantFactory.createAppVariant(mPropertyBag)
				.then(function(oAppVariant) {
					oAppVariantClosure = merge({}, oAppVariant);
					return _prepareTransportInfo(oAppVariantClosure, mPropertyBag);
				})
				.then(function(oTransportInfo) {
					return _setTransportInfoForAppVariant(oAppVariantClosure, oTransportInfo);
				})
				.then(function() {
					bArePersistencesEqual = _arePersistenciesTheSame(mPropertyBag.selector);
					var aDescrChanges = _reactOnChangesBasedOnPersistences(mPropertyBag.selector, bArePersistencesEqual, oAppVariantClosure);
					return _getInlineChangesFromDescrChanges(aDescrChanges);
				})
				.then(function(aAllInlineChanges) {
					return _inlineDescriptorChanges(aAllInlineChanges, oAppVariantClosure);
				})
				.then(function() {
					// Save the app variant to backend
					return oAppVariantClosure.submit()
						.catch(function(oError) {
							oError.messageKey = "MSG_SAVE_APP_VARIANT_FAILED";
							oError.saveAsFailed = true;
							throw oError;
						});
				})
				.then(function(oResult) {
					oAppVariantResultClosure = merge({}, oResult);

					if (bArePersistencesEqual) {
						_deleteDescrChangesFromPersistence(mPropertyBag.selector);
					}

					var oFlexController = ChangesController.getFlexControllerInstance(mPropertyBag.selector);
					// Save the dirty UI changes to backend => firing PersistenceWriteApi.save
					return oFlexController.saveAll(true)
						.catch(function(oError) {
							if (bArePersistencesEqual) {
								_deleteDescrChangesFromPersistence(mPropertyBag.selector);
							}

							// Delete the inconsistent app variant if the UI changes failed to save
							return this.deleteAppVar({
								referenceAppId: mPropertyBag.id
							})
								.then(function() {
									oError.messageKey = "MSG_COPY_UNSAVED_CHANGES_FAILED";
									throw oError;
								});
						}.bind(this));
				}.bind(this))
				.then(function() {
					//Reference Application Usecase: Since the UI changes have been successfully saved, the descriptor inline changes will now be removed from persistence
					if (!bArePersistencesEqual) {
						_deleteDescrChangesFromPersistence(mPropertyBag.selector);
					}
					return oAppVariantResultClosure;
				})
				.catch(function(oError) {
					Log.error("the app variant could not be created.", oError.message || oError.name);
					throw oError;
				});
		},
		deleteAppVar: function(mPropertyBag) {
			var oAppVariantClosure;
			return DescriptorVariantFactory.loadAppVariant(mPropertyBag.referenceAppId, true)
				.catch(function(oError) {
					oError.messageKey = "MSG_LOAD_APP_VARIANT_FAILED";
					throw oError;
				})
				.then(function(oAppVariant) {
					oAppVariantClosure = merge({}, oAppVariant);
					return _getTransportInfo(oAppVariantClosure, mPropertyBag);
				})
				.then(function(oTransportInfo) {
					// Sets the transport info for app variant
					if (oTransportInfo) {
						if (oTransportInfo.transport) {
							return oAppVariantClosure.setTransportRequest(oTransportInfo.transport);
						}
						return oTransportInfo;
					}
					throw new Error("Transport information could not be determined");
				})
				.then(function () {
					return oAppVariantClosure.submit()
						.catch(function(oError) {
							oError.messageKey = "MSG_DELETE_APP_VARIANT_FAILED";
							throw oError;
						});
				})
				.catch(function(oError) {
					Log.error("the app variant could not be deleted.", oError.message || oError.name);
					throw oError;
				});
		}
	};
	return SaveAs;
}, true);