import { useTranslation } from "react-i18next";
import { useRef } from "react";
import InputWithError from "../shared/InputWithError";
import * as Validator from "../shared/validator";

export default function ProjectForm({ namePlaceholder, nameValue, submitValue, submitAction }) {
  const { t } = useTranslation();
  const nameRef = useRef();
  const submitForm = e => {
    e.preventDefault();
    submitAction({ name: nameRef.current.value });
  };
  return (
    <form onSubmit={submitForm}>
      <InputWithError name="name" type="text" placeholder={namePlaceholder} value={nameValue}
        inputRef={nameRef}
        isValid={name => Validator.hasLength(name, 3, 50)}
        errorMessage={t('errors.ProjectNameValidationException')}>
      </InputWithError>
      <input className="button-like px-8 py-2 mt-4" type="submit" value={submitValue}></input>
    </form>);
}