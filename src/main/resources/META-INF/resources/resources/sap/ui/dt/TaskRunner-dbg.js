/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([
	"sap/ui/dt/TaskManager",
	"sap/ui/dt/Util",
	"sap/base/Log"

],
function(
	TaskManager,
	DtUtil,
	BaseLog
) {
	"use strict";

	/**
	 * Constructor for a new TaskRunner.
	 *
	 * @param {object} mParam - initial settings for the new object
	 * @param {object} mParam.taskManager - TaskManager to be observed
	 *
	 * @class
	 * TaskRunner run tasks defined in sap.ui.dt.TaskManager.
	 *
	 * @author SAP SE
	 * @version 1.71.1
	 *
	 * @constructor
	 * @private
	 * @since 1.69
	 * @alias sap.ui.dt.TaskManager
	 * @experimental Since 1.69. This class is experimental and provides only limited functionality. Also the API might be changed in future.
	 */
	var TaskRunner = function (mParam) {
		if (!mParam || !mParam.taskManager || !(mParam.taskManager instanceof TaskManager)) {
			throw DtUtil.createError("TaskRunner#constructor", "sap.ui.dt.TaskRunner: TaskManager required");
		}
		this._oTaskManager = mParam.taskManager;
		this._sInitialTaskType = mParam.taskType;
		this._sObservedTaskType = mParam.taskType;

		this._iRequestId = undefined;
		this.bIsStopped = true;
	};

	// TaskRunner.prototype._breakObserve = function () {
	TaskRunner.prototype._shouldObserveBreak = function () {
		if (
			this.bIsStopped
			|| !this._oTaskManager
			|| this._oTaskManager.bIsDestroyed
		) {
			this.bIsStopped = true;
			return true;
		}
		return false;
	};

	TaskRunner.prototype._observe = function () {
		this._checkTasks();
		if (!this._shouldObserveBreak()) {
			this._iRequestId = window.requestAnimationFrame(this._observe.bind(this));
		}
	};

	TaskRunner.prototype._unobserve = function () {
		if (this._iRequestId) {
			window.cancelAnimationFrame(this._iRequestId);
			this._iRequestId = undefined;
		}
	};

	TaskRunner.prototype._checkTasks = function () {
		var aTasks = this._oTaskManager.getList(this._sObservedTaskType);
		if (aTasks.length) {
			this._runTasks(aTasks);
		}
	};

	TaskRunner.prototype._runTasks = function (aTasks) {
		for (var i = 0, n = aTasks.length; i < n; i++) {
			if (aTasks[i].callbackFn) {
				try {
					aTasks[i].callbackFn();
				} catch (vError) {
					BaseLog.error(DtUtil.errorToString(vError));
				}
			}
			this._oTaskManager.complete(aTasks[i].id);
		}
	};

	TaskRunner.prototype.run = function (sTaskType) {
		this._sObservedTaskType = sTaskType || this._sInitialTaskType;
		this.bIsStopped = false;
		this._observe();
	};

	TaskRunner.prototype.stop = function () {
		this.bIsStopped = true;
		this._unobserve();
	};

	return TaskRunner;
});