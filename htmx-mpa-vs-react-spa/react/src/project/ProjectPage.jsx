import { useParams, Link } from "react-router";
import { useState, useEffect } from "react";
import { useTranslation } from "react-i18next";
import { api } from "../shared/api";
import { Events } from "../shared/events";
import ProjectForm from "./ProjectForm";

export default function ProjectPage() {
	const { t } = useTranslation();
	const projectId = useParams().id;

	const [project, setProject] = useState();
	const [projectFormHidden, setProjectFormHidden] = useState(true);
	const toggleProjectForm = () => setProjectFormHidden(prev => !prev);

	useEffect(() => {
		getProject(projectId, setProject);
	}, []);

	if (!project) {
		return null;
	}

	const updateProject = async (projectForm) => {
		const response = await api.put(`/projects/${project.id}`, { name: projectForm.name });
		Events.showErrorModalOrRun(response, () => {
			setProjectFormHidden(true);
			setProject(response.data);
		});
	};

	document.title = `${project.name} ${t('projectPage.project')}`;
	return (<>
		<h1 className="text-2xl my-8">{project.name} {t('projectPage.project')}</h1>
		<div className="text-xl my-4 underline cursor-pointer" onClick={toggleProjectForm}>{t('projectPage.edit')}</div>
		{!projectFormHidden &&
			(<div className="mb-8">
				<ProjectForm
					namePlaceholder={t('projectPage.namePlaceholder')}
					nameValue={project.name}
					submitValue={t('projectPage.save')}
					submitAction={updateProject} />
			</div>)}
		<Link className="text-xl my-4 underline block" to={`/tasks?project=${project.name}`}>{t('projectPage.tasks')}</Link>
		<Link className="text-xl my-4 underline" to="/projects">{t('projectPage.projects')}</Link>
	</>);
}

async function getProject(id, setProject) {
	const response = await api.get(`/projects/${id}`);
	Events.showErrorModalOrRun(response, () => setProject(response.data));
}