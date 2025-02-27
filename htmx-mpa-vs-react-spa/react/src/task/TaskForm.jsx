import { useTranslation } from "react-i18next";
import { useRef, useState } from "react";
import InputWithError from "../shared/InputWithError";
import * as Validator from "../shared/validator";
import { classesWithClassIf } from "../shared/ReactUtils";

export default function TaskForm({
  namePlaceholder, nameValue,
  projectPlaceholder, projectValue, projectOptions, projectAllowedValues,
  statusPlaceholder, statusValue, statusOptions, statusAllowedValues,
  submitValue, submitAction }) {
  const { t } = useTranslation();
  const nameRef = useRef();
  const projectRef = useRef();
  const statusRef = statusValue ? useRef() : null;

  const submitForm = e => {
    e.preventDefault();
    submitAction({
      name: nameRef.current.value,
      project: projectRef.current.value,
      status: statusRef?.current.value
    });
  };

  const [projectOptionsVisible, setProjectOptionsVisible] = useState(true);
  const projectAllowedValuesText = projectAllowedValues.join(", ");

  const [statusOptionsVisible, setStatusOptionsVisible] = useState(true);
  let statusAllowedValuesText;
  if (statusValue) {
    statusAllowedValuesText = statusAllowedValues.join(", ");
  }

  return (
    <form onSubmit={submitForm}>
      <InputWithError name="name" type="text" placeholder={namePlaceholder} value={nameValue}
        inputRef={nameRef}
        isValid={isNameValid}
        errorMessage={t('errors.TaskNameValidationException')}
      />
      <InputWithError
        addInputClass={"mt-4"}
        name="project" type="text" placeholder={projectPlaceholder} value={projectValue}
        inputRef={projectRef}
        isValid={p => isProjectValid(p, projectAllowedValues, setProjectOptionsVisible)}
        errorMessage={t('errors.TaskProjectValidationException', { projects: projectAllowedValuesText })}
      />
      <p className={classesWithClassIf("italic", "hidden", !projectOptionsVisible)}>{projectOptions}: {projectAllowedValuesText}</p>
      {statusValue &&
        (<>
          <InputWithError
            addInputClass={"mt-4"}
            name="status" type="text" placeholder={statusPlaceholder} value={statusValue}
            inputRef={statusRef}
            isValid={s => isStatusValid(s, statusAllowedValues, setStatusOptionsVisible)}
            errorMessage={t('errors.TaskStatusValidationException', { statuses: statusAllowedValuesText })}
          />
          <p className={classesWithClassIf("italic", "hidden", !statusOptionsVisible)}>{statusOptions}: {statusAllowedValuesText}</p>
        </>)}
      <input className="button-like px-8 py-2 mt-4" type="submit" value={submitValue}></input>
    </form>);
}

function isNameValid(name) {
  return Validator.hasLength(name, 3, 50);
}

function isProjectValid(project, allowedProjects, setProjectOptionsVisible) {
  if (!project || !allowedProjects.includes(project)) {
    setProjectOptionsVisible(false);
    return false;
  }
  setProjectOptionsVisible(true);
  return true;
}

function isStatusValid(status, allowedStatuses, setStatusOptionsVisible) {
  if (!status || !allowedStatuses.includes(status)) {
    setStatusOptionsVisible(false);
    return false;
  }
  setStatusOptionsVisible(true);
  return true;
}