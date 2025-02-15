import { StrictMode } from 'react';
import { createRoot } from 'react-dom/client';
import { BrowserRouter, Routes, Route } from 'react-router';
import './index.css';
import './i18n';
import { UserProvider } from './shared/UserContext';
import RoutesGuard from './shared/RoutesGuard';
import ProjectsPage from './project/ProjectsPage';
import TasksPage from './task/TasksPage';
import SignInPage from './user/SignInPage';
import AccountPage from './user/AccountPage';
import ProtectedRoute from './shared/ProtectedRoute';
import App from './App';
import CreateProjectPage from './project/CreateProjectPage';
import ProjectPage from './project/ProjectPage';
import { CreateTaskPage } from './task/CreateTaskPage';
import TaskPage from './task/TaskPage';

createRoot(document.getElementById('root')).render(
  <StrictMode>
    <UserProvider>
      <BrowserRouter>
        <RoutesGuard>
          <App>
            <Routes>
              <Route path="/sign-in" element={<SignInPage />}></Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/" element={<ProjectsPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/projects" element={<ProjectsPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/projects/create" element={<CreateProjectPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/projects/:id" element={<ProjectPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/tasks" element={<TasksPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/tasks/create" element={<CreateTaskPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/tasks/:id" element={<TaskPage />}></Route>
              </Route>
              <Route element={<ProtectedRoute />}>
                <Route path="/account" element={<AccountPage />}></Route>
              </Route>
            </Routes>
          </App>
        </RoutesGuard>
      </BrowserRouter>
    </UserProvider>
  </StrictMode>
)
