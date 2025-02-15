import { useTranslation } from "react-i18next";
import { useParams, Link, useSearchParams } from 'react-router';
import TaskForm from "./TaskForm";
import { useState, useEffect } from "react";
import { tasksOps } from "./task-ops";
import { Events } from "../shared/events";
import { api } from "../shared/api";
import * as types from "./types";

export default function TaskPage() {
  const { t } = useTranslation();
  const taskId = useParams().id;

  const [searchParams] = useSearchParams();
  const tasksSearchParam = searchParams.get("tasksSearch");
  const tasksPath = tasksSearchParam ? `/tasks?${atob(tasksSearchParam)}` : `/tasks`;

  const [task, setTask] = useState();
  const [taskProject, setTaskProject] = useState();
  const [allowedProjects, setAllowedProjects] = useState();

  const [taskFormHidden, setTaskFormHidden] = useState(true);
  const toggleTaskForm = () => setTaskFormHidden(prev => !prev);

  useEffect(() => {
    getEnrichedTask(taskId, setTask, setTaskProject);
    getAllowedProjects(setAllowedProjects);
  }, []);

  const updateTask = async (task) => {
    const response = await api.put(`/tasks/${taskId}`, task);
    Events.showErrorModalOrRun(response, () => {
      setTaskFormHidden(true);
      setTask(response.data);
      setTaskProject(task.project);
    }, { statuses: types.TaskStatusValues.join(", ") })
  };

  if (!task || !allowedProjects) {
    return null;
  }

  const title = `${task.name} ${t('taskPage.task')}`;
  document.title = title;
  return (
    <>
      <h1 className="text-2xl my-8">{title}</h1>
      <div>{t('taskPage.projectLabel')}: {taskProject}</div>
      <div>{t('taskPage.statusLabel')}: {task.status}</div>
      <div className="text-xl my-8 underline cursor-pointer select-none" onClick={toggleTaskForm}>{t('taskPage.edit')}</div>
      {!taskFormHidden &&
        (<div className="mb-8">
          <TaskForm
            namePlaceholder={t('taskPage.namePlaceholder')}
            nameValue={task.name}
            projectOptions={t('taskPage.projectOptions')}
            projectAllowedValues={allowedProjects}
            projectPlaceholder={t('taskPage.projectPlaceholder')}
            projectValue={taskProject}
            statusOptions={t('taskPage.statusOptions')}
            statusAllowedValues={types.TaskStatusValues}
            statusValue={task.status}
            submitValue={t('taskPage.save')}
            submitAction={updateTask}
          />
        </div>)}
      <Link className="text-xl my-4 underline block" to={tasksPath}>{t('taskPage.tasks')}</Link>
    </>
  );
}

async function getEnrichedTask(id, setTask, setTaskProject) {
  const response = await api.get(`/tasks/${id}`);
  Events.showErrorModalOrRun(response, () => {
    const { task, projectName } = response.data;
    setTask(task);
    setTaskProject(projectName);
  });
}

async function getAllowedProjects(setAllowedProjects) {
  const response = await tasksOps.getAllowedProjects();
  Events.showErrorModalOrRun(response, () => setAllowedProjects(response.data));
}