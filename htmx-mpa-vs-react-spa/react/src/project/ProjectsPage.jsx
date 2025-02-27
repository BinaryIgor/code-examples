import { useTranslation } from "react-i18next";
import { useState, useEffect } from "react";
import ConfirmableModal from "../shared/ConfirmableModal";
import { api } from "../shared/api";
import { Events } from "../shared/events";
import { useNavigate } from "react-router";
import { Link } from "react-router";

/**
 * @typedef Project
 * @property {string} id
 * @property {string} name
 */

/**
 * @typedef ProjectWithTasks
 * @property {Project} project
 * @property {number} tasks
 */

export default function ProjectsPage() {
  const { t } = useTranslation();
  /**
   * @type {ProjectWithTasks[]} projects
   */
  const [projects, setProjects] = useState(null);

  useEffect(() => {
    getProjects(setProjects);
  }, []);

  document.title = t('projectsPage.title');
  return (<>
    <h1 className="text-2xl my-8">{t('projectsPage.title')}</h1>
    {projects && <ProjectsList projects={projects} setProjects={setProjects} />}
    {projects != null && projects.length == 0 && (<div>{t("projectsPage.noProjects")}</div>)}
    <div className="my-8">
      <Link className="underline" to="/projects/create">{t('projectsPage.add')}</Link>
    </div>
  </>);
}

async function getProjects(setProjects) {
  const response = await api.get("projects");
  Events.showErrorModalOrRun(response, () => {
    setProjects(response.data);
  });
}

const DELETE_PROJECT_MODAL_ID = "delete-project-modal";

/**
 * @param {Object} params
 * @param {ProjectWithTasks[]} params.projects 
 * @param {Function} params.setProjects
 */
function ProjectsList({ projects, setProjects }) {
  const navigate = useNavigate();
  const { t } = useTranslation();

  const [toDeleteProject, setToDeleteProject] = useState();

  const deleteProject = async () => {
    const response = await api.delete(`projects/${toDeleteProject.id}`);
    Events.showErrorModalOrRun(response, () => {
      const updatedProjects = projects.filter(p => p.project.id != toDeleteProject.id);
      setProjects(updatedProjects);
    });
  };

  const showDeleteProjectModal = (project) => {
    setToDeleteProject(project);
    Events.showConfirmableModal(DELETE_PROJECT_MODAL_ID);
  };

  return (<>
    <ConfirmableModal
      modalId={DELETE_PROJECT_MODAL_ID}
      title={t('projectsPage.deleteModalTitle')}
      content={t('projectsPage.deleteModalContent', { project: toDeleteProject?.name })}
      left={t('projectsPage.deleteModalLeft')}
      right={t('projectsPage.deleteModalRight')}
      onRight={deleteProject}
    />
    <div className="space-y-2">
      {projects.map(p => {
        const { project, tasks } = p;
        return <div key={project.id} className="relative border rounded border-black max-w-80 cursor-pointer">
          <div className="p-4" onClick={() => navigate(`/projects/${project.id}`)}>
            {project.name}, {t('projectsPage.tasks')}: {tasks}
          </div>
          <span className="absolute top-0 right-0 text-3xl p-2 cursor-pointer"
            onClick={() => showDeleteProjectModal(project)}>
            &times;
          </span>
        </div>
      })}
    </div>
  </>);
}