import { useTranslation } from "react-i18next";
import { Link, useNavigate } from 'react-router';
import { useState, useEffect } from "react";
import { api } from "../shared/api";
import * as types from './types';
import { Events } from '../shared/events';
import { useSearchParams } from "react-router";
import ConfirmableModal from "../shared/ConfirmableModal";

/**
 * @typedef TasksSearchResult
 * @property {types.Task[]} tasks
 * @property {string[]} availableProjects
 */

export default function TasksPage() {
  const { t } = useTranslation();
  const [searchParams, setSearchParams] = useSearchParams();

  /**
   * @type {TasksSearchResult[]} searchResults
   */
  const [searchResults, setSearchResults] = useState();

  useEffect(() => {
    searchProjects(searchParams, setSearchResults);
  }, [searchParams]);

  const onTaskDeleted = task => {
    const tasks = searchResults.tasks.filter(t => t.id != task.id);
    setSearchResults({ tasks, availableProjects: searchResults.availableProjects });
  };

  let pageBody;
  if (searchResults == null) {
    pageBody = null;
  } else if (searchResults.availableProjects.length == 0) {
    pageBody = <div className="my-8">
      <Link className="underline" to="/projects">{t('tasksPage.noProjects')}</Link>
    </div>;
  } else {
    const searchParamsStr = searchParams.toString();
    const tasksSearch = searchParamsStr ? `?tasksSearch=${btoa(searchParams)}` : "";

    const projectOptions = searchResults.availableProjects.join(", ");
    const statusOptions = types.TaskStatusValues.join(", ");
    let searchQueued = false;
    pageBody = <div className="mt-8">
      <input className="p-4" name="project" placeholder={t('tasksPage.projectsFilterPlaceholder')}
        defaultValue={searchParams.get("project")}
        onInput={e => {
          if (searchQueued) {
            return;
          }
          searchQueued = true;
          setTimeout(() => {
            searchQueued = false;
            setSearchParams(prev => {
              const projectInput = e.target.value;
              if (projectInput) {
                prev.set("project", projectInput);
              } else {
                prev.delete("project");
              }
              return prev;
            });
          }, 500);
        }}></input>
      <p className="italic ml-2">{t('tasksPage.options')}: {projectOptions}</p>
      <input className="p-4 mt-4" name="status" placeholder={t('tasksPage.statusesFilterPlaceholder')}
        defaultValue={searchParams.get("status")}
        onInput={e => {
          if (searchQueued) {
            return;
          }
          searchQueued = true;
          setTimeout(() => {
            searchQueued = false;
            setSearchParams(prev => {
              const statusInput = e.target.value;
              if (statusInput) {
                prev.set("status", statusInput);
              } else {
                prev.delete("status");
              }
              return prev;
            });
          }, 500);
        }}></input>
      <p className="italic ml-2">{t('tasksPage.options')}: {statusOptions}</p>

      <TasksSearchResults tasks={searchResults.tasks} tasksSearch={tasksSearch}
        onTaskDeleted={onTaskDeleted} />

      <div className="my-8">
        <Link className="underline" to={`/tasks/create${tasksSearch}`}>{t('tasksPage.add')}</Link>
      </div>
    </div>;
  }

  document.title = t('tasksPage.title');
  return (<>
    <h1 className="text-2xl my-8">{t('tasksPage.title')}</h1>
    {pageBody}
  </>);
}

async function searchProjects(searchParams, setSearchResults) {
  const path = searchParams ? `/tasks?${searchParams}` : `/tasks`;
  const response = await api.get(path);
  Events.showErrorModalOrRun(response, () => setSearchResults(response.data));
}

const DELETE_TASK_MODAL_ID = "delete-task-modal";

function TasksSearchResults({ tasks, tasksSearch, onTaskDeleted }) {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const [toDeleteTask, setToDeleteTask] = useState();

  const deleteTask = async () => {
    const response = await api.delete(`/tasks/${toDeleteTask.id}`);
    Events.showErrorModalOrRun(response, () => onTaskDeleted(toDeleteTask));
  };

  const showDeleteTaskModal = task => {
    setToDeleteTask(task);
    Events.showConfirmableModal(DELETE_TASK_MODAL_ID);
  };

  return (<>
    <ConfirmableModal
      modalId={DELETE_TASK_MODAL_ID}
      title={t('tasksPage.deleteModalTitle')}
      content={t('tasksPage.deleteModalContent', { task: toDeleteTask?.name })}
      left={t('tasksPage.deleteModalLeft')}
      right={t('tasksPage.deleteModalRight')}
      onRight={deleteTask}
    />
    <div className="space-y-2 my-12">
      {tasks.length > 0 &&
        (tasks.map(task => {
          return (
            <div className="relative border rounded border-black max-w-80 cursor-pointer" key={task.id}>
              <div className="p-4"
                onClick={() => navigate(`/tasks/${task.id}${tasksSearch}`)}>{task.name}, {t('tasksPage.status')}: {task.status}</div>
              <span className="absolute top-0 right-0 text-3xl p-2 cursor-pointer"
                onClick={() => showDeleteTaskModal(task)}>
                &times;
              </span>
            </div>);
        }))}
      {tasks.length == 0 && (<div>{t('tasksPage.noTasks')}</div>)}
    </div>
  </>);
}