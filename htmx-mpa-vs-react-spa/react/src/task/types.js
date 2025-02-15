/**
 * @enum {string}
 */
export const TaskStatus = {
	TODO: "TODO",
	IN_PROGRESS: "IN_PROGRESS",
	CANCELED: "CANCELED",
	DONE: "DONE"
};

export const TaskStatusValues = Object.values(TaskStatus);


/**
 * @typedef Task
 * @property {string} id
 * @property {string} name
 * @property {string} projectId
 * @property {TaskStatus}
 */
