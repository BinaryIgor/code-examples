import { useLocation } from "react-router";
import { useEffect } from "react";
import { api } from "./api";
import { Events } from "./events";
import { CurrentUser, useUser } from "./UserContext";
import { eventBus } from "./event-bus";

export const PUBLIC_ROUTES = ["/sign-in"];

export default function RoutesGuard({ children }) {
	const location = useLocation();
	const { user, setUser } = useUser();

	useEffect(() => {
		console.log("Location...", location);
		if (!user.data) {
			getCurrentUser(setUser);
		}
	}, [location]);

	useEffect(() => {
		const userSignedInSubscriber = eventBus.subscribe(Events.USER_SIGNED_IN, () => {
			// useEffect [location] follows immediately
			setUser(CurrentUser.loading());
		});
		const userSignedOutSubscriber = eventBus.subscribe(Events.USER_SIGNED_OUT, () => {
			setUser(CurrentUser.loaded(null));
		});
		const refreshUserDataSubscriber = eventBus.subscribe(Events.REFRESH_USER_DATA, () => {
			getCurrentUser(setUser);
		});
		return () => {
			eventBus.unsubscribe(userSignedInSubscriber);
			eventBus.unsubscribe(userSignedOutSubscriber);
			eventBus.unsubscribe(refreshUserDataSubscriber);
		};
	}, []);

	return (<>{children}</>);
}

async function getCurrentUser(setUser) {
	const response = await api.get("/user-info");
	if (response.success) {
		const user = response.data;
		setUser(CurrentUser.loaded(user));
	} else {
		setUser(CurrentUser.loaded(null));
		Events.showErrorModal(response.error());
	}
}