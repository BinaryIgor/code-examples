/**
 * 
 * @param {string} classes 
 * @param {string} className 
 * @param {boolean} condition 
 * @returns {boolean}
 */
export function classesWithClassIf(classes, className, condition) {
	return `${classes}` + (condition ? ` ${className}` : "");
}