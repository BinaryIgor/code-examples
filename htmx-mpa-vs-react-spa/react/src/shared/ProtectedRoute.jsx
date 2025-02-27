import { Navigate, Outlet } from 'react-router';
import { useUser } from './UserContext';

export default function ProtectedRoute() {
	const { user } = useUser();
	if (user.loading) {
		return null;
	}
	if (!user.data) {
		return <Navigate to="/sign-in" replace></Navigate>;
	}
	return <Outlet />;
}