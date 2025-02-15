import { useState, useEffect } from "react";
import { Link, useLocation } from "react-router";
import { useTranslation } from 'react-i18next';
import { PUBLIC_ROUTES } from "./RoutesGuard";
import { classesWithClassIf } from "./ReactUtils";

export default function Navigation() {
	const [hidden, setHidden] = useState(true);
	const [projectsRoute, setProjectsRoute] = useState(false);
	const [tasksRoute, setTasksRoute] = useState(false);
	const [accountRoute, setAccountRoute] = useState(false);

	const { t } = useTranslation();
	const location = useLocation();

	useEffect(() => {
		const path = location.pathname;
		const hide = PUBLIC_ROUTES.find(l => path.includes(l)) != null;
		setHidden(hide);
		setProjectsRoute(path == "" || path == "/" || path.startsWith("/projects"));
		setTasksRoute(path.startsWith("/tasks"));
		setAccountRoute(path.startsWith("/account"));
	}, [location]);

	if (hidden) {
		return null;
	}

	return (
		<div className="space-y-4 px-8 pt-16 border-r-2 border-solid border-black">
			<Link to="/projects" className={classesWithClassIf("block", "underline", projectsRoute)}>{t('navigation.projects')}</Link>
			<Link to="/tasks" className={classesWithClassIf("block", "underline", tasksRoute)}>{t('navigation.tasks')}</Link>
			<Link to="/account" className={classesWithClassIf("block", "underline", accountRoute)}>{t('navigation.account')}</Link>
		</div>
	);
}