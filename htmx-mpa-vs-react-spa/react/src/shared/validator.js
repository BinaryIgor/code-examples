/**
 * 
 * @param {string} str 
 * @returns {boolean}
 */
export function hasAnyContent(str) {
	return str && str.trim().length > 0;
}

/**
 * 
 * @param {string} str 
 * @param {number} min
 * @param {number} max
 * @returns {boolean}
 */
export function hasLength(str, min, max) {
	return hasAnyContent(str) && str.length >= min && str.length <= max;
}