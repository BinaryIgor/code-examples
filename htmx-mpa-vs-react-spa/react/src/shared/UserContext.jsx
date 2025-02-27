import { createContext, useContext, useState } from "react";

/**
 * @typedef CurrentUserData
 * @property {string} id
 * @param {string} email 
 * @param {string} name 
 * @param {string} language 
 */

export class CurrentUser {
	/**
	 * 
	 * @param {boolean} loading 
	 * @param {CurrentUserData | null} data 
	 */
	constructor(loading, data) {
		this.loading = loading;
		this.data = data;
	}

	static loading() {
		return new CurrentUser(true, null);
	}

	static loaded(data) {
		return new CurrentUser(false, data);
	}
}

const UserContext = createContext();

export function UserProvider({ children }) {
	const [user, setUser] = useState(CurrentUser.loading());
	return (
		<UserContext.Provider value={{ user, setUser }}>
			{children}
		</UserContext.Provider>
	);
}

export const useUser = () => useContext(UserContext);