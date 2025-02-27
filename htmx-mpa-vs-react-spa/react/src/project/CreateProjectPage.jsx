import { useTranslation } from "react-i18next";
import ProjectForm from "./ProjectForm";
import { api } from "../shared/api";
import { Events } from "../shared/events";
import { useNavigate } from "react-router";

export default function CreateProjectPage() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  const createProject = async (project) => {
    const response = await api.post("projects", { name: project.name });
    Events.showErrorModalOrRun(response, () => navigate("/projects"));
  };

  document.title = t('createProjectPage.title');
  return (<>
    <h1 className="text-2xl my-8">{t('createProjectPage.title')}</h1>
    <ProjectForm
      namePlaceholder={t('createProjectPage.namePlaceholder')}
      submitValue={t('createProjectPage.create')}
      submitAction={createProject} />
  </>);
}