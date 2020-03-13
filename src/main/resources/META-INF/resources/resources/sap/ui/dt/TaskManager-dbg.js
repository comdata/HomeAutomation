/*!
 * OpenUI5
 * (c) Copyright 2009-2019 SAP SE or an SAP affiliate company.
 * Licensed under the Apache License, Version 2.0 - see LICENSE.txt.
 */
sap.ui.define([
	"sap/ui/base/ManagedObject",
	"sap/base/util/isPlainObject"
],
function(
	ManagedObject,
	isPlainObject
) {
	"use strict";

	/**
	 * Constructor for a new TaskManager.
	 *
	 * @param {object} [mSettings] initial settings for the new object
	 *
	 * @class
	 * The TaskManager keeps list of task and allows to manage them via simple API.
	 *
	 * @author SAP SE
	 * @version 1.71.1
	 *
	 * @constructor
	 * @private
	 * @since 1.54
	 * @alias sap.ui.dt.TaskManager
	 * @experimental Since 1.54. This class is experimental and provides only limited functionality. Also the API might be changed in future.
	 */
	var TaskManager = ManagedObject.extend("sap.ui.dt.TaskManager", {
		metadata: {
			library: "sap.ui.dt",
			properties: {
				suppressEvents: {
					type: "boolean",
					defaultValue: false
				}
			},
			events: {
				add: {
					parameters: {
						taskId: "int"
					}
				},
				complete: {
					parameters: {
						taskId: "array"
					}
				}
			}
		},
		constructor: function () {
			ManagedObject.apply(this, arguments);
			this._mList = {};
		},
		/**
		 * IDs counter
		 * @type {number}
		 * @private
		 */
		_iNextId: 0,
		_iTaskCounter: 0
	});

	TaskManager.prototype._validateTask = function(mTask) {
		if (
			!isPlainObject(mTask)
			|| !mTask.type
			|| typeof mTask.type !== "string"
		) {
			throw new Error('Invalid task specified');
		}
	};

	/**
	 * Adds new task into the list
	 * @param {object} mTask - Task definition map
	 * @param {string} mTask.type - Task type
	 * @return {number} Task ID
	 */
	TaskManager.prototype.add = function (mTask) {
		this._validateTask(mTask);

		var iTaskId = this._iNextId++;
		this._mList[mTask.type] = this._mList[mTask.type] || [];
		this._mList[mTask.type].push(Object.assign({}, mTask, {
			id: iTaskId
		}));
		this._iTaskCounter++;
		if (!this.getSuppressEvents()) {
			this.fireAdd({
				taskId: iTaskId
			});
		}
		return iTaskId;
	};

	/**
	 * Completes the task by its ID
	 * @param {number} iTaskId - Task ID
	 */
	TaskManager.prototype.complete = function (iTaskId) {
		Object.keys(this._mList).forEach(function (sTypeName) {
			this._mList[sTypeName] = this._mList[sTypeName].filter(function (mTask) {
				if (mTask.id === iTaskId) {
					this._iTaskCounter--;
					return false;
				}
				return true;
			}.bind(this));
		}, this);
		if (!this.getSuppressEvents()) {
			this.fireComplete({
				taskId: [iTaskId]
			});
		}
	};

	/**
	 * Completes the tasks by the task definition. It is also possible to filter
	 * by parts of the existing task definitions.
	 * @param {object} mTask - Task definition map
	 * @param {object} mTask.type - Task type
	 */
	TaskManager.prototype.completeBy = function (mTask) {
		this._validateTask(mTask);
		var aCompledTaskIds = [];
		// TODO: get rid of filtering other task parameters then type for performance reasons
		this._mList[mTask.type] = this._mList[mTask.type].filter(function (mLocalTask) {
			var bCompleteTask = Object.keys(mTask).every(function(sKey) {
				return mLocalTask[sKey] && mLocalTask[sKey] === mTask[sKey];
			});
			if (bCompleteTask) {
				this._iTaskCounter--;
				aCompledTaskIds.push(mLocalTask.id);
				return false;
			}
			return true;
		}.bind(this));
		if (!this.getSuppressEvents()) {
			this.fireComplete({
				taskId: aCompledTaskIds
			});
		}
	};

	/**
	 * Cancels the task by its ID
	 * @param {number} iTaskId - Task ID
	 */
	TaskManager.prototype.cancel = function (iTaskId) {
		this.complete(iTaskId);
	};

	/**
	 * Checks if the queue is empty
	 * @return {boolean} <code>true</code> if there is no pending task
	 */
	TaskManager.prototype.isEmpty = function () {
		return this._iTaskCounter === 0;
	};

	/**
	 * Returns amount of the tasks in the queue
	 * @param {string} [sType] - type of pending tasks to be counted. When <code>undefined</code> the count will be returned for all tasks
	 * @return {number} Amount of tasks
	 */
	TaskManager.prototype.count = function (sType) {
		return this.getList(sType).length;
	};

	/**
	 * Returns list of pending tasks
	 * @param {string} [sType] - type of pending tasks to be returned. When <code>undefined</code> all tasks will be returned
	 * @return {array} List copy of pending tasks
	 */
	TaskManager.prototype.getList = function (sType) {
		if (sType) {
			return this._mList[sType] ? this._mList[sType].slice(0) : [];
		}
		return Object.keys(this._mList).reduce(function(aResult, sType) {
			return aResult.concat(this._mList[sType]);
		}.bind(this), []);
	};

	/**
	 * @override
	 */
	TaskManager.prototype.destroy = function () {
		this.setSuppressEvents(true);
		this.getList().forEach(function (oTask) {
			this.cancel(oTask.id);
		}, this);
		ManagedObject.prototype.destroy.apply(this, arguments);
	};

	return TaskManager;
});