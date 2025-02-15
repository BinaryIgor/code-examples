import { useTranslation } from "react-i18next";
import TaskForm from "./TaskForm";
import { useState, useEffect } from "react";
import { tasksOps } from "./task-ops";
import { Events } from "../shared/events";
import { api } from "../shared/api";
import { useNavigate, useSearchParams } from "react-router";

export function CreateTaskPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const tasksSearchParam = searchParams.get("tasksSearch");
  const tasksPath = tasksSearchParam ? `/tasks?${atob(tasksSearchParam)}` : `/tasks`;

  const [allowedProjects, setAllowedProjects] = useState();

  useEffect(() => {
    getAllowedProjects(setAllowedProjects);
  }, []);

  const createTask = async (task) => {
    const response = await api.post("/tasks", { name: task.name, project: task.project });
    Events.showErrorModalOrRun(response, () => navigate(tasksPath));
  };

  if (!allowedProjects) {
    return null;
  }

  document.title = t('createTaskPage.title');
  return (<>
    <h1 className="text-2xl my-8">{t('createTaskPage.title')}</h1>
    <TaskForm
      namePlaceholder={t('createTaskPage.namePlaceholder')}
      projectOptions={t('createTaskPage.projectOptions')}
      projectAllowedValues={allowedProjects}
      projectPlaceholder={t('createTaskPage.projectPlaceholder')}
      submitValue={t('createTaskPage.create')}
      submitAction={createTask}>
    </TaskForm>
  </>);
}

async function getAllowedProjects(setAllowedProjects) {
  const response = await tasksOps.getAllowedProjects();
  Events.showErrorModalOrRun(response, () => setAllowedProjects(response.data));
}