import { useState } from "react";
import { classesWithClassIf } from "./ReactUtils";

/**
 * 
 * @param {Object} params 
 * @param {string} params.type: input type
 * @param {string} params.name: input name
 * @param {?string} params.value: input value
 * @param {?string} params.placeholder: input placeholder
 * @param {Ref} params.inputRef: React ref to get current input value
 * @param {Function} params.isValid: single arg and returning boolean validation function
 * @param {string} params.errorMessage: error message to show when isValid returns false
 * @param {?string} params.addInputClass: additional classes to customize input element
 * @returns {JSX.Element}
 */
export default function InputWithError({ type, name, value = "", placeholder = "", inputRef, isValid, errorMessage,
	addInputClass = ""
}) {
	const [error, setError] = useState();

	const validate = () => {
		if (isValid(inputRef.current.value)) {
			setError("");
		} else {
			setError(errorMessage);
		}
	};
	return (<>
		<input className={classesWithClassIf("block p-4", addInputClass, addInputClass)} type={type} name={name} placeholder={placeholder}
			ref={inputRef} defaultValue={value}
			onChange={validate}></input>
		<p className={classesWithClassIf("error mb-2", "hidden", !error)}>{error}</p>
	</>);
}