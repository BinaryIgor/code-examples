import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';

const enTranslations = {
	navigation: {
		projects: "Projects",
		tasks: "Tasks",
		account: "Account"
	},
	signInPage: {
		title: "Sign In",
		emailPlaceholder: "Email",
		passwordPlaceholder: "Password",
		signIn: "Sign in"
	},
	projectsPage: {
		title: "Projects",
		tasks: "Tasks",
		noProjects: "There are no projects!",
		add: "Add new project",
		deleteModalTitle: "Delete project",
		deleteModalLeft: "Cancel",
		deleteModalContent: "Are you sure to delete {{project}} project?",
		deleteModalRight: "Ok"
	},
	createProjectPage: {
		title: "New Project",
		namePlaceholder: "Name",
		create: "Create"
	},
	projectPage: {
		project: "project",
		edit: "Edit",
		namePlaceholder: "Name",
		save: "Save",
		tasks: "Tasks",
		projects: "Projects"
	},
	tasksPage: {
		title: "Tasks",
		noTasks: "There are no tasks!",
		projectsFilterPlaceholder: "Comma-separated projects",
		statusesFilterPlaceholder: "Comma-separated statuses",
		deleteModalTitle: "Delete task",
		deleteModalContent: "Are you sure to delete {{task}} task?",
		deleteModalLeft: "Cancel",
		deleteModalRight: "Ok",
		options: "Options",
		status: "Status",
		add: "Add new task",
		noProjects: "There are no projects! Create one first."
	},
	createTaskPage: {
		title: "New Task",
		namePlaceholder: "Name",
		projectOptions: "Options",
		projectPlaceholder: "Project",
		create: "Create"
	},
	taskPage: {
		task: "task",
		editTitle: "edit",
		edit: "Edit",
		namePlaceholder: "Name",
		projectOptions: "Options",
		projectPlaceholder: "Project",
		statusOptions: "Options",
		statusPlaceholder: "Status",
		projectLabel: "Project",
		statusLabel: "Status",
		save: "Save",
		tasks: "Tasks"
	},
	accountPage: {
		title: "Account",
		name: "Name",
		email: "Email",
		language: "Language",
		signOut: "Sign out"
	},
	errors: {
		title: "Something went wrong...",
		ProjectNameValidationException: "Name can't be blank and needs to have between 3 and 50 characters",
		ProjectNameConflictException: "Project of given name already exists",
		ProjectOwnerException: "Project doesn't belong to the current user",
		ProjectDoesNotExistException: "Project doesn't exist",
		TaskNameValidationException: "Name can't be blank and needs to have between 3 and 50 characters",
    TaskProjectRequiredException: "Task project is required",
		TaskProjectValidationException: "Invalid task project. Allowed projects are: {{projects}}",
		TaskStatusValidationException: "Invalid status. Valid values are {{statuses}}",
		TaskOwnerException: "Task doesn't belong to the current user",
		TaskProjectOwnerException: "Task project doesn't belong to the current user",
		UserEmailValidationException: "Given email is not valid. It must contain '@' sign and a valid domain",
		UserPasswordValidationException: "Invalid password. It must have between 8 and 50 characters",
		UserIncorrectPasswordException: "Password is incorrect",
    ApiUnavailable: "Server not available"
	}
};

i18n.use(initReactI18next)
	.init({
		resources: {
			en: {
				translation: enTranslations
			}
		},
		lng: "en",
		// language to use, more information here: https://www.i18next.com/overview/configuration-options#languages-namespaces-resources
		// you can use the i18n.changeLanguage function to change the language manually: https://www.i18next.com/overview/api#changelanguage
		// if you're using a language detector, do not define the lng option
		interpolation: {
			escapeValue: false // react already safes from xss
		}
	});

export default i18n;